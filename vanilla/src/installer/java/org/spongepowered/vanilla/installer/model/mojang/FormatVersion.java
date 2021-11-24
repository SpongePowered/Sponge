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
package org.spongepowered.vanilla.installer.model.mojang;

/**
 * A version of the Minecraft bundler metadata format.
 *
 * @param major the major version
 * @param minor the minor version
 */
public record FormatVersion(int major, int minor) implements Comparable<FormatVersion> {

    /**
     * An attribute in the manifest of bundler jars containing a format version.
     *
     * @see #parse(String)
     */
    public static final String MANIFEST_ATTRIBUTE = "Bundler-Format";

    /**
     * Parse the manifest attribute version.
     *
     * @param attribute the attribute value
     * @return a parsed version
     * @throws NumberFormatException if any elements of the attribute are non-numeric
     * @throws IllegalArgumentException if the version does not have at least two elements
     */
    static FormatVersion parse(final String attribute) {
        final String[] split = attribute.split("\\.");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid version " + attribute);
        }
        return new FormatVersion(
            Integer.parseInt(split[0]),
            Integer.parseInt(split[1])
        );
    }

    /**
     * Whether a version of {@code other} can interpret data in this format version.
     *
     * @param other the compared version
     * @return whether other is compatible with this version's data
     */
    public boolean compatibleWith(final FormatVersion other) {
        return other.major() == this.major()
            && other.minor() >= this.minor();
    }

    @Override
    public int compareTo(final FormatVersion other) {
        if (this.major() != other.major()) {
            return Integer.compare(this.major(), other.major());
        } else {
            return Integer.compare(this.minor(), other.minor());
        }
    }
}
