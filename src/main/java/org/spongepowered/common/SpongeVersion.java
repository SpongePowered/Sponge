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

import static com.google.common.base.Objects.firstNonNull;

import org.spongepowered.api.MinecraftVersion;

import java.util.Optional;

public final class SpongeVersion {

    // TODO: Keep up to date
    public static final MinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion("1.8", 47);

    public static final String API_NAME = firstNonNull(getPackage().getSpecificationTitle(), SpongeImpl.API_NAME);
    public static final String API_VERSION = firstNonNull(getPackage().getSpecificationVersion(), "DEV");

    public static final Optional<String> IMPLEMENTATION_NAME = Optional.ofNullable(getPackage().getImplementationTitle());
    public static final String IMPLEMENTATION_VERSION =  firstNonNull(getPackage().getImplementationVersion(), "DEV");

    private SpongeVersion() {
    }

    private static Package getPackage() {
        return SpongeVersion.class.getPackage();
    }

}
