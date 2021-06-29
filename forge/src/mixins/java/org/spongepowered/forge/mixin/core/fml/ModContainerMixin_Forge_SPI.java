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
package org.spongepowered.forge.mixin.core.fml;

import net.minecraftforge.fml.ModContainer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Mixin(ModContainer.class)
public abstract class ModContainerMixin_Forge_SPI implements PluginContainer {

    @Override
    public PluginMetadata metadata() {
        // TODO IModInfo -> PluginMetadata
        return null;
    }

    @Override
    public Path path() {
        return null;
    }

    @Override
    public Logger logger() {
        return null;
    }

    @Override
    public Object instance() {
        return null;
    }

    @Override
    public Optional<URL> locateResource(URL relative) {
        return Optional.empty();
    }

    @Override
    public Optional<InputStream> openResource(URL relative) {
        return PluginContainer.super.openResource(relative);
    }
}
