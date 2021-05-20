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

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AddSpongeBackToClasspathModLocator extends AbstractJarFileLocator {
    private static final Path INVALID_PATH = Paths.get("This", "Path", "Should", "Never", "Exist", "Because", "That", "Would", "Be", "Stupid", "CON", "AUX", "/dev/null"); // via ModDiscoverer, thanks :)

    // Paths that will not be loaded through the TCL
    // does this even make sense?
    private static final String[] EXCLUDED_PATHS = {
        "org/spongepowered/common/applaunch/",
        "org/spongepowered/forge/applaunch/",
        "org/spongepowered/forge/launch/plugin/"
    };

    @Override
    public List<IModFile> scanMods() {
        try {
            final ModFile file = ModFile.newFMLInstance(Paths.get(SpongeModLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI()), this);
            this.modJars.compute(file, (mf, fs) -> this.createFileSystem(mf));
            return Collections.singletonList(file);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String name() {
        return "sponge-injector";
    }

    @Override
    public Path findPath(final IModFile modFile, final String... path) {
        final Path ret = super.findPath(modFile, path);
        if (this.isExcluded(ret)) {
            return AddSpongeBackToClasspathModLocator.INVALID_PATH;
        } else {
            return ret;
        }
    }

    @Override
    public void scanFile(final IModFile file, final Consumer<Path> pathConsumer) {
        super.scanFile(file, path -> {
            if (!this.isExcluded(path)) {
                pathConsumer.accept(path);
            }
        });
    }

    private boolean isExcluded(final Path excluded) {
        final String path = excluded.toString();
        for (final String test : AddSpongeBackToClasspathModLocator.EXCLUDED_PATHS) {
            if (path.startsWith(test)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
    }
}
