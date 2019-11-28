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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.type.SpongeNotePitch;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.mixin.core.entity.item.EntityFireworkRocketAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketEntityMetadataAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketEntityStatusAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketSpawnObjectAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class SpongeParticleHelper {

    /**
     * Gets the list of packets that are needed to spawn the particle effect at
     * the position. This method tries to minimize the amount of packets for
     * better performance and lower bandwidth use.
     *
     * @param effect The particle effect
     * @param position The position
     * @return The packets
     */
    public static List<IPacket<?>> toPackets(final SpongeParticleEffect effect, final Vector3d position) {
        ICachedParticleEffect cachedPacket = effect.cachedParticle;
        if (cachedPacket == null) {
            cachedPacket = effect.cachedParticle = toCachedPacket(effect);
        }
        if (cachedPacket == EmptyCachedPacket.INSTANCE) {
            return Collections.emptyList();
        }
        final List<IPacket<?>> packets = new ArrayList<>();
        cachedPacket.process(position, packets);
        return packets;
    }

    @SuppressWarnings("deprecation")
    private static int getBlockState(final SpongeParticleEffect effect, final Optional<BlockState> defaultBlockState) {
        final Optional<BlockState> blockState = effect.getOption(ParticleOptions.BLOCK_STATE);
        if (blockState.isPresent()) {
            return Block.func_176210_f((net.minecraft.block.BlockState) blockState.get());
        }
        final Optional<ItemStackSnapshot> optSnapshot = effect.getOption(ParticleOptions.ITEM_STACK_SNAPSHOT);
        if (optSnapshot.isPresent()) {
            final ItemStackSnapshot snapshot = optSnapshot.get();
            final Optional<BlockType> blockType = snapshot.getType().getBlock();
            if (blockType.isPresent()) {
                return Block.func_176210_f(((Block) blockType.get()).func_176203_a(
                        ((SpongeItemStackSnapshot) snapshot).getDamageValue()));
            }
            return 0;
        }
        return Block.func_176210_f((net.minecraft.block.BlockState) defaultBlockState.get());
    }

    private static int getDirectionData(Direction direction) {
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

    @SuppressWarnings("ConstantConditions")
    private static ICachedParticleEffect toCachedPacket(final SpongeParticleEffect effect) {
        final SpongeParticleType type = effect.getType();

        final EnumParticleTypes internal = type.getInternalType();
        // Special cases
        if (internal == null) {
            if (type == ParticleTypes.FIREWORKS) {
                final List<FireworkEffect> effects = effect.getOption(ParticleOptions.FIREWORK_EFFECTS).orElse(type.getDefaultOption(ParticleOptions.FIREWORK_EFFECTS).get());
                if (effects.isEmpty()) {
                    return EmptyCachedPacket.INSTANCE;
                }
                final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(Items.field_151152_bP);
                FireworkUtils.setFireworkEffects(itemStack, effects);
                final SEntityMetadataPacket packetEntityMetadata = new SEntityMetadataPacket();
                ((SPacketEntityMetadataAccessor) packetEntityMetadata).accessor$setEntityId(CachedFireworkPacket.FIREWORK_ROCKET_ID);
                ((SPacketEntityMetadataAccessor) packetEntityMetadata).accessor$setManagerEntires(new ArrayList<>());
                ((SPacketEntityMetadataAccessor) packetEntityMetadata).accessor$getManagerEntires().add(new EntityDataManager.DataEntry<>(
                    EntityFireworkRocketAccessor.accessor$getFireworkItemParameter(), itemStack));
                return new CachedFireworkPacket(packetEntityMetadata);
            }
            if (type == ParticleTypes.FERTILIZER) {
                final int quantity = effect.getOptionOrDefault(ParticleOptions.QUANTITY).get();
                return new CachedEffectPacket(2005, quantity, false);
            } else if (type == ParticleTypes.SPLASH_POTION) {
                final Effect potion = (Effect) effect.getOptionOrDefault(ParticleOptions.POTION_EFFECT_TYPE).get();
                for (final Potion potionType : Potion.field_185176_a) {
                    for (final net.minecraft.potion.EffectInstance potionEffect : potionType.func_185170_a()) {
                        if (potionEffect.func_188419_a() == potion) {
                            return new CachedEffectPacket(2002, Potion.field_185176_a.func_148757_b(potionType), false);
                        }
                    }
                }
                return EmptyCachedPacket.INSTANCE;
            } else if (type == ParticleTypes.BREAK_BLOCK) {
                final int state = getBlockState(effect, type.getDefaultOption(ParticleOptions.BLOCK_STATE));
                if (state == 0) {
                    return EmptyCachedPacket.INSTANCE;
                }
                return new CachedEffectPacket(2001, state, false);
            } else if (type == ParticleTypes.MOBSPAWNER_FLAMES) {
                return new CachedEffectPacket(2004, 0, false);
            } else if (type == ParticleTypes.ENDER_TELEPORT) {
                return new CachedEffectPacket(2003, 0, false);
            } else if (type == ParticleTypes.DRAGON_BREATH_ATTACK) {
                return new CachedEffectPacket(2006, 0, false);
            } else if (type == ParticleTypes.FIRE_SMOKE) {
                final Direction direction = effect.getOptionOrDefault(ParticleOptions.DIRECTION).get();
                return new CachedEffectPacket(2000, getDirectionData(direction), false);
            }
            return EmptyCachedPacket.INSTANCE;
        }

        final Vector3f offset = effect.getOption(ParticleOptions.OFFSET).map(Vector3d::toFloat).orElse(Vector3f.ZERO);

        final int quantity = effect.getOption(ParticleOptions.QUANTITY).orElse(1);
        int[] extra = null;

        // The extra values, normal behavior offsetX, offsetY, offsetZ
        double f0 = 0f;
        double f1 = 0f;
        double f2 = 0f;

        // Depends on behavior
        // Note: If the count > 0 -> speed = 0f else if count = 0 -> speed = 1f

        final Optional<BlockState> defaultBlockState;
        if (internal != EnumParticleTypes.ITEM_CRACK && (defaultBlockState = type.getDefaultOption(ParticleOptions.BLOCK_STATE)).isPresent()) {
            final int state = getBlockState(effect, defaultBlockState);
            if (state == 0) {
                return EmptyCachedPacket.INSTANCE;
            }
            extra = new int[] { state };
        }

        final Optional<ItemStackSnapshot> defaultSnapshot;
        if (extra == null && (defaultSnapshot = type.getDefaultOption(ParticleOptions.ITEM_STACK_SNAPSHOT)).isPresent()) {
            final Optional<ItemStackSnapshot> optSnapshot = effect.getOption(ParticleOptions.ITEM_STACK_SNAPSHOT);
            if (optSnapshot.isPresent()) {
                final ItemStackSnapshot snapshot = optSnapshot.get();
                extra = new int[] { Item.func_150891_b((Item) snapshot.getType()), ((SpongeItemStackSnapshot) snapshot).getDamageValue() };
            } else {
                final Optional<BlockState> optBlockState = effect.getOption(ParticleOptions.BLOCK_STATE);
                if (optBlockState.isPresent()) {
                    final BlockState blockState = optBlockState.get();
                    final Optional<ItemType> optItemType = blockState.getType().getItem();
                    if (optItemType.isPresent()) {
                        extra = new int[] { Item.func_150891_b((Item) optItemType.get()),
                                ((Block) blockState.getType()).func_176201_c((net.minecraft.block.BlockState) blockState) };
                    } else {
                        return EmptyCachedPacket.INSTANCE;
                    }
                } else {
                    final ItemStackSnapshot snapshot = defaultSnapshot.get();
                    extra = new int[] { Item.func_150891_b((Item) snapshot.getType()), ((SpongeItemStackSnapshot) snapshot).getDamageValue() };
                }
            }
        }

        if (extra == null) {
            extra = new int[0];
        }

        final Optional<Double> defaultScale = type.getDefaultOption(ParticleOptions.SCALE);
        final Optional<Color> defaultColor;
        final Optional<NotePitch> defaultNote;
        final Optional<Vector3d> defaultVelocity;
        if (defaultScale.isPresent()) {
            double scale = effect.getOption(ParticleOptions.SCALE).orElse(defaultScale.get());

            // The formula of the large explosion acts strange
            // Client formula: sizeClient = 1 - sizeServer * 0.5
            // The particle effect returns the client value so
            // Server formula: sizeServer = (-sizeClient * 2) + 2
            if (internal == EnumParticleTypes.EXPLOSION_LARGE || internal == EnumParticleTypes.SWEEP_ATTACK) {
                scale = (-scale * 2f) + 2f;
            }

            if (scale == 0f) {
                return new CachedParticlePacket(internal, offset, quantity, extra);
            }

            f0 = scale;
        } else if ((defaultColor = type.getDefaultOption(ParticleOptions.COLOR)).isPresent()) {
            Color color = effect.getOption(ParticleOptions.COLOR).orElse(null);

            final boolean isSpell = internal == EnumParticleTypes.SPELL_MOB || internal == EnumParticleTypes.SPELL_MOB_AMBIENT;

            if (!isSpell && (color == null || color.equals(defaultColor.get()))) {
                return new CachedParticlePacket(internal, offset, quantity, extra);
            } else if (isSpell && color == null) {
                color = defaultColor.get();
            }

            f0 = color.getRed() / 255f;
            f1 = color.getGreen() / 255f;
            f2 = color.getBlue() / 255f;

            // Make sure that the x and z component are never 0 for these effects,
            // they would trigger the slow horizontal velocity (unsupported on the server),
            // but we already chose for the color, can't have both
            if (isSpell) {
                f0 = Math.max(f0, 0.001f);
                f2 = Math.max(f2, 0.001f);
            }

            // If the f0 value 0 is, the redstone will set it automatically to red 255
            if (f0 == 0f && internal == EnumParticleTypes.REDSTONE) {
                f0 = 0.00001f;
            }
        } else if ((defaultNote = type.getDefaultOption(ParticleOptions.NOTE)).isPresent()) {
            final NotePitch notePitch = effect.getOption(ParticleOptions.NOTE).orElse(defaultNote.get());
            final float note = ((SpongeNotePitch) notePitch).getByteId();

            if (note == 0f) {
                return new CachedParticlePacket(internal, offset, quantity, extra);
            }

            f0 = note / 24f;
        } else if ((defaultVelocity = type.getDefaultOption(ParticleOptions.VELOCITY)).isPresent()) {
            final Vector3d velocity = effect.getOption(ParticleOptions.VELOCITY).orElse(defaultVelocity.get());

            f0 = velocity.getX();
            f1 = velocity.getY();
            f2 = velocity.getZ();

            final Optional<Boolean> slowHorizontalVelocity = type.getDefaultOption(ParticleOptions.SLOW_HORIZONTAL_VELOCITY);
            if (slowHorizontalVelocity.isPresent() &&
                    effect.getOption(ParticleOptions.SLOW_HORIZONTAL_VELOCITY).orElse(slowHorizontalVelocity.get())) {
                f0 = 0f;
                f2 = 0f;
            }

            // The y value won't work for this effect, if the value isn't 0 the velocity won't work
            if (internal == EnumParticleTypes.WATER_SPLASH) {
                f1 = 0f;
            }

            if (f0 == 0f && f1 == 0f && f2 == 0f) {
                return new CachedParticlePacket(internal, offset, quantity, extra);
            }
        }

        // Is this check necessary?
        if (f0 == 0f && f1 == 0f && f2 == 0f) {
            return new CachedParticlePacket(internal, offset, quantity, extra);
        }

        return new CachedOffsetParticlePacket(internal, new Vector3f(f0, f1, f2), offset, quantity, extra);
    }

    private static final class EmptyCachedPacket implements ICachedParticleEffect {

        public static final EmptyCachedPacket INSTANCE = new EmptyCachedPacket();

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static final class CachedFireworkPacket implements ICachedParticleEffect {

        // Get the next free entity id
        private static final int FIREWORK_ROCKET_ID;
        private static final UUID FIREWORK_ROCKET_UNIQUE_ID;

        private static final SDestroyEntitiesPacket DESTROY_FIREWORK_ROCKET_DUMMY;
        private static final SEntityStatusPacket FIREWORK_ROCKET_DUMMY_EFFECT;

        static {
            FIREWORK_ROCKET_ID = EntityAccessor.accessor$getNextEntityId();
            EntityAccessor.accessor$setNextEntityId(FIREWORK_ROCKET_ID + 1);
            FIREWORK_ROCKET_UNIQUE_ID = MathHelper.func_180182_a(new Random());

            DESTROY_FIREWORK_ROCKET_DUMMY = new SDestroyEntitiesPacket(FIREWORK_ROCKET_ID);

            FIREWORK_ROCKET_DUMMY_EFFECT = new SEntityStatusPacket();
            ((SPacketEntityStatusAccessor) FIREWORK_ROCKET_DUMMY_EFFECT).accessor$setEntityId(FIREWORK_ROCKET_ID);
            // The status index that is used to trigger the fireworks effect,
            // can be found at: EntityFireworkRocket#handleStatusUpdate
            // or: EntityFireworkRocket#onUpdate -> setEntityState
            ((SPacketEntityStatusAccessor) FIREWORK_ROCKET_DUMMY_EFFECT).accessor$setLogicOpcoe((byte) 17);
        }

        private final SEntityMetadataPacket entityMetadataPacket;

        private CachedFireworkPacket(final SEntityMetadataPacket entityMetadataPacket) {
            this.entityMetadataPacket = entityMetadataPacket;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final SSpawnObjectPacket packetSpawnObject = new SSpawnObjectPacket();
            final SPacketSpawnObjectAccessor accessor = ((SPacketSpawnObjectAccessor) packetSpawnObject);
            accessor.accessor$setEntityId(FIREWORK_ROCKET_ID);
            accessor.accessor$setUniqueId(FIREWORK_ROCKET_UNIQUE_ID);
            accessor.accessor$setX(position.getX());
            accessor.accessor$setY(position.getY());
            accessor.accessor$setZ(position.getZ());
            // The internal id that that is used to spawn a "EntityFireworkRocket" on the client,
            // can be found at: EntityTrackerEntry#createSpawnPacket
            // or: NetHandlerPlayClient#handleSpawnObject
            accessor.accessor$setType(76);
            output.add(packetSpawnObject);
            output.add(this.entityMetadataPacket);
            output.add(FIREWORK_ROCKET_DUMMY_EFFECT);
            output.add(DESTROY_FIREWORK_ROCKET_DUMMY);
        }
    }

    private static final class CachedParticlePacket implements ICachedParticleEffect {

        private final EnumParticleTypes particleType;
        private final Vector3f offset;
        private final int quantity;
        private final int[] extra;

        private CachedParticlePacket(final EnumParticleTypes particleType, final Vector3f offset, final int quantity, final int[] extra) {
            this.particleType = particleType;
            this.quantity = quantity;
            this.offset = offset;
            this.extra = extra;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final float px = (float) position.getX();
            final float py = (float) position.getY();
            final float pz = (float) position.getZ();

            final float odx = this.offset.getX();
            final float ody = this.offset.getY();
            final float odz = this.offset.getZ();

            final SSpawnParticlePacket message = new SSpawnParticlePacket(
                    this.particleType, true, px, py, pz, odx, ody, odz, 0f, this.quantity, this.extra);
            output.add(message);
        }
    }

    private static final class CachedOffsetParticlePacket implements ICachedParticleEffect {

        private final EnumParticleTypes particleType;
        private final Vector3f offsetData;
        private final Vector3f offset;
        private final int quantity;
        private final int[] extra;

        private CachedOffsetParticlePacket(final EnumParticleTypes particleType, final Vector3f offsetData, final Vector3f offset, final int quantity, final int[] extra) {
            this.particleType = particleType;
            this.offsetData = offsetData;
            this.quantity = quantity;
            this.offset = offset;
            this.extra = extra;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final float px = (float) position.getX();
            final float py = (float) position.getY();
            final float pz = (float) position.getZ();

            final float odx = this.offsetData.getX();
            final float ody = this.offsetData.getY();
            final float odz = this.offsetData.getZ();

            if (this.offset.equals(Vector3f.ZERO)) {
                final SSpawnParticlePacket message = new SSpawnParticlePacket(
                        this.particleType, true, px, py, pz, odx, ody, odz, 1f, 0, this.extra);
                for (int i = 0; i < this.quantity; i++) {
                    output.add(message);
                }
            } else {
                final Random random = new Random();

                final float ox = this.offset.getX();
                final float oy = this.offset.getY();
                final float oz = this.offset.getZ();

                for (int i = 0; i < this.quantity; i++) {
                    final float px0 = px + (random.nextFloat() * 2f - 1f) * ox;
                    final float py0 = py + (random.nextFloat() * 2f - 1f) * oy;
                    final float pz0 = pz + (random.nextFloat() * 2f - 1f) * oz;

                    final SSpawnParticlePacket message = new SSpawnParticlePacket(
                            this.particleType, true, px0, py0, pz0, odx, ody, odz, 1f, 0, this.extra);
                    output.add(message);
                }
            }
        }
    }

    private static final class CachedEffectPacket implements ICachedParticleEffect {

        private final int type;
        private final int data;
        private final boolean broadcast;

        private CachedEffectPacket(final int type, final int data, final boolean broadcast) {
            this.broadcast = broadcast;
            this.type = type;
            this.data = data;
        }

        @Override
        public void process(final Vector3d position, final List<IPacket<?>> output) {
            final BlockPos blockPos = new BlockPos(position.getFloorX(), position.getFloorY(), position.getFloorZ());
            output.add(new SPlaySoundEventPacket(this.type, blockPos, this.data, this.broadcast));
        }
    }

    private SpongeParticleHelper() {
    }
}
