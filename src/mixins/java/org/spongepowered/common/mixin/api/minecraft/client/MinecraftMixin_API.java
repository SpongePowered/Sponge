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
package org.spongepowered.common.mixin.api.minecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.repository.PackRepository;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.client.LocalServer;
import org.spongepowered.api.entity.living.player.client.LocalPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.ClientScheduler;
import org.spongepowered.common.util.BlockDestructionIdCache;
import org.spongepowered.common.util.LocaleCache;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_API implements SpongeClient, SpongeRegistryHolder {

    // @formatter:off
    @Shadow public net.minecraft.client.multiplayer.ClientLevel level;
    @Shadow public net.minecraft.client.player.LocalPlayer player;
    @Shadow @Nullable private Connection pendingConnection;
    @Shadow @Final public Options options;

    @Shadow @Nullable public abstract IntegratedServer shadow$getSingleplayerServer();
    @Shadow public abstract PackRepository shadow$getResourcePackRepository();
    @Shadow public abstract net.minecraft.server.packs.resources.ResourceManager shadow$getResourceManager();
    @Shadow @Nullable public abstract ClientPacketListener shadow$getConnection();
    // @formatter:on

    private final ClientScheduler api$scheduler = new ClientScheduler();
    private final RegistryHolderLogic api$registryHolder = new RegistryHolderLogic();
    private final BlockDestructionIdCache api$blockDestructionIdCache = new BlockDestructionIdCache(Integer.MIN_VALUE, AtomicInteger::incrementAndGet);

    @Override
    public Optional<LocalPlayer> player() {
        return Optional.ofNullable((LocalPlayer) this.player);
    }

    @Override
    public Optional<LocalServer> server() {
        final MinecraftBridge minecraftBridge = (MinecraftBridge) (this);
        final IntegratedServer integratedServer = minecraftBridge.bridge$getTemporaryIntegratedServer();

        if (integratedServer != null) {
            return (Optional<LocalServer>) (Object) Optional.ofNullable(integratedServer);
        }

        return (Optional<LocalServer>) (Object) Optional.ofNullable(this.shadow$getSingleplayerServer());
    }

    @Override
    public Optional<ClientWorld> world() {
        return Optional.ofNullable((ClientWorld) this.level);
    }

    @Override
    public Optional<ClientSideConnection> connection() {
        final @Nullable ClientPacketListener connection = this.shadow$getConnection();
        if (connection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ClientSideConnection) ((ConnectionBridge) connection.getConnection()).bridge$getEngineConnection());
    }

    @Override
    public Game game() {
        return Sponge.game();
    }

    @Override
    public CauseStackManager causeStackManager() {
        return PhaseTracker.getCauseStackManager();
    }

    @Override
    public org.spongepowered.api.resource.pack.PackRepository packRepository() {
        return (org.spongepowered.api.resource.pack.PackRepository) this.shadow$getResourcePackRepository();
    }

    @Override
    public ResourceManager resourceManager() {
        return (ResourceManager) this.shadow$getResourceManager();
    }

    @Override
    public ClientScheduler scheduler() {
        return this.api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return ((Minecraft) (Object) this).isSameThread();
    }

    @Override
    public Locale locale() {
        return LocaleCache.getLocale(this.options.languageCode);
    }

    @Override
    public RegistryHolderLogic registryHolder() {
        return this.api$registryHolder;
    }

    @Override
    public BlockDestructionIdCache getBlockDestructionIdCache() {
        return this.api$blockDestructionIdCache;
    }
}
