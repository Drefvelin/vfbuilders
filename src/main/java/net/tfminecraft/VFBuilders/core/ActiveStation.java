package net.tfminecraft.VFBuilders.core;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;

import me.Plugins.TLibs.Utils.TimeFormatter;
import net.tfminecraft.VFBuilders.events.VehicleConstructEvent;
import net.tfminecraft.VFBuilders.loaders.BlueprintLoader;
import net.tfminecraft.VehicleFramework.VFLogger;
import net.tfminecraft.VehicleFramework.VehicleFramework;
import net.tfminecraft.VehicleFramework.Managers.VehicleManager;
import net.tfminecraft.VehicleFramework.Vehicles.ActiveVehicle;

public class ActiveStation {
    private UUID id;
    private Station station;

    private int timeLeft;
    private Blueprint blueprint;
    private Location loc;
    private Location spawnLoc;
    private UUID constructorUuid;

    private ArmorStand hologramTitle;
    private ArmorStand hologramTime;

    
    public ActiveStation(Location loc, Station stored) {
        id = UUID.randomUUID();
        this.loc = loc;
        station = stored;
        timeLeft = 0;
    }

    public ActiveStation(Location loc, Station station, String blueprintId, int timeLeft, Location spawnLoc) {
        this(loc, station, blueprintId, timeLeft, spawnLoc, null);
    }

    public ActiveStation(
            Location loc,
            Station station,
            String blueprintId,
            int timeLeft,
            Location spawnLoc,
            UUID constructorUuid) {
        this.id = UUID.randomUUID();
        this.loc = loc;
        this.station = station;
        this.timeLeft = timeLeft;
        this.constructorUuid = constructorUuid;

        if (blueprintId != null) {
            Blueprint blueprint = BlueprintLoader.getByString(blueprintId);
            if (blueprint != null) {
                this.blueprint = blueprint;
                updateHologram();
            }
        }

        if (spawnLoc != null) {
            setSpawnLocation(spawnLoc);
        }
    }


    public void selectBlueprint(Blueprint b, UUID constructorUuid) {
        blueprint = b;
        timeLeft = b.getTime();
        this.constructorUuid = constructorUuid;
        updateHologram();
    }

    public boolean tick() {
        if (timeLeft <= 0) {
            removeHolograms();
            return true;
        }

        timeLeft--;
        updateHologram();

        if (timeLeft == 0) {
            removeHolograms();
        }

        return timeLeft == 0;
    }

    public void complete() {
        if (spawnLoc == null || blueprint == null) {
            return;
        }

        // Check if any player is within 96 blocks
        boolean hasNearbyPlayer = spawnLoc.getWorld().getPlayers().stream()
            .anyMatch(p -> p.getLocation().distanceSquared(spawnLoc) <= (96 * 96));

        if (!hasNearbyPlayer) {
            return; // Delay completion until a player is nearby
        }

        VehicleManager manager = VehicleFramework.getVehicleManager();
        ActiveVehicle vehicle = manager.spawn(spawnLoc, blueprint.getVehicle());
        if (vehicle == null) {
            VFLogger.log("Failed to spawn vehicle for blueprint " + blueprint.getId() + " at station " + loc);
            return;
        }

        Location completedSpawn = spawnLoc.clone();
        Bukkit.getPluginManager().callEvent(
            new VehicleConstructEvent(constructorUuid, vehicle, blueprint, completedSpawn, this));

        // Visual effects
        Location center = completedSpawn.clone().add(0.5, 1, 0.5);

        center.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, center, 10, 0.3, 0.3, 0.3, 0.05);
        center.getWorld().spawnParticle(Particle.CLOUD, center, 20, 0.5, 0.5, 0.5, 0.01);
        center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, center, 40, 0.6, 1, 0.6, 0);

        center.getWorld().playSound(center, Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1.2f);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);

        spawnLoc = null;
        blueprint = null;
        constructorUuid = null;
    }

    public void cancelConstruction() {
        if (blueprint != null) {
            blueprint.drop(loc.clone().add(0.5, 1, 0.5));
        }
        removeHolograms();
        spawnLoc = null;
        blueprint = null;
        constructorUuid = null;
        timeLeft = 0;
    }

    public UUID getUuid() {
        return id;
    }

    public Location getLocation() {
        return loc;
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

    public void setTimeLeft(int i) {
        timeLeft = i;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public boolean hasSpawnLocation() {
        return spawnLoc != null;
    }

    public Location getSpawnLocation() {
        return spawnLoc;
    }

    public void setSpawnLocation(Location loc) {
        spawnLoc = loc;
    }

    public UUID getConstructorUuid() {
        return constructorUuid;
    }

    public void setConstructorUuid(UUID constructorUuid) {
        this.constructorUuid = constructorUuid;
    }

    private void updateHologram() {
        if (!hasBlueprint()) return;
        if (loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) return;

        String title = "§eConstructing §6" + blueprint.getVehicle().getName();
        String time = "§7Time: §f" + TimeFormatter.formatTime(timeLeft);

        Location baseLoc = loc.clone().add(0.5, 1.4, 0.5);
        Location timeLoc = baseLoc.clone().subtract(0, 0.25, 0); // Slightly below

        if (hologramTitle == null || hologramTitle.isDead()) {
            hologramTitle = spawnHologram(baseLoc, title);
        } else {
            hologramTitle.teleport(baseLoc);
            hologramTitle.setCustomName(title);
        }

        if (hologramTime == null || hologramTime.isDead()) {
            hologramTime = spawnHologram(timeLoc, time);
        } else {
            hologramTime.teleport(timeLoc);
            hologramTime.setCustomName(time);
        }
    }

    private ArmorStand spawnHologram(Location loc, String text) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);
        stand.setMarker(true);
        stand.setSilent(true);
        return stand;
    }



    public void removeHolograms() {
        if (hologramTitle != null && !hologramTitle.isDead()) {
            hologramTitle.remove();
        }
        if (hologramTime != null && !hologramTime.isDead()) {
            hologramTime.remove();
        }
        hologramTitle = null;
        hologramTime = null;
    }

}
