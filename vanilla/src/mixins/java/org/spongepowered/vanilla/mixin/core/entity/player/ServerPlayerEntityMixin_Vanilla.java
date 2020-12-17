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
package org.spongepowered.vanilla.mixin.core.entity.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.packet.ChangeViewerEnvironmentPacket;
import org.spongepowered.common.network.packet.SpongePacketHandler;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Vanilla implements ServerPlayerEntityBridge  {

    @Shadow public abstract ServerWorld shadow$getLevel();

    @Override
    public void bridge$sendDimensionData(final NetworkManager manager, final RegistryKey<World> key) {
        if (this.bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, new RegisterDimensionTypePacket(dimensionType));
        }
    }

    @Override
    public void bridge$sendChangeDimension(final DimensionType toDimensionType, long seed, final GameType gameType, final GameType previousGameType) {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (this.bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            player.connection.send(new SRespawnPacket(toDimensionType, seed, generator, gameType));
        } else {
            this.vanilla$hackChangeVanillaClientDimension(((DimensionTypeBridge) toDimensionType).bridge$getSpongeDimensionType(), seed, generator,
                    gameType, true);
        }
    }

    @Override
    public void bridge$sendViewerEnvironment(final DimensionType dimensionType) {
        if (this.bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, new ChangeViewerEnvironmentPacket(dimensionType));
        } else {
            final WorldType generator = ((ServerPlayerEntity) (Object) this).getEntityWorld().getWorldInfo().getGenerator();
            final GameType gameType = ((ServerPlayerEntity) (Object) this).interactionManager.getGameType();

            this.vanilla$hackChangeVanillaClientDimension(dimensionType, WorldInfo.byHashing(this.shadow$getLevel().getSeed()), generator,
                gameType, false);
        }
    }

    private void vanilla$hackChangeVanillaClientDimension(final SpongeDimensionType logicType, final long seed, final WorldType generator,
            final GameType gameType,
            boolean actualWorldChange) {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        final SpongeDimensionType currentLogicType =
                ((DimensionTypeBridge) this.shadow$getServerWorld().dimension.getType()).bridge$getSpongeDimensionType();

        // Trick the Vanilla client to dump it's rendered chunks as we cannot send unknown registered dimensions to the client
        if (currentLogicType == logicType) {
            if (logicType == DimensionTypes.OVERWORLD.get()) {
                player.connection.send(new SRespawnPacket(DimensionType.THE_NETHER, seed, generator, gameType));
            } else if (logicType == DimensionTypes.THE_NETHER.get()) {
                player.connection.send(new SRespawnPacket(DimensionType.OVERWORLD, seed, generator, gameType));
            } else {
                player.connection.send(new SRespawnPacket(DimensionType.THE_NETHER, seed, generator, gameType));
            }
        }

        // Now send the fake client type
        if (logicType == DimensionTypes.OVERWORLD.get()) {
            player.connection.send(new SRespawnPacket(DimensionType.OVERWORLD, seed, generator, gameType));
        } else if (logicType == DimensionTypes.THE_NETHER.get()) {
            player.connection.send(new SRespawnPacket(DimensionType.THE_NETHER, seed, generator, gameType));
        } else {
            player.connection.send(new SRespawnPacket(DimensionType.THE_END, seed, generator, gameType));
        }

        if (!actualWorldChange) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
            // TODO This needs more work for Vanilla clients...
        }
    }
}
