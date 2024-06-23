/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ryderbelserion.map.banners.markers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import com.ryderbelserion.map.banners.Pl3xMapBanners;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Icon {

    BLACK, BLUE, BROWN, CYAN, GREEN, GREY, LIGHT_BLUE, LIGHT_GREY, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, YELLOW, WHITE;

    private final String key;
    private final String type;

    Icon() {
        this.type = name().toLowerCase(Locale.ROOT);
        this.key = String.format("pl3xmap_%s_banner", this.type);
    }

    public @NotNull String getKey() {
        return this.key;
    }

    public static void saveGimpSrc() {
        Pl3xMapBanners plugin = Pl3xMapBanners.getPlugin(Pl3xMapBanners.class);
        String filename = String.format("icons%sbanners.xcf", File.separator);
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }
    }

    private static final Map<String, Icon> BY_NAME = new HashMap<>();
    private static final Map<DyeColor, Icon> BY_COLOR = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(icon -> BY_NAME.put(icon.name(), icon));
        Arrays.stream(DyeColor.values()).forEach(color -> BY_COLOR.put(color, BY_NAME.get(color.name())));
    }

    public static @Nullable Icon get(@NotNull DyeColor color) {
        return BY_COLOR.get(color);
    }

    public static void register() {
        Pl3xMapBanners plugin = Pl3xMapBanners.getPlugin(Pl3xMapBanners.class);
        for (Icon icon : values()) {
            String bannerFilename = String.format("icons%s%s.png", File.separator, icon.type);
            File bannerFile = new File(plugin.getDataFolder(), bannerFilename);

            if (!bannerFile.exists()) {
                plugin.saveResource(bannerFilename, false);
            }

            try {
                Pl3xMap.api().getIconRegistry().register(new IconImage(icon.key, ImageIO.read(bannerFile), "png"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,"Failed to register icon (" + icon.type + ") " + bannerFilename, e);
            }
        }
    }
}