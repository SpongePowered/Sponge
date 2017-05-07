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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntityHanging;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityItemFrame.class)
public abstract class MixinEntityItemFrame extends MixinEntityHanging implements ItemFrame {

    @Shadow @Nullable public abstract net.minecraft.item.ItemStack getDisplayedItem();
    @Shadow public abstract void setDisplayedItem(@Nullable net.minecraft.item.ItemStack p_82334_1_);

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getRotation();

    @Shadow
    public abstract void setItemRotation(int p_82336_1_);

    public Optional<ItemStack> getItem() {
        return Optional.ofNullable(ItemStackUtil.fromNative(getDisplayedItem()));
    }

    public void setItem(@Nullable ItemStack item) {
        setDisplayedItem(ItemStackUtil.toNative(item));
    }

    public Rotation getItemRotation() {
        return SpongeImpl.getGame().getRegistry().getRotationFromDegree(shadow$getRotation() * 45).get();
    }

    public void setRotation(Rotation itemRotation) {
        setItemRotation(itemRotation.getAngle() / 45);
    }
}
