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
package net.pl3x.map.banners.markers;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import net.pl3x.map.banners.Pl3xMapBanners;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.bukkit.Material;

public enum Icon {
    BLACK, BLUE, BROWN, CYAN, GREEN, GREY, LIGHT_BLUE, LIGHT_GREY, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, YELLOW, WHITE;

    private final String key;
    private final String type;

    Icon() {
        this.type = name().toLowerCase(Locale.ROOT);
        this.key = String.format("pl3xmap_%s_banner", this.type);
    }

    public String getKey() {
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

    public static Icon get(Material type) {
        return switch (type) {
            case BLACK_BANNER, BLACK_WALL_BANNER -> BLACK;
            case BLUE_BANNER, BLUE_WALL_BANNER -> BLUE;
            case BROWN_BANNER, BROWN_WALL_BANNER -> BROWN;
            case CYAN_BANNER, CYAN_WALL_BANNER -> CYAN;
            case GREEN_BANNER, GREEN_WALL_BANNER -> GREEN;
            case GRAY_BANNER, GRAY_WALL_BANNER -> GREY;
            case LIGHT_BLUE_BANNER, LIGHT_BLUE_WALL_BANNER -> LIGHT_BLUE;
            case LIGHT_GRAY_BANNER, LIGHT_GRAY_WALL_BANNER -> LIGHT_GREY;
            case LIME_BANNER, LIME_WALL_BANNER -> LIME;
            case MAGENTA_BANNER, MAGENTA_WALL_BANNER -> MAGENTA;
            case ORANGE_BANNER, ORANGE_WALL_BANNER -> ORANGE;
            case PINK_BANNER, PINK_WALL_BANNER -> PINK;
            case PURPLE_BANNER, PURPLE_WALL_BANNER -> PURPLE;
            case RED_BANNER, RED_WALL_BANNER -> RED;
            case YELLOW_BANNER, YELLOW_WALL_BANNER -> YELLOW;
            default -> WHITE;
        };
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
                plugin.getLogger().warning("Failed to register icon (" + icon.type + ") " + bannerFilename);
                e.printStackTrace();
            }
        }
    }
}
