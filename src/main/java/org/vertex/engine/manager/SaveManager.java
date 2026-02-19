package org.vertex.engine.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.records.Save;
import org.vertex.engine.util.RuntimeTypeAdapterFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveManager {
    private static final String AUTOSAVE_FILE = "autosave.json";
    private static final String ACHIEVEMENTS_FILE = "achievements.json";
    private final Gson gson;
    private final Path savePath;
    private final Path achievementsPath;
    private static final Logger log = LoggerFactory.getLogger(SaveManager.class);

    public SaveManager() {
        Path saveFolder = Path.of(System.getProperty("user.home"), ".vertex");
        this.achievementsPath = saveFolder.resolve(ACHIEVEMENTS_FILE);
        RuntimeTypeAdapterFactory<Piece> pieceAdapter =
                RuntimeTypeAdapterFactory
                        .of(Piece.class, "classType")
                        .registerSubtype(Pawn.class, "PAWN")
                        .registerSubtype(Rook.class, "ROOK")
                        .registerSubtype(Knight.class, "KNIGHT")
                        .registerSubtype(Bishop.class, "BISHOP")
                        .registerSubtype(Queen.class, "QUEEN")
                        .registerSubtype(King.class, "KING")
                        .registerSubtype(Checker.class, "CHECKER")
                        .registerSubtype(Lance.class, "LANCE")
                        .registerSubtype(Gold.class, "GOLD")
                        .registerSubtype(Silver.class, "SILVER");
        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(pieceAdapter)
                .setPrettyPrinting()
                .create();

        this.savePath = saveFolder.resolve(AUTOSAVE_FILE);
        try {
            Files.createDirectories(saveFolder);
        } catch(IOException e) {
            log.error("Failed to create save directory: {}", e.getMessage());
        }
    }

    public void saveGame(Save save) {
        Path temp = savePath.resolveSibling("autosave.tmp");
        try(FileWriter writer = new FileWriter(temp.toFile())) {
            gson.toJson(save, writer);
        } catch(IOException e) {
            log.error("Failed to write temp autosave: {}", e.getMessage());
            return;
        }

        try {
            Files.move(temp, savePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e) {
            log.error("Failed to rename temp autosave: {}", e.getMessage());
        }
    }

    public Save loadGame() {
        if(!Files.exists(savePath)) {
            log.debug("No autosave found.");
            return null;
        }
        try {
            if(Files.size(savePath) == 0) {
                log.warn("Autosave file is empty.");
                return null;
            }

            try(FileReader reader = new FileReader(savePath.toFile())) {
                return gson.fromJson(reader, Save.class);
            }

        } catch(IOException e) {
            log.error("Failed to load autosave: {}", e.getMessage());
            return null;
        }
    }

    public void saveAchievements(List<Achievement> achievements) {
        try (FileWriter writer = new FileWriter(achievementsPath.toFile())) {
            gson.toJson(achievements, writer);
            log.debug("Achievements saved.");
        } catch (IOException e) {
            log.error("Failed to save achievements: {}", e.getMessage());
        }
    }

    public List<Achievement> loadAchievements() {
        if (!Files.exists(achievementsPath)) {
            return new ArrayList<>();
        }
        try(FileReader reader = new FileReader(achievementsPath.toFile())) {
            Achievement[] arr = gson.fromJson(reader, Achievement[].class);
            if(arr == null) return new ArrayList<>();
            List<Achievement> list = new ArrayList<>(Arrays.asList(arr));
            return list;
        } catch(IOException e) {
            log.error("Failed to load achievements, returning empty list.", e);
            return new ArrayList<>();
        }
    }

    public boolean autosaveExists() {
        return Files.exists(savePath) && savePath.toFile().length() > 0;
    }
}