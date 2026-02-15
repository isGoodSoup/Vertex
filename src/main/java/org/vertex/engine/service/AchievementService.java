package org.vertex.engine.service;

import org.vertex.engine.animations.ToastAnimation;
import org.vertex.engine.entities.Achievement;
import org.vertex.engine.enums.Achievements;
import org.vertex.engine.manager.SaveManager;
import org.vertex.engine.render.AchievementSprites;
import org.vertex.engine.render.RenderContext;

import java.util.*;

public class AchievementService {
    private Map<Achievements, Achievement> achievements;
    private List<Achievement> achievementList;
    private List<Achievement> sortedList;

    private AnimationService animationService;
    private SaveManager saveManager;

    public AchievementService() {
        achievements = new HashMap<>();
        for(Achievements type : Achievements.values()) {
            achievements.put(type, new Achievement(type));
        }
        achievementList = new ArrayList<>(achievements.values());
        getSortedAchievements();
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
        saveManager.autoSave();
        Achievement achievement = achievements.get(type);
        if(achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            animationService.add(new ToastAnimation
                    (achievement.getId().getTitle(),
                            achievement.getId().getDescription(),
                            RenderContext.BASE_HEIGHT,
                            AchievementSprites.getSprite(achievement)));
        }
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
}
