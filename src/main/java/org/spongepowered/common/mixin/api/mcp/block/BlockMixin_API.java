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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

@Mixin(value = Block.class, priority = 999)
public abstract class BlockMixin_API implements BlockType {

    @Shadow @Final @org.spongepowered.asm.mixin.Mutable protected boolean ticksRandomly;
    @Shadow @Final protected SoundType soundType;
    @Shadow public abstract Item shadow$asItem();
    @Shadow public abstract String shadow$getTranslationKey();
    @Shadow public abstract net.minecraft.block.BlockState shadow$getDefaultState();

    @Nullable private CatalogKey api$key;

    @Override
    public CatalogKey getKey() {
        if (this.api$key == null) {
            this.api$key = (CatalogKey) (Object) Registry.BLOCK.getKey((Block) (Object) this);
        }

        return this.api$key;
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) this.shadow$getDefaultState();
    }

    @Override
    public Optional<ItemType> getItem() {
        final Item item = this.shadow$asItem();
        if (item == Items.AIR) {
            return Optional.empty();
        }
        return Optional.ofNullable((ItemType) item);
    }

    @Override
    public boolean doesUpdateRandomly() {
        return this.ticksRandomly;
    }

    @Override
    public void setUpdateRandomly(boolean updateRandomly) {
        this.ticksRandomly = updateRandomly;
    }

    @Override
    public BlockSoundGroup getSoundGroup() {
        return (BlockSoundGroup) (Object) this.soundType;
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(this.shadow$getTranslationKey());
    }
}
