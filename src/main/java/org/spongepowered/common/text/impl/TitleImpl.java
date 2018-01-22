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
package org.spongepowered.common.text.impl;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public final class TitleImpl implements Title {

    public static final TitleImpl EMPTY = new TitleImpl();
    public static final TitleImpl CLEAR = new TitleImpl(null, null, null, null, null, null, true, false);
    public static final TitleImpl RESET = new TitleImpl(null, null, null, null, null, null, false, true);

    final Optional<Text> title;
    final Optional<Text> subtitle;
    final Optional<Text> actionBar;
    final Optional<Integer> fadeIn;
    final Optional<Integer> stay;
    final Optional<Integer> fadeOut;
    final boolean clear;
    final boolean reset;
    @Nullable private List<SPacketTitle> packets;

    private TitleImpl() {
        this(null, null, null, null, null, null, false, false);
    }

    TitleImpl(@Nullable final Text title, @Nullable final Text subtitle, @Nullable final Text actionBar, @Nullable final Integer fadeIn, @Nullable final Integer stay,
            @Nullable final Integer fadeOut, final boolean clear, final boolean reset) {
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
    public BuilderImpl toBuilder() {
        return new BuilderImpl(this);
    }

    public void send(final EntityPlayerMP player) {
        for (final SPacketTitle packet : this.getPackets()) {
            player.connection.sendPacket(packet);
        }
    }

    private List<SPacketTitle> getPackets() {
        if (this.packets == null) {
            this.packets = new ArrayList<>();
            if (this.clear) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.RESET, null)); // SPacketTitle.Type.RESET is actually CLEAR
            }
            if (this.reset) {
                this.packets.add(new SPacketTitle(SPacketTitle.Type.RESET, null));
            }
            if (this.fadeIn.isPresent() || this.stay.isPresent() || this.fadeOut.isPresent()) {
                this.packets.add(new SPacketTitle(this.fadeIn.orElse(20), this.stay.orElse(60), this.fadeOut.orElse(20)));
            }
            this.subtitle.ifPresent(text -> this.packets.add(new SPacketTitle(SPacketTitle.Type.SUBTITLE, ((TextImpl) text).asComponentCopy())));
            this.actionBar.ifPresent(text -> this.packets.add(new SPacketTitle(SPacketTitle.Type.ACTIONBAR, ((TextImpl) text).asComponentCopy())));
            this.title.ifPresent(text -> this.packets.add(new SPacketTitle(SPacketTitle.Type.TITLE, ((TextImpl) text).asComponentCopy())));
        }

        return this.packets;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TitleImpl)) {
            return false;
        }

        final TitleImpl that = (TitleImpl) o;
        return this.title.equals(that.title)
                && this.subtitle.equals(that.subtitle)
                && this.actionBar.equals(that.actionBar)
                && this.fadeIn.equals(that.fadeIn)
                && this.stay.equals(that.stay)
                && this.fadeOut.equals(that.fadeOut)
                && this.clear == that.clear
                && this.reset == that.reset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.subtitle, this.actionBar, this.fadeIn, this.stay, this.fadeOut, this.clear, this.reset);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("title", this.title.orElse(null))
                .add("subtitle", this.subtitle.orElse(null))
                .add("actionBar", this.actionBar.orElse(null))
                .add("fadeIn", this.fadeIn.orElse(null))
                .add("stay", this.stay.orElse(null))
                .add("fadeOut", this.fadeOut.orElse(null))
                .add("clear", this.clear)
                .add("reset", this.reset)
                .toString();
    }

    public static final class BuilderImpl implements Title.Builder {

        @Nullable private Text title;
        @Nullable private Text subtitle;
        @Nullable private Text actionBar;
        @Nullable private Integer fadeIn;
        @Nullable private Integer stay;
        @Nullable private Integer fadeOut;
        private boolean clear;
        private boolean reset;

        public BuilderImpl() {
        }

        BuilderImpl(final Title title) {
            this.title = title.getTitle().orElse(null);
            this.subtitle = title.getSubtitle().orElse(null);
            this.actionBar = title.getActionBar().orElse(null);
            this.fadeIn = title.getFadeIn().orElse(null);
            this.stay = title.getStay().orElse(null);
            this.fadeOut = title.getFadeOut().orElse(null);
            this.clear = title.isClear();
            this.reset = title.isReset();
        }

        @Override
        public Optional<Text> getTitle() {
            return Optional.ofNullable(this.title);
        }

        @Override
        public BuilderImpl title(@Nullable final Text title) {
            this.title = title;
            return this;
        }

        @Override
        public Optional<Text> getSubtitle() {
            return Optional.ofNullable(this.subtitle);
        }

        @Override
        public BuilderImpl subtitle(@Nullable final Text subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        @Override
        public Optional<Text> getActionBar() {
            return Optional.ofNullable(this.actionBar);
        }

        @Override
        public BuilderImpl actionBar(@Nullable final Text actionBar) {
            this.actionBar = actionBar;
            return this;
        }

        @Override
        public Optional<Integer> getFadeIn() {
            return Optional.ofNullable(this.fadeIn);
        }

        @Override
        public BuilderImpl fadeIn(@Nullable final Integer fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        @Override
        public Optional<Integer> getStay() {
            return Optional.ofNullable(this.stay);
        }

        @Override
        public BuilderImpl stay(@Nullable final Integer stay) {
            this.stay = stay;
            return this;
        }

        @Override
        public Optional<Integer> getFadeOut() {
            return Optional.ofNullable(this.fadeOut);
        }

        @Override
        public BuilderImpl fadeOut(@Nullable final Integer fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        @Override
        public boolean isClear() {
            return this.clear;
        }

        @Override
        public BuilderImpl clear() {
            return this.clear(true);
        }

        @Override
        public BuilderImpl clear(final boolean clear) {
            if (this.clear = clear) {
                this.title = null; // No need to send title if we clear it after
                // that again
            }
            return this;
        }

        @Override
        public boolean isReset() {
            return this.reset;
        }

        @Override
        public BuilderImpl doReset() {
            return this.doReset(true);
        }

        @Override
        public BuilderImpl doReset(final boolean reset) {
            if (this.reset = reset) {
                // No need for these if we reset it again after that
                this.title = null;
                this.subtitle = null;
                this.fadeIn = null;
                this.stay = null;
                this.fadeOut = null;
            }
            return this;
        }

        @Override
        public Title build() {
            // If the title has no other properties and is either empty, just clears
            // or just resets we can return a special instance
            if (this.title == null
                    && this.subtitle == null
                    && this.actionBar == null
                    && this.fadeIn == null
                    && this.stay == null
                    && this.fadeOut == null) {
                if (this.clear) {
                    if (!this.reset) {
                        return CLEAR;
                    }
                } else if (this.reset) {
                    return RESET;
                } else {
                    return EMPTY;
                }
            }

            return new TitleImpl(
                    this.title, this.subtitle, this.actionBar,
                    this.fadeIn, this.stay, this.fadeOut,
                    this.clear, this.reset);
        }

        @Override
        public Builder from(final Title value) {
            return new BuilderImpl(value);
        }

        @Override
        public Builder reset() {
            return new BuilderImpl();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BuilderImpl)) {
                return false;
            }

            final BuilderImpl that = (BuilderImpl) o;
            return Objects.equals(this.title, that.title)
                    && Objects.equals(this.subtitle, that.subtitle)
                    && Objects.equals(this.actionBar, that.actionBar)
                    && Objects.equals(this.fadeIn, that.fadeIn)
                    && Objects.equals(this.stay, that.stay)
                    && Objects.equals(this.fadeOut, that.fadeOut)
                    && this.clear == that.clear
                    && this.reset == that.reset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.title, this.subtitle, this.actionBar, this.fadeIn, this.stay, this.fadeOut, this.clear, this.reset);
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
    }
}
