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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class OptimizationCategory extends ConfigCategory {

    private static final String PRE_MERGE_COMMENT = "If enabled, block item drops are pre-processed to avoid \n"
                                                    + "having to spawn extra entities that will be merged post spawning.\n"
                                                    + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge\n"
                                                    + "and when not to, however, in certain cases, some mods rely on items not being\n"
                                                    + "pre-merged and actually spawned, in which case, the items will flow right through\n"
                                                    + "without being merged.";

    @Setting(value = "ignore-unloaded-chunks-on-get-light", comment = "This prevents chunks being loaded for getting light values at specific block positions. May have side effects.")
    private boolean ignoreUnloadedChunkLighting = true;

    @Setting(value = "chunk-map-caching", comment = "Caches chunks internally for faster returns when querying at various positions")
    private boolean useCachedChunkMap = true;

    @Setting(value = "drops-pre-merge", comment = PRE_MERGE_COMMENT)
    private boolean preItemDropMerge = true;

    public boolean useIgnoreUloadedChunkLightingPatch() {
        return this.ignoreUnloadedChunkLighting;
    }

    public boolean isUseCachedChunkMap() {
        return this.useCachedChunkMap;
    }

    public boolean doDropsPreMergeItemDrops() {
        return this.preItemDropMerge;
    }

}