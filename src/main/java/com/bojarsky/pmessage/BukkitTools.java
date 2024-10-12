package com.bojarsky.pmessage;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;

public class BukkitTools {

    @Nullable
    public static <T extends Keyed> T matchRegistry(@NotNull Registry<T> registry, @NotNull String input) {
        final String filtered = input.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
        if (filtered.isEmpty())
            return null;

        final NamespacedKey key = NamespacedKey.fromString(filtered);
        return key != null ? registry.get(key) : null;
    }

    public static Material matchMaterialName(String name) {
        return matchRegistry(Registry.MATERIAL, name);
    }
}
