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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.state.BooleanStateProperty;
import org.spongepowered.api.state.EnumStateProperty;
import org.spongepowered.api.state.IntegerStateProperty;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockState;
import org.spongepowered.common.interfaces.block.IMixinPropertyHolder;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.provider.BlockPropertyIdProvider;
import org.spongepowered.common.registry.type.block.BooleanStatePropertyRegistryModule;
import org.spongepowered.common.registry.type.block.EnumStatePropertyRegistryModule;
import org.spongepowered.common.registry.type.block.IntegerStatePropertyRegistryModule;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;

import java.util.Map;

@RegisterCatalog(BlockTypes.class)
@RegistrationDependency(BlockChangeFlagRegistryModule.class)
public class BlockTypeRegistryModule extends AbstractCatalogRegistryModule<BlockType> implements SpongeAdditionalCatalogRegistryModule<BlockType> {

    public static BlockTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(BlockType catalog) {
        this.registerCustomBlock((ResourceLocation) (Object) catalog.getKey(), catalog);
    }

    public void registerFromGameData(ResourceLocation id, BlockType blockType) {
        this.registerCustomBlock(id, blockType);
    }

    private void registerCustomBlock(ResourceLocation id, BlockType blockType) {
        checkNotNull(id);
        checkNotNull(blockType);

        this.map.put((CatalogKey) (Object) id, blockType);
        registerBlockTrait(id.toString(), blockType);
        ((IMixinBlock) blockType).initializeTrackerState();
    }


    private void registerBlockTrait(String id, BlockType block) {
        Block nmsBlock = (Block) block;
        for (IBlockState state : nmsBlock.getStateContainer().getValidStates()) {
            ((IMixinBlockState) state).generateId(nmsBlock);
            BlockStateRegistryModule.getInstance().registerBlockState((BlockState) state);
        }
        for (Map.Entry<StateProperty<?>, ?> mapEntry : block.getDefaultState().getStatePropertyMap().entrySet()) {
            StateProperty<?> property = mapEntry.getKey();
            final CatalogKey propertyId = BlockPropertyIdProvider.getIdAndTryRegistration((IProperty<?>) property, (Block) block, id);
            if (property instanceof IMixinPropertyHolder) {
                ((IMixinPropertyHolder) property).setId(propertyId);
            }
            if (property instanceof BooleanStateProperty) {
                EnumStatePropertyRegistryModule.getInstance().registerBlock(propertyId, block, (EnumStateProperty<?>) property);
            } else if (property instanceof IntegerStateProperty) {
                IntegerStatePropertyRegistryModule.getInstance().registerBlock(propertyId, block, (IntegerStateProperty) property);
            } else if (property instanceof BooleanStateProperty) {
                BooleanStatePropertyRegistryModule.getInstance().registerBlock(propertyId, block, (BooleanStateProperty) property);
            }
        }
    }

    @Override
    public void registerDefaults() {
        BlockSnapshot NONE_SNAPSHOT = new SpongeBlockSnapshotBuilder().worldId(BlockUtil.INVALID_WORLD_UUID).position(new Vector3i(0, 0, 0)).blockState((BlockState) Blocks.AIR.getDefaultState()).build();
        RegistryHelper.setFinalStatic(BlockSnapshot.class, "NONE", NONE_SNAPSHOT);
        this.map.put(CatalogKey.sponge("none"), (BlockType) Blocks.AIR);
    }

    private BlockTypeRegistryModule() { }

    private static final class Holder {
        static final BlockTypeRegistryModule INSTANCE = new BlockTypeRegistryModule();
    }
}
