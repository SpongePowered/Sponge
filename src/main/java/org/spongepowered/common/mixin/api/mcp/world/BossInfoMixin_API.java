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
public abstract class BossInfoMixin_API implements BossBar {

    @SuppressWarnings("NullableProblems")
    @Shadow protected ITextComponent name;
    @Shadow protected float percent;
    @SuppressWarnings("NullableProblems")
    @Shadow protected BossInfo.Color color;
    @SuppressWarnings("NullableProblems")
    @Shadow protected BossInfo.Overlay overlay;
    @Shadow public abstract UUID shadow$getUniqueId();
    @Shadow public abstract ITextComponent shadow$getName();
    @Shadow public abstract float shadow$getPercent();
    @Shadow public abstract void shadow$setPercent(float percentIn);
    @Shadow public abstract BossInfo.Color shadow$getColor();
    @Shadow public abstract BossInfo.Overlay shadow$getOverlay();
    @Shadow public abstract boolean shadow$shouldDarkenSky();
    @Shadow public abstract BossInfo shadow$setDarkenSky(boolean darkenSkyIn);
    @Shadow public abstract boolean shadow$shouldPlayEndBossMusic();
    @Shadow public abstract BossInfo shadow$setPlayEndBossMusic(boolean playEndBossMusicIn);
    @Shadow public abstract BossInfo shadow$setCreateFog(boolean createFogIn);
    @Shadow public abstract boolean shadow$shouldCreateFog();

    @Intrinsic
    public UUID bar$getUniqueId() {
        return this.shadow$getUniqueId();
    }

    @Override
    public Text getName() {
        return SpongeTexts.toText(this.shadow$getName());
    }

    @Override
    public BossBar setName(final Text name) {
        this.name = SpongeTexts.toComponent(checkNotNull(name, "name"));
        return this;
    }

    @Intrinsic
    public float bar$getPercent() {
        return this.shadow$getPercent();
    }

    @Override
    public BossBar setPercent(final float percent) {
        checkArgument(percent >= 0.0 && percent <= 1.0, "percent must be between 0.0f and 1.0f (was %s)", percent);
        this.shadow$setPercent(percent);
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BossBarColor getColor() {
        return (BossBarColor) (Object) this.shadow$getColor();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BossBar setColor(final BossBarColor color) {
        this.color = (BossInfo.Color) (Object) checkNotNull(color, "color");
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BossBarOverlay getOverlay() {
        return (BossBarOverlay) (Object) this.shadow$getOverlay();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BossBar setOverlay(final BossBarOverlay overlay) {
        this.overlay = (BossInfo.Overlay) (Object) checkNotNull(overlay, "overlay");
        return this;
    }

    @Intrinsic
    public boolean bar$shouldDarkenSky() {
        return this.shadow$shouldDarkenSky();
    }

    @Override
    public BossBar setDarkenSky(final boolean darkenSky) {
        this.shadow$setDarkenSky(darkenSky);
        return this;
    }

    @Intrinsic
    public boolean bar$shouldPlayEndBossMusic() {
        return this.shadow$shouldPlayEndBossMusic();
    }

    @Override
    public BossBar setPlayEndBossMusic(final boolean playEndBossMusic) {
        this.shadow$setPlayEndBossMusic(playEndBossMusic);
        return this;
    }

    @Intrinsic
    public boolean bar$shouldCreateFog() {
        return this.shadow$shouldCreateFog();
    }

    @Override
    public BossBar setCreateFog(final boolean createFog) {
        this.shadow$setCreateFog(createFog);
        return this;
    }

}
