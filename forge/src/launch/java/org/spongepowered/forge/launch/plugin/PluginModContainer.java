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
package org.spongepowered.forge.launch.plugin;

import static net.minecraftforge.fml.Logging.LOADING;

import com.google.inject.Injector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.AutomaticEventSubscriber;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.inject.plugin.PluginModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.forge.accessor.fml.javafmlmod.FMLModContainerAccessor;
import org.spongepowered.forge.launch.event.ForgeEventManager;
import org.spongepowered.plugin.PluginContainer;

// If this class name changes, fix FMLModContainerMixin_Forge
public final class PluginModContainer extends FMLModContainer {

    private static final Logger LOGGER = LogManager.getLogger();

    public PluginModContainer(final IModInfo info, final String className, final ClassLoader modClassLoader,
            final ModFileScanData modFileScanResults) {
        super(info, className, modClassLoader, modFileScanResults);

        this.activityMap.put(ModLoadingStage.CONSTRUCT, this::constructPlugin);
    }

    private void constructPlugin() {
        final FMLModContainerAccessor accessor = (FMLModContainerAccessor) (Object) this;
        try {
            PluginModContainer.LOGGER.trace(Logging.LOADING, "Loading plugin instance {} of type {}", getModId(), accessor.accessor$modClass().getName());
            final Injector childInjector = Launch.instance().lifecycle().platformInjector().createChildInjector(new PluginModule((PluginContainer) (Object) this, accessor.accessor$modClass()));
            final Object instance = childInjector.getInstance(accessor.accessor$modClass());
            accessor.accessor$setModInstance(instance);
            ((ForgeEventManager) MinecraftForge.EVENT_BUS).registerListeners((PluginContainer) (Object) this, instance);
            PluginModContainer.LOGGER.trace(Logging.LOADING, "Loaded plugin instance {} of type {}", getModId(), accessor.accessor$modClass().getName());
        } catch (final Throwable e) {
            PluginModContainer.LOGGER.error(Logging.LOADING,"Failed to create plugin instance. PluginID: {}, class {}", getModId(), accessor.accessor$modClass().getName(), e);
            throw new ModLoadingException(this.modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e, accessor.accessor$modClass());
        }
        try {
            PluginModContainer.LOGGER.trace(Logging.LOADING, "Injecting Automatic event subscribers for {}", getModId());
            AutomaticEventSubscriber.inject(this, accessor.accessor$scanResults(), accessor.accessor$modClass().getClassLoader());
            PluginModContainer.LOGGER.trace(Logging.LOADING, "Completed Automatic event subscribers for {}", getModId());
        } catch (final Throwable e) {
            LOGGER.error(LOADING,"Failed to register automatic subscribers. PluginID: {}, class {}", getModId(),
                    accessor.accessor$modClass().getName(), e);
            throw new ModLoadingException(this.modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e, accessor.accessor$modClass());
        }
    }
}
