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

import static com.google.common.base.Preconditions.checkNotNull;

@Mixin(BossInfo.class)
@Implements(@Interface(iface = BossBar.class, prefix = "bar$"))
public abstract class MixinBossInfo {

    @Shadow protected ITextComponent name;
    @Shadow protected BossInfo.Color color;
    @Shadow protected BossInfo.Overlay overlay;
    @Shadow public abstract UUID getUniqueId();
    public abstract ITextComponent getName();
    public abstract float getPercent();
    public abstract void setPercent(float percentIn);
    public abstract BossInfo.Color getColor();
    public abstract BossInfo.Overlay getOverlay();
    public abstract boolean shouldDarkenSky();
    public abstract BossInfo setDarkenSky(boolean darkenSkyIn);
    public abstract boolean shouldPlayEndBossMusic();
    public abstract BossInfo setPlayEndBossMusic(boolean playEndBossMusicIn);
    public abstract BossInfo setCreateFog(boolean createFogIn);
    public abstract boolean shouldCreateFog();

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
