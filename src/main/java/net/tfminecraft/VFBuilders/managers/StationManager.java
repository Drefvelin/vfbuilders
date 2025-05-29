package net.tfminecraft.VFBuilders.managers;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Objects.API.SubAPI.BlockChecker;
import net.tfminecraft.VFBuilders.VFBuilders;
import net.tfminecraft.VFBuilders.core.ActiveStation;
import net.tfminecraft.VFBuilders.core.BlueprintCategory;
import net.tfminecraft.VFBuilders.core.Station;
import net.tfminecraft.VFBuilders.enums.VFBGUI;
import net.tfminecraft.VFBuilders.holders.VFBHolder;
import net.tfminecraft.VFBuilders.loaders.CategoryLoader;
import net.tfminecraft.VFBuilders.loaders.StationLoader;

public class StationManager implements Listener {
    private HashMap<Location, ActiveStation> stations = new HashMap<>();
    InventoryManager inv = new InventoryManager();

    public ActiveStation getStation(Block b) {
        if(stations.containsKey(b.getLocation())) return stations.get(b.getLocation());
        BlockChecker checker = TLibs.getBlockAPI().getChecker();
		for(Station station : StationLoader.get().values()) {
            if(checker.checkBlock(b, station.getBlock())) return new ActiveStation(station);
        }
        return null;
	}

    @EventHandler
    public void stationInteract(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        ActiveStation station = getStation(b);
        if(station == null) return;
        e.setCancelled(true);
        if(!station.hasBlueprint()) {
            inv.categoryView(null, p, station, true);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if(!(e.getView().getTopInventory().getHolder() instanceof VFBHolder)) return;
        VFBHolder h = (VFBHolder) e.getView().getTopInventory().getHolder();
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack i = e.getCurrentItem();
        if(i == null) return;
        if(i.getType().equals(Material.BARRIER)) {
            if(h.getType().equals(VFBGUI.BLUEPRINT)) {
                inv.categoryView(null, p, h.getStation(), true);
            }
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            return;
        }
        ItemMeta m = i.getItemMeta();
        NamespacedKey key = new NamespacedKey(VFBuilders.plugin, "vfb_category_id");
        String id = m.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(id != null) {
            BlueprintCategory cat = CategoryLoader.getByString(id);
            inv.blueprintView(null, p, cat, h.getStation(), true);
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            return;
        }
        key = new NamespacedKey(VFBuilders.plugin, "vfb_blueprint_id");
        id = m.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(id != null) {
            BlueprintCategory cat = CategoryLoader.getByString(id);
            inv.blueprintView(null, p, cat, h.getStation(), true);
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            return;
        }
    }
}
