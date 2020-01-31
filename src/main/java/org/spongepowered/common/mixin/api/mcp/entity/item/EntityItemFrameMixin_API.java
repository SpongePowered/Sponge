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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RotationalData;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.api.mcp.entity.EntityHangingMixin_API;

import java.util.Collection;

@Mixin(EntityItemFrame.class)
public abstract class EntityItemFrameMixin_API extends EntityHangingMixin_API implements ItemFrame {

    @Override
    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        this.get(RepresentedItemData.class).ifPresent(manipulators::add);
        this.get(RotationalData.class).ifPresent(manipulators::add);
    }
}
