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
package org.spongepowered.common.registry.type.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.data.type.SpongeTreeType;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(ItemTypeRegistryModule.class)
@RegisterCatalog(TreeTypes.class)
public final class TreeTypeRegistryModule extends AbstractCatalogRegistryModule<TreeType> implements SpongeAdditionalCatalogRegistryModule<TreeType> {

    public static TreeTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private TreeTypeRegistryModule() {
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(TreeType treeType) {
        this.map.put(treeType.getKey(), treeType);
    }

    @Override
    public void registerDefaults() {
        final Map<String, String> names = new HashMap<>();
        names.put("oak", CatalogKey.MINECRAFT_NAMESPACE);
        names.put("birch", CatalogKey.MINECRAFT_NAMESPACE);
        names.put("acacia", CatalogKey.MINECRAFT_NAMESPACE);
        names.put("dark_oak", CatalogKey.MINECRAFT_NAMESPACE);
        names.put("jungle", CatalogKey.MINECRAFT_NAMESPACE);
        names.put("chorus", CatalogKey.MINECRAFT_NAMESPACE); // Is this a real tree?

        // Extract all possible tree types from item names
        for (ItemType itemType : ItemTypeRegistryModule.getInstance().getAll()) {
            String name = itemType.getKey().getValue();
            if (name.endsWith("_log")) {
                name = name.substring(0, name.lastIndexOf('_'));
            }
            names.putIfAbsent(name, itemType.getKey().getNamespace());
        }

        names.forEach((key, namespace) -> new SpongeTreeType(CatalogKey.of(namespace, key), key));
    }

    public static Optional<TreeType> getTreeType(String name) {
        for (TreeType treeType : getInstance().map.values()) {
            if (name.startsWith(treeType.getName())) {
                return Optional.of(treeType);
            }
        }
        return Optional.empty();
    }

    public static Optional<TreeType> getTreeType(IBlockState blockState) {
        final Block block = blockState.getBlock();
        Optional<TreeType> treeType = ((IMixinBlock) block).getTreeType();
        if (treeType != null) {
            return treeType;
        }
        treeType = getTreeType(((BlockType) block).getKey().getValue());
        ((IMixinBlock) block).setTreeType(treeType);
        return treeType;
    }

    private static final class Holder {
        static final TreeTypeRegistryModule INSTANCE = new TreeTypeRegistryModule();
    }
}
