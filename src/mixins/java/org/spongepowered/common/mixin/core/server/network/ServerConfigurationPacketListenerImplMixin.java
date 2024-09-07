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
package org.spongepowered.common.mixin.core.server.network;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.network.ConnectionAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.channel.TransactionStore;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.LocaleCache;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImplMixin {

    // @formatter:off
    @Shadow @Final private GameProfile gameProfile;
    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;
    @Shadow @Nullable private ConfigurationTask currentTask;

    @Shadow public abstract void shadow$handleConfigurationFinished(ServerboundFinishConfigurationPacket $$0);
    @Shadow protected abstract void shadow$finishCurrentTask(ConfigurationTask.Type $$0);
    // @formatter:on

    private static final ConfigurationTask.Type impl$SPONGE_CONFIGURATION_TYPE = new ConfigurationTask.Type("sponge_configuration");

    private boolean impl$skipBanService;

    @Inject(method = "handleConfigurationFinished", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V"))
    private void impl$onHandleConfigurationFinished(final ServerboundFinishConfigurationPacket $$0, final CallbackInfo ci) {
        if (this.impl$skipBanService) {
            return;
        }

        ci.cancel();

        ((PlayerListBridge) this.server.getPlayerList()).bridge$canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile)
                .handle((componentOpt, throwable) -> {
                    if (throwable != null) {
                        // An error occurred during login checks so we ask to abort.
                        ((ConnectionBridge) this.connection).bridge$setKickReason(Component.literal("An error occurred checking ban/whitelist status."));
                        SpongeCommon.logger().error("An error occurred when checking the ban/whitelist status of {}.", this.gameProfile.getId().toString());
                        SpongeCommon.logger().error(throwable);
                    } else if (componentOpt != null) {
                        // We handle this later
                        ((ConnectionBridge) this.connection).bridge$setKickReason(componentOpt);
                    }

                    try {
                        ((SpongeServer) SpongeCommon.server()).userManager().handlePlayerLogin(this.gameProfile);
                    } catch (final IOException e) {
                        throw new CompletionException(e);
                    }
                    return null;
                }).handleAsync((ignored, throwable) -> {
                    if (throwable != null) {
                        // We're just going to disconnect here, because something went horribly wrong.
                        if (throwable instanceof CompletionException) {
                            throw (CompletionException) throwable;
                        } else {
                            throw new CompletionException(throwable);
                        }
                    }

                    try {
                        this.impl$skipBanService = true;
                        this.shadow$handleConfigurationFinished($$0);
                        // invalidate just to be sure there is no user cached for the online player anymore
                        ((SpongeServer) SpongeCommon.server()).userManager().removeFromCache(this.gameProfile.getId());
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        this.impl$skipBanService = false;
                    }

                    return null;
                }, SpongeCommon.server()).exceptionally(throwable -> {
                    SpongeCommon.logger().error("Forcibly disconnecting user {}({}) due to an error during login.", this.gameProfile.getName(), this.gameProfile.getId(), throwable);
                    this.shadow$disconnect(Component.literal("Internal Server Error: unable to complete login."));
                    return null;
                });
    }

    @Redirect(method = "handleConfigurationFinished",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component impl$onCanPlayerLogin(final PlayerList instance, final SocketAddress $$0, final GameProfile $$1) {
        //Skip as we check it at the start.
        return null;
    }

    @Inject(method = "addOptionalTasks", at = @At("TAIL"))
    private void impl$callConfigurationEvent(final CallbackInfo ci) {
        this.configurationTasks.add(new ConfigurationTask() {
            @Override
            public void start(final Consumer<Packet<?>> var1) {
                final ServerSideConnection connection = (ServerSideConnection) ((ConnectionBridge) ServerConfigurationPacketListenerImplMixin.this.connection).bridge$getEngineConnection();
                final ServerSideConnectionEvent.Configuration event = SpongeEventFactory.createServerSideConnectionEventConfiguration(
                        PhaseTracker.getCauseStackManager().currentCause(),
                        connection,
                        SpongeGameProfile.of(ServerConfigurationPacketListenerImplMixin.this.gameProfile));
                SpongeCommon.post(event);
                final TransactionStore store = ConnectionUtil.getTransactionStore(connection);
                if (store.isEmpty()) {
                    ServerConfigurationPacketListenerImplMixin.this.shadow$finishCurrentTask(ServerConfigurationPacketListenerImplMixin.impl$SPONGE_CONFIGURATION_TYPE);
                }
            }

            @Override
            public Type type() {
                return ServerConfigurationPacketListenerImplMixin.impl$SPONGE_CONFIGURATION_TYPE;
            }
        });
    }

    @Override
    protected void impl$onCustomPayload() {
        if (this.currentTask == null || this.currentTask.type() != ServerConfigurationPacketListenerImplMixin.impl$SPONGE_CONFIGURATION_TYPE) {
            return;
        }

        final ServerSideConnection connection = (ServerSideConnection) ((ConnectionBridge) this.connection).bridge$getEngineConnection();
        final TransactionStore store = ConnectionUtil.getTransactionStore(connection);
        if (store.isEmpty()) {
            this.shadow$finishCurrentTask(ServerConfigurationPacketListenerImplMixin.impl$SPONGE_CONFIGURATION_TYPE);
        }
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private void impl$sendChannels(final CallbackInfo ci) {
        ((SpongeChannelManager) SpongeCommon.game().channelManager()).sendChannelRegistrations(
                ((ConnectionBridge) this.connection).bridge$getEngineConnection());
    }

    @Inject(method = "handleClientInformation", at = @At("TAIL"))
    private void impl$onHandleClientInformation(final ServerboundClientInformationPacket packet, final CallbackInfo ci) {
        final Channel channel = ((ConnectionAccessor) this.connection).accessor$channel();
        channel.attr(SpongeAdventure.CHANNEL_LOCALE).set(LocaleCache.getLocale(packet.information().language()));
    }
}
