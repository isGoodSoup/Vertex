package org.chess.service;

import org.chess.animations.ToastAnimation;
import org.chess.entities.Achievement;
import org.chess.enums.Achievements;
import org.chess.render.AchievementSprites;
import org.chess.render.RenderContext;

import java.util.*;

public class AchievementService {
    private Map<Achievements, Achievement> achievements;
    private List<Achievement> achievementList;
    private AnimationService animationService;

    public AchievementService() {
        achievements = new HashMap<>();
        for(Achievements type : Achievements.values()) {
            achievements.put(type, new Achievement(type));
        }
        achievementList = new ArrayList<>(achievements.values());
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    public void setAnimationService(AnimationService animationService) {
        this.animationService = animationService;
    }

    public void unlock(Achievements type) {
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
}
