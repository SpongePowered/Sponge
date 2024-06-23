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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.api.effect.ForwardingViewer;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.common.bridge.effect.ViewerBridge;

public interface SpongeForwardingViewer extends SpongeViewer, ForwardingViewer, ViewerBridge {

    // ViewerBridge

    @Override
    default void bridge$sendSpongePacketToViewer(final org.spongepowered.api.network.channel.packet.Packet packet) {
        this.audiences().forEach(viewer -> ((ViewerBridge) viewer).bridge$sendSpongePacketToViewer(packet));
    }

    @Override
    default void bridge$sendToViewer(final Packet<ClientGamePacketListener> packet) {
        this.audiences().forEach(viewer -> ((ViewerBridge) viewer).bridge$sendToViewer(packet));
    }

    // Viewer

    @Override
    default void playMusicDisc(final int x, final int y, final int z, final MusicDisc musicDisc) {
        this.audiences().forEach(viewer -> viewer.playMusicDisc(x, y, z, musicDisc));
    }

    @Override
    default void resetBlockChange(final int x, final int y, final int z) {
        this.audiences().forEach(viewer -> viewer.resetBlockChange(x, y, z));
    }

    @Override
    default void sendBlockProgress(final int x, final int y, final int z, final double progress) {
        this.audiences().forEach(viewer -> viewer.sendBlockProgress(x, y, z, progress));
    }

    @Override
    default void resetBlockProgress(final int x, final int y, final int z) {
        this.audiences().forEach(viewer -> viewer.resetBlockProgress(x, y, z));
    }

    // Audience

    @Override
    default void stopSound(final SoundStop stop) {
        SpongeViewer.super.stopSound(stop);
    }

    @Override
    default void sendActionBar(final Component message) {
        SpongeViewer.super.sendActionBar(message);
    }

    @Override
    default void showTitle(final Title title) {
        SpongeViewer.super.showTitle(title);
    }

    @Override
    default <T> void sendTitlePart(final TitlePart<T> part, final T value) {
        SpongeViewer.super.sendTitlePart(part, value);
    }

    @Override
    default void clearTitle() {
        SpongeViewer.super.clearTitle();
    }

    @Override
    default void resetTitle() {
        SpongeViewer.super.resetTitle();
    }
}
