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
package org.spongepowered.common.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.keyboard.KeyCategories;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.text.Text;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

public class SpongeKeyBindingBuilder implements KeyBinding.Builder {

    @Nullable private KeyCategory keyCategory;
    @Nullable private BiConsumer<Player, KeyBinding> pressExecutor;
    @Nullable private BiConsumer<Player, KeyBinding> releaseExecutor;
    @Nullable private BiConsumer<Player, KeyBinding> tickExecutor;
    private Text displayName;
    private String id;

    @Override
    public KeyBinding.Builder category(@Nullable KeyCategory category) {
        this.keyCategory = category;
        return this;
    }

    @Override
    public KeyBinding.Builder id(String id) {
        checkNotNull(id, "id");
        checkArgument(!id.isEmpty(), "The id may not be empty");
        this.id = id;
        return this;
    }

    @Override
    public KeyBinding.Builder displayName(Text displayName) {
        this.displayName = checkNotNull(displayName, "displayName");
        return this;
    }

    @Override
    public KeyBinding.Builder pressExecutor(@Nullable BiConsumer<Player, KeyBinding> executor) {
        this.pressExecutor = executor;
        return this;
    }

    @Override
    public KeyBinding.Builder releaseExecutor(@Nullable BiConsumer<Player, KeyBinding> executor) {
        this.releaseExecutor = executor;
        return this;
    }

    @Override
    public KeyBinding.Builder tickExecutor(@Nullable BiConsumer<Player, KeyBinding> executor) {
        this.tickExecutor = executor;
        return this;
    }

    @Override
    public KeyBinding build() {
        checkState(this.displayName != null, "The display name must be set");
        checkState(this.id != null, "The id must be set");
        return new SpongeKeyBinding(this.id, (SpongeKeyCategory) (this.keyCategory == null ? KeyCategories.MISC : this.keyCategory),
                this.displayName, this.pressExecutor, this.releaseExecutor, this.tickExecutor);
    }

    @Override
    public KeyBinding.Builder from(KeyBinding value) {
        this.keyCategory = value.getCategory();
        this.id = value.getId();
        this.displayName = value.getDisplayName();
        return this;
    }

    @Override
    public KeyBinding.Builder reset() {
        this.keyCategory = null;
        this.id = null;
        this.displayName = null;
        return this;
    }
}
