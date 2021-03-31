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
package org.spongepowered.common.world.volume.buffer.blockentity;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class ObjectArrayMutableBlockEntityBuffer extends AbstractMutableBlockEntityBuffer<ObjectArrayMutableBlockEntityBuffer> {

    private final ArrayList<BlockEntity> blockEntities;

    public ObjectArrayMutableBlockEntityBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        // Very naive, but effectively creates enough to cover a 2d slice of area with block entities.
        this.blockEntities = new ArrayList<>();
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.blockEntities.add(blockEntity);
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.blockEntities.removeIf(be -> {
            final Vector3i pos = be.blockPosition();
            return pos.getX() == x && pos.getZ() == z && pos.getY() == y;
        });
    }


    @Override
    public VolumeStream<ObjectArrayMutableBlockEntityBuffer, BlockEntity> blockEntityStream(final Vector3i min, final Vector3i max,
            final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.blockMin(), this.blockMax(), options);

        final Stream<VolumeElement<ObjectArrayMutableBlockEntityBuffer, BlockEntity>> blockEntityStream = this.blockEntities.stream()
            .map(be -> VolumeElement.of(this, be, be.blockPosition()));
        return new SpongeVolumeStream<>(blockEntityStream, () -> this);
    }

    @Override
    public Collection<? extends BlockEntity> blockEntities() {
        return this.blockEntities;
    }

    @Override
    public Optional<? extends BlockEntity> blockEntity(final int x, final int y, final int z) {
        return this.blockEntities.stream().filter(be -> {
            final Vector3i pos = be.blockPosition();
            return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
        }).findFirst();
    }

    public net.minecraft.world.level.block.entity.@Nullable BlockEntity getTileEntity(final BlockPos blockPos) {
        return (net.minecraft.world.level.block.entity.BlockEntity) this.blockEntity(blockPos.getX(), blockPos.getY(), blockPos.getZ()).orElse(null);
    }
}
