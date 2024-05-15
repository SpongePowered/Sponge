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
package org.spongepowered.common.mixin.api.minecraft.world.item;

import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemRarity;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(Item.class)
public abstract class ItemMixin_API implements ItemType, SpongeImmutableDataHolder<ItemType> {

    // @formatter:off
    @Shadow @Final private Holder.Reference<Item> builtInRegistryHolder;
    @Shadow @Final private DataComponentMap components;

    @Shadow public abstract String shadow$getDescriptionId();
    @Shadow @Nullable public abstract Item shadow$getCraftingRemainingItem();

    @Shadow public abstract int shadow$getDefaultMaxStackSize();
    // @formatter:on


    @Nullable protected BlockType api$blockType = null;

    @Override
    public Component asComponent() {
        return Component.translatable(this.shadow$getDescriptionId());
    }

    @Override
    public int maxStackQuantity() {
        return this.shadow$getDefaultMaxStackSize();
    }

    @Override
    public ItemRarity rarity() {
        return (ItemRarity) (Object) this.components.get(DataComponents.RARITY);
    }

    @Override
    public Optional<BlockType> block() {
        return Optional.ofNullable(this.api$blockType);
    }

    @Override
    public boolean isAnyOf(Supplier<? extends ItemType>... types) {
        return Arrays.stream(types).map(Supplier::get).anyMatch(type -> type == this);
    }

    @Override
    public boolean isAnyOf(ItemType... types) {
        return Arrays.stream(types).anyMatch(type -> type == this);
    }

    @Override
    public DefaultedRegistryType<ItemType> registryType() {
        return RegistryTypes.ITEM_TYPE;
    }

    @Override
    public Collection<Tag<ItemType>> tags() {
        return this.registryType().get().tags().filter(this::is).collect(Collectors.toSet());
    }

    @Override
    public boolean is(Tag<ItemType> tag) {
        return this.builtInRegistryHolder.is((TagKey<Item>) (Object) tag);
    }

    @Override
    public Optional<ItemType> container() {
        final Item craftingRemainingItem = this.shadow$getCraftingRemainingItem();
        return Optional.ofNullable((ItemType) craftingRemainingItem);
    }
}
