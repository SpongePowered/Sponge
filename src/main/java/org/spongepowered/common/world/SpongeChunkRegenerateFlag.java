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
package org.spongepowered.common.world;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.ChunkRegenerateFlags;
import org.spongepowered.common.registry.type.world.ChunkRegenerateFlagRegistryModule;

/**
 * A flag of sorts that determines whether a chunk regeneration will perform various
 * tasks such as creating a chunk, loading a chunk, or keeping entities.
 */
public final class SpongeChunkRegenerateFlag implements ChunkRegenerateFlag {

    private final boolean create;
    private final boolean entities;
    private final int rawFlag;
    private final String name;

    public SpongeChunkRegenerateFlag(String name, int flag) {
        this.create = (name.equals("all") || name.contains("create"));
        this.entities = (name.equals("all") || name.contains("entities"));
        this.rawFlag = flag;
        this.name = name;
    }

    @Override
    public boolean create() {
        return this.create;
    }

    @Override
    public boolean entities() {
        return this.entities;
    }

    @Override
    public SpongeChunkRegenerateFlag withCreate(boolean create) {
        if (this.create == create) {
            return this;
        }
        return create ? andFlag(ChunkRegenerateFlags.CREATE) : andNotFlag(ChunkRegenerateFlags.CREATE);
    }

    @Override
    public SpongeChunkRegenerateFlag withEntities(boolean entities) {
        if (this.entities == entities) {
            return this;
        }
        return entities ? andFlag(ChunkRegenerateFlags.ENTITIES) : andNotFlag(ChunkRegenerateFlags.ENTITIES);
    }

    @Override
    public SpongeChunkRegenerateFlag andFlag(ChunkRegenerateFlag flag) {
        final SpongeChunkRegenerateFlag o = (SpongeChunkRegenerateFlag) flag;
        final int maskedFlag = (this.create || o.create ? ChunkRegenerateFlagRegistryModule.Flags.CREATE : 0)
                               | (this.entities || o.entities ? 0 : ChunkRegenerateFlagRegistryModule.Flags.ENTITIES);
        return ChunkRegenerateFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeChunkRegenerateFlag andNotFlag(ChunkRegenerateFlag flag) {
        final SpongeChunkRegenerateFlag o = (SpongeChunkRegenerateFlag) flag;
        final int maskedFlag = (this.create && !o.create ? ChunkRegenerateFlagRegistryModule.Flags.CREATE : 0)
                               | (this.entities && !o.entities ? 0 : ChunkRegenerateFlagRegistryModule.Flags.ENTITIES);
        return ChunkRegenerateFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    public String getName() {
        return this.name;
    }

    public int getRawFlag() {
        return this.rawFlag;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("rawFlag", this.rawFlag)
            .add("create", this.create)
            .add("entities", this.entities)
            .toString();
    }
}
