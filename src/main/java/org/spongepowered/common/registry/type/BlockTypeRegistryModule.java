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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.block.trait.BooleanTrait;
import org.spongepowered.api.block.trait.EnumTrait;
import org.spongepowered.api.block.trait.IntegerTrait;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.provider.BlockPropertyIdProvider;
import org.spongepowered.common.registry.type.block.BooleanTraitRegistryModule;
import org.spongepowered.common.registry.type.block.EnumTraitRegistryModule;
import org.spongepowered.common.registry.type.block.IntegerTraitRegistryModule;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(BlockChangeFlagRegistryModule.class)
public class BlockTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<BlockType>, AlternateCatalogRegistryModule<BlockType> {

    public static BlockTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(BlockTypes.class)
    private final Map<String, BlockType> blockTypeMappings = Maps.newHashMap();

    private final BiMap<String, BlockTrait<?>> blockTraitMap = HashBiMap.create();

    public String getIdFor(IProperty<?> blockTrait) {
        return checkNotNull(this.blockTraitMap.inverse().get(blockTrait), "BlockTrait doesn't have a registered id!");
    }

    @Override
    public Map<String, BlockType> provideCatalogMap() {
        Map<String, BlockType> blockMap = new HashMap<>();
        for (Map.Entry<String, BlockType> entry : this.blockTypeMappings.entrySet()) {
            blockMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return blockMap;
    }

    @Override
    public Optional<BlockType> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":") && !id.equals("none")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.blockTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<BlockType> getAll() {
        return ImmutableSet.copyOf(this.blockTypeMappings.values());
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(BlockType extraCatalog) {
        this.registerCustomBlock(extraCatalog.getId(), extraCatalog);
    }

    public void registerFromGameData(String id, BlockType blockType) {
        this.registerCustomBlock(id, blockType);
    }

    private void registerCustomBlock(String id, BlockType blockType) {
        this.blockTypeMappings.put(id.toLowerCase(Locale.ENGLISH), blockType);
        registerBlockTrait(id, blockType);
        ((BlockBridge) blockType).bridge$initializeTrackerState();
    }


    private void registerBlockTrait(String id, BlockType block) {
        Block nmsBlock = (Block) block;
        for (IBlockState state : nmsBlock.func_176194_O().func_177619_a()) {
            BlockStateRegistryModule.getInstance().registerBlockState((BlockState) state);
        }
        for (Map.Entry<BlockTrait<?>, ?> mapEntry : block.getDefaultState().getTraitMap().entrySet()) {
            BlockTrait<?> property = mapEntry.getKey();
            final String propertyId = BlockPropertyIdProvider.getIdAndTryRegistration((IProperty<?>) property, (Block) block, id);
            if (property instanceof EnumTrait) {
                EnumTraitRegistryModule.getInstance().registerBlock(propertyId, block, (EnumTrait<?>) property);
            } else if (property instanceof IntegerTrait) {
                IntegerTraitRegistryModule.getInstance().registerBlock(propertyId, block, (IntegerTrait) property);
            } else if (property instanceof BooleanTrait) {
                BooleanTraitRegistryModule.getInstance().registerBlock(propertyId, block, (BooleanTrait) property);
            }
        }
    }

    @Override
    public void registerDefaults() {
        BlockSnapshot NONE_SNAPSHOT = SpongeBlockSnapshotBuilder.pooled()
            .worldId(Constants.World.INVALID_WORLD_UUID)
            .position(new Vector3i(0, 0, 0))
            .blockState(Blocks.field_150350_a.func_176223_P())
            .build();
        RegistryHelper.setFinalStatic(BlockSnapshot.class, "NONE", NONE_SNAPSHOT);
        this.blockTypeMappings.put("none", (BlockType) Blocks.field_150350_a);
    }

    BlockTypeRegistryModule() { }

    private static final class Holder {
        static final BlockTypeRegistryModule INSTANCE = new BlockTypeRegistryModule();
    }
}
