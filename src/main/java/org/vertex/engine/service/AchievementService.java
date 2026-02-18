package org.vertex.engine.service;

import org.vertex.engine.animations.ToastAnimation;
import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.Achievements;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.events.*;
import org.vertex.engine.manager.EventBus;
import org.vertex.engine.manager.SaveManager;
import org.vertex.engine.render.AchievementSprites;
import org.vertex.engine.render.RenderContext;

import java.util.*;

public class AchievementService {
    private Map<Achievements, Achievement> achievements;
    private List<Achievement> achievementList;
    private List<Achievement> sortedList;

    private Map<Long, Integer> moveCount;
    private Map<Long, Integer> winCount;
    private Map<Long, Integer> promotionCount;
    private Map<Long, Integer> checkCount;

    private Set<Long> unlockedIDs;
    private Set<Tint> kingsChecked;

    private int castlingCount = 0;

    private boolean isFirstCapture;
    private boolean isFirstToggle;
    private boolean isFirstWin;
    private boolean isQuickWin;

    private AnimationService animationService;
    private SaveManager saveManager;
    private ServiceFactory service;
    private EventBus eventBus;

    public AchievementService(EventBus eventBus) {
        achievements = new HashMap<>();
        this.eventBus = eventBus;

        this.moveCount = new HashMap<>();
        this.winCount = new HashMap<>();
        this.promotionCount = new HashMap<>();
        this.checkCount = new HashMap<>();

        this.unlockedIDs = new HashSet<>();
        this.kingsChecked = new HashSet<>();

        this.isFirstCapture = true;
        this.isFirstToggle = true;
        this.isFirstWin = true;
        this.isQuickWin = true;

        for(Achievements type : Achievements.values()) {
            achievements.put(type, new Achievement(type));
        }
        achievementList = new ArrayList<>(achievements.values());
        getSortedAchievements();

        eventBus.register(TotalMovesEvent.class, this::onMove);
        eventBus.register(ToggleEvent.class, this::onToggle);
        eventBus.register(CaptureEvent.class, this::onCapture);
        eventBus.register(CheckEvent.class, this::onCheck);
        eventBus.register(CheckmateEvent.class, this::onCheckmate);
        eventBus.register(CastlingEvent.class, this:: onCastling);
        eventBus.register(PromotionEvent.class, this::onPromotion);
        eventBus.register(HardEvent.class, this::onHardGame);
        eventBus.register(StalemateEvent.class, this::onStalemate);
        eventBus.register(ChessMasterEvent.class, event -> {
            long chessStartId = 1001L;
            long chessEndId   = 1011L;
            long unlockedChess = event.achievements().stream()
                    .map(a -> a.getId().getId())
                    .filter(id -> id >= chessStartId && id <= chessEndId)
                    .count();
            long totalChess = getSortedAchievements().stream()
                    .map(a -> a.getId().getId())
                    .filter(id -> id >= chessStartId && id <= chessEndId)
                    .count();
            if(unlockedChess >= totalChess) {
                unlock(Achievements.MASTER_OF_NONE);
            }
        });
        eventBus.register(GrandmasterEvent.class, this::onGrandmaster);
    }

    public List<Achievement> init() {
        List<Achievement> loaded = saveManager.loadAchievements();
        setUnlockedAchievements(loaded);
        return loaded;
    }

    public ServiceFactory getService() {
        return service;
    }

    public void setService(ServiceFactory service) {
        this.service = service;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public void setSaveManager(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    public void setAnimationService(AnimationService animationService) {
        this.animationService = animationService;
    }

    public void unlock(Achievements type) {
        if(!BooleanService.canDoAchievements) { return; }
        Achievement achievement = achievements.get(type);

        if (unlockedIDs.contains(type.getId())) {
            return;
        }

        unlockedIDs.add(type.getId());
        if(achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            animationService.add(new ToastAnimation
                    (achievement.getId().getTitle(),
                            achievement.getId().getDescription(),
                            RenderContext.BASE_HEIGHT,
                            AchievementSprites.getSprite(achievement)));
           service.getSound().playFX(5);
           service.getGameService().autoSave();
            saveManager.saveAchievements(getUnlockedAchievements());
            eventBus.fire(new ChessMasterEvent(getUnlockedAchievements()));
        }
        eventBus.fire(new GrandmasterEvent(Collections
                .unmodifiableList(getUnlockedAchievements())));
    }

    public void lock(Achievements type) {
        Achievement achievement = achievements.get(type);
        if(achievement != null && achievement.isUnlocked()) {
            achievement.setUnlocked(false);
        }
    }

    public void unlockAllAchievements() {
        for(Map.Entry<Achievements, Achievement> a
                : achievements.entrySet()) {
            a.getValue().setUnlocked(true);
        }
    }

    public void lockAllAchievements() {
        for(Map.Entry<Achievements, Achievement> a
                : achievements.entrySet()) {
            a.getValue().setUnlocked(false);
        }
    }

    public Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }

    public List<Achievement> getAchievementList() {
        achievementList.sort(Comparator.comparingInt(a -> a.getId().ordinal()));
        return achievementList;
    }

    public List<Achievement> getUnlockedAchievements() {
        return achievements.values()
                .stream()
                .filter(Achievement::isUnlocked)
                .toList();
    }

    public void setUnlockedAchievements(List<Achievement> loadedAchievements) {
        unlockedIDs.clear();

        for (Achievement loaded : loadedAchievements) {
            Achievements type = loaded.getId();
            Achievement existing = achievements.get(type);

            if (existing != null) {
                existing.setUnlocked(true);
                unlockedIDs.add(type.getId());
            }
        }
    }

    public List<Achievement> getSortedAchievements() {
        Collection<Achievement> achievements = achievementList;
        sortedList = new ArrayList<>(achievements);
        sortedList.sort(
                Comparator.comparingInt(a -> a.getId().ordinal())
        );
        return sortedList;
    }

    private void onMove(TotalMovesEvent event) {
        Piece piece = event.piece();
        moveCount.merge(piece.getID(), 1, Integer::sum);

        if(service.getGameService().getGame() == Games.CHESS && moveCount.get(piece.getID()) < 5 && isQuickWin) {
            unlock(Achievements.QUICK_WIN);
            isQuickWin = false;
        }

        if(service.getGameService().getGame() == Games.CHECKERS && moveCount.get(piece.getID()) < 8 && isQuickWin) {
            unlock(Achievements.QUICK_START);
            isQuickWin = false;
        }
    }

    private void onCapture(CaptureEvent event) {
        Piece attacker = event.piece();
        Piece captured = event.captured();
        if(attacker.getColor() != Tint.LIGHT) return;

        if(service.getGameService().getGame() == Games.CHESS && isFirstCapture) {
            unlock(Achievements.FIRST_CAPTURE);
            isFirstCapture = false;
        }

        if(service.getGameService().getGame() == Games.CHECKERS && isFirstCapture) {
            unlock(Achievements.ROUND_CAPTURE);
            isFirstCapture = false;
        }
    }


    private void onToggle(ToggleEvent event) {
        if(isFirstToggle) {
            unlock(Achievements.SECRET_TOGGLE);
            isFirstToggle = false;
        }
    }

    private void onCastling(CastlingEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        castlingCount++;
        if(castlingCount == 10) {
            unlock(Achievements.CASTLING_MASTER);
        }
    }

    private void onCheck(CheckEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        Piece piece = event.piece();
        Piece king = event.king();
        checkCount.merge(piece.getID(), 1, Integer::sum);

        if(checkCount.get(piece.getID()) == 4) {
            unlock(Achievements.CHECK_OVER);
        }

        kingsChecked.add(king.getColor());
    }

    private void onCheckmate(CheckmateEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        Piece piece = event.piece();
        winCount.merge(piece.getID(), 1, Integer::sum);
        if(isFirstWin) {
            unlock(Achievements.CHECKMATE);
            isFirstWin = false;
        }

        if(winCount.get(piece.getID()) == 128) {
            unlock(Achievements.HEAVY_CROWN);
        }

        if(!kingsChecked.contains(Tint.LIGHT)) {
            unlock(Achievements.UNTOUCHABLE);
        }
    }

    private void onHardGame(HardEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        Piece piece = event.piece();
        unlock(Achievements.HARD_GAME);
    }

    private void onPromotion(PromotionEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        Piece piece = event.piece();
        promotionCount.merge(piece.getID(), 1, Integer::sum);

        if(promotionCount.get(piece.getID()) == 4) {
            unlock(Achievements.KING_PROMOTER);
        }
    }

    private void onStalemate(StalemateEvent event) {
        if(service.getGameService().getGame() != Games.CHESS) { return; }
        Piece piece = event.piece();
        unlock(Achievements.ALL_PIECES);
    }

    private void onGrandmaster(GrandmasterEvent event) {
        List<Achievement> unlocked = event.achievementsList();

        long totalAchievements = achievementList.stream()
                .filter(a -> a.getId() != Achievements.GRANDMASTER)
                .count();

        if(unlocked.size() >= totalAchievements) {
            unlock(Achievements.GRANDMASTER);
        }
    }
}
