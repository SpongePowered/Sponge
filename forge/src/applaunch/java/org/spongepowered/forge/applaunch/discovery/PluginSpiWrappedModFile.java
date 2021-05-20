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
package org.spongepowered.forge.applaunch.discovery;

import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import org.spongepowered.plugin.PluginResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PluginSpiWrappedModFile implements IModFile {
    private final SpongeModLocator locator;
    private final PluginResource resource;

    public PluginSpiWrappedModFile(final SpongeModLocator locator, final PluginResource resource) {
        this.locator = locator;
        this.resource = resource;
    }

    @Override
    public IModLanguageProvider getLoader() {
        return null;
    }

    @Override
    public Path findResource(final String className) {
        return this.resource.fileSystem().getPath(className);
    }

    @Override
    public Supplier<Map<String, Object>> getSubstitutionMap() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.MOD;
    }

    @Override
    public Path getFilePath() {
        return this.resource.path();
    }

    @Override
    public List<IModInfo> getModInfos() {
        return null;
    }

    @Override
    public ModFileScanData getScanResult() {
        return null; // todo;
    }

    @Override
    public String getFileName() {
        return this.resource.path().getFileName().toString();
    }

    @Override
    public SpongeModLocator getLocator() {
        return this.locator;
    }

    @Override
    public IModFileInfo getModFileInfo() {
        return null;
    }
}
