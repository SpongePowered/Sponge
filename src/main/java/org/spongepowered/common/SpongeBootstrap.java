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
package org.spongepowered.common;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.spongepowered.api.Sponge;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.inject.SpongeGuice;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.plugin.Blackboard;

import java.util.List;

public final class SpongeBootstrap {

    private static SpongeLifecycle lifecycle;

    public static Blackboard.Key<Injector> PARENT_INJECTOR = Blackboard.Key.of("parent_injector", Injector.class);

    public static void perform(final String engineName, final Runnable engineStart) {
        final Stage stage = SpongeGuice.getInjectorStage(Launch.getInstance().getInjectionStage());
        SpongeCommon.getLogger().debug("Creating injector in stage '{}'", stage);
        final List<Module> modules = Lists.newArrayList(
                new SpongeModule(),
                new SpongeCommonModule()
        );
        final Injector bootstrapInjector = Guice.createInjector(stage, modules);

        Launch.getInstance().getPluginEngine().getPluginEnvironment().getBlackboard().getOrCreate(SpongeBootstrap.PARENT_INJECTOR,
                () -> bootstrapInjector);
        SpongeBootstrap.lifecycle = bootstrapInjector.getInstance(SpongeLifecycle.class);
        SpongeBootstrap.lifecycle.establishFactories();
        SpongeBootstrap.lifecycle.establishBuilders();
        SpongeBootstrap.lifecycle.initTimings();
        Launch.getInstance().loadPlugins();
        SpongeBootstrap.lifecycle.registerPluginListeners();
        SpongeBootstrap.lifecycle.callConstructEvent();
        SpongeBootstrap.lifecycle.callRegisterFactoryEvent();
        SpongeBootstrap.lifecycle.callRegisterBuilderEvent();
        SpongeBootstrap.lifecycle.callRegisterChannelEvent();
        SpongeBootstrap.lifecycle.establishGameServices();
        SpongeBootstrap.lifecycle.establishDataKeyListeners();

        SpongePacketHandler.init((SpongeChannelRegistry) Sponge.channelRegistry());

        Launch.getInstance().getLogger().info("Loading Minecraft {}, please wait...", engineName);
        engineStart.run();
    }

    public static SpongeLifecycle getLifecycle() {
        return SpongeBootstrap.lifecycle;
    }
}
