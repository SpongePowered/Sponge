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

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;

@Mixin(BossEvent.class)
public abstract class BossEventMixin implements BossEventBridge {

    @Shadow protected Component name;
    @Shadow protected float progress;
    @Shadow protected BossEvent.BossBarColor color;
    @Shadow protected BossEvent.BossBarOverlay overlay;
    @Shadow protected boolean darkenScreen;
    @Shadow protected boolean playBossMusic;
    @Shadow protected boolean createWorldFog;

    protected BossBar impl$adventure;

    @Override
    public void bridge$copy(final BossBar adventure) {
        this.darkenScreen = adventure.hasFlag(BossBar.Flag.DARKEN_SCREEN);
        this.playBossMusic = adventure.hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
        this.createWorldFog = adventure.hasFlag(BossBar.Flag.CREATE_WORLD_FOG);
    }

    @Override
    public BossBar bridge$asAdventure() {
        if (this.impl$adventure == null) {
            this.bridge$setAdventure(BossBar.bossBar(
                SpongeAdventure.asAdventure(this.name),
                this.progress,
                SpongeAdventure.asAdventure(this.color),
                SpongeAdventure.asAdventure(this.overlay),
                SpongeAdventure.asAdventureFlags(this.darkenScreen, this.playBossMusic, this.createWorldFog)
            ));
            ((BossBarBridge) this.impl$adventure).bridge$setVanilla((BossEvent) (Object) this);
            ((BossBarBridge) this.impl$adventure).bridge$assignImplementation();
        }
        return this.impl$adventure;
    }

    @Override
    public void bridge$setAdventure(final BossBar adventure) {
        this.impl$adventure = adventure;
    }

    @Override
    public void bridge$replacePlayer(final ServerPlayer oldPlayer, final ServerPlayer newPlayer) {
        // no-op
    }

    // Redirect setters
    @Redirect(method = "setName", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;name:Lnet/minecraft/network/chat/Component;"))
    private void adventureName(final BossEvent $this, final Component name) {
        this.bridge$asAdventure().name(SpongeAdventure.asAdventure(name));
    }

    @Redirect(method = "setProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;progress:F"))
    private void adventurePercent(final BossEvent $this, final float percent) {
        this.bridge$asAdventure().progress(percent);
    }

    @Redirect(method = "setColor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;color:Lnet/minecraft/world/BossEvent$BossBarColor;"))
    private void adventureColor(final BossEvent $this, final BossEvent.BossBarColor color) {
        this.bridge$asAdventure().color(SpongeAdventure.asAdventure(color));
    }

    @Redirect(method = "setOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;overlay:Lnet/minecraft/world/BossEvent$BossBarOverlay;"))
    private void adventureOverlay(final BossEvent $this, final BossEvent.BossBarOverlay overlay) {
        this.bridge$asAdventure().overlay(SpongeAdventure.asAdventure(overlay));
    }

    @Redirect(method = "setDarkenScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;darkenScreen:Z"))
    private void adventureDarkenScreen(final BossEvent $this, final boolean darkenScreen) {
        if (darkenScreen) {
            this.bridge$asAdventure().addFlag(BossBar.Flag.DARKEN_SCREEN);
        } else {
            this.bridge$asAdventure().removeFlag(BossBar.Flag.DARKEN_SCREEN);
        }
    }

    @Redirect(method = "setPlayBossMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;playBossMusic:Z"))
    private void adventurePlayBossMusic(final BossEvent $this, final boolean playBossMusic) {
        if (playBossMusic) {
            this.bridge$asAdventure().addFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
        } else {
            this.bridge$asAdventure().removeFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
        }
    }

    @Redirect(method = "setCreateWorldFog", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;createWorldFog:Z"))
    private void adventureCreateWorldFog(final BossEvent $this, final boolean createWorldFog) {
        if (createWorldFog) {
            this.bridge$asAdventure().addFlag(BossBar.Flag.CREATE_WORLD_FOG);
        } else {
            this.bridge$asAdventure().removeFlag(BossBar.Flag.CREATE_WORLD_FOG);
        }
    }

    // Redirect getters

    @Redirect(method = "getName", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;name:Lnet/minecraft/network/chat/Component;"))
    private Component nameRead(final BossEvent $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().name());
    }

    @Redirect(method = "getProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;progress:F"))
    private float percentRead(final BossEvent $this) {
        return this.bridge$asAdventure().progress();
    }

    @Redirect(method = "getColor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;color:Lnet/minecraft/world/BossEvent$BossBarColor;"))
    private BossEvent.BossBarColor colorRead(final BossEvent $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().color());
    }

    @Redirect(method = "getOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;overlay:Lnet/minecraft/world/BossEvent$BossBarOverlay;"))
    private BossEvent.BossBarOverlay overlayRead(final BossEvent $this) {
        return SpongeAdventure.asVanilla(this.bridge$asAdventure().overlay());
    }

    @Redirect(method = "shouldDarkenScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;darkenScreen:Z"))
    private boolean darkenSkyRead(final BossEvent $this) {
        return this.bridge$asAdventure().hasFlag(BossBar.Flag.DARKEN_SCREEN);
    }

    @Redirect(method = "shouldPlayBossMusic", at =@At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;playBossMusic:Z"))
    private boolean playEndBossMusicRead(final BossEvent $this) {
        return this.bridge$asAdventure().hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
    }

    @Redirect(method = "shouldCreateWorldFog", at = @At(value = "FIELD", target = "Lnet/minecraft/world/BossEvent;createWorldFog:Z"))
    private boolean createFogRead(final BossEvent $this) {
        return this.bridge$asAdventure().hasFlag(BossBar.Flag.CREATE_WORLD_FOG);
    }
}
