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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockSilverfish;
import org.spongepowered.api.data.type.DisguisedBlockType;
import org.spongepowered.api.data.type.DisguisedBlockTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class DisguisedBlockTypeRegistryModule implements CatalogRegistryModule<DisguisedBlockType> {

    @RegisterCatalog(DisguisedBlockTypes.class)
    private final Map<String, DisguisedBlockType> disguisedBlockTypeMappings = new ImmutableMap.Builder<String, DisguisedBlockType>()
        .put("stone", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.STONE)
        .put("cobblestone", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.COBBLESTONE)
        .put("stonebrick", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.STONEBRICK)
        .put("mossy_stonebrick", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.MOSSY_STONEBRICK)
        .put("cracked_stonebrick", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.CRACKED_STONEBRICK)
        .put("chiseled_stonebrick", (DisguisedBlockType) (Object) BlockSilverfish.EnumType.CHISELED_STONEBRICK)
        .build();

    @Override
    public Optional<DisguisedBlockType> getById(String id) {
        return Optional.ofNullable(this.disguisedBlockTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DisguisedBlockType> getAll() {
        return ImmutableList.copyOf(this.disguisedBlockTypeMappings.values());
    }

}
