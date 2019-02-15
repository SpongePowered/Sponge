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
package org.spongepowered.common.mixin.core.item;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.EnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.LoreData;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

@Mixin(Item.class)
public abstract class MixinItem implements ItemType, IMixinItem {

    public Optional<BlockType> blockType = Optional.empty();

    @Shadow private String translationKey;

    @Shadow public abstract String getTranslationKey();

    @Shadow public abstract int getMaxStackSize();

    // A item stack used to retrieve properties
    @Nullable private org.spongepowered.api.item.inventory.ItemStack propertyItemStack;

    @Override
    public String getName() {
        return getKey().getValue();
    }

    @Override
    public CatalogKey getKey() {
        final ResourceLocation resourceLocation = IRegistry.ITEM.getKey((Item) (Object) this);
        checkState(resourceLocation != null, "Attempted to access the id before the Item is registered.");
        return (CatalogKey) (Object) resourceLocation;
    }

    private org.spongepowered.api.item.inventory.ItemStack getPropertyItemStack() {
        if (this.propertyItemStack == null) {
            this.propertyItemStack = ItemStackUtil.fromNative(new ItemStack((Item) (Object) this));
        }
        return this.propertyItemStack;
    }

    @Override
    public <V> Optional<V> getProperty(Property<V> property) {
        return getPropertyItemStack().getProperty(property);
    }

    @Override
    public OptionalInt getIntProperty(Property<Integer> property) {
        return getPropertyItemStack().getIntProperty(property);
    }

    @Override
    public OptionalDouble getDoubleProperty(Property<Double> property) {
        return getPropertyItemStack().getDoubleProperty(property);
    }

    @Override
    public Map<Property<?>, ?> getProperties() {
        return getPropertyItemStack().getProperties();
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getTranslationKey() + ".name");
    }

    @Override
    public int getMaxStackQuantity() {
        return getMaxStackSize();
    }

    @Override
    public Optional<BlockType> getBlock() {
        return this.blockType;
    }

    @Override
    public void getManipulatorsFor(ItemStack itemStack, List<DataManipulator<?, ?>> list) {
        if (!itemStack.hasTag()) {
            return;
        }

        org.spongepowered.api.item.inventory.ItemStack spongeStack = (org.spongepowered.api.item.inventory.ItemStack) (Object) itemStack;
        if (itemStack.isEnchanted()) {
            list.add(getData(itemStack, EnchantmentData.class));
        }
        spongeStack.get(DisplayNameData.class).ifPresent(list::add);
        spongeStack.get(LoreData.class).ifPresent(list::add);
    }

    protected final <T extends DataManipulator<T, ?>> T getData(ItemStack itemStack, Class<T> manipulatorClass) {
        return ((org.spongepowered.api.item.inventory.ItemStack) (Object) itemStack).get(manipulatorClass).get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.translationKey)
                .toString();
    }
}
