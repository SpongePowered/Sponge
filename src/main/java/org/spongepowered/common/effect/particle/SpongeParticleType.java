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
package org.spongepowered.common.effect.particle;

import com.google.common.base.Objects.ToStringHelper;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.SpongeCatalogType;

public class SpongeParticleType extends SpongeCatalogType implements ParticleType {

    private EnumParticleTypes type;
    private boolean motion;

    public SpongeParticleType(EnumParticleTypes type, String id, boolean motion) {
        super(id);
        this.motion = motion;
        this.type = type;
    }

    public EnumParticleTypes getInternalType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.type.getParticleName();
    }

    @Override
    public boolean hasMotion() {
        return this.motion;
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("type", this.type);
    }

    public static class Colorable extends SpongeParticleType implements ParticleType.Colorable {

        private Color color;

        public Colorable(EnumParticleTypes type, String id, Color color) {
            super(type, id, false);
            this.color = color;
        }

        @Override
        public Color getDefaultColor() {
            return this.color;
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("color", this.color);
        }

    }

    public static class Resizable extends SpongeParticleType implements ParticleType.Resizable {

        private float size;

        public Resizable(EnumParticleTypes type, String id, float size) {
            super(type, id, false);
            this.size = size;
        }

        @Override
        public float getDefaultSize() {
            return this.size;
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("size", this.size);
        }

    }

    public static class Note extends SpongeParticleType implements ParticleType.Note {

        private NotePitch note;

        public Note(EnumParticleTypes type, String id, NotePitch note) {
            super(type, id, false);
            this.note = note;
        }

        @Override
        public NotePitch getDefaultNote() {
            return this.note;
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("note", this.note);
        }

    }

    public static class Item extends SpongeParticleType implements ParticleType.Item {

        private ItemStack item;

        public Item(EnumParticleTypes type, String id, net.minecraft.item.ItemStack item, boolean motion) {
            super(type, id, motion);
            this.item = (ItemStack) item;
        }

        @Override
        public ItemStack getDefaultItem() {
            return this.item.copy();
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("item", this.item);
        }

    }

    public static class Block extends SpongeParticleType implements ParticleType.Block {

        private BlockState block;

        public Block(EnumParticleTypes type, String id, BlockState block, boolean motion) {
            super(type, id, motion);
            this.block = block;
        }

        @Override
        public BlockState getDefaultBlockState() {
            return this.block;
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("block", this.block);
        }

    }

}
