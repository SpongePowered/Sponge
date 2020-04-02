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
package org.spongepowered.common.data.builder.tileentity;

import com.google.common.base.Preconditions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractTileEntityBuilder<T extends BlockEntity> extends AbstractDataBuilder<T> implements DataBuilder<T> {
    private final Supplier<BlockType> defaultBlockType;

    protected AbstractTileEntityBuilder(Class<T> requiredClass, Supplier<BlockType> defaultBlockType, int supportedVersion) {
        super(requiredClass, supportedVersion);
        this.defaultBlockType = defaultBlockType;
    }

    @Override
    protected Optional<T> buildContent(final DataView container) throws InvalidDataException {
        Preconditions.checkNotNull(container, "DataContainer cannot be null in deserialization");

        if (!this.validate(container)) {
            return Optional.empty();
        }

        String worldName = container.getString(Constants.TileEntity.WORLD).orElseThrow(() -> new InvalidDataException("The provided container references a world that does not exist!"));
        ServerWorld serverWorld = Sponge.getGame().getServer().getWorldManager().getWorld(worldName).orElseThrow(() -> new InvalidDataException("The provided container references a world that does not exist!"));

        final BlockEntityType type = container.getCatalogType(Constants.TileEntity.TILE_TYPE, BlockEntityType.class).orElseThrow(() -> new InvalidDataException("The provided container references a block entity type that does not exist!"));
        final BlockType blockType = this.defaultBlockType.get();

        // Now we should be ready to actually translate the TileEntity with the right block.
        final int x = container.getInt(Constants.DataSerializers.X_POS).get();
        final int y = container.getInt(Constants.DataSerializers.Y_POS).get();
        final int z = container.getInt(Constants.DataSerializers.Z_POS).get();

        // If a plugin or mod cancels this, then return
        if (!serverWorld.getLocation(x, y, z).setBlockType(blockType)) {
            return Optional.empty();
        }

        final BlockPos pos = new BlockPos(x, y, z);
        TileEntity tileEntity = ((net.minecraft.world.World) serverWorld).getTileEntity(pos);
        if (tileEntity == null) {
            return Optional.empty(); // TODO throw exception maybe?
        }

        return Optional.of((T) tileEntity);
    }

    /**
     * Validates whether this data view contains all required data to build a block entity.
     * Implementations that need to validate additional data should call 'super' and then do additional checks.
     *
     * @param container the container
     * @return if false, no block entity will be built. Otherwise an attempt will be made to create the block entity.
     */
    protected boolean validate(final DataView container) {
        return container.contains(Constants.TileEntity.TILE_TYPE, Constants.TileEntity.WORLD, Queries.POSITION_X, Queries.POSITION_Y, Queries.POSITION_Z);
    }
}
