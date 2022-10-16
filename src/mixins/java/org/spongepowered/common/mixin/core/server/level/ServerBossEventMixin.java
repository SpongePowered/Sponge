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
package org.spongepowered.common.mixin.core.server.level;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.mixin.core.world.BossEventMixin;

import java.util.Set;

@Mixin(ServerBossEvent.class)
public abstract class ServerBossEventMixin extends BossEventMixin implements BossBar.Listener {

    private static final float EPSILON = 1e-2f;

    private float impl$lastSentProgress = 0f;

    @Shadow protected abstract void broadcast(final ClientboundBossEventPacket.Operation operation);
    @Shadow @Final private Set<ServerPlayer> players;
    @Shadow private boolean visible;

    @Override
    public void bridge$setAdventure(final BossBar adventure) {
        final BossBar oldAdventure = this.impl$adventure;
        super.bridge$setAdventure(adventure);
        if (oldAdventure != adventure) {
            if (oldAdventure != null) {
                oldAdventure.removeListener(this); // TODO(adventure): how to update viewers?
            }
            adventure.addListener(this);

            // Apply invalid data where possible, avoid sameness checks
            this.name = null;
            this.percent = Float.MIN_VALUE;
            this.color = null;
            this.overlay = null;
            // flags have to be done separately
        }
    }

    @Override
    public void bridge$replacePlayer(final ServerPlayer oldPlayer, final ServerPlayer newPlayer) {
        super.bridge$replacePlayer(oldPlayer, newPlayer);
        if(this.players.remove(oldPlayer)) {
            this.players.add(newPlayer);
        }
    }

    @Inject(method = "setDarkenScreen", at = @At("HEAD"))
    private void impl$forceDarkenSkyUpdate(final boolean darkenSky, final CallbackInfoReturnable<BossEvent> ci) {
        this.darkenScreen = !darkenSky;
    }

    @Inject(method = "setPlayBossMusic", at = @At("HEAD"))
    private void forcePlayEndBossMusicUpdate(final boolean endBossMusic, final CallbackInfoReturnable<BossEvent> ci) {
        this.playBossMusic = !endBossMusic;
    }

    @Inject(method = "setCreateWorldFog", at = @At("HEAD"))
    private void forceCreateFogUpdate(final boolean createFog, final CallbackInfoReturnable<BossEvent> ci) {
        this.createWorldFog = !createFog;
    }

    // Convert to using BossBar.Listener

    @Redirect(method = {"setPercent", "setColor", "setOverlay", "setDarkenScreen", "setPlayBossMusic", "setCreateWorldFog", "setName"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerBossEvent;broadcast(Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket$Operation;)V"))
    private void redirectUpdatePacket(final ServerBossEvent $this, final ClientboundBossEventPacket.Operation op) {
        // This becomes a no-op, the Adventure BossBar's listener calls this update operation
    }

    @Override
    public void bossBarNameChanged(final BossBar bar, final Component oldName, final Component newName) {
        this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_NAME);
    }

    @Override
    public void bossBarProgressChanged(final BossBar bar, final float oldProgress, final float newProgress) {
        if (Math.abs(newProgress - this.impl$lastSentProgress) > ServerBossEventMixin.EPSILON) {
            this.impl$lastSentProgress = newProgress;
            this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PCT);
        }
    }

    @Override
    public void bossBarColorChanged(final BossBar bar, final BossBar.Color oldColor, final BossBar.Color newColor) {
        this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarOverlayChanged(final BossBar bar, final BossBar.Overlay oldOverlay, final BossBar.Overlay newOverlay) {
        this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarFlagsChanged(final BossBar bar, final Set<BossBar.Flag> flagsAdded, final Set<BossBar.Flag> flagsRemoved) {
        this.broadcast(ClientboundBossEventPacket.Operation.UPDATE_PROPERTIES);
    }

    // Tracking for registration (designed for localization handling)

    @Inject(method = "addPlayer", at = @At("TAIL"))
    private void impl$addPlayer(final ServerPlayer player, final CallbackInfo ci) {
        if (!this.players.isEmpty() && this.visible) {
            SpongeAdventure.registerBossBar((ServerBossEvent) (Object) this);
        }
    }

    @Inject(method = "removePlayer", at = @At("TAIL"))
    private void impl$removePlayer(final ServerPlayer player, final CallbackInfo ci) {
        if (this.players.isEmpty()) {
            SpongeAdventure.unregisterBossBar((ServerBossEvent) (Object) this);
        }
    }

    @Inject(method = "removeAllPlayers", at = @At("HEAD"))
    private void impl$removeAllPlayers(final CallbackInfo ci) {
        SpongeAdventure.unregisterBossBar((ServerBossEvent) (Object) this);
    }

    @Inject(method = "setVisible", at = @At("HEAD"))
    private void impl$setVisible(final boolean visible, final CallbackInfo ci) {
        if (!this.players.isEmpty()) {
            if (visible) {
                SpongeAdventure.registerBossBar((ServerBossEvent) (Object) this);
            } else {
                SpongeAdventure.unregisterBossBar((ServerBossEvent) (Object) this);
            }
        }
    }
}
