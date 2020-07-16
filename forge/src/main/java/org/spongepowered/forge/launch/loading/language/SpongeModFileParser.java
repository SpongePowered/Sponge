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
package org.spongepowered.forge.launch.loading.language;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for providing Forge with Sponge metadata in their
 * implementation-specific format.
 *
 * @author Jamie Mansfield
 */
public final class SpongeModFileParser {

    private static final Logger log = LogManager.getLogger();

    public static IModFileInfo readModList(final SpongeModFile modFile, final PluginMetadata metadata) {
        final Config config = Config.inMemory();
        config.set("modLoader", "sponge");
        config.set("loaderVersion", "[28,)");

        final List<UnmodifiableConfig> mods = new ArrayList<>();
        final Config modConfig = Config.inMemory();
        modConfig.set("modId", metadata.getId());
        metadata.getName().ifPresent(name -> {
            modConfig.set("displayName", name);
        });
        modConfig.set("version", metadata.getVersion());
        metadata.getDescription().ifPresent(description -> {
            modConfig.set("description", description);
        });
        mods.add(modConfig);

        final Config properties = Config.inMemory();
//        properties.set("sponge-metadata", metadata);

        final Config modProperties = Config.inMemory();
        modProperties.set(metadata.getId(), properties);

        config.set("mods", mods);
        config.set("modproperties", modProperties);

        return create(modFile, config);
    }

    public static ModFileInfo create(final ModFile file, final UnmodifiableConfig metadata) {
        try {
            final Constructor<ModFileInfo> cstr = ModFileInfo.class.getDeclaredConstructor(ModFile.class, UnmodifiableConfig.class);
            cstr.setAccessible(true);
            return cstr.newInstance(file, metadata);
        }
        catch (final Throwable ex) {
            log.error("Failed to create ModFileInfo", ex);
            return null;
        }
    }


    private SpongeModFileParser() {
    }

}
