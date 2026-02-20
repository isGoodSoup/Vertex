package org.vertex.engine.entities;

import org.vertex.engine.enums.Achievements;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;

public class Achievement implements Clickable {
    private Achievements id;
    private boolean isUnlocked;

    public Achievement(Achievements id) {
        this.id = id;
    }

    public Achievements getId() {
        return id;
    }

    public void setId(Achievements id) {
        this.id = id;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }

    @Override
    public void onClick(GameService gameService) {
        BooleanService.canZoomIn ^= true;
    }
}
