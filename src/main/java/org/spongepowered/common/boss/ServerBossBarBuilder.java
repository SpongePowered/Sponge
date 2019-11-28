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
package org.spongepowered.common.boss;

import net.minecraft.world.BossInfo;
import net.minecraft.world.ServerBossInfo;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class ServerBossBarBuilder implements ServerBossBar.Builder {

    @Nullable private Text name;
    private float percent;
    @Nullable private BossBarColor color;
    @Nullable private BossBarOverlay overlay;
    private boolean darkenSky;
    private boolean playEndBossMusic;
    private boolean createFog;
    private boolean visible = true;

    @Override
    public ServerBossBar.Builder name(Text name) {
        this.name = name;
        return this;
    }

    @Override
    public ServerBossBar.Builder percent(float percent) {
        checkArgument(percent >= 0.0 && percent <= 1.0, "percent must be between 0.0f and 1.0f (was %s)", percent);
        this.percent = percent;
        return this;
    }

    @Override
    public ServerBossBar.Builder color(BossBarColor color) {
        this.color = checkNotNull(color, "color");
        return this;
    }

    @Override
    public ServerBossBar.Builder overlay(BossBarOverlay overlay) {
        this.overlay = checkNotNull(overlay, "overlay");
        return this;
    }

    @Override
    public ServerBossBar.Builder darkenSky(boolean darkenSky) {
        this.darkenSky = darkenSky;
        return this;
    }

    @Override
    public ServerBossBar.Builder playEndBossMusic(boolean playEndBossMusic) {
        this.playEndBossMusic = playEndBossMusic;
        return this;
    }

    @Override
    public ServerBossBar.Builder createFog(boolean createFog) {
        this.createFog = createFog;
        return this;
    }

    @Override
    public ServerBossBar.Builder visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public ServerBossBar.Builder from(ServerBossBar value) {
        this.name = value.getName();
        this.percent = value.getPercent();
        this.color = value.getColor();
        this.overlay = value.getOverlay();
        this.darkenSky = value.shouldDarkenSky();
        this.playEndBossMusic = value.shouldPlayEndBossMusic();
        this.createFog = value.shouldCreateFog();
        this.visible = value.isVisible();
        return this;
    }

    @Override
    public ServerBossBar.Builder reset() {
        this.name = null;
        this.percent = 0.0f;
        this.color = null;
        this.overlay = null;
        this.darkenSky = false;
        this.playEndBossMusic = false;
        this.createFog = false;
        this.visible = true;
        return this;
    }

    @Override
    public ServerBossBar build() {
        checkState(this.name != null, "name must be set");
        checkState(this.color != null, "color must be set");
        checkState(this.overlay != null, "overlay must be set");

        ServerBossInfo bar = new ServerBossInfo(
            SpongeTexts.toComponent(this.name),
            (BossInfo.Color) (Object) this.color,
            (BossInfo.Overlay) (Object) this.overlay
        );
        bar.setPercent(this.percent);
        bar.setDarkenSky(this.darkenSky);
        bar.setPlayEndBossMusic(this.playEndBossMusic);
        bar.setCreateFog(this.createFog);
        bar.setVisible(this.visible);

        return (ServerBossBar) bar;
    }

}
