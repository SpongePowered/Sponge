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

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.holder.SimpleNbtDataHolder;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.entity.player.SpongeUser;

import java.util.List;

@Mixin({TileEntity.class, Entity.class, SpongeUser.class, ItemStack.class,
        SpongeEntityArchetype.class,
        SpongeEntitySnapshot.class,
        SimpleNbtDataHolder.class})
public abstract class CustomDataHolderMixin implements CustomDataHolderBridge {

    private DataManipulator.Mutable impl$manipulator;
    private List<DataView> impl$failedData = Lists.newArrayList();

    @Override
    public DataManipulator.Mutable bridge$getManipulator() {
        if (this.impl$manipulator == null) {
            this.impl$manipulator = DataManipulator.mutableOf();
            CustomDataHolderBridge.syncTagToCustom(this);
        }
        return this.impl$manipulator;
    }

    @Override
    public void bridge$mergeDeserialized(DataManipulator.Mutable manipulator) {
        if (this.impl$manipulator == null) {
            this.impl$manipulator = DataManipulator.mutableOf();
        }
        this.impl$manipulator.copyFrom(manipulator);
    }

    @Override
    public void bridge$clearCustomData() {
        this.impl$manipulator = null;
        this.impl$failedData = Lists.newArrayList();
    }

    @Override
    public List<DataView> bridge$getFailedData() {
        return this.impl$failedData;
    }

}
