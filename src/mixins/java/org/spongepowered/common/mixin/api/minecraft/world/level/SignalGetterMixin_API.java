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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.SignalType;
import org.spongepowered.api.world.volume.game.SignalAwareVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.world.SpongeSignalType;

import java.util.Objects;
import java.util.function.ToIntBiFunction;

@Mixin(SignalGetter.class)
public interface SignalGetterMixin_API extends BlockGetter, SignalAwareVolume {

    @Shadow int shadow$getSignal(BlockPos $$0, net.minecraft.core.Direction $$1);
    @Shadow int shadow$getBestNeighborSignal(BlockPos $$0);
    @Shadow int shadow$getDirectSignal(BlockPos $$0, net.minecraft.core.Direction $$1);
    @Shadow int shadow$getDirectSignalTo(BlockPos $$0);
    @Shadow boolean shadow$hasNeighborSignal(BlockPos $$0);

    @Override
    default boolean canConductSignal(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        return this.getBlockState(pos).isRedstoneConductor(this, pos);
    }

    @Override
    default boolean canEmitSignal(final SignalType type, final int x, final int y, final int z) {
        Objects.requireNonNull(type, "type");
        final BlockPos pos = new BlockPos(x, y, z);
        if (type == SpongeSignalType.ANALOG) {
            return this.getBlockState(pos).hasAnalogOutputSignal();
        } else if (type == SpongeSignalType.DIRECT || type == SpongeSignalType.INDIRECT || type == SpongeSignalType.COMPOSITE) {
            return this.getBlockState(pos).isSignalSource();
        }

        throw this.impl$unsupportedType(type);
    }

    @Override
    default int signalFrom(final SignalType type, final int x, final int y, final int z, final Direction direction) {
        Objects.requireNonNull(type, "type");
        final BlockPos pos = new BlockPos(x, y, z);
        if (type == SpongeSignalType.ANALOG) {
            return this.impl$analogSignalFrom(pos);
        } else if (type == SpongeSignalType.DIRECT) {
            return this.impl$regularSignalFrom(pos, DirectionUtil.getForOrThrow(direction).getOpposite());
        } else if (type == SpongeSignalType.INDIRECT) {
            return this.shadow$getDirectSignal(pos, DirectionUtil.getForOrThrow(direction).getOpposite());
        } else if (type == SpongeSignalType.COMPOSITE) {
            return this.shadow$getSignal(pos, DirectionUtil.getForOrThrow(direction).getOpposite());
        }

        throw this.impl$unsupportedType(type);
    }

    @Override
    default int highestSignalAt(final SignalType type, final int x, final int y, final int z) {
        Objects.requireNonNull(type, "type");
        final BlockPos pos = new BlockPos(x, y, z);
        if (type == SpongeSignalType.ANALOG) {
            return this.impl$highestSignalAt(pos, (relativePos, $) -> this.impl$analogSignalFrom(relativePos));
        } else if (type == SpongeSignalType.DIRECT) {
            return this.impl$highestSignalAt(pos, this::impl$regularSignalFrom);
        } else if (type == SpongeSignalType.INDIRECT) {
            return this.shadow$getDirectSignalTo(pos);
        } else if (type == SpongeSignalType.COMPOSITE) {
            return this.shadow$getBestNeighborSignal(pos);
        }

        throw this.impl$unsupportedType(type);
    }

    @Override
    default boolean hasSignalAt(final SignalType type, final int x, final int y, final int z) {
        Objects.requireNonNull(type, "type");
        final BlockPos pos = new BlockPos(x, y, z);
        if (type == SpongeSignalType.ANALOG) {
            return this.impl$hasSignalAt(pos, (relativePos, $) -> this.impl$analogSignalFrom(relativePos));
        } else if (type == SpongeSignalType.DIRECT) {
            return this.impl$hasSignalAt(pos, this::impl$regularSignalFrom);
        } else if (type == SpongeSignalType.INDIRECT) {
            return this.impl$hasSignalAt(pos, this::shadow$getDirectSignal);
        } else if (type == SpongeSignalType.COMPOSITE) {
            return this.shadow$hasNeighborSignal(pos);
        }

        throw this.impl$unsupportedType(type);
    }

    private int impl$highestSignalAt(
        final BlockPos pos, final ToIntBiFunction<BlockPos, net.minecraft.core.Direction> signalProvider
    ) {
        int maxSignal = 0;
        for (final net.minecraft.core.Direction direction : SignalGetter.DIRECTIONS) {
            final int signal = signalProvider.applyAsInt(pos.relative(direction), direction);
            if (signal >= 15) {
                return 15;
            }

            if (signal > maxSignal) {
                maxSignal = signal;
            }
        }
        return maxSignal;
    }

    private boolean impl$hasSignalAt(
        final BlockPos pos, final ToIntBiFunction<BlockPos, net.minecraft.core.Direction> signalProvider
    ) {
        for (final net.minecraft.core.Direction direction : SignalGetter.DIRECTIONS) {
            final int signal = signalProvider.applyAsInt(pos.relative(direction), direction);
            if (signal > 0) {
                return true;
            }
        }
        return false;
    }

    private int impl$analogSignalFrom(final BlockPos pos) {
        if (this instanceof final Level level) {
            return this.getBlockState(pos).getAnalogOutputSignal(level, pos);
        } else {
            throw new UnsupportedOperationException("This volume doesn't support getting analog signal");
        }
    }

    private int impl$regularSignalFrom(final BlockPos pos, final net.minecraft.core.Direction direction) {
        return this.getBlockState(pos).getSignal(this, pos, direction);
    }

    private RuntimeException impl$unsupportedType(final SignalType type) {
        return new IllegalArgumentException("Unsupported signal type: " + type.key(RegistryTypes.SIGNAL_TYPE).asString());
    }
}
