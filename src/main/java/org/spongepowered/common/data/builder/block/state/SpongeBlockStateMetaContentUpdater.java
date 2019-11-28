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
package org.spongepowered.common.data.builder.block.state;

import net.minecraft.block.Block;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeBlockStateMetaContentUpdater implements DataContentUpdater {

    @Override
    public int getInputVersion() {
        return Constants.Sponge.BlockState.BLOCK_TYPE_WITH_DAMAGE_VALUE;
    }

    @Override
    public int getOutputVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DataView update(DataView content) {
        // Right, so we have to get the block type id
        final String blockTypeId = content.getString(Constants.Block.BLOCK_TYPE).get();
        // Check if it's there....
        final Optional<BlockType> blockType = Sponge.getRegistry().getType(BlockType.class, blockTypeId);
        if (!blockType.isPresent()) {
            // sure, throw an exception to throw down the chain
            throw new InvalidDataException("Could not find a block type for the given id: " + blockTypeId);
        }
        // Get the meta, if it wasn't available, just default to the default block state.
        final int meta = content.getInt(Constants.Block.BLOCK_STATE_UNSAFE_META).orElse(0);
        // Get the block type
        final BlockType type = blockType.get();
        // Cast to internal and get the block state from damage value, this is purely
        // implementation of minecraft, mods may change this in the future, not really known how
        // they will handle it?

        final net.minecraft.block.BlockState blockState = ((Block) type).getStateFromMeta(meta);
        // Now that we have the actual block state, delete the old data
        content.remove(Constants.Block.BLOCK_TYPE);
        content.remove(Constants.Block.BLOCK_STATE_UNSAFE_META);

        // Cast to the API state to get the id
        final BlockState apiState = (BlockState) blockState;
        // set the id
        content.set(Constants.Block.BLOCK_STATE, apiState.getId());
        // set the version!!
        content.set(Queries.CONTENT_VERSION, 2);
        // Presto!
        return content;
    }
}
