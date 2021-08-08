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
package org.spongepowered.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.WorldPersistenceHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Client;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.forge.data.SpongeLevelDataPersistence;
import org.spongepowered.forge.hook.ForgeEventHooks;

@Mod(Constants.MOD_ID)
public final class SpongeForge {

    private final Logger logger = LogManager.getLogger(Constants.MOD_ID);

    public SpongeForge() {
        WorldPersistenceHooks.addHook(SpongeLevelDataPersistence.INSTANCE);

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // modBus: add all FML events with it
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onClientSetup);

        // annotation events, for non-FML things
        MinecraftForge.EVENT_BUS.register(this);

        // Set platform hooks as required
        PlatformHooks.INSTANCE.setEventHooks(new ForgeEventHooks());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callConstructEvent();
        lifecycle.callRegisterFactoryEvent();
        lifecycle.callRegisterBuilderEvent();
        lifecycle.callRegisterChannelEvent();
        lifecycle.establishGameServices();
        lifecycle.establishDataKeyListeners();

        SpongePacketHandler.init((SpongeChannelManager) Sponge.channelManager());

        // TODO Add attributes for HumanEntity to relevant event

        this.logger.info("SpongeForge v{} initialized", Launch.instance().platformPlugin().metadata().version());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        final Client minecraft = (Client) event.getMinecraftSupplier().get();
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.establishClientRegistries(minecraft);
        lifecycle.callStartingEngineEvent(minecraft);
    }

    @SubscribeEvent
    public void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishServerFeatures();

        lifecycle.establishServerRegistries((Server) event.getServer());
        lifecycle.callStartingEngineEvent((Server) event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(final FMLServerStartedEvent event) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStartedEngineEvent((Server) event.getServer());
    }

    @SubscribeEvent
    public void onGameStopped(final FMLServerStoppedEvent event) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStoppingEngineEvent((Server) event.getServer());
    }

}
