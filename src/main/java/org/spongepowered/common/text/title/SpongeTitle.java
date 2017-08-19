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
package org.spongepowered.common.text.title;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpongeTitle implements Title {

    private final Optional<Text> title;
    private final Optional<Text> subtitle;
    private final Optional<Text> actionBar;
    private final Optional<Integer> fadeIn;
    private final Optional<Integer> stay;
    private final Optional<Integer> fadeOut;
    private final boolean clear;
    private final boolean reset;

    @Nullable private List<SPacketTitle> packets;

    SpongeTitle(@Nullable Text title, @Nullable Text subtitle, @Nullable Text actionBar, @Nullable Integer fadeIn,
            @Nullable Integer stay, @Nullable Integer fadeOut, boolean clear, boolean reset) {
        this.title = Optional.ofNullable(title);
        this.subtitle = Optional.ofNullable(subtitle);
        this.actionBar = Optional.ofNullable(actionBar);
        this.fadeIn = Optional.ofNullable(fadeIn);
        this.stay = Optional.ofNullable(stay);
        this.fadeOut = Optional.ofNullable(fadeOut);
        this.clear = clear;
        this.reset = reset;
    }

    @Override
    public Optional<Text> getTitle() {
        return this.title;
    }

    @Override
    public Optional<Text> getSubtitle() {
        return this.subtitle;
    }

    @Override
    public Optional<Text> getActionBar() {
        return this.actionBar;
    }

    @Override
    public Optional<Integer> getFadeIn() {
        return this.fadeIn;
    }

    @Override
    public Optional<Integer> getStay() {
        return this.stay;
    }

    @Override
    public Optional<Integer> getFadeOut() {
        return this.fadeOut;
    }

    @Override
    public boolean isClear() {
        return this.clear;
    }

    @Override
    public boolean isReset() {
        return this.reset;
    }

    @Override
    public Builder toBuilder() {
        return Title.builder().from(this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.title, this.subtitle, this.actionBar, this.fadeIn, this.stay, this.fadeOut, this.clear, this.reset);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("title", this.title)
                .add("subtitle", this.subtitle)
                .add("actionBar", this.actionBar)
                .add("fadeIn", this.fadeIn)
                .add("stay", this.stay)
                .add("fadeOut", this.fadeOut)
                .add("clear", this.clear)
                .add("reset", this.reset)
                .toString();
    }

    public void sendTo(EntityPlayerMP player) {
        for (SPacketTitle packet : this.getPackets()) {
            player.connection.sendPacket(packet);
        }
    }

    @SuppressWarnings("OptionalIsPresent")
    private List<SPacketTitle> getPackets() {
        if (this.packets == null) {
            this.packets = new ArrayList<>();
            if (this.clear) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.CLEAR, null));
            }
            if (this.reset) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.RESET, null));
            }
            if (this.fadeIn.isPresent() || this.stay.isPresent() || this.fadeOut.isPresent()) {
                this.packets.add(new SPacketTitle(this.fadeIn.orElse(20), this.stay.orElse(60), this.fadeOut.orElse(20)));
            }
            if (this.subtitle.isPresent()) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.SUBTITLE, ((IMixinText) this.subtitle.get()).toComponent()));
            }
            if (this.actionBar.isPresent()) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.ACTIONBAR, ((IMixinText) this.actionBar.get()).toComponent()));
            }
            if (this.title.isPresent()) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.TITLE, ((IMixinText) this.title.get()).toComponent()));
            }
        }

        return this.packets;
    }

}
