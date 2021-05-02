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
package org.spongepowered.common.mixin.inventory.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.inventory.LensGeneratorBridge;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

@Mixin(value = {
        BaseContainerBlockEntity.class,
        CompoundContainer.class,
        ResultContainer.class,
        CraftingContainer.class,
        SimpleContainer.class
})
public abstract class TraitMixin_Lens_Inventory implements Container, LensGeneratorBridge {

    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new LensRegistrar.BasicSlotLensProvider(this.getContainerSize());
    }

    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {
        return LensRegistrar.getLens(this, slotLensProvider, this.getContainerSize());
    }

}
