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
package org.spongepowered.common.config.tracker;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class EntityTrackerCategory implements TrackerCategory {

    @Setting("auto-populate")
    @Comment("If 'true', newly discovered entities will be added to this config with default settings.")
    public boolean autoPopulate = false;

    @Setting
    @Comment("Per-mod entity id mappings for controlling tracking behavior")
    public final Map<String, NamespacedCategory> mods = new HashMap<>();

    public EntityTrackerCategory() {
        // TODO This should be checked over and added/modified or moved elsewhere
        final NamespacedCategory vanilla = this.namespacedOrCreate("minecraft");
        vanilla.valueOrCreate("item").setCaptureBlocksInBulk(false);
        vanilla.valueOrCreate("experience_orb").setCaptureBlocksInBulk(false);
        vanilla.valueOrCreate("leash_hitch").setCaptureBlocksInBulk(false);
        vanilla.valueOrCreate("painting").setCaptureBlocksInBulk(false);
        vanilla.valueOrCreate("armor_stand").setCaptureBlocksInBulk(false);
        vanilla.valueOrCreate("llama_spit").setCaptureBlocksInBulk(false);
    }

    @Override
    public boolean autoPopulate() {
        return this.autoPopulate;
    }

    @Override
    public NamespacedCategory namespacedOrCreate(final String namespace) {
        return this.mods.computeIfAbsent(namespace, k -> new NamespacedCategory());
    }
}
