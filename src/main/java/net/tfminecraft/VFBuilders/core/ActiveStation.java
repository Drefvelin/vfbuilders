package net.tfminecraft.VFBuilders.core;

import java.util.UUID;

public class ActiveStation {
    private UUID id;
    private Station station;

    private int timeLeft;
    private Blueprint blueprint;
    
    public ActiveStation(Station stored) {
        id = UUID.randomUUID();
        station = stored;
        timeLeft = 0;
    }

    public UUID getUuid() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public boolean hasBlueprint() {
        return blueprint != null;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
