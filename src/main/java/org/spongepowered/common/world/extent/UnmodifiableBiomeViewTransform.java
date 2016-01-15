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
package org.spongepowered.common.world.extent;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.util.DiscreteTransform2;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;
import org.spongepowered.api.world.extent.worker.BiomeAreaWorker;
import org.spongepowered.common.util.gen.ByteArrayImmutableBiomeBuffer;
import org.spongepowered.common.world.extent.worker.SpongeBiomeAreaWorker;

public class UnmodifiableBiomeViewTransform extends AbstractBiomeViewTransform<UnmodifiableBiomeArea> implements UnmodifiableBiomeArea {

    public UnmodifiableBiomeViewTransform(UnmodifiableBiomeArea area, DiscreteTransform2 transform) {
        super(area, transform);
    }

    @Override
    public UnmodifiableBiomeArea getBiomeView(Vector2i newMin, Vector2i newMax) {
        return new UnmodifiableBiomeViewDownsize(this.area, this.inverseTransform.transform(newMin), this.inverseTransform.transform(newMax))
            .getBiomeView(this.transform);
    }

    @Override
    public UnmodifiableBiomeArea getBiomeView(DiscreteTransform2 transform) {
        return new UnmodifiableBiomeViewTransform(this.area, this.transform.withTransformation(transform));
    }

    @Override
    public ImmutableBiomeArea getImmutableBiomeCopy() {
        return ByteArrayImmutableBiomeBuffer.newWithoutArrayClone(ExtentBufferUtil.copyToArray(this, this.min, this.max, this.size), this.min,
            this.size);
    }

    @Override
    public BiomeAreaWorker<? extends UnmodifiableBiomeArea> getBiomeWorker() {
        return new SpongeBiomeAreaWorker<>(this);
    }

}
