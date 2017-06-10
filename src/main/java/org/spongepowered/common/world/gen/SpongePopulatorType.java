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
package org.spongepowered.common.world.gen;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.gen.PopulatorType;

import java.util.Locale;

public class SpongePopulatorType implements PopulatorType {

    public final String populatorName;
    public final String modId;

    public SpongePopulatorType(String name) {
        this(name.toLowerCase(Locale.ENGLISH), "minecraft");
    }

    public SpongePopulatorType(String name, String modId) {
        this.populatorName = name.toLowerCase(Locale.ENGLISH);
        this.modId = modId.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getId() {
        return this.modId + ":" + this.populatorName;
    }

    @Override
    public String getName() {
        return this.populatorName;
    }

    public String getModId() {
        return this.modId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpongePopulatorType other = (SpongePopulatorType) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.getId())
                .add("name", this.populatorName)
                .add("modid", this.modId)
                .toString();
    }

    @Override
    public Translation getTranslation() {
        return null;
    }
}
