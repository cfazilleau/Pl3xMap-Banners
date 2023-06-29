package net.pl3x.map.banners.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class PaperBannerListener extends BannerListener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockDestroyEvent event) {
        tryRemoveBanner(event.getBlock().getState());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected String getCustomName(org.bukkit.block.Banner banner) {
        return banner.getCustomName();
    }
}
