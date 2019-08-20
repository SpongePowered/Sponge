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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.BlockPlanks;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.type.MinecraftEnumBasedCatalogTypeModule;

import java.util.Map;

@RegisterCatalog(TreeTypes.class)
public final class TreeTypeRegistryModule extends MinecraftEnumBasedCatalogTypeModule<BlockPlanks.EnumType, TreeType> {

    private static final class Holder {
        private static final TreeTypeRegistryModule INSTANCE = new TreeTypeRegistryModule();
    }

    public static TreeTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("ConstantConditions")
    public static BlockPlanks.EnumType getFor(final TreeType treeType) {
        return (BlockPlanks.EnumType) (Object) treeType;
    }

    @SuppressWarnings("ConstantConditions")
    public static TreeType getFor(final BlockPlanks.EnumType planks) {
        return (TreeType) (Object) planks;
    }

    TreeTypeRegistryModule() {
        super();
    }

    @Override
    protected BlockPlanks.EnumType[] getValues() {
        return BlockPlanks.EnumType.values();
    }
}
