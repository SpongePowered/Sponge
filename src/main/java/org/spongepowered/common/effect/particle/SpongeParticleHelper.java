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

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class SpongeParticleHelper {

    public static void sendPackets(final ParticleEffect particleEffect, final Vector3d position, final int radius, final ResourceKey<Level> type,
                                   final PlayerList playerList) {
        final List<Packet<?>> packets = SpongeParticleHelper.toPackets(particleEffect, position);

        if (!packets.isEmpty()) {
            final double x = position.x();
            final double y = position.y();
            final double z = position.z();

            for (final Packet<?> packet : packets) {
                playerList.broadcast(null, x, y, z, radius, type, packet);
            }
        }
    }

    public static List<Packet<?>> toPackets(final ParticleEffect effect, final Vector3d position) {
        final List<Packet<?>> packets = new ArrayList<>();
        SpongeParticleHelper.getCachedPacket((SpongeParticleEffect) effect).process(position, packets);
        return packets;
    }

    public static net.minecraft.core.particles.@Nullable ParticleOptions vanillaParticleOptions(final ParticleEffect effect) {
        return SpongeParticleHelper.getCachedPacket((SpongeParticleEffect) effect).particleOptions();
    }

    private static CachedParticlePacket getCachedPacket(final SpongeParticleEffect effect) {
        if (effect.cachedPacket == null) {
            final ParticleType type = effect.type();

            effect.cachedPacket = SpongeParticleHelper.getNamedPacket(effect, (net.minecraft.core.particles.ParticleType<?>) type);
        }

        return effect.cachedPacket;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static CachedParticlePacket getNamedPacket(final ParticleEffect effect, final net.minecraft.core.particles.ParticleType<?> internalType) {
        // Named particles always support OFFSET and QUANTITY.
        final Vector3f offset = effect.optionOrDefault(ParticleOptions.OFFSET).get().toFloat();
        final int quantity = effect.optionOrDefault(ParticleOptions.QUANTITY).get();
        final Vector3f velocity = effect.optionOrDefault(ParticleOptions.VELOCITY).orElse(Vector3d.ZERO).toFloat();

        if (internalType instanceof SimpleParticleType) {
            // Simple named particle without any additional supported options.
            return new NamedCachedPacket((net.minecraft.core.particles.ParticleOptions) internalType, offset, quantity, velocity);
        }

        // The only way we can see what options are supported for a particular named particle
        // is to compare the internal type's deserializer to some static deserializer fields.
        // If only mojang had some type akin to our ParticleEffect...
        if (internalType.getDeserializer() == BlockParticleOption.DESERIALIZER) {
            // This particle type supports a block state option.
            final BlockState state = effect.optionOrDefault(ParticleOptions.BLOCK_STATE).get();
            final BlockParticleOption particleData = new BlockParticleOption(
                    (net.minecraft.core.particles.ParticleType<BlockParticleOption>) internalType,
                    (net.minecraft.world.level.block.state.BlockState) state);
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == DustColorTransitionOptions.DESERIALIZER) {
            final Color color = effect.optionOrDefault(ParticleOptions.COLOR).get();
            final Color toColor = effect.optionOrDefault(ParticleOptions.TO_COLOR).get();
            final double scale = effect.optionOrDefault(ParticleOptions.SCALE).get();
            final DustColorTransitionOptions particleData = new DustColorTransitionOptions(
                    new org.joml.Vector3f(
                            (float) color.red() / 255,
                            (float) color.green() / 255,
                            (float) color.blue() / 255
                    ),
                    new org.joml.Vector3f(
                            (float) toColor.red() / 255,
                            (float) toColor.green() / 255,
                            (float) toColor.blue() / 255
                    ),
                    (float) scale);
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == DustParticleOptions.DESERIALIZER) {
            // This particle type supports a color option.
            final Color color = effect.optionOrDefault(ParticleOptions.COLOR).get();
            final double scale = effect.optionOrDefault(ParticleOptions.SCALE).get();
            final DustParticleOptions particleData = new DustParticleOptions(new org.joml.Vector3f(
                    (float) color.red() / 255,
                    (float) color.green() / 255,
                    (float) color.blue() / 255),
                    (float) scale);
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == ItemParticleOption.DESERIALIZER) {
            // This particle type supports an item option.
            final ItemStackSnapshot snapshot = effect.optionOrDefault(ParticleOptions.ITEM_STACK_SNAPSHOT).get();
            final ItemParticleOption particleData = new ItemParticleOption(
                    (net.minecraft.core.particles.ParticleType<ItemParticleOption>) internalType,
                    (net.minecraft.world.item.ItemStack) (Object) snapshot.createStack());
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == SculkChargeParticleOptions.DESERIALIZER) {
            final double roll = effect.optionOrDefault(ParticleOptions.ROLL).get();
            final SculkChargeParticleOptions particleData = new SculkChargeParticleOptions((float) roll);
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == ShriekParticleOption.DESERIALIZER) {
            final int delay = effect.optionOrDefault(ParticleOptions.DELAY).get();
            final ShriekParticleOption particleData = new ShriekParticleOption(delay);
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        } else if (internalType.getDeserializer() == VibrationParticleOption.DESERIALIZER) {
            final Ticks delay = effect.optionOrDefault(ParticleOptions.TRAVEL_TIME).get();
            // TODO add position source
            final VibrationParticleOption particleData = new VibrationParticleOption(new BlockPositionSource(BlockPos.ZERO), (int) delay.ticks());
            return new NamedCachedPacket(particleData, offset, quantity, velocity);
        }

        // Otherwise, we don't really know how to get a valid IParticleData. Sorry mods!
        return EmptyCachedPacket.INSTANCE;
    }

    public static int getDirectionId(Direction direction) {
        if (direction.isSecondaryOrdinal()) {
            direction = Direction.closest(direction.asOffset(), Direction.Division.ORDINAL);
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

    public static int getBlockStateId(final ParticleEffect effect, final Optional<BlockState> defaultBlockState) {
        final Optional<BlockState> blockState = effect.option(ParticleOptions.BLOCK_STATE);
        if (blockState.isPresent()) {
            // Use the provided block state option.
            return Block.getId((net.minecraft.world.level.block.state.BlockState) blockState.get());
        }

        final Optional<ItemStackSnapshot> itemSnapshot = effect.option(ParticleOptions.ITEM_STACK_SNAPSHOT);
        if (itemSnapshot.isPresent()) {
            // Try to convert the item into a usable block state.
            final Optional<BlockType> blockType = itemSnapshot.get().type().block();
            // TODO: correct behavior?
            return blockType.map(type -> Block.getId((net.minecraft.world.level.block.state.BlockState) type.defaultState()))
                    .orElse(0);
        }

        // Otherwise, use the default block state option if available.
        return defaultBlockState.map(state -> Block.getId((net.minecraft.world.level.block.state.BlockState) state))
                .orElse(0);
    }

    public static ParticleEffect spongeParticleOptions(final net.minecraft.core.particles.ParticleOptions effect) {
        final net.minecraft.core.particles.ParticleType<?> type = effect.getType();
        if (type instanceof SimpleParticleType) {
            return new SpongeParticleEffect((ParticleType) type, Collections.emptyMap());
        }

        if (type.getDeserializer() == BlockParticleOption.DESERIALIZER) {
            final BlockParticleOption particleData = (BlockParticleOption) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(ParticleOptions.BLOCK_STATE.get(), particleData.getState()));
        } else if (type.getDeserializer() == DustColorTransitionOptions.DESERIALIZER) {
            final DustColorTransitionOptions particleData = (DustColorTransitionOptions) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(
                    ParticleOptions.COLOR.get(), Color.of(
                            Vector3f.from(particleData.getFromColor().x, particleData.getFromColor().y, particleData.getFromColor().z).mul(255)),
                    ParticleOptions.TO_COLOR.get(), Color.of(
                            Vector3f.from(particleData.getToColor().x, particleData.getToColor().y, particleData.getToColor().z).mul(255)),
                    ParticleOptions.SCALE.get(), particleData.getScale()
            ));
        } else if (type.getDeserializer() == DustParticleOptions.DESERIALIZER) {
            // This particle type supports a color option.
            final DustParticleOptions particleData = (DustParticleOptions) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(
                    ParticleOptions.COLOR.get(), Color.of(
                            Vector3f.from(particleData.getColor().x, particleData.getColor().y, particleData.getColor().z).mul(255)),
                    ParticleOptions.SCALE.get(), particleData.getScale()
            ));
        } else if (type.getDeserializer() == ItemParticleOption.DESERIALIZER) {
            // This particle type supports an item option.
            final ItemParticleOption particleData = (ItemParticleOption) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(ParticleOptions.BLOCK_STATE.get(), particleData.getItem().copy()));
        } else if (type.getDeserializer() == SculkChargeParticleOptions.DESERIALIZER) {
            final SculkChargeParticleOptions particleData = (SculkChargeParticleOptions) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(ParticleOptions.ROLL.get(), particleData.roll()));
        } else if (type.getDeserializer() == ShriekParticleOption.DESERIALIZER) {
            final ShriekParticleOption particleData = (ShriekParticleOption) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(ParticleOptions.DELAY.get(), particleData.getDelay()));
        } else if (type.getDeserializer() == VibrationParticleOption.DESERIALIZER) {
            // TODO add position source
            final VibrationParticleOption particleData = (VibrationParticleOption) effect;
            return new SpongeParticleEffect((ParticleType) type, Map.of(ParticleOptions.TRAVEL_TIME.get(), Ticks.of(particleData.getArrivalInTicks())));
        }

        return new SpongeParticleEffect((ParticleType) type, Collections.emptyMap());
    }

    private static final class EmptyCachedPacket implements CachedParticlePacket {

        public static final EmptyCachedPacket INSTANCE = new EmptyCachedPacket();

        @Override
        public void process(final Vector3d position, final List<Packet<?>> output) {
        }

        @Override
        public net.minecraft.core.particles.@Nullable ParticleOptions particleOptions() {
            return null;
        }
    }

    private static final class NamedCachedPacket implements CachedParticlePacket {

        private final net.minecraft.core.particles.ParticleOptions particleData;
        private final Vector3f offset;
        private final int quantity;
        private final Vector3f velocity;

        public NamedCachedPacket(final net.minecraft.core.particles.ParticleOptions particleData, final Vector3f offset, final int quantity, Vector3f velocity) {
            this.particleData = particleData;
            this.offset = offset;
            this.quantity = quantity;
            this.velocity = velocity;
        }

        @Override
        public void process(final Vector3d position, final List<Packet<?>> output) {
            final float posX = (float) position.x();
            final float posY = (float) position.y();
            final float posZ = (float) position.z();

            final float offX = this.offset.x();
            final float offY = this.offset.y();
            final float offZ = this.offset.z();

            if (this.velocity.equals(Vector3f.ZERO)) {
                final ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                        this.particleData,
                        true,
                        posX, posY, posZ,
                        offX, offY, offZ,
                        0.0F,
                        this.quantity
                );

                output.add(packet);
            } else {
                final float velocityX = this.velocity.x();
                final float velocityY = this.velocity.y();
                final float velocityZ = this.velocity.z();
                final Random random = new Random();
                for (int i = 0; i < this.quantity; i++) {
                    final float px0 = posX + (random.nextFloat() * 2f - 1f) * offX;
                    final float py0 = posY + (random.nextFloat() * 2f - 1f) * offY;
                    final float pz0 = posZ + (random.nextFloat() * 2f - 1f) * offZ;

                    final ClientboundLevelParticlesPacket message = new ClientboundLevelParticlesPacket(
                            this.particleData,
                            true,
                            px0, py0, pz0,
                            velocityX, velocityY, velocityZ,
                            1f,
                            0);
                    output.add(message);
                }
            }
        }

        @Override
        public net.minecraft.core.particles.@Nullable ParticleOptions particleOptions() {
            return this.particleData;
        }
    }
}
