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
package org.spongepowered.common.mixin.api.mcp.world;

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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.world.ServerBossInfo;

@Implements(@Interface(iface = ServerBossBar.class, prefix = "sbar$"))
@Mixin(ServerBossInfo.class)
public abstract class BossInfoServerMixin_API extends BossInfoMixin_API implements ServerBossBar {

    @Shadow private boolean visible;
    @Shadow public abstract void shadow$addPlayer(ServerPlayerEntity player);
    @Shadow public abstract void removePlayer(ServerPlayerEntity player);
    @Shadow public abstract void shadow$setVisible(boolean visibleIn);
    @Shadow public abstract Collection<ServerPlayerEntity> shadow$getPlayers();
    @Shadow private void sendUpdate(final SUpdateBossInfoPacket.Operation operation) { }

    @Override
    public ServerBossBar setName(final Text name) {
        if (this.name != name) {
            super.setName(name);
            this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_NAME);
        }

        return this;
    }

    @Override
    public ServerBossBar setPercent(final float percent) {
        if (this.percent != percent) {
            super.setPercent(percent);
            this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_PCT);
        }

        return this;
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public ServerBossBar setColor(final BossBarColor color) {
        if ((Object) this.color != color) {
            super.setColor(color);
            this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
        }

        return (ServerBossBar) this;
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public ServerBossBar setOverlay(final BossBarOverlay overlay) {
        if ((Object) this.overlay != overlay) {
            super.setOverlay(overlay);
            this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
        }

        return (ServerBossBar) this;
    }

    @Override
    public ServerBossBar setDarkenSky(final boolean darkenSky) {
        super.setDarkenSky(darkenSky);
        return this;
    }

    @Override
    public ServerBossBar setPlayEndBossMusic(final boolean playEndBossMusic) {
        super.setPlayEndBossMusic(playEndBossMusic);
        return this;
    }

    @Override
    public ServerBossBar setCreateFog(final boolean createFog) {
        this.setCreateFog(createFog);
        return this;
    }

    @Intrinsic
    @SuppressWarnings("unchecked")
    public Collection<Player> sbar$getPlayers() {
        return (Collection<Player>) (Object) this.shadow$getPlayers();
    }

    @Override
    public ServerBossBar addPlayer(final Player player) {
        this.shadow$addPlayer((ServerPlayerEntity) player);
        return this;
    }

    @Override
    public ServerBossBar removePlayer(final Player player) {
        this.removePlayer((ServerPlayerEntity) player);
        return this;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public ServerBossBar setVisible(final boolean visible) {
        this.shadow$setVisible(visible);
        return this;
    }

}
