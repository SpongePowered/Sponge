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
package org.spongepowered.forge.launch;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.plugin.PluginContainer;

/**
 * An implementation of {@link ModContainer Forge's mod container} for
 * {@link PluginContainer Sponge plugins}.
 *
 * @author Jamie Mansfield
 */
public class SpongeModContainer extends ModContainer {

    private final ClassLoader modClassLoader;

    public SpongeModContainer(final IModInfo info, final ClassLoader modClassLoader) {
        super(info);
        this.contextExtension = () -> null;

        this.modClassLoader = modClassLoader;
    }

    @Override
    public boolean matches(final Object mod) {
        return false;
    }

    @Override
    public Object getMod() {
        return null;
    }

}
