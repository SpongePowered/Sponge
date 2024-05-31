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
package org.spongepowered.common.world.volume.buffer.block;

import net.minecraft.core.BlockPos;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.volume.block.BlockVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.schematic.MutableBimapPalette;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayMutableBlockBuffer extends AbstractBlockBuffer implements BlockVolume.Mutable {

    private static final BlockState AIR = BlockTypes.AIR.get().defaultState();

    private final Palette.Mutable<BlockState, BlockType> palette;
    private final RegistryReference<BlockType> defaultState;
    private BlockBackingData data;
    private final RegistryHolder registries;

    public ArrayMutableBlockBuffer(final Vector3i start, final Vector3i size) {
        this(
            new MutableBimapPalette<>(
                PaletteTypes.BLOCK_STATE_PALETTE.get(),
                Sponge.game().registry(RegistryTypes.BLOCK_TYPE)
            ),
            BlockTypes.AIR,
            start,
            size
        );
    }

    public ArrayMutableBlockBuffer(final Palette<BlockState, BlockType> palette, final RegistryReference<BlockType> defaultState,
            final Vector3i start, final Vector3i size
    ) {
        super(start, size);
        final Palette.Mutable<BlockState, BlockType> mutablePalette = palette.asMutable(Sponge.game());
        this.palette = mutablePalette;
        final int airId = mutablePalette.orAssign(ArrayMutableBlockBuffer.AIR);

        final int dataSize = this.area();
        this.defaultState = defaultState;
        this.data = new BlockBackingData.PackedBackingData(dataSize, palette.highestId());

        // all blocks default to air
        if (airId != 0) {
            for (int i = 0; i < dataSize; i++) {
                this.data.set(i, airId);
            }
        }
        this.registries = Sponge.game();
    }

    public ArrayMutableBlockBuffer(final Palette<BlockState, BlockType> palette, final Vector3i start, final Vector3i size, final char[] blocks) {
        super(start, size);
        this.palette = palette.asMutable(Sponge.game());
        this.data = new BlockBackingData.CharBackingData(blocks);
        this.defaultState = BlockTypes.AIR;
        this.registries = Sponge.game();
    }

    /**
     * Does not clone!
     *
     * @param palette The palette
     * @param blocks The backing data
     * @param start The start block position
     * @param size The block size
     */
    ArrayMutableBlockBuffer(final Palette<BlockState, BlockType> palette, final BlockBackingData blocks, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.palette = palette.asMutable(Sponge.game());
        this.data = blocks;
        this.defaultState = BlockTypes.AIR;
        this.registries = Sponge.game();
    }

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.palette;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        this.checkRange(x, y, z);
        final int id = this.palette.orAssign(block);
        if (id > this.data.getMax()) {

            final int highId = this.palette.highestId();
            final int dataSize = this.area();
            final BlockBackingData newdata = new BlockBackingData.PackedBackingData(dataSize, highId);
            for (int i = 0; i < dataSize; i++) {
                newdata.set(i, this.data.get(i));
            }
            this.data = newdata;
        }
        this.data.set(this.getIndex(x, y, z), id);
        return true;
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.setBlock(x, y, z, BlockTypes.AIR.get().defaultState());
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        final int id = this.data.get(this.getIndex(x, y, z));
        return this.palette.get(id, this.registries)
                .orElseGet(() -> this.defaultState.get(this.registries).defaultState());
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        return this.block(x, y, z).fluidState();
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return 0;
    }

    private int area() {
        return this.size.x() * this.size.y() * this.size.z();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ArrayMutableBlockBuffer that = (ArrayMutableBlockBuffer) o;
        return this.palette.equals(that.palette) &&
                this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.palette, this.data);
    }

    @Override
    public VolumeStream<BlockVolume.Mutable, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.min(), this.max(), options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = this.copy();
        } else {
            buffer = this;
        }
        final Stream<VolumeElement<BlockVolume.Mutable, BlockState>> stateStream = IntStream.rangeClosed(min.x(), max.x())
                .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                        .mapToObj(z -> IntStream.rangeClosed(min.y(), max.y())
                                .mapToObj(y -> VolumeElement.of((BlockVolume.Mutable) this, () -> buffer.block(x, y, z), new Vector3d(x, y, z)))
                        ).flatMap(Function.identity())
                ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    public void setBlock(final BlockPos pos, final net.minecraft.world.level.block.state.BlockState blockState) {
        this.setBlock(pos.getX(), pos.getY(), pos.getZ(), (BlockState) blockState);
    }

    public net.minecraft.world.level.block.state.BlockState getBlock(final BlockPos blockPos) {
        return (net.minecraft.world.level.block.state.BlockState) this.block(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public ArrayMutableBlockBuffer copy() {
        return  new ArrayMutableBlockBuffer(this.palette, this.data.copyOf(), this.start, this.size);
    }

    public BlockBackingData getCopiedBackingData() {
        return this.data.copyOf();
    }

}
