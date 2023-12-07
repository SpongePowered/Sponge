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
package org.spongepowered.common.mixin.core.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.holder.SimpleNBTDataHolder;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.entity.player.SpongeUserData;

@Mixin({BlockEntity.class, Entity.class, SpongeUserData.class, ItemStack.class,
        SpongeEntityArchetype.class,
        SpongeEntitySnapshot.class,
        SpongeBlockSnapshot.class,
        SimpleNBTDataHolder.class,
        MapItemSavedData.class,
        LevelChunk.class})
public abstract class SpongeDataHolderMixin implements SpongeDataHolderBridge {

    private DataManipulator.Mutable impl$manipulator;
    private Multimap<DataQuery, DataView> impl$failedData;
    private boolean deserializing = false;

    @Override
    public DataManipulator.Mutable bridge$getManipulator() {
        if (this.impl$manipulator == null) {
            this.impl$manipulator = DataManipulator.mutableOf();
            DataUtil.syncTagToData(this);
        }
        return this.impl$manipulator;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void bridge$mergeDeserialized(final DataManipulator.Mutable manipulator) {
        if (this.impl$manipulator == null) {
            this.impl$manipulator = DataManipulator.mutableOf();
        }

        try {
            this.deserializing = true;
            if (this instanceof DataHolder.Mutable && !(this instanceof org.spongepowered.api.item.inventory.ItemStack)) {
                // Does not work when adding ItemStacks to inventory because the Item may be empty (see Inventory#addResource)
                for (final Value.Immutable<?> value : manipulator.getValues()) {
                    final DataProvider provider = SpongeDataManager.getProviderRegistry().getProvider(value.key(), this.getClass());
                    provider.offerValue((DataHolder.Mutable) this, value);
                }
            } else {
                this.impl$manipulator.copyFrom(manipulator);
            }
        } finally {
            this.deserializing = false;
        }
    }

    @Override
    public void bridge$clear() {
        this.impl$manipulator = null;
        this.impl$failedData = HashMultimap.create();
    }

    @Override
    public Multimap<DataQuery, DataView> bridge$getFailedData() {
        if (this.impl$failedData == null) {
            return ImmutableMultimap.of();
        }
        return this.impl$failedData;
    }

    @Override
    public void bridge$invalidateFailedData() {
        this.impl$failedData = null;
    }

    @Override
    public void bridge$addFailedData(final DataQuery nameSpace, final DataView keyedData) {
        if (this.impl$failedData == null) {
            this.impl$failedData  = HashMultimap.create();
        }
        this.impl$failedData.put(nameSpace, keyedData);
    }

    @Override
    public boolean brigde$isDeserializing() {
        return this.deserializing;
    }
}
