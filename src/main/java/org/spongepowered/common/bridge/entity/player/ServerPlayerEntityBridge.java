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
package org.spongepowered.common.bridge.entity.player;

import net.kyori.adventure.text.Component;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.bridge.network.NetworkManagerBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.packet.ChangeViewerEnvironmentPacket;
import org.spongepowered.common.network.packet.RegisterDimensionTypePacket;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ServerPlayerEntityBridge {

    default ClientType bridge$getClientType() {
        final ServerPlayerEntity mPlayer = (ServerPlayerEntity) this;
        if (mPlayer.connection == null) {
            return ClientType.VANILLA;
        }

        return ((NetworkManagerBridge) mPlayer.connection.netManager).bridge$getClientType();
    }

    int bridge$getViewDistance();

    @Nullable
    User bridge$getUserObject();

    void bridge$setVelocityOverride(@Nullable Vector3d velocity);

    void bridge$sendBlockChange(BlockPos pos, BlockState state);

    void bridge$initScoreboard();

    void bridge$removeScoreboardOnRespawn();

    void bridge$setScoreboardOnRespawn(Scoreboard scoreboard);

    void bridge$restorePacketItem(Hand hand);

    void bridge$setPacketItem(ItemStack itemstack);

    void bridge$refreshExp();

    PlayerOwnBorderListener bridge$getWorldBorderListener();

    void bridge$setHealthScale(double scale);

    double bridge$getHealthScale();

    float bridge$getInternalScaledHealth();

    boolean bridge$isHealthScaled();

    void bridge$refreshScaledHealth();

    void bridge$injectScaledHealth(Collection<IAttributeInstance> set);

    boolean bridge$hasForcedGamemodeOverridePermission();

    void bridge$setContainerDisplay(Component displayName);

    void bridge$setDelegateAfterRespawn(ServerPlayerEntity delegate);

    Scoreboard bridge$getScoreboard();

    void bridge$replaceScoreboard(@Nullable Scoreboard scoreboard);

    Set<SkinPart> bridge$getSkinParts();

    @Nullable
    User bridge$getUser();

    boolean bridge$hasDelegate();

    @Nullable
    ServerPlayerEntity bridge$getDelegate();

    @Nullable
    Vector3d bridge$getVelocityOverride();

    @Nullable GameProfile bridge$getPreviousGameProfile();

    void bridge$setPreviousGameProfile(@Nullable GameProfile gameProfile);

    default void bridge$sendDimensionData(final NetworkManager manager, final DimensionType dimensionType) {
    }

    default void bridge$sendChangeDimension(final DimensionType type, final long hashedSeed, final WorldType generator, final GameType gameType) {
        ((ServerPlayerEntity) this).connection.sendPacket(new SRespawnPacket(type, hashedSeed, generator, gameType));
    }

    default void bridge$sendViewerEnvironment(final SpongeDimensionType dimensionType) {
    }
}
