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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.type.SpongeNotePitch;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
    public static List<Packet<?>> toPackets(SpongeParticleEffect effect, Vector3d position) {
        SpongeParticleType type = effect.getType();
        EnumParticleTypes internal = type.getInternalType();

        Vector3d offset = effect.getOption(ParticleOptions.OFFSET).orElse(Vector3d.ZERO);

        int count = effect.getOption(ParticleOptions.COUNT).orElse(1);
        int[] extra = null;

        float px = (float) position.getX();
        float py = (float) position.getY();
        float pz = (float) position.getZ();

        double ox = offset.getX();
        double oy = offset.getY();
        double oz = offset.getZ();

        // The extra values, normal behavior offsetX, offsetY, offsetZ
        double f0 = 0f;
        double f1 = 0f;
        double f2 = 0f;

        // Depends on behavior
        // Note: If the count > 0 -> speed = 0f else if count = 0 -> speed = 1f

        Optional<BlockState> defaultBlockState;
        if (internal != EnumParticleTypes.ITEM_CRACK && (defaultBlockState = type.getDefaultOption(ParticleOptions.BLOCK_STATE)).isPresent()) {
            Optional<BlockState> blockState = effect.getOption(ParticleOptions.BLOCK_STATE);
            if (blockState.isPresent()) {
                extra = new int[] { Block.getStateId((IBlockState) blockState.get()) };
            } else {
                Optional<ItemStackSnapshot> optSnapshot = effect.getOption(ParticleOptions.ITEM_STACK_SNAPSHOT);
                if (optSnapshot.isPresent()) {
                    ItemStackSnapshot snapshot = optSnapshot.get();
                    Optional<BlockType> blockType = snapshot.getType().getBlock();
                    if (blockType.isPresent()) {
                        extra = new int[] { Block.getStateId(((Block) blockType.get()).getStateFromMeta(
                                ((SpongeItemStackSnapshot) snapshot).getDamageValue())) };
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    extra = new int[] { Block.getStateId((IBlockState) defaultBlockState.get()) };
                }
            }
        }

        Optional<ItemStackSnapshot> defaultSnapshot;
        if (extra == null && (defaultSnapshot = type.getDefaultOption(ParticleOptions.ITEM_STACK_SNAPSHOT)).isPresent()) {
            Optional<ItemStackSnapshot> optSnapshot = effect.getOption(ParticleOptions.ITEM_STACK_SNAPSHOT);
            if (optSnapshot.isPresent()) {
                ItemStackSnapshot snapshot = optSnapshot.get();
                extra = new int[] { Item.getIdFromItem((Item) snapshot.getType()), ((SpongeItemStackSnapshot) snapshot).getDamageValue() };
            } else {
                Optional<BlockState> optBlockState = effect.getOption(ParticleOptions.BLOCK_STATE);
                if (optBlockState.isPresent()) {
                    BlockState blockState = optBlockState.get();
                    Optional<ItemType> optItemType =  blockState.getType().getItem();
                    if (optItemType.isPresent()) {
                        extra = new int[] { Item.getIdFromItem((Item) optItemType.get()),
                                ((Block) blockState.getType()).getMetaFromState((IBlockState) blockState) };
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    ItemStackSnapshot snapshot = defaultSnapshot.get();
                    extra = new int[] { Item.getIdFromItem((Item) snapshot.getType()), ((SpongeItemStackSnapshot) snapshot).getDamageValue() };
                }
            }
        }

        if (extra == null) {
            extra = new int[0];
        }

        Optional<Double> defaultScale = type.getDefaultOption(ParticleOptions.SCALE);
        Optional<Color> defaultColor;
        Optional<NotePitch> defaultNote;
        Optional<Vector3d> defaultVelocity;
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
                return Collections.singletonList(
                        new SPacketParticles(internal, true, px, py, pz, (float) ox, (float) oy, (float) oz, 0f, count, extra));
            }

            f0 = scale;
        } else if ((defaultColor = type.getDefaultOption(ParticleOptions.COLOR)).isPresent()) {
            Color color = effect.getOption(ParticleOptions.COLOR).orElse(null);

            if (color == null || color.equals(defaultColor.get())) {
                return Collections.singletonList(
                        new SPacketParticles(internal, true, px, py, pz, (float) ox, (float) oy, (float) oz, 0f, count, extra));
            }

            f0 = color.getRed() / 255f;
            f1 = color.getGreen() / 255f;
            f2 = color.getBlue() / 255f;

            // If the f0 value 0 is, the redstone will set it automatically to red 255
            if (f0 == 0f && internal == EnumParticleTypes.REDSTONE) {
                f0 = 0.00001f;
            }
        } else if ((defaultNote = type.getDefaultOption(ParticleOptions.NOTE)).isPresent()) {
            NotePitch notePitch = effect.getOption(ParticleOptions.NOTE).orElse(defaultNote.get());
            float note = ((SpongeNotePitch) notePitch).getByteId();

            if (note == 0f) {
                return Collections.singletonList(
                        new SPacketParticles(internal, true, px, py, pz, (float) ox, (float) oy, (float) oz, 0f, count, extra));
            }

            f0 = note / 24f;
        } else if ((defaultVelocity = type.getDefaultOption(ParticleOptions.VELOCITY)).isPresent()) {
            Vector3d velocity = effect.getOption(ParticleOptions.VELOCITY).orElse(defaultVelocity.get());

            f0 = velocity.getX();
            f1 = velocity.getY();
            f2 = velocity.getZ();

            // The y value won't work for this effect, if the value isn't 0 the motion won't work
            if (internal == EnumParticleTypes.WATER_SPLASH) {
                f1 = 0f;
            }

            if (f0 == 0f && f1 == 0f && f2 == 0f) {
                return Collections.singletonList(
                        new SPacketParticles(internal, true, px, py, pz, (float) ox, (float) oy, (float) oz, 0f, count, extra));
            }
        }

        // Is this check necessary?
        if (f0 == 0f && f1 == 0f && f2 == 0f) {
            return Collections.singletonList(new SPacketParticles(internal, true, px, py, pz, (float) ox, (float) oy, (float) oz, 0f, count, extra));
        }

        List<Packet<?>> packets = new ArrayList<>(count);

        if (ox == 0f && oy == 0f && oz == 0f) {
            for (int i = 0; i < count; i++) {
                packets.add(new SPacketParticles(internal, true, px, py, pz, (float) f0, (float) f1, (float) f2, 1f, 0, extra));
            }
        } else {
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                double px0 = (px + (random.nextFloat() * 2f - 1f) * ox);
                double py0 = (py + (random.nextFloat() * 2f - 1f) * oy);
                double pz0 = (pz + (random.nextFloat() * 2f - 1f) * oz);

                packets.add(new SPacketParticles(internal, true, (float) px0, (float) py0, (float) pz0, (float) f0, (float) f1, (float) f2, 1f, 0,
                        extra));
            }
        }

        return packets;
    }

    private SpongeParticleHelper() {
    }
}
