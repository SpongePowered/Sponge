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
package org.spongepowered.common.world.gen.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.type.MushroomType;

public class SpongeMushroomType implements MushroomType {

    private final CatalogKey key;
    private final String name;
    private PopulatorObject obj;

    public SpongeMushroomType(final CatalogKey key, String name, PopulatorObject o) {
        this.key = key;
        this.name = name;
        this.obj = o;
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PopulatorObject getPopulatorObject() {
        return this.obj;
    }

    @Override
    public void setPopulatorObject(PopulatorObject object) {
        this.obj = checkNotNull(object);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SpongeBiomeTreeType)) {
            return false;
        }
        SpongeBiomeTreeType b = (SpongeBiomeTreeType) o;
        return getKey().equals(b.getKey());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.getKey())
                .add("popobj", this.obj.getClass().getName())
                .toString();
    }

}
