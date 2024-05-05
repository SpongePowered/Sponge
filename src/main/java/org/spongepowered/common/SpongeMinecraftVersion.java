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
package org.spongepowered.common;

import net.minecraft.SharedConstants;
import org.spongepowered.api.MinecraftVersion;

import java.util.OptionalInt;
import java.util.StringJoiner;

public final class SpongeMinecraftVersion implements MinecraftVersion {

    private final String name;
    private final int protocol;

    public SpongeMinecraftVersion(String name, int protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int protocolVersion() {
        return this.protocol;
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public OptionalInt dataVersion() {
        return OptionalInt.of(SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MinecraftVersion)) {
            return false;
        }
        return compareTo((MinecraftVersion) o) == 0;
    }

    @Override
    public int hashCode() {
        return this.protocol;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeMinecraftVersion.class.getSimpleName() + "[", "]")
                .add("name=" + this.name)
                .add("protocol=" + this.protocol)
                .toString();
    }
}
