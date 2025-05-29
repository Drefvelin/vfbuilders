package net.tfminecraft.VFBuilders.core;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.VFBuilders.VFBuilders;
import net.tfminecraft.VFBuilders.loaders.CategoryLoader;
import net.tfminecraft.VehicleFramework.VFLogger;

public class Blueprint {
    private String id;
    private BlueprintCategory category;
    private ItemStack item;

    private int time;
    private HashMap<String, Integer> inputs = new HashMap<>();

    private String permission;

    public Blueprint(String key, ConfigurationSection config) {
        id = key;
        category = CategoryLoader.getByString(config.getString("category", "none"));
        if(category != null) {
            category.addBlueprint(this);
        }
        if(config.isConfigurationSection("item")) item = TLibs.getItemAPI().getCreator().getItemFromConfig(config.getConfigurationSection("item"));
        else item = new ItemStack(Material.DIRT, 1);
        time = config.getInt("time", 10);
        for(String s : config.getStringList("inputs")) {
            String input = s.split("\\s+")[0];
            Integer amount = 1;
            try {
                amount = Integer.parseInt(s.split("\\s+")[1]);
            } catch (Exception e) {
                VFLogger.log(VFBuilders.plugin, s+" is not a correctly formulated input for the blueprint "+key);
            }
            inputs.put(input, amount);
        }
        permission = config.getString("permission", null);
    }

    public String getId() {
        return id;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getTime() {
        return time;
    }

    public HashMap<String, Integer> getInputs() {
        return inputs;
    }
}
