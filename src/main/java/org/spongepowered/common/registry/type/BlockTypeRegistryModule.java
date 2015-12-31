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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.block.trait.BooleanTrait;
import org.spongepowered.api.block.trait.EnumTrait;
import org.spongepowered.api.block.trait.IntegerTrait;
import org.spongepowered.common.interfaces.block.IMixinPropertyHolder;
import org.spongepowered.common.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.AlternateCatalogRegistryModule;
import org.spongepowered.common.registry.type.block.BooleanTraitRegistryModule;
import org.spongepowered.common.registry.type.block.EnumTraitRegistryModule;
import org.spongepowered.common.registry.type.block.IntegerTraitRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlockTypeRegistryModule implements AdditionalCatalogRegistryModule<BlockType>, AlternateCatalogRegistryModule<BlockType> {

    public static BlockTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(BlockTypes.class)
    private final Map<String, BlockType> blockTypeMappings = Maps.newHashMap();

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
        if (!id.contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.blockTypeMappings.get(checkNotNull(id).toLowerCase()));
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
        this.blockTypeMappings.put(extraCatalog.getId().toLowerCase(), extraCatalog);
        registerBlockTrait(extraCatalog.getId(), extraCatalog);
    }

    public void registerFromGameData(String id, BlockType blockType) {
        this.blockTypeMappings.put(id.toLowerCase(), blockType);
        registerBlockTrait(id, blockType);
    }

    private void registerBlockTrait(String id, BlockType block) {
        for (Map.Entry<BlockTrait<?>, ?> mapEntry : block.getDefaultState().getTraitMap().entrySet()) {
            BlockTrait<?> property = mapEntry.getKey();
            ((IMixinPropertyHolder) property).setId(id.toLowerCase() + "_" + property.getName().toLowerCase());
            if (property instanceof EnumTrait) {
                EnumTraitRegistryModule.getInstance().registerBlock(id, block, (EnumTrait<?>) property);
            } else if (property instanceof IntegerTrait) {
                IntegerTraitRegistryModule.getInstance().registerBlock(id, block, (IntegerTrait) property);
            } else if (property instanceof BooleanTrait) {
                BooleanTraitRegistryModule.getInstance().registerBlock(id, block, (BooleanTrait) property);
            }
        }
    }

    private BlockTypeRegistryModule() {
    }

    private static final class Holder {

        private static final BlockTypeRegistryModule INSTANCE = new BlockTypeRegistryModule();
    }
}
