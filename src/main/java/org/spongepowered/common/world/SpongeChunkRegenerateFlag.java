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

import org.spongepowered.api.world.ChunkRegenerateFlag;

import java.util.Objects;

public final class SpongeChunkRegenerateFlag implements ChunkRegenerateFlag {

    private final boolean create;
    private final boolean entities;

    public SpongeChunkRegenerateFlag(final boolean create, final boolean entities) {
        this.create = create;
        this.entities = entities;
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
    public ChunkRegenerateFlag withCreate(final boolean create) {
        return new SpongeChunkRegenerateFlag(create, this.entities);
    }

    @Override
    public ChunkRegenerateFlag withEntities(final boolean entities) {
        return new SpongeChunkRegenerateFlag(this.create, entities);
    }

    @Override
    public ChunkRegenerateFlag andFlag(final ChunkRegenerateFlag flag) {
        return new SpongeChunkRegenerateFlag(this.create && flag.create(), this.entities && flag.entities());
    }

    @Override
    public ChunkRegenerateFlag andNotFlag(final ChunkRegenerateFlag flag) {
        return new SpongeChunkRegenerateFlag(this.create && !flag.create(), this.entities && !flag.entities());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeChunkRegenerateFlag that = (SpongeChunkRegenerateFlag) o;
        return this.create == that.create && this.entities == that.entities;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.create, this.entities);
    }

}
