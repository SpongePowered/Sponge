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
package org.spongepowered.common.util;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.channel.local.LocalAddress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.mixin.core.server.PlayerListAccessor;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class NetworkUtil {

    public static final String LOCAL_ADDRESS = "local";

    private NetworkUtil() {
    }

    /**
     * Returns a string representation of the host of a given {@link SocketAddress}.
     *
     * <ul>
     *     <li>For {@link InetSocketAddress} this will be a textual representation
     *     according to {@link InetSocketAddress#getHostString()}</li>
     *     <li>For {@link LocalAddress} this will be {@link #LOCAL_ADDRESS}</li>
     *     <li>For every other type this will be {@link SocketAddress#toString()}</li>
     * </ul>
     *
     * @param address The address to get the string representation of
     * @return The string representation of the host
     */
    public static String getHostString(final SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress) address).getHostString();
        } else if (address instanceof LocalAddress) {
            return LOCAL_ADDRESS;
        }

        return address.toString();
    }

    /**
     * Returns the cleaned hostname for the input sent by the client.
     *
     * @param host The host sent by the client
     * @return The cleaned hostname
     */
    public static String cleanVirtualHost(String host) {
        // FML appends a marker to the host to recognize FML clients (\0FML\0)
        host = substringBefore(host, '\0');

        // When clients connect with a SRV record, their host contains a trailing '.'
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }

        return host;
    }

    public static String substringBefore(final String s, final char c) {
        final int pos = s.indexOf(c);
        return pos >= 0 ? s.substring(0, pos) : s;
    }

    /**
     * Specifically de-duplicated clone from {@link PlayerList#initializeConnectionToPlayer(NetworkManager, EntityPlayerMP)}
     * but because Forge changes the signature, we cannot provide the override in SpongeCommon, therefor we have to bridge
     * it through an overwrite in SpongeVanilla and one in SpongeForge.
     * @param playerList
     * @param netManager
     * @param playerIn
     * @param handler
     */
    public static void initializeConnectionToPlayer(final PlayerList playerList, final NetworkManager netManager, final EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        final GameProfile gameprofile = playerIn.func_146103_bH();
        final PlayerProfileCache playerprofilecache = ((PlayerListAccessor) playerList).accessor$getPlayerListServer().func_152358_ax();
        final GameProfile gameprofile1 = playerprofilecache.func_152652_a(gameprofile.getId());
        final String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.func_152649_a(gameprofile);
        
        // Sponge start - save changes to offline User before reading player data
        final SpongeUser user = (SpongeUser) ((EntityPlayerMPBridge) playerIn).bridge$getUserObject();
        if (SpongeUser.dirtyUsers.contains(user)) {
            user.save();
        } else {
            user.invalidate();
        }
        // Sponge end

        final NBTTagCompound nbttagcompound = playerList.func_72380_a(playerIn);
        WorldServer worldServer = ((PlayerListAccessor) playerList).accessor$getPlayerListServer().func_71218_a(playerIn.field_71093_bK);
        final int actualDimensionId = ((WorldServerBridge) worldServer).bridge$getDimensionId();
        final BlockPos spawnPos;
        // Join data
        final Optional<Instant> firstJoined = SpongePlayerDataHandler.getFirstJoined(playerIn.func_110124_au());
        final Instant lastJoined = Instant.now();
        SpongePlayerDataHandler.setPlayerInfo(playerIn.func_110124_au(), firstJoined.orElse(lastJoined), lastJoined);

        if (actualDimensionId != playerIn.field_71093_bK) {
            SpongeImpl.getLogger().warn("Player [{}] has attempted to login to unloaded world [{}]. This is not safe so we have moved them to "
                                        + "the default world's spawn point.", playerIn.func_70005_c_(), playerIn.field_71093_bK);
            if (!firstJoined.isPresent()) {
                spawnPos = SpongeImplHooks.getRandomizedSpawnPoint(worldServer);
            } else {
                spawnPos = worldServer.func_175694_M();
            }
            playerIn.field_71093_bK = actualDimensionId;
            playerIn.func_70107_b(spawnPos.func_177958_n(), spawnPos.func_177956_o(), spawnPos.func_177952_p());
        }

        // Sponge start - fire login event
        @Nullable final String kickReason = playerList.func_148542_a(netManager.func_74430_c(), gameprofile);
        final Text disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeTexts.fromLegacy(kickReason);
        } else {
            disconnectMessage = Text.of("You are not allowed to log in to this server.");
        }

        final Player player = (Player) playerIn;
        final Transform<World> fromTransform = player.getTransform().setExtent((World) worldServer);

        final ClientConnectionEvent.Login loginEvent;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(user);
            loginEvent = SpongeEventFactory.createClientConnectionEventLogin(
                frame.getCurrentCause(), fromTransform, fromTransform, (RemoteConnection) netManager,
                new MessageEvent.MessageFormatter(disconnectMessage), (org.spongepowered.api.profile.GameProfile) gameprofile, player, false
            );

            if (kickReason != null) {
                loginEvent.setCancelled(true);
            }

            if (SpongeImpl.postEvent(loginEvent)) {
                Sponge.getCauseStackManager().popCause();
                final Optional<Text> message = loginEvent.isMessageCancelled() ? Optional.empty() : Optional.of(loginEvent.getMessage());
                final ITextComponent reason;
                if (message.isPresent()) {
                    reason = SpongeTexts.toComponent(message.get());
                } else {
                    reason = new TextComponentTranslation("disconnect.disconnected");
                }

                try {
                    ((PlayerListAccessor) playerList).accessor$getPlayerListLogger().info("Disconnecting " + (gameprofile != null ? gameprofile.toString() + " (" + netManager.func_74430_c().toString() + ")" : String.valueOf(netManager.func_74430_c() + ": " + reason.func_150260_c())));
                    netManager.func_179290_a(new SPacketDisconnect(reason));
                    netManager.func_150718_a(reason);
                } catch (Exception exception) {
                    ((PlayerListAccessor) playerList).accessor$getPlayerListLogger().error("Error whilst disconnecting player", exception);
                }
                return;
            }
        }

        // Sponge end

        worldServer = (WorldServer) loginEvent.getToTransform().getExtent();
        final double x = loginEvent.getToTransform().getPosition().getX();
        final double y = loginEvent.getToTransform().getPosition().getY();
        final double z = loginEvent.getToTransform().getPosition().getZ();
        final float pitch = (float) loginEvent.getToTransform().getPitch();
        final float yaw = (float) loginEvent.getToTransform().getYaw();

        playerIn.field_71093_bK = ((WorldServerBridge) worldServer).bridge$getDimensionId();
        playerIn.func_70029_a(worldServer);
        playerIn.field_71134_c.func_73080_a((WorldServer) playerIn.field_70170_p);
        playerIn.func_70080_a(x, y, z, yaw, pitch);
        // make sure the chunk is loaded for login
        worldServer.func_72863_F().func_186028_c(loginEvent.getToTransform().getLocation().getChunkPosition().getX(), loginEvent.getToTransform().getLocation().getChunkPosition().getZ());
        // Sponge end

        String s1 = "local";

        if (netManager.func_74430_c() != null) {
            s1 = netManager.func_74430_c().toString();
        }

        final WorldInfo worldinfo = worldServer.func_72912_H();
        final BlockPos spawnBlockPos = worldServer.func_175694_M();
        ((PlayerListAccessor) playerList).accessor$setPlayerGameType(playerIn, null, worldServer);

        // Sponge start
        if (handler == null) {
            // Create the handler here (so the player's gets set)
            handler = new NetHandlerPlayServer(((PlayerListAccessor) playerList).accessor$getPlayerListServer(), netManager, playerIn);
        }
        playerIn.field_71135_a = handler;
        SpongeImplHooks.fireServerConnectionEvent(netManager);
        // Sponge end

        // Support vanilla clients logging into custom dimensions
        final int dimensionId = WorldManager.getClientDimensionId(playerIn, worldServer);

        WorldManager.sendDimensionRegistration(playerIn, worldServer.field_73011_w);

        handler.func_147359_a(new SPacketJoinGame(playerIn.func_145782_y(), playerIn.field_71134_c.func_73081_b(), worldinfo
                .func_76093_s(), dimensionId, worldServer.func_175659_aa(), playerList.func_72352_l(), worldinfo
                .func_76067_t(), worldServer.func_82736_K().func_82766_b("reducedDebugInfo")));
        handler.func_147359_a(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).func_180714_a(playerList
                .func_72365_p().getServerModName())));
        handler.func_147359_a(new SPacketServerDifficulty(worldinfo.func_176130_y(), worldinfo.func_176123_z()));
        handler.func_147359_a(new SPacketSpawnPosition(spawnBlockPos));
        handler.func_147359_a(new SPacketPlayerAbilities(playerIn.field_71075_bZ));
        handler.func_147359_a(new SPacketHeldItemChange(playerIn.field_71071_by.field_70461_c));
        playerList.func_187243_f(playerIn);
        playerIn.func_147099_x().func_150877_d();
        playerIn.func_192037_E().func_192826_c(playerIn);
        ((PlayerListAccessor) playerList).accessor$getPlayerListServer().func_147132_au();

        handler.func_147364_a(x, y, z, yaw, pitch);
        playerList.func_72377_c(playerIn);

        // Sponge start - add world name to message
        ((PlayerListAccessor) playerList).accessor$getPlayerListLogger().info("{} [{}] logged in with entity id [{}] in {} ({}/{}) at ({}, {}, {}).", playerIn.func_70005_c_(), s1, playerIn.func_145782_y(),
            worldServer.func_72912_H().func_76065_j(), ((DimensionType) (Object) worldServer.field_73011_w.func_186058_p()).getId(),
            ((WorldServerBridge) worldServer).bridge$getDimensionId(), playerIn.field_70165_t, playerIn.field_70163_u, playerIn.field_70161_v);
        // Sponge end

        playerList.func_72354_b(playerIn, worldServer);

        // Sponge Start - Use the server's ResourcePack object
        final Optional<ResourcePack> pack = ((Server) ((PlayerListAccessor) playerList).accessor$getPlayerListServer()).getDefaultResourcePack();
        pack.ifPresent(((Player) playerIn)::sendResourcePack);
        // Sponge End

        // Sponge Start
        //
        // This sends the objective/score creation packets
        // to the player, without attempting to remove them from their
        // previous scoreboard (which is set in a field initializer).
        // This allows #getWorldScoreboard to function
        // as normal, without causing issues when it is initialized on the client.

        ((EntityPlayerMPBridge) playerIn).bridge$initScoreboard();

        for (final PotionEffect potioneffect : playerIn.func_70651_bq()) {
            handler.func_147359_a(new SPacketEntityEffect(playerIn.func_145782_y(), potioneffect));
        }

        if (nbttagcompound != null) {
            if (nbttagcompound.func_150297_b("RootVehicle", 10)) {
                final NBTTagCompound nbttagcompound1 = nbttagcompound.func_74775_l("RootVehicle");
                final Entity entity2 = AnvilChunkLoader.func_186051_a(nbttagcompound1.func_74775_l("Entity"), worldServer, true);

                if (entity2 != null) {
                    final UUID uuid = nbttagcompound1.func_186857_a("Attach");

                    if (entity2.func_110124_au().equals(uuid)) {
                        playerIn.func_184205_a(entity2, true);
                    } else {
                        for (final Entity entity : entity2.func_184182_bu()) {
                            if (entity.func_110124_au().equals(uuid)) {
                                playerIn.func_184205_a(entity, true);
                                break;
                            }
                        }
                    }

                    if (!playerIn.func_184218_aH()) {
                        ((PlayerListAccessor) playerList).accessor$getPlayerListLogger().warn("Couldn\'t reattach entity to player");
                        worldServer.func_72973_f(entity2);

                        for (final Entity entity3 : entity2.func_184182_bu()) {
                            worldServer.func_72973_f(entity3);
                        }
                    }
                }
            } else if (nbttagcompound.func_150297_b("Riding", 10)) {
                final Entity entity1 = AnvilChunkLoader.func_186051_a(nbttagcompound.func_74775_l("Riding"), worldServer, true);

                if (entity1 != null) {
                    playerIn.func_184205_a(entity1, true);
                }
            }
        }

        playerIn.func_71116_b();

        final TextComponentTranslation chatcomponenttranslation;

        if (!playerIn.func_70005_c_().equalsIgnoreCase(s))
        {
            chatcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined.renamed", playerIn.func_145748_c_(), s);
        }
        else
        {
            chatcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined", playerIn.func_145748_c_());
        }

        chatcomponenttranslation.func_150256_b().func_150238_a(TextFormatting.YELLOW);

        // Fire PlayerJoinEvent
        final Text originalMessage = SpongeTexts.toText(chatcomponenttranslation);
        final MessageChannel originalChannel = player.getMessageChannel();
        Sponge.getCauseStackManager().pushCause(player);
        final ClientConnectionEvent.Join event = SpongeEventFactory.createClientConnectionEventJoin(
                Sponge.getCauseStackManager().getCurrentCause(), originalChannel, Optional.of(originalChannel),
                new MessageEvent.MessageFormatter(originalMessage), player, false
        );
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        // Send to the channel
        if (!event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
        // Sponge end
    }
}
