package org.vertex.engine.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.records.Save;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.ServiceFactory;
import org.vertex.engine.util.RuntimeTypeAdapterFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private final Gson gson;
    private final Path saveFolder;
    private Save currentSave;
    private static final Logger log = LoggerFactory.getLogger(SaveManager.class);
    private ServiceFactory service;

    public SaveManager() {
        this.saveFolder = Path.of(System.getProperty("user.home"), ".vertex", "chess", "saves");
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
            log.error("Failed to create saves folder: {}", e.getMessage());
        }
    }

    public ServiceFactory getServiceFactory() {
        return service;
    }

    public void setServiceFactory(ServiceFactory service) {
        this.service = service;
    }

    public Save getCurrentSave() {
        return currentSave;
    }

    public void setCurrentSave(Save currentSave) {
        this.currentSave = currentSave;
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
            log.info("Game saved: {}", saveFile);
        } catch (IOException e) {
            log.error("Failed to save game: {}", e.getMessage());
        }
    }

    public Save loadGame(String saveName) {
        Path saveFile = saveFolder.resolve(saveName + ".json");
        if (!Files.exists(saveFile)) {
            log.error("Save file not found: {}", saveFile);
            return null;
        }

        try (FileReader fr = new FileReader(saveFile.toFile())) {
            return gson.fromJson(fr, Save.class);
        } catch (IOException e) {
            log.error("Failed to load game: {}", e.getMessage());
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
            log.error("Failed to delete save file: {}", saveFile.getAbsolutePath());
        }
    }


    public List<String> listSaves() {
        List<String> saveNames = new ArrayList<>();
        try (var stream = Files.list(saveFolder)) {
            stream.filter(f -> f.toString().endsWith(".json"))
                    .forEach(f -> saveNames.add(f.getFileName().toString().replace(".json", "")));
        } catch (IOException e) {
            log.error("Failed to list saves: {}", e.getMessage());
        }
        return saveNames;
    }

    public boolean doesSaveExist(String autosave) {
        File f = new File(saveFolder.resolve(autosave + ".json").toUri());
        return f.exists();
    }

    public void autoSave() {
        if(currentSave != null) {
            Save updated = new Save(
                    GameService.getGame(),
                    currentSave.name(),
                    GameService.getCurrentTurn(),
                    service.getPieceService().getPieces(),
                    service.getAchievementService().getUnlockedAchievements()
            );
            currentSave = updated;
            saveGame(updated);
        }
    }
}
