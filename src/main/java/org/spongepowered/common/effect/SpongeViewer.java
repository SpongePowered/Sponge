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
package org.spongepowered.common.effect;

import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.common.bridge.effect.ViewerBridge;
import org.spongepowered.common.effect.util.ViewerPacketUtil;

import java.util.Objects;

public interface SpongeViewer extends Viewer {

    // Viewer

    @Override
    default void sendWorldType(final WorldType worldType) {
        ((ViewerBridge) this).bridge$sendSpongePacketToViewer(ViewerPacketUtil.changeEnvironment(worldType));
    }

    @Override
    default void spawnParticles(final ParticleEffect particleEffect, final double x, final double y, final double z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.spawnParticles(particleEffect, x, y, z));
    }

    @Override
    default void playMusicDisc(final int x, final int y, final int z, final MusicDisc musicDisc) {
    }

    @Override
    default void stopMusicDisc(final int x, final int y, final int z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.stopMusicDisc(x, y, z));
    }

    @Override
    default void sendBlockChange(final int x, final int y, final int z, final BlockState state) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.blockUpdate(x, y, z, state));
    }

    @Override
    default void resetBlockChange(final int x, final int y, final int z) {
    }

    @Override
    default void sendBlockProgress(final int x, final int y, final int z, final double progress) {
    }

    @Override
    default void resetBlockProgress(final int x, final int y, final int z) {
    }

    // Audience

    @Override
    default void stopSound(final SoundStop stop) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.stopSound(stop));
    }

    @Override
    default void sendActionBar(final Component message) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.setActionBarText(message));
    }

    @Override
    default void showTitle(final Title title) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.showTitle(title));
    }

    @Override
    default <T> void sendTitlePart(final TitlePart<T> part, final T value) {
        Objects.requireNonNull(value, "value");
        if (part == TitlePart.TITLE) {
            ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.setTitleText((Component) value));
        } else if (part == TitlePart.SUBTITLE) {
            ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.setSubtitleText((Component) value));
        } else if (part == TitlePart.TIMES) {
            ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.setTitlesAnimation((Title.Times) value));
        } else {
            throw new IllegalArgumentException("Unknown TitlePart '" + part + "'");
        }
    }

    @Override
    default void clearTitle() {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.clearTitles(false));
    }

    @Override
    default void resetTitle() {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.clearTitles(true));
    }
}
