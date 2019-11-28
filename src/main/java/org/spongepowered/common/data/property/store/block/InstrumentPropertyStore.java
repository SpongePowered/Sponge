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
package org.spongepowered.common.data.property.store.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.spongepowered.api.data.property.block.InstrumentProperty;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.property.store.common.AbstractBlockPropertyStore;

import java.util.Optional;

import javax.annotation.Nullable;

public class InstrumentPropertyStore extends AbstractBlockPropertyStore<InstrumentProperty> {

    public InstrumentPropertyStore() {
        super(true);
    }

    @Override
    protected Optional<InstrumentProperty> getForBlock(@Nullable Location<?> location, IBlockState block) {
        return Optional.of(new InstrumentProperty(getInstrumentType(block)));
    }

    @SuppressWarnings("deprecation")
    private InstrumentType getInstrumentType(IBlockState block) {
        final Block blockType = block.func_177230_c();
        if (blockType == Blocks.field_150435_aG) {
            return InstrumentTypes.FLUTE;
        } else if (blockType == Blocks.field_150340_R) {
            return InstrumentTypes.BELL;
        } else if (blockType == Blocks.field_150325_L) {
            return InstrumentTypes.GUITAR;
        } else if (blockType == Blocks.field_150403_cj) {
            return InstrumentTypes.CHIME;
        } else if (blockType == Blocks.field_189880_di) {
            return InstrumentTypes.XYLOPHONE;
        }
        final Material material = block.func_177230_c().func_149688_o(block);
        if (material == Material.field_151576_e) {
            return InstrumentTypes.BASS_DRUM;
        } else if (material == Material.field_151595_p) {
            return InstrumentTypes.SNARE;
        } else if (material == Material.field_151592_s) {
            return InstrumentTypes.HIGH_HAT;
        } else if (material == Material.field_151575_d) {
            return InstrumentTypes.BASS_ATTACK;
        }
        return InstrumentTypes.HARP;
    }
}
