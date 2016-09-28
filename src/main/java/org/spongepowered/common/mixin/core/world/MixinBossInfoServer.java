/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Implements(@Interface(iface = ServerBossBar.class, prefix = "sbar$"))
@Mixin(BossInfoServer.class)
@SuppressWarnings("WeakerAccess")
public abstract class MixinBossInfoServer extends MixinBossInfo {

    @Shadow private boolean visible;
    @Shadow public abstract void addPlayer(EntityPlayerMP player);
    @Shadow public abstract void removePlayer(EntityPlayerMP player);
    @Shadow public abstract void setVisible(boolean visibleIn);
    @Shadow public abstract Collection<EntityPlayerMP> getPlayers();
    @Shadow private void sendUpdate(SPacketUpdateBossInfo.Operation operation) {
    };

    public ServerBossBar sbar$setName(Text name) {
        if (this.name != name) {
            super.bar$setName(name);
            this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_NAME);
        }

        return (ServerBossBar) this;
    }

    public ServerBossBar sbar$setPercent(float percent) {
        if (this.percent != percent) {
            super.bar$setPercent(percent);
            this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PCT);
        }

        return (ServerBossBar) this;
    }

    @SuppressWarnings("RedundantCast")
    public ServerBossBar sbar$setColor(BossBarColor color) {
        if ((Object) this.color != color) {
            super.bar$setColor(color);
            this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_STYLE);
        }

        return (ServerBossBar) this;
    }

    @SuppressWarnings("RedundantCast")
    public ServerBossBar sbar$setOverlay(BossBarOverlay overlay) {
        if ((Object) this.overlay != overlay) {
            super.bar$setOverlay(overlay);
            this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_STYLE);
        }

        return (ServerBossBar) this;
    }

    public ServerBossBar sbar$setDarkenSky(boolean darkenSky) {
        this.setDarkenSky(darkenSky);
        return (ServerBossBar) this;
    }

    public ServerBossBar sbar$setPlayEndBossMusic(boolean playEndBossMusic) {
        this.setPlayEndBossMusic(playEndBossMusic);
        return (ServerBossBar) this;
    }

    public ServerBossBar sbar$setCreateFog(boolean createFog) {
        this.setCreateFog(createFog);
        return (ServerBossBar) this;
    }

    @Intrinsic
    @SuppressWarnings("unchecked")
    public Collection<Player> sbar$getPlayers() {
        return (Collection<Player>) (Object) this.getPlayers();
    }

    public ServerBossBar sbar$addPlayer(Player player) {
        this.addPlayer((EntityPlayerMP) player);
        return (ServerBossBar) this;
    }

    public ServerBossBar sbar$removePlayer(Player player) {
        this.removePlayer((EntityPlayerMP) player);
        return (ServerBossBar) this;
    }

    public boolean sbar$isVisible() {
        return this.visible;
    }

    public ServerBossBar sbar$setVisible(boolean visible) {
        this.setVisible(visible);
        return (ServerBossBar) this;
    }

}
