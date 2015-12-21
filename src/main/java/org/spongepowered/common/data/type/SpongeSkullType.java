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
package org.spongepowered.common.data.type;

import com.google.common.base.Objects;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.translation.SpongeTranslation;

public class SpongeSkullType implements SkullType, TextRepresentable {

    private final byte id;
    private final String name;
    private final Translation translation;

    public SpongeSkullType(byte id, String name) {
        this.id = id;
        this.name = name;
        this.translation = translate(name);
    }

    protected Translation translate(String name) {
        if (name.equalsIgnoreCase("SKELETON")) {
            return new SpongeTranslation("item.skull.skeleton.name");
        } else if (name.equalsIgnoreCase("WITHER_SKELETON")) {
            return new SpongeTranslation("item.skull.wither.name");
        } else if (name.equalsIgnoreCase("ZOMBIE")) {
            return new SpongeTranslation("item.skull.zombie.name");
        } else if (name.equalsIgnoreCase("PLAYER")) {
            return new SpongeTranslation("item.skull.char.name");
        } else if (name.equalsIgnoreCase("CREEPER")) {
            return new SpongeTranslation("item.skull.creeper.name");
        } else {
            throw new IllegalArgumentException("Unsupported Skull Type \"" + name + "\"!");
        }
    }

    @Override
    public String getId() {
        return this.name; // todo later
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

    public byte getByteId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id, this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeSkullType other = (SpongeSkullType) obj;
        return Objects.equal(this.id, other.id)
               && Objects.equal(this.name, other.name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", this.id)
                .add("name", this.name)
                .toString();
    }

    @Override
    public Text toText() {
        return SpongeTexts.toText(this);
    }

}
