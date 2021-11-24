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
package org.spongepowered.vanilla.installer.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;

public record GroupArtifactVersion(String group, String artifact, @Nullable String version) {

    public static GroupArtifactVersion parse(final String notation) {
        final String[] split = notation.split(":");
        if (split.length > 4 || split.length < 2) {
            throw new IllegalArgumentException("Unsupported notation '" + notation + "', must be in the format of group:artifact[:version[:classifier]]");
        }
        return new GroupArtifactVersion(split[0], split[1], split.length > 2 ? split[2] : null);
    }

    public Path resolve(final Path base) {
        final Path artifact = base.resolve(this.group.replace('.', '/')).resolve(this.artifact);
        return this.version == null ? artifact : artifact.resolve(this.version);
    }

    @Override
    public String toString() {
        return this.group() + ':' + this.artifact() + (this.version() == null ? "" : ':' + this.version());
    }

}
