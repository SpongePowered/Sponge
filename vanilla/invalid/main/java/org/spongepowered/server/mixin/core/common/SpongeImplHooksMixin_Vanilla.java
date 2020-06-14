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
package org.spongepowered.server.mixin.core.common;

import static org.spongepowered.server.launch.VanillaLaunch.Environment.DEVELOPMENT;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.server.launch.VanillaLaunch;
import org.spongepowered.server.launch.plugin.PluginSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Mixin(value = SpongeImplHooks.class, remap = false)
public abstract class SpongeImplHooksMixin_Vanilla {

    /**
     * @author Aaron1011
     * @reason Return true when in deobfuscated environment
     */
    @Overwrite
    public static boolean isDeobfuscatedEnvironment() {
        return VanillaLaunch.ENVIRONMENT == DEVELOPMENT;
    }

    /**
     * @author Minecrell
     * @reason Return correct mod ID for plugins
     */
    @Overwrite
    public static String getModIdFromClass(Class<?> clazz) {
        final String className = clazz.getName();
        if (className.startsWith("net.minecraft.")) {
            return "minecraft";
        }
        if (className.startsWith("org.spongepowered.")) {
            return "sponge";
        }

        // Lookup source location of the class and try to match it to a plugin
        return PluginSource.find(clazz).map(classSource -> {
            for (PluginContainer plugin : Sponge.getPluginManager().getPlugins()) {
                Optional<Path> pluginSource = plugin.getSource();
                try {
                    if (pluginSource.isPresent() && Files.isSameFile(classSource, pluginSource.get())) {
                        return plugin.getId();
                    }
                } catch (IOException ignored) {
                }
            }

            return null;
        }).orElse("unknown");
    }

    /**
     * @author gabizou - October 9th, 2018
     * @reason Since the common implementation does not know
     * what type of ecosystem this is, we have to overwrite it
     * to return the correct ecosystem id, while sponge common
     * keeps "sponge".
     * @return This implementation's ecosystem id.
     */
    @Overwrite
    public static String getImplementationId() {
        return "spongevanilla";
    }

}
