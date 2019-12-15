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
package org.spongepowered.common.advancement;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.advancements.DisplayInfoBridge;

import java.util.Optional;

public final class SpongeAdvancementTree implements AdvancementTree {

    private final Advancement rootAdvancement;
    private final CatalogKey key;
    private final Translation translation;

    public SpongeAdvancementTree(Advancement rootAdvancement, CatalogKey key, Translation translation) {
        this.rootAdvancement = rootAdvancement;
        this.key = key;
        this.translation = translation;
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return this.translation.get();
    }

    @Override
    public Advancement getRootAdvancement() {
        return this.rootAdvancement;
    }

    @Override
    public String getBackgroundPath() {
        final Optional<DisplayInfo> displayInfo = this.rootAdvancement.getDisplayInfo();
        if (displayInfo.isPresent()) {
            return ((DisplayInfoBridge) displayInfo.get()).bridge$getBackground();
        }
        return "SPONGE_MISSING_BACKGROUND_PATH";
    }
}
