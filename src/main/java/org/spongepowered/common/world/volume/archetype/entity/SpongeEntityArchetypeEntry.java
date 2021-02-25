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
package org.spongepowered.common.world.volume.archetype.entity;

import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.math.vector.Vector3d;

import java.util.Objects;
import java.util.StringJoiner;

public class SpongeEntityArchetypeEntry implements EntityArchetypeEntry {
    private final EntityArchetype archetype;
    private final Vector3d position;

    public SpongeEntityArchetypeEntry(final EntityArchetype archetype, final Vector3d position) {
        this.archetype = archetype;
        this.position = position;
    }

    @Override
    public EntityArchetype getArchetype() {
        return this.archetype;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeEntityArchetypeEntry that = (SpongeEntityArchetypeEntry) o;
        return this.archetype.equals(that.archetype) && this.position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.archetype, this.position);
    }

    @Override
    public String toString() {
        return new StringJoiner(
            ", ",
            SpongeEntityArchetypeEntry.class.getSimpleName() + "[",
            "]"
        )
            .add("archetype=" + this.archetype)
            .add("position=" + this.position)
            .toString();
    }
}
