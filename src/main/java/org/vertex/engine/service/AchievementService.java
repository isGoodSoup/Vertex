package org.vertex.engine.service;

import org.vertex.engine.animations.ToastAnimation;
import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.Achievements;
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
        eventBus.register(GrandmasterEvent.class, this::onGrandmaster);
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
        saveManager.autoSave();
        Achievement achievement = achievements.get(type);
        if(achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            animationService.add(new ToastAnimation
                    (achievement.getId().getTitle(),
                            achievement.getId().getDescription(),
                            RenderContext.BASE_HEIGHT,
                            AchievementSprites.getSprite(achievement)));
            service.getGuiService().getFx().playFX(5);
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

    public void setUnlockedAchievements(List<Achievement> achievements) {
        List<Achievement> a = getAchievementList();
        a = achievements;
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

        if(moveCount.get(piece.getID()) < 5 && isQuickWin) {
            unlock(Achievements.QUICK_WIN);
            isQuickWin = false;
        }
    }

    private void onCapture(CaptureEvent event) {
        if (isFirstCapture) {
            unlock(Achievements.FIRST_CAPTURE);
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
        castlingCount++;
        if(castlingCount == 10) {
            unlock(Achievements.CASTLING_MASTER);
        }
    }

    private void onCheck(CheckEvent event) {
        Piece piece = event.piece();
        Piece king = event.king();
        checkCount.merge(piece.getID(), 1, Integer::sum);

        if(checkCount.get(piece.getID()) == 4) {
            unlock(Achievements.CHECK_OVER);
        }

        kingsChecked.add(king.getColor());
    }

    private void onCheckmate(CheckmateEvent event) {
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
        Piece piece = event.piece();
        unlock(Achievements.HARD_GAME);
    }

    private void onPromotion(PromotionEvent event) {
        Piece piece = event.piece();
        promotionCount.merge(piece.getID(), 1, Integer::sum);

        if(promotionCount.get(piece.getID()) == 4) {
            unlock(Achievements.KING_PROMOTER);
        }
    }

    private void onStalemate(StalemateEvent event) {
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
