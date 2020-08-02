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
package org.spongepowered.common.mixin.api.mcp.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.client.LocalServer;
import org.spongepowered.api.entity.living.player.client.LocalPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.resource.pack.PackList;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.scheduler.ClientScheduler;
import org.spongepowered.common.scheduler.SpongeScheduler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_API extends RecursiveEventLoop<Runnable> implements SpongeClient {

    @Shadow public net.minecraft.client.world.ClientWorld world;
    @Shadow public ClientPlayerEntity player;
    @Shadow @Nullable private NetworkManager networkManager;
    @Shadow private IReloadableResourceManager resourceManager;
    @Shadow @Final private ResourcePackList<ClientResourcePackInfo> resourcePackRepository;
    @Shadow @Nullable public abstract IntegratedServer shadow$getIntegratedServer();

    private final SpongeScheduler api$scheduler = new ClientScheduler();

    public MinecraftMixin_API(String name) {
        super(name);
    }

    @Override
    public Optional<LocalPlayer> getPlayer() {
        return Optional.ofNullable((LocalPlayer) this.player);
    }

    @Override
    public Optional<LocalServer> getServer() {
        final MinecraftBridge minecraftBridge = (MinecraftBridge) (this);
        final IntegratedServer integratedServer = minecraftBridge.bridge$getTemporaryIntegratedServer();
        if (integratedServer != null) {
            return (Optional<LocalServer>) (Object) Optional.ofNullable(integratedServer);
        }

        return (Optional<LocalServer>) (Object) Optional.ofNullable(this.shadow$getIntegratedServer());
    }

    @Override
    public Optional<ClientWorld> getWorld() {
        return Optional.ofNullable((ClientWorld) this.world);
    }

    @Override
    public Optional<ClientSideConnection> getConnection() {
        if (this.networkManager == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ClientSideConnection) this.networkManager.getNetHandler());
    }

    @Override
    public Game getGame() {
        return Sponge.getGame();
    }

    @Override
    public CauseStackManager getCauseStackManager() {
        return PhaseTracker.getCauseStackManager();
    }

    @Override
    public Scheduler getScheduler() {
        return this.api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return this.isOnExecutionThread();
    }

    @Override
    public ResourceManager getResourceManager() {
        return (ResourceManager) this.resourceManager;
    }

    @Override
    public PackList getPackList() {
        return (PackList) this.resourcePackRepository;
    }

    @Override
    @Invoker
    public abstract CompletableFuture<Void> reloadResources();
}
