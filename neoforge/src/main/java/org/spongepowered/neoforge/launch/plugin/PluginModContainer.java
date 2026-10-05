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
package org.spongepowered.neoforge.launch.plugin;

import net.neoforged.bus.EventBusErrorMessage;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.Logging;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.plugin.loader.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.Optional;

public final class PluginModContainer extends ModContainer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final PluginCandidate candidate;
    private final Module module;

    private final ModFileScanData scanData;
    private final IEventBus eventBus;

    private @MonotonicNonNull PluginContainer container;

    public PluginModContainer(final IModInfo info, final ModFileScanData scanData, final ModuleLayer layer) {
        super(info);
        LOGGER.debug(Logging.LOADING, "Creating PluginModContainer instance for {}", info.getModId());
        this.scanData = scanData;
        this.eventBus = BusBuilder.builder()
            .setExceptionHandler(this::onEventFailed)
            .markerType(IModBusEvent.class)
            .allowPerPhasePost()
            .build();

        final PluginResource resource = info.getOwningFile().getConfig().<PluginResource>getConfigElement("spongeResource").get();
        final PluginMetadata metadata = info.getConfig().<PluginMetadata>getConfigElement("spongeMetadata").get();
        this.candidate = Launch.instance().pluginFactory().create(resource, metadata);
        this.module = layer.findModule(info.getOwningFile().getFile().getId()).orElseThrow();
    }

    private void onEventFailed(IEventBus iEventBus, Event event, EventListener[] iEventListeners, int i, Throwable throwable) {
        LOGGER.error(new EventBusErrorMessage(event, i, iEventListeners, throwable));
    }

    @Override
    protected void constructMod() {
        try {
            LOGGER.trace(Logging.LOADING, "Loading plugin container {}", getModId());
            this.container = this.candidate.load();
            LOGGER.trace(Logging.LOADING, "Loaded plugin container {}", getModId());
        } catch (Exception e) {
            LOGGER.error(Logging.LOADING, "Failed to create plugin container {}.", getModId(), e);
            throw new ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod").withCause(e).withAffectedMod(this.modInfo));
        }

        try {
            LOGGER.trace(Logging.LOADING, "Injecting Automatic event subscribers for {}", getModId());
            AutomaticEventSubscriber.inject(this, this.scanData, this.module);
            LOGGER.trace(Logging.LOADING, "Completed Automatic event subscribers for {}", getModId());
        } catch (Throwable e) {
            LOGGER.error(Logging.LOADING, "Failed to register automatic subscribers. ModID: {}", getModId(), e);
            throw new ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod").withCause(e).withAffectedMod(this.modInfo));
        }
    }

    @Override
    public IEventBus getEventBus() {
        return this.eventBus;
    }

    public Optional<PluginContainer> container() {
        return Optional.ofNullable(this.container);
    }
}
