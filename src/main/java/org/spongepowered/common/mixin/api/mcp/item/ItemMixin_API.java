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
package org.spongepowered.common.mixin.api.mcp.item;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Item.class)
public abstract class ItemMixin_API implements ItemType {

    @Shadow public abstract int getItemStackLimit();
    @Shadow public abstract String getTranslationKey();
    @Shadow private Item containerItem;

    @Nullable protected BlockType blockType = null;
    @Nullable private org.spongepowered.api.item.inventory.ItemStack propertyItemStack;

    @Override
    public final String getId() {
        final ResourceLocation resourceLocation = SpongeImplHooks.getItemResourceLocation((Item) (Object) this);
        checkState(resourceLocation != null, "Attempted to access the id before the Item is registered.");
        return resourceLocation.toString();
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getDefaultProperty(Class<T> propertyClass) {
        if (this.propertyItemStack == null) {
            this.propertyItemStack = ItemStackUtil.fromNative(new ItemStack((Item) (Object) this));
        }
        return SpongeImpl.getPropertyRegistry().getStore(propertyClass).flatMap(store -> store.getFor(this.propertyItemStack));
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getTranslationKey() + ".name");
    }

    @Override
    public int getMaxStackQuantity() {
        return getItemStackLimit();
    }

    @Override
    public Optional<BlockType> getBlock() {
        return Optional.ofNullable(this.blockType);
    }

    @Override
    public Optional<ItemType> getContainer() {
        return Optional.ofNullable((ItemType) this.containerItem);
    }

}
