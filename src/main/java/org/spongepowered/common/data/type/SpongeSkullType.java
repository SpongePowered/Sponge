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

import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.text.translation.SpongeTranslation;

public class SpongeSkullType extends SpongeCatalogType.Translatable implements SkullType {

    private final byte dataId;
    private final String name;

    public SpongeSkullType(byte dataId, String id, String name) {
        this(dataId, id, name, translate(name));
    }

    public SpongeSkullType(byte dataId, String id, String name, Translation translation) {
        super(id, translation);
        this.dataId = dataId;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected static Translation translate(String name) {
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
        } else if (name.equalsIgnoreCase("ENDER_DRAGON")) {
            return new SpongeTranslation("item.skull.dragon.name");
        } else {
            throw new IllegalArgumentException("Unsupported Skull Type \"" + name + "\"!");
        }
    }

    public byte getByteId() {
        return this.dataId;
    }

}
