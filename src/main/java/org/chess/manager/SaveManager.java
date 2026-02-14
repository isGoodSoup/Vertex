package org.chess.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.chess.entities.*;
import org.chess.records.Save;
import org.chess.service.BooleanService;
import org.chess.util.RuntimeTypeAdapterFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private final Gson gson;
    private final Path saveFolder;
    private Save currentSave;

    public SaveManager() {
        this.saveFolder = Path.of(System.getProperty("user.home"), ".chess", "saves");
        RuntimeTypeAdapterFactory<Piece> pieceAdapter =
                RuntimeTypeAdapterFactory
                        .of(Piece.class, "type")
                        .registerSubtype(Pawn.class, "PAWN")
                        .registerSubtype(Rook.class, "ROOK")
                        .registerSubtype(Knight.class, "KNIGHT")
                        .registerSubtype(Bishop.class, "BISHOP")
                        .registerSubtype(Queen.class, "QUEEN")
                        .registerSubtype(King.class, "KING");
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(pieceAdapter)
                .setPrettyPrinting()
                .create();
        try {
            if (!Files.exists(saveFolder)) {
                Files.createDirectories(saveFolder);
            }
        } catch (IOException e) {
            System.err.println("Failed to create saves folder: " + e.getMessage());
        }
    }

    public List<Save> getSaves() {
        List<Save> freshSaves = new ArrayList<>();
        for (String name : listSaves()) {
            Save s = loadGame(name);
            if (s != null) {
                freshSaves.add(s);
            }
        }
        return freshSaves;
    }

    public void saveGame(Save save) {
        if(!BooleanService.canSave) { return; }
        Path saveFile = saveFolder.resolve(save.name() + ".json");
        try (FileWriter fw = new FileWriter(saveFile.toFile())) {
            gson.toJson(save, fw);
            System.out.println("Game saved: " + saveFile);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    public Save loadGame(String saveName) {
        Path saveFile = saveFolder.resolve(saveName + ".json");
        if (!Files.exists(saveFile)) {
            System.err.println("Save file not found: " + saveFile);
            return null;
        }

        try (FileReader fr = new FileReader(saveFile.toFile())) {
            return gson.fromJson(fr, Save.class);
        } catch (IOException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }

    public void removeSave(String saveName) {
        List<Save> saves = getSaves();
        if(saveName == null || saveName.isEmpty()) {
            return;
        }

        saves.removeIf(s -> s.name().equals(saveName));
        if(currentSave != null && currentSave.name().equals(saveName)) {
            currentSave = null;
        }

        File saveFile = new File(Path.of(System.getProperty("user.home")) + saveName +".json");
        if (saveFile.exists() && !saveFile.delete()) {
            System.err.println("Failed to delete save file: " + saveFile.getAbsolutePath());
        }
    }


    public List<String> listSaves() {
        List<String> saveNames = new ArrayList<>();
        try (var stream = Files.list(saveFolder)) {
            stream.filter(f -> f.toString().endsWith(".json"))
                    .forEach(f -> saveNames.add(f.getFileName().toString().replace(".json", "")));
        } catch (IOException e) {
            System.err.println("Failed to list saves: " + e.getMessage());
        }
        return saveNames;
    }

    public boolean doesSaveExist(String autosave) {
        File f = new File(saveFolder.resolve(autosave + ".json").toUri());
        return f.exists();
    }

    public Save getCurrentSave() {
        return currentSave;
    }

    public void setCurrentSave(Save currentSave) {
        this.currentSave = currentSave;
    }

    public void autoSave() {
        if(currentSave != null) {
            saveGame(currentSave);
        }
    }
}
