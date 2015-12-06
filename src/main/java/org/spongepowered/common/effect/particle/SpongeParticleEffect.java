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

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ColoredParticle;
import org.spongepowered.api.effect.particle.ItemParticle;
import org.spongepowered.api.effect.particle.NoteParticle;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ResizableParticle;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;


public class SpongeParticleEffect implements ParticleEffect {

    private SpongeParticleType type;

    private Vector3d motion;
    private Vector3d offset;

    private int count;

    public SpongeParticleEffect(SpongeParticleType type, Vector3d motion, Vector3d offset, int count) {
        this.motion = motion;
        this.offset = offset;
        this.count = count;
        this.type = type;
    }

    @Override
    public SpongeParticleType getType() {
        return this.type;
    }

    @Override
    public Vector3d getMotion() {
        return this.motion;
    }

    @Override
    public Vector3d getOffset() {
        return this.offset;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    public static class Colored extends SpongeParticleEffect implements ColoredParticle {

        private Color color;

        public Colored(SpongeParticleType type, Vector3d motion, Vector3d offset, Color color, int count) {
            super(type, motion, offset, count);
            this.color = color;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

    }

    public static class Resized extends SpongeParticleEffect implements ResizableParticle {

        private float size;

        public Resized(SpongeParticleType type, Vector3d motion, Vector3d offset, float size, int count) {
            super(type, motion, offset, count);
            this.size = size;
        }

        @Override
        public float getSize() {
            return this.size;
        }

    }

    public static class Note extends SpongeParticleEffect implements NoteParticle {

        private NotePitch note;

        public Note(SpongeParticleType type, Vector3d motion, Vector3d offset, NotePitch note, int count) {
            super(type, motion, offset, count);
            this.note = note;
        }

        @Override
        public NotePitch getNote() {
            return this.note;
        }

    }

    public static class Materialized extends SpongeParticleEffect implements ItemParticle {

        private ItemStackSnapshot item;

        public Materialized(SpongeParticleType type, Vector3d motion, Vector3d offset, ItemStackSnapshot item, int count) {
            super(type, motion, offset, count);
            this.item = item;
        }

        @Override
        public ItemStackSnapshot getItem() {
            return this.item;
        }

    }

}
