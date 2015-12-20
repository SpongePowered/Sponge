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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.BlockParticle;
import org.spongepowered.api.effect.particle.ColoredParticle;
import org.spongepowered.api.effect.particle.ItemParticle;
import org.spongepowered.api.effect.particle.NoteParticle;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ResizableParticle;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;


public class SpongeParticleEffectBuilder extends AbstractParticleEffectBuilder<ParticleEffect, ParticleEffect.Builder> implements ParticleEffect.Builder {



    @Override
    public SpongeParticleEffect build() throws IllegalStateException {
        return new SpongeParticleEffect(this.type, this.motion, this.offset, this.count);
    }

    public static class BuilderColorable extends AbstractParticleEffectBuilder<ColoredParticle, ColoredParticle.Builder> implements ColoredParticle.Builder {

        private Color color;

        @Override
        public BuilderColorable color(Color color) {
            checkNotNull(color, "The color cannot be null!");
            this.color = color;
            return this;
        }


        @Override
        public SpongeParticleEffect.Colored build() {
            return new SpongeParticleEffect.Colored(this.type, this.motion, this.offset, this.color, this.count);
        }

        @Override
        public BuilderColorable reset() {
            super.reset();
            this.color = null;
            return (BuilderColorable) super.reset();
        }

    }

    public static class BuilderResizable extends AbstractParticleEffectBuilder<ResizableParticle, ResizableParticle.Builder> implements ResizableParticle.Builder {

        private float size;


        @Override
        public BuilderResizable size(float size) {
            checkArgument(size >= 0f, "The size has to be greater or equal to zero!");
            this.size = size;
            return this;
        }

        @Override
        public SpongeParticleEffect.Resized build() {
            return new SpongeParticleEffect.Resized(this.type, this.motion, this.offset, this.size, this.count);
        }

        @Override
        public BuilderResizable reset() {
            this.size = 0;
            return (BuilderResizable) super.reset();
        }

    }

    public static class BuilderNote extends AbstractParticleEffectBuilder<NoteParticle, NoteParticle.Builder> implements NoteParticle.Builder {

        private NotePitch note;

        @Override
        public BuilderNote note(NotePitch note) {
            checkNotNull(note, "The note has to scale between 0 and 24!");
            this.note = note;
            return this;
        }

        @Override
        public BuilderNote type(ParticleType particleType) {
            checkArgument(particleType instanceof ParticleType.Note);
            return (BuilderNote) super.type(particleType);
        }


        @Override
        public NoteParticle.Builder from(NoteParticle value) {
            return super.from(value);
        }

        @Override
        public SpongeParticleEffect.Note build() {
            return new SpongeParticleEffect.Note(this.type, this.motion, this.offset, this.note, this.count);
        }


        @Override
        public BuilderNote reset() {
            this.note = null;
            return (BuilderNote) super.reset();
        }

    }

    public static class BuilderMaterial extends AbstractParticleEffectBuilder<ItemParticle, ItemParticle.Builder> implements ItemParticle.Builder {

        private ItemStackSnapshot item;

        @Override
        public BuilderMaterial item(ItemStackSnapshot item) {
            checkNotNull(item, "The item type cannot be null!");
            this.item = item;
            return this;
        }

        @Override
        public BuilderMaterial type(ParticleType particleType) {
            checkArgument(particleType instanceof ParticleType.Material);
            return (BuilderMaterial) super.type(particleType);
        }


        @Override
        public SpongeParticleEffect.Materialized build() {
            return new SpongeParticleEffect.Materialized(this.type, this.motion, this.offset, this.item, this.count);
        }

        @Override
        public BuilderMaterial reset() {
            this.item = null;
            return (BuilderMaterial) super.reset();
        }

    }

    public static class BuilderBlock extends AbstractParticleEffectBuilder<BlockParticle, BlockParticle.Builder> implements BlockParticle.Builder {

        private BlockState blockState;

        @Override
        public BuilderBlock block(BlockState blockState) {
            this.blockState = checkNotNull(blockState);
            return this;
        }

        @Override
        public BuilderBlock type(ParticleType particleType) {
            checkArgument(particleType instanceof ParticleType.Material);
            return (BuilderBlock) super.type(particleType);
        }

        @Override
        public SpongeParticleEffect.Block build() {
            checkState(this.blockState != null, "BlockState cannot be null!");
            return new SpongeParticleEffect.Block(this.type, this.motion, this.offset, this.count, this.blockState);
        }

        @Override
        public BuilderBlock reset() {
            super.reset();
            this.blockState = null;
            return this;
        }
    }

}
