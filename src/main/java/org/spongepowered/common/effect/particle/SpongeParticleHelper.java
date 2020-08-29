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
package org.spongepowered.common.effect.particle;

import net.minecraft.block.Block;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.*;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpongeParticleHelper {

    public static void sendPackets(ParticleEffect particleEffect, Vector3d position, int radius, DimensionType type, PlayerList playerList) {
        final List<IPacket<?>> packets = toPackets(particleEffect, position);

        if (!packets.isEmpty()) {
            final double x = position.getX();
            final double y = position.getY();
            final double z = position.getZ();

            for (final IPacket<?> packet : packets) {
                playerList.sendToAllNearExcept(null, x, y, z, radius, type, packet);
            }
        }
    }

    public static List<IPacket<?>> toPackets(final ParticleEffect effect, final Vector3d position) {
        SpongeParticleEffect spongeEffect = (SpongeParticleEffect) effect;

        CachedParticlePacket cachedPacket = spongeEffect.cachedPacket;
        if (cachedPacket == null) {
            // Also save the generated packet cache for repeated uses.
            cachedPacket = spongeEffect.cachedPacket = getCachedPacket(spongeEffect);
        }

        final List<IPacket<?>> packets = new ArrayList<>();
        cachedPacket.process(position, packets);
        return packets;
    }

    public static CachedParticlePacket getCachedPacket(final SpongeParticleEffect effect) {
        final ParticleType type = effect.getType();

        if (type instanceof NumericalParticleType) {
            // Special cased particle types with numerical IDs.
            return getNumericalPacket(effect, (NumericalParticleType) type);
        } else {
            // Normal named particle type.
            return getNamedPacket(effect, (net.minecraft.particles.ParticleType<?>) type);
        }
    }

    private static CachedParticlePacket getNumericalPacket(final ParticleEffect effect, final NumericalParticleType type) {
        int effectId = type.getId();

        if (type == ParticleTypes.FIRE_SMOKE.get()) { // id: 2000
            final Direction direction = effect.getOptionOrDefault(ParticleOptions.DIRECTION).get();
            return new NumericalCachedPacket(effectId, getDirectionId(direction), false);
        } else if (type == ParticleTypes.BREAK_BLOCK.get()) { // id: 2001
            final int stateId = getBlockStateId(effect, type.getDefaultOption(ParticleOptions.BLOCK_STATE));
            if (stateId == 0) {
                return EmptyCachedPacket.INSTANCE;
            }
            return new NumericalCachedPacket(effectId, stateId, false);
        }

        return EmptyCachedPacket.INSTANCE;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static CachedParticlePacket getNamedPacket(final ParticleEffect effect, final net.minecraft.particles.ParticleType<?> internalType) {
        // Named particles always support OFFSET and QUANTITY.
        final Vector3f offset = effect.getOptionOrDefault(ParticleOptions.OFFSET).get().toFloat();
        final int quantity = effect.getOptionOrDefault(ParticleOptions.QUANTITY).get();

        if (internalType instanceof BasicParticleType) {
            // Simple named particle without any additional supported options.
            return new NamedCachedPacket((IParticleData) internalType, offset, quantity);
        }

        // The only way we can see what options are supported for a particular named particle
        // is to compare the internal type's deserializer to some static deserializer fields.
        // If only mojang had some type akin to our ParticleEffect...
        if (internalType.getDeserializer() == BlockParticleData.DESERIALIZER) {
            // This particle type supports a block state option.
            final BlockState state = effect.getOptionOrDefault(ParticleOptions.BLOCK_STATE).get();
            final BlockParticleData particleData = new BlockParticleData(
                    (net.minecraft.particles.ParticleType<BlockParticleData>) internalType,
                    (net.minecraft.block.BlockState) state);
            return new NamedCachedPacket(particleData, offset, quantity);
        } else if (internalType.getDeserializer() == ItemParticleData.DESERIALIZER) {
            // This particle type supports an item option.
            final ItemStackSnapshot snapshot = effect.getOptionOrDefault(ParticleOptions.ITEM_STACK_SNAPSHOT).get();
            final ItemParticleData particleData = new ItemParticleData(
                    (net.minecraft.particles.ParticleType<ItemParticleData>) internalType,
                    (net.minecraft.item.ItemStack) (Object) snapshot.createStack());
            return new NamedCachedPacket(particleData, offset, quantity);
        } else if (internalType.getDeserializer() == RedstoneParticleData.DESERIALIZER) {
            // This particle type supports a color option.
            final Color color = effect.getOptionOrDefault(ParticleOptions.COLOR).get();
            final RedstoneParticleData particleData = new RedstoneParticleData(
                    (float) color.getRed() / 255,
                    (float) color.getGreen() / 255,
                    (float) color.getBlue() / 255,
                    1.0f);
            return new NamedCachedPacket(particleData, offset, quantity);
        }

        // Otherwise, we don't really know how to get a valid IParticleData. Sorry mods!
        return EmptyCachedPacket.INSTANCE;
    }

    private static int getDirectionId(Direction direction) {
        if (direction.isSecondaryOrdinal()) {
            direction = Direction.getClosest(direction.asOffset(), Direction.Division.ORDINAL);
        }
        switch (direction) {
            case SOUTHEAST:
                return 0;
            case SOUTH:
                return 1;
            case SOUTHWEST:
                return 2;
            case EAST:
                return 3;
            case WEST:
                return 5;
            case NORTHEAST:
                return 6;
            case NORTH:
                return 7;
            case NORTHWEST:
                return 8;
            default:
                return 4;
        }
    }

    private static int getBlockStateId(final ParticleEffect effect, final Optional<BlockState> defaultBlockState) {
        final Optional<BlockState> blockState = effect.getOption(ParticleOptions.BLOCK_STATE);
        if (blockState.isPresent()) {
            // Use the provided block state option.
            return Block.getStateId((net.minecraft.block.BlockState) blockState.get());
        }

        final Optional<ItemStackSnapshot> itemSnapshot = effect.getOption(ParticleOptions.ITEM_STACK_SNAPSHOT);
        if (itemSnapshot.isPresent()) {
            // Try to convert the item into a usable block state.
            final Optional<BlockType> blockType = itemSnapshot.get().getType().getBlock();
            // TODO: correct behavior?
            return blockType.map(type -> Block.getStateId((net.minecraft.block.BlockState) type.getDefaultState()))
                    .orElse(0);
        }

        // Otherwise, use the default block state option if available.
        return defaultBlockState.map(state -> Block.getStateId((net.minecraft.block.BlockState) state))
                .orElse(0);
    }

    private static final class EmptyCachedPacket implements CachedParticlePacket {

        public static final EmptyCachedPacket INSTANCE = new EmptyCachedPacket();

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
        }
    }

    private static final class NamedCachedPacket implements CachedParticlePacket {

        private final IParticleData particleData;
        private final Vector3f offset;
        private final int quantity;

        public NamedCachedPacket(final IParticleData particleData, final Vector3f offset, final int quantity) {
            this.particleData = particleData;
            this.offset = offset;
            this.quantity = quantity;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final float posX = (float) position.getX();
            final float posY = (float) position.getY();
            final float posZ = (float) position.getZ();

            final float offX = offset.getX();
            final float offY = offset.getY();
            final float offZ = offset.getZ();

            final SSpawnParticlePacket packet = new SSpawnParticlePacket(
                    this.particleData,
                    true,
                    posX, posY, posZ,
                    offX, offY, offZ,
                    0.0F,
                    this.quantity
            );

            output.add(packet);
        }
    }

    private static final class NumericalCachedPacket implements CachedParticlePacket {

        private final int type;
        private final int data;
        private final boolean broadcast;

        public NumericalCachedPacket(final int type, final int data, final boolean broadcast) {
            this.type = type;
            this.data = data;
            this.broadcast = broadcast;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final BlockPos blockPos = new BlockPos(position.getFloorX(), position.getFloorY(), position.getFloorZ());
            final SPlaySoundEventPacket packet = new SPlaySoundEventPacket(this.type, blockPos, this.data, this.broadcast);
            output.add(packet);
        }
    }
}