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
package org.spongepowered.common.data.builder.block.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.accessor.tileentity.TileEntityTypeAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.Set;

/**
 * This is the base abstract {@link DataBuilder} for all vanilla
 * {@link TileEntity}(ies).
 *
 * @param <T> The type of sponge tile entity
 */
public abstract class AbstractTileBuilder<T extends org.spongepowered.api.block.entity.BlockEntity> extends AbstractDataBuilder<T> implements DataBuilder<T> {

    protected AbstractTileBuilder(Class<T> clazz, int version) {
        super(clazz, version);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<T> buildContent(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(Constants.TileEntity.TILE_TYPE, Constants.TileEntity.WORLD, Queries.POSITION_X, Queries.POSITION_Y, Queries.POSITION_Z)) {
            return Optional.empty();
        }
        final String worldName = container.getString(Constants.TileEntity.WORLD).get();
        final Optional<ServerWorld> worldOptional = Sponge.getGame().getServer().getWorldManager().getWorld(worldName);
        if (!worldOptional.isPresent()) {
            throw new InvalidDataException("The provided container references a world that does not exist!");
        }

        final String tile = container.getString(Constants.TileEntity.TILE_TYPE)
            .orElseThrow(() -> new InvalidDataException("Could not find BlockEntityType"));
        final Optional<BlockEntityType> type = SpongeImpl.getRegistry().getCatalogRegistry().get(BlockEntityType.class, CatalogKey.resolve(tile));
        if (!type.isPresent()) {
            // TODO do we want to throw an InvalidDataException since the type is not registered?
            return Optional.empty(); // basically we didn't manage to find the type and the type isn't even registered with MC
        }

        final Set<Block> blocks = ((TileEntityTypeAccessor) type.get()).accessor$getValidBlocks();
        if (blocks.isEmpty()) {
            // TODO do we want to throw an InvalidDataException? This should be impossible
            return Optional.empty();
        }

        final Block block = blocks.iterator().next();
        // Now we should be ready to actually translate the TileEntity with the right block.

        final int x = container.getInt(Constants.DataSerializers.X_POS).get();
        final int y = container.getInt(Constants.DataSerializers.Y_POS).get();
        final int z = container.getInt(Constants.DataSerializers.Z_POS).get();

        final World world = (World) worldOptional.get();
        final BlockPos pos = new BlockPos(x, y, z);
        world.setBlockState(pos, block.getDefaultState());
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return Optional.empty(); // TODO throw exception maybe?
        }
        // We really need to validate only after the implementing class deems it ready...
        tileEntity.remove();
        return Optional.of((T) tileEntity);
    }

}
