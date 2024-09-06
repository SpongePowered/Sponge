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
package org.spongepowered.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Client;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.entity.SpongeEntityTypes;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.neoforge.hook.NeoChannelHooks;
import org.spongepowered.neoforge.hook.NeoEventHooks;
import org.spongepowered.neoforge.hook.NeoGeneralHooks;
import org.spongepowered.neoforge.hook.NeoItemHooks;
import org.spongepowered.neoforge.hook.NeoWorldHooks;

@Mod("spongeneo")
public final class SpongeNeoMod {

    private final Logger logger = LogManager.getLogger("spongeneo");

    public SpongeNeoMod(IEventBus modBus) {
        // modBus: add all FML events with it
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onEntityAttributeCreationEvent);

        // annotation events, for non-FML things
        NeoForge.EVENT_BUS.register(this);

        // Set platform hooks as required
        PlatformHooks.INSTANCE.setEventHooks(new NeoEventHooks());
        PlatformHooks.INSTANCE.setWorldHooks(new NeoWorldHooks());
        PlatformHooks.INSTANCE.setGeneralHooks(new NeoGeneralHooks());
        PlatformHooks.INSTANCE.setChannelHooks(new NeoChannelHooks());
        PlatformHooks.INSTANCE.setItemHooks(new NeoItemHooks());
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

        this.logger.info("SpongeNeo v{} initialized", Launch.instance().platformPlugin().metadata().version());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        final Client minecraft = (Client) Minecraft.getInstance();
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        lifecycle.establishClientRegistries(minecraft);
        lifecycle.callStartingEngineEvent(minecraft);
    }

    @SubscribeEvent
    public void onServerAboutToStart(final ServerAboutToStartEvent event) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishServerFeatures();

        lifecycle.establishServerRegistries((Server) event.getServer());
        lifecycle.callStartingEngineEvent((Server) event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStartedEngineEvent((Server) event.getServer());

        lifecycle.callLoadedGameEvent();
    }

    @SubscribeEvent
    public void onServerStoppingEvent(final ServerStoppingEvent event) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStoppingEngineEvent((Server) event.getServer());
    }

    public void onRegister(RegisterEvent event) {
        // TODO NeoForge
        /* if (event.getRegistryKey() == NeoForgeRegistries.Keys.ENTITY_TYPES) {
            SpongeEntityTypes.HUMAN = EntityType.Builder.of(HumanEntity::new, MobCategory.MISC)
                    .noSave()
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(org.spongepowered.common.util.Constants.Entity.Player.TRACKING_RANGE)
                    .updateInterval(2)
                    .build("sponge:human")
            ;

            event.register(NeoForgeRegistries.Keys.ENTITY_TYPES, helper -> helper.register(HumanEntity.KEY, SpongeEntityTypes.HUMAN));
        } */
    }

    public void onEntityAttributeCreationEvent(final EntityAttributeCreationEvent event) {
        event.put(SpongeEntityTypes.HUMAN, HumanEntity.createAttributes());
    }
}
