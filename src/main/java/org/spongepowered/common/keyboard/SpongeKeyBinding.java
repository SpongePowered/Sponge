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

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.keyboard.KeyBinding;
import org.spongepowered.api.text.Text;

import java.util.Locale;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

public class SpongeKeyBinding implements KeyBinding {

    private final SpongeKeyCategory keyCategory;
    private final Text displayName;
    private final String id;
    private final String name;
    private final boolean def;

    @Nullable private final BiConsumer<Player, KeyBinding> pressExecutor;
    @Nullable private final BiConsumer<Player, KeyBinding> releaseExecutor;
    @Nullable private final BiConsumer<Player, KeyBinding> tickExecutor;

    private int internalId = -1;

    private SpongeKeyBinding(String id, String name, SpongeKeyCategory keyCategory, boolean def, Text displayName,
            @Nullable BiConsumer<Player, KeyBinding> pressExecutor,
            @Nullable BiConsumer<Player, KeyBinding> releaseExecutor,
            @Nullable BiConsumer<Player, KeyBinding> tickExecutor) {
        this.id = id.toLowerCase(Locale.ENGLISH);
        this.releaseExecutor = releaseExecutor;
        this.pressExecutor = pressExecutor;
        this.tickExecutor = tickExecutor;
        this.keyCategory = keyCategory;
        this.displayName = displayName;
        this.name = name;
        this.def = def;
    }

    public SpongeKeyBinding(String pluginId, String name, SpongeKeyCategory keyCategory, Text displayName, boolean def) {
        this(pluginId + ':' + name, name, keyCategory, def, displayName, null, null, null);
    }

    public SpongeKeyBinding(String id, SpongeKeyCategory keyCategory, Text displayName,
            @Nullable BiConsumer<Player, KeyBinding> pressExecutor,
            @Nullable BiConsumer<Player, KeyBinding> releaseExecutor,
            @Nullable BiConsumer<Player, KeyBinding> tickExecutor) {
        this(id, SpongeKeyCategory.getName(id), keyCategory, false, displayName, pressExecutor, releaseExecutor, tickExecutor);
    }

    public SpongeKeyBinding(String id, SpongeKeyCategory keyCategory, Text displayName) {
        this(id, SpongeKeyCategory.getName(id), keyCategory, false, displayName, null, null, null);
    }

    @Override
    public SpongeKeyCategory getCategory() {
        return this.keyCategory;
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public int getInternalId() {
        return this.internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public boolean isDefault() {
        return this.def;
    }

    @Nullable
    public BiConsumer<Player, KeyBinding> getPressExecutor() {
        return this.pressExecutor;
    }

    @Nullable
    public BiConsumer<Player, KeyBinding> getReleaseExecutor() {
        return this.releaseExecutor;
    }

    @Nullable
    public BiConsumer<Player, KeyBinding> getTickExecutor() {
        return this.tickExecutor;
    }
}
