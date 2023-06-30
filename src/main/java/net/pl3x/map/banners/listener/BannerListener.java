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
package net.pl3x.map.banners.listener;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.pl3x.map.banners.markers.Banner;
import net.pl3x.map.banners.markers.BannersLayer;
import net.pl3x.map.banners.markers.Icon;
import net.pl3x.map.banners.markers.Position;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.world.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BannerListener implements Listener {
    @EventHandler
    public void onClickBanner(@NotNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            // no block was clicked; ignore
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Banner banner)) {
            // clicked block is not a banner; ignore
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FILLED_MAP) {
            // player was not holding a filled map; ignore
            return;
        }

        if (!event.getPlayer().hasPermission("pl3xmap.banners.admin")) {
            // player does not have permission; ignore
            return;
        }

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                // cancel event to stop banner from breaking
                event.setCancelled(true);
                tryRemoveBanner(banner);
            }
            case RIGHT_CLICK_BLOCK -> tryAddBanner(banner);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockDropItemEvent event) {
        tryRemoveBanner(event.getBlockState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockBurnEvent event) {
        tryRemoveBanner(event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveBanner(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull EntityExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveBanner(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockPistonExtendEvent event) {
        event.getBlocks().forEach(block -> tryRemoveBanner(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockPistonRetractEvent event) {
        event.getBlocks().forEach(block -> tryRemoveBanner(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBannerBreak(@NotNull BlockFromToEvent event) {
        tryRemoveBanner(event.getToBlock().getState());
    }

    protected void tryAddBanner(@NotNull BlockState state) {
        if (state instanceof org.bukkit.block.Banner banner) {
            Location loc = banner.getLocation();
            Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            tryAddBanner(banner, pos);
        }
    }

    protected void tryAddBanner(@NotNull org.bukkit.block.Banner banner, Position pos) {
        BannersLayer layer = getLayer(banner);
        if (layer == null) {
            // world has no banners layer; ignore
            return;
        }

        Icon icon = Icon.get(banner.getBaseColor());
        if (icon == null) {
            // material is not a registered banner; ignore
            return;
        }

        layer.putBanner(new Banner(pos, icon, getCustomName(banner)));

        // play fancy particles as visualizer
        particles(banner.getLocation(), layer.getConfig().BANNER_ADD_PARTICLES, layer.getConfig().BANNER_ADD_SOUND);
    }

    protected void tryRemoveBanner(@NotNull BlockState state) {
        if (state instanceof org.bukkit.block.Banner banner) {
            tryRemoveBanner(banner);
        }
    }

    protected void tryRemoveBanner(@NotNull org.bukkit.block.Banner banner) {
        BannersLayer layer = getLayer(banner);
        if (layer == null) {
            // world has no banners layer; ignore
            return;
        }

        Location loc = banner.getLocation();
        Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        if (layer.removeBanner(pos)) {
            // play fancy particles as visualizer
            particles(banner.getLocation(), layer.getConfig().BANNER_REMOVE_PARTICLES, layer.getConfig().BANNER_REMOVE_SOUND);
        }
    }

    protected String getCustomName(org.bukkit.block.Banner banner) {
        try {
            Method method = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
            method.setAccessible(true);
            BannerBlockEntity nms = (BannerBlockEntity) method.invoke(banner);
            return nms.hasCustomName() ? CraftChatMessage.fromComponent(nms.getCustomName()) : "";
        } catch (Throwable t) {
            return "";
        }
    }

    protected @Nullable BannersLayer getLayer(@NotNull BlockState state) {
        World world = Pl3xMap.api().getWorldRegistry().get(state.getWorld().getName());
        if (world == null || !world.isEnabled()) {
            // world is missing or not enabled; ignore
            return null;
        }
        return (BannersLayer) world.getLayerRegistry().get(BannersLayer.KEY);
    }

    protected void particles(@NotNull Location loc, @NotNull Particle particle, @NotNull Sound sound) {
        loc.getWorld().playSound(loc, sound, 1.0F, 1.0F);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < 20; ++i) {
            double x = loc.getX() + rand.nextGaussian();
            double y = loc.getY() + rand.nextGaussian();
            double z = loc.getZ() + rand.nextGaussian();
            loc.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, null, true);
        }
    }
}
