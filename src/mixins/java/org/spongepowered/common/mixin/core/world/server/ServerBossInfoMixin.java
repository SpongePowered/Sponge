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
package org.spongepowered.common.mixin.core.world.server;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.mixin.core.world.BossInfoMixin;

import java.util.Set;

@Mixin(ServerBossInfo.class)
public abstract class ServerBossInfoMixin extends BossInfoMixin implements BossBar.Listener {

    private static final float EPSILON = 1e-2f;

    private float impl$lastSentPercent = 0f;

    @Shadow protected abstract void sendUpdate(final SUpdateBossInfoPacket.Operation operation);

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

    @Inject(method = "setDarkenSky", at = @At("HEAD"))
    private void forceDarkenSkyUpdate(final boolean darkenSky, final CallbackInfoReturnable<BossInfo> ci) {
        this.darkenSky = !darkenSky;
    }

    @Inject(method = "setPlayEndBossMusic", at = @At("HEAD"))
    private void forcePlayEndBossMusicUpdate(final boolean endBossMusic, final CallbackInfoReturnable<BossInfo> ci) {
        this.playEndBossMusic = !endBossMusic;
    }

    @Inject(method = "setCreateFog", at = @At("HEAD"))
    private void forceCreateFogUpdate(final boolean createFog, final CallbackInfoReturnable<BossInfo> ci) {
        this.createFog = !createFog;
    }

    // Convert to using BossBar.Listener

    @Redirect(method = {"setPercent", "setColor", "setOverlay", "setDarkenSky", "setPlayEndBossMusic", "setCreateFog", "setName"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerBossInfo;sendUpdate(Lnet/minecraft/network/play/server/SUpdateBossInfoPacket$Operation;)V"))
    private void redirectUpdatePacket(final ServerBossInfo $this, final SUpdateBossInfoPacket.Operation op) {
        // This becomes a no-op, the Adventure BossBar's listener calls this update operation
    }

    @Override
    public void bossBarNameChanged(final BossBar bar, final Component oldName, final Component newName) {
        this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_NAME);
    }

    @Override
    public void bossBarPercentChanged(final BossBar bar, final float oldPercent, final float newPercent) {
        if (Math.abs(newPercent - this.impl$lastSentPercent) > EPSILON) {
            this.impl$lastSentPercent = newPercent;
            this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_PCT);
        }
    }

    @Override
    public void bossBarColorChanged(final BossBar bar, final BossBar.Color oldColor, final BossBar.Color newColor) {
        this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarOverlayChanged(final BossBar bar, final BossBar.Overlay oldOverlay, final BossBar.Overlay newOverlay) {
        this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
    }

    @Override
    public void bossBarFlagsChanged(final BossBar bar, final Set<BossBar.Flag> flagsAdded, final Set<BossBar.Flag> flagsRemoved) {
        this.sendUpdate(SUpdateBossInfoPacket.Operation.UPDATE_PROPERTIES);
    }
}
