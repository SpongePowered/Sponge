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
package org.spongepowered.vanilla.mixin.api.client;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import org.spongepowered.api.client.LocalServer;
import org.spongepowered.api.entity.living.player.client.LocalPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.SpongeCauseStackManager;
import org.spongepowered.common.scheduler.ClientScheduler;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.vanilla.inject.SpongeVanillaModule;
import org.spongepowered.vanilla.inject.client.VanillaClientModule;
import org.spongepowered.vanilla.launch.VanillaClient;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Vanilla_API extends RecursiveEventLoop<Runnable> implements VanillaClient {

    @Shadow public net.minecraft.client.world.ClientWorld world;
    @Shadow public ClientPlayerEntity player;
    @Shadow @Nullable public abstract IntegratedServer shadow$getIntegratedServer();

    private final SpongeCauseStackManager vanilla_api$causeStackManager = new SpongeCauseStackManager();
    private final SpongeScheduler vanilla_api$scheduler = new ClientScheduler();

    public MinecraftMixin_Vanilla_API(String name) {
        super(name);
    }

    @Override
    public Optional<LocalPlayer> getPlayer() {
        return Optional.ofNullable((LocalPlayer) this.player);
    }

    @Override
    public Optional<LocalServer> getServer() {
        return Optional.ofNullable((LocalServer) this.shadow$getIntegratedServer());
    }

    @Override
    public Optional<ClientWorld> getWorld() {
        return Optional.ofNullable((ClientWorld) this.world);
    }

    @Override
    public CauseStackManager getCauseStackManager() {
        return this.vanilla_api$causeStackManager;
    }

    @Override
    public Scheduler getScheduler() {
        return this.vanilla_api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return this.isOnExecutionThread();
    }

    @Override
    public List<Module> createInjectionModules() {
        return Lists.newArrayList(
            new SpongeVanillaModule(),
            new VanillaClientModule(this)
        );
    }
}
