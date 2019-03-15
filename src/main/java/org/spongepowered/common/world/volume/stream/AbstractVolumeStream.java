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
package org.spongepowered.common.world.volume.stream;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.world.volume.MutableVolume;
import org.spongepowered.api.world.volume.UnmodifiableVolume;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.worker.VolumeResult;
import org.spongepowered.api.world.volume.worker.VolumeStream;
import org.spongepowered.api.world.volume.worker.function.VolumeConsumer;
import org.spongepowered.api.world.volume.worker.function.VolumeMapper;
import org.spongepowered.api.world.volume.worker.function.VolumeMerger;
import org.spongepowered.api.world.volume.worker.function.VolumePredicate;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public abstract class AbstractVolumeStream<V extends Volume, U extends UnmodifiableVolume, I, M extends MutableVolume> implements VolumeStream<V, U, I, M> {

    private final WeakReference<V> targetVolume;
    @Nullable private List<VolumePredicate<V, U, I, M>> predicates;
    @Nullable private VolumeMapper<I, U, ?> mapper;

    public AbstractVolumeStream(V target) {
        this.targetVolume = new WeakReference<>(target);
    }

    private V getValidVolume() {
        final V volume = this.targetVolume.get();
        return checkNotNull(volume, "Volume has lost it's reference!");
    }


    @Override
    public final V getVolume() {
        return getValidVolume();
    }

    @Override
    public VolumeStream<V, U, I, M> filter(VolumePredicate<V, U, I, M> predicate) {
        return null;
    }

    @Override
    public <T> VolumeStream<V, U, T, M> map(VolumeMapper<I, U, T> mapper) {
        return null;
    }

    @Override
    public boolean allMatch(VolumePredicate<? super V, ? super U, ? super I, ? super M> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<VolumeResult<? super V, ? super I>> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(VolumePredicate<? super V, ? super U, ? super I, ? super M> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(Predicate<VolumeResult<? super V, ? super I>> predicate) {
        return false;
    }

    @Override
    public boolean anyMatch(VolumePredicate<? super V, ? super U, ? super I, ? super M> predicate) {
        return false;
    }

    @Override
    public boolean anyMatch(Predicate<VolumeResult<? super V, ? super I>> predicate) {
        return false;
    }

    @Override
    public Optional<VolumeResult<V, I>> findFirst() {
        return Optional.empty();
    }

    @Override
    public Optional<VolumeResult<V, I>> findAny() {
        return Optional.empty();
    }

    @Override
    public Stream<VolumeResult<V, I>> toStream() {
        return null;
    }

    @Override
    public void merge(V second, VolumeMerger<I, U> merger, M destination) {

    }

    @Override
    public void forEach(VolumeConsumer<V, I> visitor) {

    }

    @Override
    public void forEach(Consumer<VolumeResult<V, I>> consumer) {

    }
}
