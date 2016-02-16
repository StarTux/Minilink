package com.winthier.minilink.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Misc utility functions to set or get metadata easily.
 */
public class Meta {
    public static void set(JavaPlugin plugin, Metadatable metadatable, String key, Object value) {
        if (value == null) metadatable.removeMetadata(key, plugin);
        metadatable.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(JavaPlugin plugin, Metadatable metadatable, String key) {
        for (MetadataValue value : metadatable.getMetadata(key)) {
            if (plugin == value.getOwningPlugin()) {
                try {
                    return (T)value.value();
                } catch (ClassCastException cce) {
                    cce.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}
