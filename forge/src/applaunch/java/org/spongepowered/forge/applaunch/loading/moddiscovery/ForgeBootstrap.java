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
package org.spongepowered.forge.applaunch.loading.moddiscovery;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.Launcher;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.ModFileFactory;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.forge.applaunch.loading.moddiscovery.library.LibraryModFileFactory;
import org.spongepowered.forge.applaunch.loading.moddiscovery.library.LibraryModFileInfoParser;
import org.spongepowered.forge.applaunch.plugin.ForgePluginPlatform;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ForgeBootstrap extends AbstractJarFileLocator {
    private static final Path INVALID_PATH = Paths.get("This", "Path", "Should", "Never", "Exist", "Because", "That", "Would", "Be", "Stupid", "CON", "AUX", "/dev/null"); // via ModDiscoverer, thanks :)

    // Paths that will not be loaded through the TCL
    // does this even make sense?
    private static final String[] EXCLUDED_PATHS = {
        "org/spongepowered/common/applaunch/",
        "org/spongepowered/forge/applaunch/",
    };

    @Override
    public List<IModFile> scanMods() {
        // Add SpongeForge itself back as a Mod since FML dictates that any jars that have locators are *not* mods
        // TODO I think the actual fix here is to ship the locator code as a jar in jar...
        final List<IModFile> jars = new ArrayList<>();

        try {
            final ModFile spongeapi = this.newDummySpongeFile(Paths.get(ForgeBootstrap.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()), this, "spongeapimod");
            this.modJars.compute(spongeapi, (mf, fs) -> this.createFileSystem(mf));
            jars.add(spongeapi);

            final ModFile sponge = this.newDummySpongeFile(Paths.get(ForgeBootstrap.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()), this, "spongemod");
            this.modJars.compute(sponge, (mf, fs) -> this.createFileSystem(mf));
            jars.add(sponge);

            final ModFile spongeforge = ModFile.newFMLInstance(Paths.get(ForgeBootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI()), this);
            this.modJars.compute(spongeforge, (mf, fs) -> this.createFileSystem(mf));
            jars.add(spongeforge);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        // Add Sponge-specific libraries
        if (!FMLEnvironment.production) {
            for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
                try {
                    final URI uri = url.toURI();
                    final Path path = Paths.get(uri);
                    if (this.isLibrary(path)) {
                        final IModFile file = LibraryModFileFactory.INSTANCE.build(path, this, LibraryModFileInfoParser.INSTANCE);
                        this.modJars.compute(file, (mf, fs) -> this.createFileSystem(mf));
                        jars.add(file);
                    }
                } catch (final Exception ignore) {
                }
            }
        } else {
            // TODO We need to pull down our specific libraries (ala SV's installer), put into /libraries, then inject them into the TCL
        }

        return jars;
    }

    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public Path findPath(IModFile modFile, String... path) {
        final Path foundPath = super.findPath(modFile, path);
        if (this.isTCLExcluded(foundPath)) {
            return ForgeBootstrap.INVALID_PATH;
        }

        return foundPath;
    }

    @Override
    public void scanFile(final IModFile file, final Consumer<Path> pathConsumer) {
        super.scanFile(file, path -> {
            if (!this.isTCLExcluded(path)) {
                pathConsumer.accept(path);
            }
        });
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
        final ForgePluginPlatform platform = new ForgePluginPlatform(Launcher.INSTANCE.environment());
        AppLaunch.setPluginPlatform(platform);
        platform.init();
    }

    private boolean isTCLExcluded(final Path excluded) {
        final String path = excluded.toString();
        for (final String test : ForgeBootstrap.EXCLUDED_PATHS) {
            if (path.startsWith(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is so bad...
     */
    private boolean isLibrary(final Path path) {
        final String completePath = path.toString();
        return completePath.contains("kyori") || completePath.contains("SpongeAPI\\build\\libs");
    }

    private ModFile newDummySpongeFile(final Path path, final IModLocator locator, final String fileName) {
        return (ModFile) ModFileFactory.FACTORY.build(path, locator, file -> ModFileParsers.dummySpongeModParser(fileName, file));
    }
}
