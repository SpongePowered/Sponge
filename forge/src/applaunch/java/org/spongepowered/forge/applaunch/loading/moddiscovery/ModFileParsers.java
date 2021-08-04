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

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.spongepowered.common.applaunch.AppLaunch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModFileParsers {

    private static Constructor<ModFileInfo> modFileInfoConstructor;
    private static Field modFileInfoField;

    static {
        try {
            ModFileParsers.modFileInfoConstructor = ModFileInfo.class.getDeclaredConstructor(ModFile.class, IConfigurable.class);
            ModFileParsers.modFileInfoField = NightConfigWrapper.class.getDeclaredField("file");
        } catch (final NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static IModFileInfo dummySpongeModParser(final String fileName, final IModFile imodFile) {
        ModFile modFile = (ModFile)imodFile;
        AppLaunch.logger().debug("Considering mod file candidate {}", modFile.getFilePath());
        final Path modsjson = modFile.getLocator().findPath(modFile, fileName + ".toml");
        if (!Files.exists(modsjson)) {
            AppLaunch.logger().warn("Mod file {} is missing mods.toml file", modFile);
            return null;
        } else {
            final FileConfig fileConfig = FileConfig.builder(modsjson).build();
            fileConfig.load();
            fileConfig.close();
            final NightConfigWrapper configWrapper = new NightConfigWrapper(fileConfig);
            ModFileInfo modFileInfo;
            try {
                ModFileParsers.modFileInfoConstructor.setAccessible(true);
                modFileInfo = ModFileParsers.modFileInfoConstructor.newInstance(modFile, configWrapper);
                ModFileParsers.modFileInfoConstructor.setAccessible(false);
                ModFileParsers.modFileInfoField.setAccessible(true);
                ModFileParsers.modFileInfoField.set(configWrapper, modFileInfo);
                ModFileParsers.modFileInfoField.setAccessible(false);
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
            return modFileInfo;
        }
    }

    private ModFileParsers() {
    }
}
