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
package org.spongepowered.common.mixin.api.mcp.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Optional;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
@Implements(@Interface(iface = BlockType.class, prefix = "block$"))
public abstract class BlockMixin_API implements BlockType {

    @Shadow @Final protected BlockStateContainer blockState;
    @Shadow protected boolean needsRandomTick;
    @Shadow protected SoundType blockSoundType;

    @Shadow public abstract String getTranslationKey();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract boolean shadow$getTickRandomly();
    @Shadow public abstract BlockStateContainer getBlockState();

    @Override
    public final String getId() {
        return this.getNameFromRegistry();
    }

    @Override
    public String getName() {
        return this.getNameFromRegistry();
    }

    private String getNameFromRegistry() {
        // This should always succeed when things are working properly,
        // so we just catch the exception instead of doing a null check.
        try {
            return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
        } catch (NullPointerException e) {
            throw new RuntimeException(String.format("Block '%s' (class '%s') is not registered with the block registry! This is likely a bug in the corresponding mod.", this, this.getClass().getName()), e);
        }
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) shadow$getDefaultState();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<BlockState> getAllBlockStates() {
        return (Collection<BlockState>) (Collection<?>) this.blockState.getValidStates();
    }

    @Override
    public Optional<ItemType> getItem() {
        if (this == BlockTypes.AIR) {
            return Optional.of(ItemTypes.AIR);
        }
        ItemType itemType = (ItemType) Item.getItemFromBlock((Block) (Object) this);
        return Items.AIR.equals(itemType) ? Optional.empty() : Optional.of(itemType);
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getTranslationKey() + ".name");
    }

    @Intrinsic
    public boolean block$getTickRandomly() {
        return shadow$getTickRandomly();
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }


    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return getDefaultState().getTraits();
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        return getDefaultState().getTrait(blockTrait);
    }

    @Override
    public BlockSoundGroup getSoundGroup() {
        return (BlockSoundGroup) this.blockSoundType;
    }

}
