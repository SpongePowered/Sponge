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

import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.registry.SpongeGameDictionaryEntry;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Optional;

@Mixin(Item.class)
public abstract class MixinItem implements ItemType, IMixinItem, SpongeGameDictionaryEntry {

    public Optional<BlockType> blockType = Optional.empty();

    @Shadow private String unlocalizedName;

    @Shadow public abstract int getItemStackLimit();
    @Shadow public abstract String getUnlocalizedName();

    @Override
    public String getId() {
        return Item.REGISTRY.getNameForObject((Item) (Object) this).toString();
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getDefaultProperty(Class<T> propertyClass) {
        return Optional.empty(); // TODO
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getUnlocalizedName() + ".name");
    }

    @Override
    public int getMaxStackQuantity() {
        return getItemStackLimit();
    }

    @Override
    public Optional<BlockType> getBlock() {
        return this.blockType;
    }

    @Override
    public void getManipulatorsFor(ItemStack itemStack, List<DataManipulator<?, ?>> list) {
        if (!itemStack.hasTagCompound()) {
            return;
        }

        org.spongepowered.api.item.inventory.ItemStack spongeStack = ((org.spongepowered.api.item.inventory.ItemStack) itemStack);
        if (itemStack.isItemEnchanted()) {
            list.add(getData(itemStack, EnchantmentData.class));
        }
        spongeStack.get(DisplayNameData.class).ifPresent(list::add);
        spongeStack.get(LoreData.class).ifPresent(list::add);
    }

    protected final <T extends DataManipulator<T, ?>> T getData(ItemStack itemStack, Class<T> manipulatorClass) {
        return ((org.spongepowered.api.item.inventory.ItemStack) itemStack).get(manipulatorClass).get();
    }

    @Override
    public ItemStack createDictionaryStack(int wildcardValue) {
        return new ItemStack((Item) (Object) this, 1, wildcardValue);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.unlocalizedName)
                .toString();
    }
}
