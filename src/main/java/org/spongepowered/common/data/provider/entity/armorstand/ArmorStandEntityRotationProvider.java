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
package org.spongepowered.common.data.provider.entity.armorstand;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.Rotations;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ArmorStandEntityRotationProvider<G, S> extends GenericMutableDataProvider<ArmorStandEntity, Vector3d> {

    private final Function<G, Rotations> getter;
    private final BiConsumer<S, Rotations> setter;

    public ArmorStandEntityRotationProvider(Supplier<? extends Key<? extends Value<Vector3d>>> key,
            Function<G, Rotations> getter, BiConsumer<S, Rotations> setter) {
        super(key);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected Optional<Vector3d> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(VecHelper.toVector3d(this.getter.apply((G) dataHolder)));
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Vector3d value) {
        this.setter.accept((S) dataHolder, VecHelper.toRotation(value));
        return true;
    }
}
