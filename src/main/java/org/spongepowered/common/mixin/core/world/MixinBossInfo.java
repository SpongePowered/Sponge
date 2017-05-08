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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.SpongeTexts;

import java.util.UUID;

@Implements(@Interface(iface = BossBar.class, prefix = "bar$"))
@Mixin(BossInfo.class)
@SuppressWarnings("WeakerAccess")
public abstract class MixinBossInfo {

    @SuppressWarnings("NullableProblems")
    @Shadow protected ITextComponent name;
    @Shadow protected float percent;
    @SuppressWarnings("NullableProblems")
    @Shadow protected BossInfo.Color color;
    @SuppressWarnings("NullableProblems")
    @Shadow protected BossInfo.Overlay overlay;
    @Shadow public abstract UUID getUniqueId();
    @Shadow public abstract ITextComponent getName();
    @Shadow public abstract float getPercent();
    @Shadow public abstract void setPercent(float percentIn);
    @Shadow public abstract BossInfo.Color getColor();
    @Shadow public abstract BossInfo.Overlay getOverlay();
    @Shadow public abstract boolean shouldDarkenSky();
    @Shadow public abstract BossInfo setDarkenSky(boolean darkenSkyIn);
    @Shadow public abstract boolean shouldPlayEndBossMusic();
    @Shadow public abstract BossInfo setPlayEndBossMusic(boolean playEndBossMusicIn);
    @Shadow public abstract BossInfo setCreateFog(boolean createFogIn);
    @Shadow public abstract boolean shouldCreateFog();

    @Intrinsic
    public UUID bar$getUniqueId() {
        return this.getUniqueId();
    }

    public Text bar$getName() {
        return SpongeTexts.toText(this.getName());
    }

    public BossBar bar$setName(Text name) {
        this.name = SpongeTexts.toComponent(checkNotNull(name, "name"));
        return (BossBar) this;
    }

    @Intrinsic
    public float bar$getPercent() {
        return this.getPercent();
    }

    public BossBar bar$setPercent(float percent) {
        checkArgument(percent >= 0.0 && percent <= 1.0, "percent must be between 0.0f and 1.0f (was %s)", percent);
        this.setPercent(percent);
        return (BossBar) this;
    }

    public BossBarColor bar$getColor() {
        return (BossBarColor) (Object) this.getColor();
    }

    public BossBar bar$setColor(BossBarColor color) {
        this.color = (BossInfo.Color) (Object) checkNotNull(color, "color");
        return (BossBar) this;
    }

    public BossBarOverlay bar$getOverlay() {
        return (BossBarOverlay) (Object) this.getOverlay();
    }

    public BossBar bar$setOverlay(BossBarOverlay overlay) {
        this.overlay = (BossInfo.Overlay) (Object) checkNotNull(overlay, "overlay");
        return (BossBar) this;
    }

    @Intrinsic
    public boolean bar$shouldDarkenSky() {
        return this.shouldDarkenSky();
    }

    public BossBar bar$setDarkenSky(boolean darkenSky) {
        this.setDarkenSky(darkenSky);
        return (BossBar) this;
    }

    @Intrinsic
    public boolean bar$shouldPlayEndBossMusic() {
        return this.shouldPlayEndBossMusic();
    }

    public BossBar bar$setPlayEndBossMusic(boolean playEndBossMusic) {
        this.setPlayEndBossMusic(playEndBossMusic);
        return (BossBar) this;
    }

    @Intrinsic
    public boolean bar$shouldCreateFog() {
        return this.shouldCreateFog();
    }

    public BossBar bar$setCreateFog(boolean createFog) {
        this.setCreateFog(createFog);
        return (BossBar) this;
    }

}
