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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

import javax.annotation.Nullable;

public class SpongeTitleBuilder implements Title.Builder {

    private static final Title EMPTY = new SpongeTitle(null, null, null, null, null, null, false, false);
    private static final Title CLEAR = new SpongeTitle(null, null, null, null, null, null, true, false);
    private static final Title RESET = new SpongeTitle(null, null, null, null, null, null, false, true);

    @Nullable private Text title;
    @Nullable private Text subtitle;
    @Nullable private Text actionBar;
    @Nullable private Integer fadeIn;
    @Nullable private Integer stay;
    @Nullable private Integer fadeOut;
    private boolean clear;
    private boolean reset;

    @Override
    public Builder from(Title value) {
        this.title = value.getTitle().orElse(null);
        this.subtitle = value.getSubtitle().orElse(null);
        this.actionBar = value.getActionBar().orElse(null);
        this.fadeIn = value.getFadeIn().orElse(null);
        this.stay = value.getStay().orElse(null);
        this.fadeOut = value.getFadeOut().orElse(null);
        this.clear = value.isClear();
        this.reset = value.isReset();
        return this;
    }

    @Override
    public Builder title(@Nullable Text title) {
        this.title = title;
        return this;
    }

    @Override
    public Builder subtitle(@Nullable Text subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @Override
    public Builder actionBar(@Nullable Text actionBar) {
        this.actionBar = actionBar;
        return this;
    }

    @Override
    public Builder fadeIn(@Nullable Integer fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    @Override
    public Builder stay(@Nullable Integer stay) {
        this.stay = stay;
        return this;
    }

    @Override
    public Builder fadeOut(@Nullable Integer fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    @Override
    public Builder clear(boolean clear) {
        if (this.clear = clear) {
            this.title = null; // No need to send title if we clear it after that again
        }
        return this;
    }

    @Override
    public Builder reset(boolean reset) {
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
        // If the title has no other properties and is either empty, just clears or just resets so we can return a special instance
        if (this.title == null && this.subtitle == null && this.actionBar == null
                && this.fadeIn == null && this.stay == null && this.fadeOut == null) {
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

        return new SpongeTitle(this.title, this.subtitle, this.actionBar, this.fadeIn, this.stay, this.fadeOut, this.clear, this.reset);
    }

}
