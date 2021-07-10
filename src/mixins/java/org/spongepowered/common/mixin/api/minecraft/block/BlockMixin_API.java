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
package org.spongepowered.common.mixin.api.minecraft.block;

import net.kyori.adventure.text.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.TagUtil;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(value = Block.class, priority = 999)
public abstract class BlockMixin_API extends AbstractBlockMixin_API implements SpongeImmutableDataHolder<BlockType> {

    // @formatter:off
    @Shadow @Final protected StateDefinition<Block, net.minecraft.world.level.block.state.BlockState> stateDefinition;
    @Shadow public abstract String shadow$getDescriptionId();
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$defaultBlockState();
    // @formatter:on

    @Override
    public BlockState defaultState() {
        return (BlockState) this.shadow$defaultBlockState();
    }

    @Override
    public Optional<ItemType> item() {
        final Item item = this.shadow$asItem();
        if (item == Items.AIR) {
            return Optional.empty();
        }
        return Optional.ofNullable((ItemType) item);
    }

    @Override
    public Component asComponent() {
        return Component.translatable(this.shadow$getDescriptionId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<StateProperty<?>> stateProperties() {
        return (Collection<StateProperty<?>>) (Object) this.stateDefinition.getProperties();
    }

    @Override
    public Optional<StateProperty<?>> findStateProperty(final String name) {
        return Optional.ofNullable((StateProperty<?>) this.stateDefinition.getProperty(name));
    }

    @Override
    public boolean isAnyOf(final Supplier<? extends BlockType>... types) {
        return Arrays.stream(types).map(Supplier::get).anyMatch(type -> type == this);
    }

    @Override
    public boolean isAnyOf(final BlockType... types) {
        return Arrays.stream(types).anyMatch(type -> type == this);
    }

    @Override
    public TagType<BlockType> tagType() {
        return TagTypes.BLOCK_TYPE.get();
    }

    @Override
    public Collection<Tag<BlockType>> tags() {
        return TagUtil.getAssociatedTags(this, RegistryTypes.BLOCK_TYPE_TAGS);
    }
}
