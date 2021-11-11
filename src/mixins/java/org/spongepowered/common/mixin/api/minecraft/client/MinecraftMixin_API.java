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
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.client.LocalServer;
import org.spongepowered.api.entity.living.player.client.LocalPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.InitialRegistryData;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.ClientScheduler;
import org.spongepowered.common.util.LocaleCache;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_API implements SpongeClient, SpongeRegistryHolder {

    // @formatter:off
    @Shadow public net.minecraft.client.multiplayer.ClientLevel level;
    @Shadow public net.minecraft.client.player.LocalPlayer player;
    @Shadow @Nullable private Connection pendingConnection;
    @Shadow @Final public Options options;
    @Shadow @Nullable public abstract IntegratedServer shadow$getSingleplayerServer();
    // @formatter:on

    private final ClientScheduler api$scheduler = new ClientScheduler();
    private final RegistryHolderLogic api$registryHolder = new RegistryHolderLogic();

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
        if (this.pendingConnection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ClientSideConnection) this.pendingConnection.getPacketListener());
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
    public <T> Registry<T> registry(final RegistryType<T> type) {
        return this.api$registryHolder.registry(Objects.requireNonNull(type, "type"));
    }

    @Override
    public <T> Optional<Registry<T>> findRegistry(final RegistryType<T> type) {
        return this.api$registryHolder.findRegistry(Objects.requireNonNull(type, "type"));
    }

    @Override
    public Stream<Registry<?>> streamRegistries(final ResourceKey root) {
        return this.api$registryHolder.streamRegistries(Objects.requireNonNull(root, "root"));
    }

    @Override
    public void setRootMinecraftRegistry(final net.minecraft.core.Registry<net.minecraft.core.Registry<?>> registry) {
        this.api$registryHolder.setRootMinecraftRegistry(registry);
    }

    @Override
    public <T> Registry<T> createRegistry(final RegistryType<T> type, @org.checkerframework.checker.nullness.qual.Nullable
    final InitialRegistryData<T> defaultValues, final boolean isDynamic,
        @org.checkerframework.checker.nullness.qual.Nullable final BiConsumer<net.minecraft.resources.ResourceKey<T>, T> callback) {
        return this.api$registryHolder.createRegistry(type, defaultValues, isDynamic, callback);
    }

    @Override
    public <T> void wrapTagHelperAsRegistry(final RegistryType<Tag<T>> type, final StaticTagHelper<T> helper) {
        this.api$registryHolder.wrapTagHelperAsRegistry(type, helper);
    }

}
