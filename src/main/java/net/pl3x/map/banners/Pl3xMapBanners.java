package net.pl3x.map.banners;

import net.pl3x.map.banners.listener.BannerListener;
import net.pl3x.map.banners.listener.WorldListener;
import net.pl3x.map.banners.markers.BannersLayer;
import net.pl3x.map.core.Pl3xMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pl3xMapBanners extends JavaPlugin {
    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            getLogger().severe("Pl3xMap not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new BannerListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
    }

    @Override
    public void onDisable() {
        Pl3xMap.api().getWorldRegistry().forEach(world -> {
            try {
                world.getLayerRegistry().unregister(BannersLayer.KEY);
            } catch (Throwable ignore) {
            }
        });
    }
}
