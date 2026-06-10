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

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.AutomaticEventSubscriber;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.plugin.loader.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginDependency;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public final class PluginModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final PluginCandidate candidate;
    private final Module module;

    private final ModFileScanData scanData;
    private final BusGroup busGroup;
    private final CountDownLatch initializationLock;

    private @MonotonicNonNull PluginContainer container;

    public PluginModContainer(final IModInfo info, final ModFileScanData scanData, final ModuleLayer layer) {
        super(info);
        LOGGER.debug(Logging.LOADING, "Creating PluginModContainer instance for {}", info.getModId());
        this.scanData = scanData;
        this.activityMap.put(ModLoadingStage.CONSTRUCT, this::constructPlugin);
        this.busGroup = BusGroup.create("modBusFor" + info.getModId());
        this.contextExtension = () -> null;
        this.initializationLock = new CountDownLatch(1);

        final PluginResource resource = info.getOwningFile().getConfig().<PluginResource>getConfigElement("spongeResource").get();
        final PluginMetadata metadata = info.getConfig().<PluginMetadata>getConfigElement("spongeMetadata").get();
        this.candidate = Launch.instance().pluginFactory().create(resource, metadata);
        this.module = layer.findModule(PluginModContainer.getModuleName(info.getOwningFile())).orElseThrow();
    }

    private static String getModuleName(final IModFileInfo fileInfo) {
        return fileInfo.getMods().isEmpty() ? fileInfo.getFile().getSecureJar().name() : fileInfo.getMods().getFirst().getModId();
    }

    private void constructPlugin() {
        try {
            LOGGER.trace(Logging.LOADING, "Loading plugin container {}", getModId());

            this.candidate.metadata().dependencies().stream()
                .filter(d -> d.loadOrder() == PluginDependency.LoadOrder.AFTER)
                .flatMap(d -> ModList.get().getModContainerById(d.id()).stream())
                .filter(m -> m instanceof PluginModContainer)
                .forEach(m -> ((PluginModContainer) m).waitForInitialization());

            ModList.get().forEachModInOrder(m -> {
                if (m instanceof PluginModContainer p && p.candidate.metadata().dependencies().stream()
                    .anyMatch(d -> d.id().equals(this.getModId()) && d.loadOrder() == PluginDependency.LoadOrder.BEFORE)) {
                    p.waitForInitialization();
                }
            });

            this.container = this.candidate.load();
            LOGGER.trace(Logging.LOADING, "Loaded plugin container {}", getModId());

            this.initializationLock.countDown();
        } catch (Exception e) {
            LOGGER.error(Logging.LOADING, "Failed to create plugin container {}.", getModId(), e);
            throw new ModLoadingException(this.modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        }

        try {
            LOGGER.trace(Logging.LOADING, "Injecting Automatic event subscribers for {}", getModId());
            AutomaticEventSubscriber.inject(this, this.scanData, this.module.getClassLoader());
            LOGGER.trace(Logging.LOADING, "Completed Automatic event subscribers for {}", getModId());
        } catch (Throwable e) {
            LOGGER.error(Logging.LOADING, "Failed to register automatic subscribers. ModID: {}", getModId(), e);
            throw new ModLoadingException(this.modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", e);
        }
    }

    @Override
    public boolean matches(Object mod) {
        return mod == this.container.instance();
    }

    @Override
    public Object getMod() {
        return this.container.instance();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends IModBusEvent> void acceptEvent(final T e) {
        try {
            LOGGER.trace(Logging.LOADING, "Firing event for modid {} : {}", this.getModId(), e);
            IModBusEvent.getBus(this.busGroup, (Class<T>) e.getClass()).post(e);
            LOGGER.trace(Logging.LOADING, "Fired event for modid {} : {}", this.getModId(), e);
        } catch (Throwable t) {
            LOGGER.error(Logging.LOADING, "Caught exception during event {} dispatch for modid {}", e, this.getModId(), t);
            throw new ModLoadingException(this.modInfo, this.modLoadingStage, "fml.modloading.errorduringevent", t);
        }
    }

    @Override
    public void dispatchConfigEvent(final IConfigEvent event) {
        EventBus.create(this.busGroup, event.self().getClass()).post(event.self());
    }

    public Optional<PluginContainer> container() {
        return Optional.ofNullable(this.container);
    }

    private void waitForInitialization() {
        try {
            this.initializationLock.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
