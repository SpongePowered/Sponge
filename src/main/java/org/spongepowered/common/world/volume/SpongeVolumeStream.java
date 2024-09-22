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
package org.spongepowered.common.world.volume;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.world.volume.MutableVolume;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.stream.VolumeCollector;
import org.spongepowered.api.world.volume.stream.VolumeConsumer;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeFlatMapper;
import org.spongepowered.api.world.volume.stream.VolumeMapper;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslator;
import org.spongepowered.api.world.volume.stream.VolumePredicate;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SpongeVolumeStream<V extends Volume, T> implements VolumeStream<V, T> {

    private final Supplier<? extends V> volumeSupplier;
    private final Stream<VolumeElement<V, T>> stream;

    public SpongeVolumeStream(final Stream<VolumeElement<V, T>> elementStream,
        final Supplier<? extends V> volumeSupplier
    ) {
        this.stream = elementStream;
        this.volumeSupplier = volumeSupplier;
    }

    @Override
    public V volume() {
        return this.volumeSupplier.get();
    }

    @Override
    public VolumeStream<V, T> filter(final VolumePredicate<V, T> predicate) {
        return new SpongeVolumeStream<>(this.stream
            .filter(element ->
                predicate.test(
                    this.volumeSupplier.get(),
                    element::type,
                    element.position().x(),
                    element.position().y(),
                    element.position().z()
                )
            ), this.volumeSupplier);
    }

    @Override
    public VolumeStream<V, T> filter(final Predicate<VolumeElement<V, ? super T>> predicate) {
        return new SpongeVolumeStream<>(this.stream.filter(predicate), this.volumeSupplier);
    }

    @Override
    public <Out> VolumeStream<V, Out> map(final VolumeMapper<V, T, Out> mapper) {
        return new SpongeVolumeStream<>(this.stream.map(element ->
            VolumeElement.of(this.volumeSupplier.get(), mapper.map(
                this.volumeSupplier.get(),
                element::type,
                element.position().x(),
                element.position().y(),
                element.position().z()
            ), element.position())
        ), this.volumeSupplier);
    }

    @Override
    public VolumeStream<V, Optional<? extends T>> flatMap(final VolumeFlatMapper<V, T> mapper) {
        return new SpongeVolumeStream<>(
            this.stream.map(element ->
                VolumeElement.of(
                    this.volumeSupplier.get(),
                    mapper.map(
                        this.volumeSupplier.get(),
                        element::type,
                        element.position().x(),
                        element.position().y(),
                        element.position().z()
                    ),
                    element.position()
                )
            ),
            this.volumeSupplier
        );
    }

    @Override
    public VolumeStream<V, T> transform(final VolumePositionTranslator<V, T> transformer) {
        return new SpongeVolumeStream<>(
            this.stream.map(transformer::apply),
            this.volumeSupplier
        );
    }

    @Override
    public <Out> VolumeStream<V, Out> map(final Function<VolumeElement<V, T>, ? extends Out> mapper) {
        return new SpongeVolumeStream<>(this.stream.map(element -> VolumeElement.of(
            this.volume(),
            mapper.apply(element),
            element.position()
        )), this.volumeSupplier);
    }

    @Override
    public long count() {
        return this.stream.count();
    }

    @Override
    public boolean allMatch(final VolumePredicate<V, ? super T> predicate) {
        return this.stream
            .allMatch(element -> predicate.test(
                this.volume(),
                element::type,
                element.position().x(),
                element.position().y(),
                element.position().z()
            ));
    }

    @Override
    public boolean allMatch(final Predicate<VolumeElement<V, ? super T>> predicate) {
        return this.stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(final VolumePredicate<V, ? super T> predicate) {
        return this.stream.noneMatch(element -> predicate.test(
            this.volume(),
            element::type,
            element.position().x(),
            element.position().y(),
            element.position().z()
        ));
    }

    @Override
    public boolean noneMatch(final Predicate<VolumeElement<V, ? super T>> predicate) {
        return this.stream.noneMatch(predicate);
    }

    @Override
    public boolean anyMatch(final VolumePredicate<V, ? super T> predicate) {
        return this.stream.anyMatch(element -> predicate.test(
            this.volume(),
            element::type,
            element.position().x(),
            element.position().y(),
            element.position().z()
        ));
    }

    @Override
    public boolean anyMatch(final Predicate<VolumeElement<V, ? super T>> predicate) {
        return this.stream.anyMatch(predicate);
    }

    @Override
    public Optional<VolumeElement<V, T>> findFirst() {
        return this.stream.findFirst();
    }

    @Override
    public Optional<VolumeElement<V, T>> findAny() {
        return this.stream.findAny();
    }

    @Override
    public Stream<VolumeElement<V, T>> toStream() {
        return this.stream;
    }

    @Override
    public <W extends MutableVolume> void apply(final VolumeCollector<W, T, ?> collector) {
        this.startPhase(() -> {
            this.stream.forEach(element -> {
                final W targetVolume = collector.target().get();
                final VolumeElement<W, T> transformed = collector.positionTransform().apply(VolumeElement.of(
                    collector.target(),
                    element::type,
                    element.position()
                ));
                collector.applicator()
                    .apply(targetVolume, transformed);
            });
        });
    }

    @Override
    public <W extends MutableVolume, R> void applyUntil(final VolumeCollector<W, T, R> collector, final Predicate<R> predicate) {
        this.startPhase(() -> {
            boolean doWork = true;
            for (final Iterator<VolumeElement<V, T>> iterator = this.stream.iterator(); doWork && iterator.hasNext(); ) {
                final W targetVolume = collector.target().get();
                final VolumeElement<V, T> element = iterator.next();
                final VolumeElement<W, T> transformed = collector.positionTransform().apply(VolumeElement.of(
                    collector.target(),
                    element::type,
                    element.position()
                ));
                final R apply = collector.applicator()
                    .apply(targetVolume, transformed);
                doWork = predicate.test(apply);
            }
        });
    }

    @Override
    public void forEach(final VolumeConsumer<V, T> visitor) {
        this.startPhase(() -> {
            this.stream.forEach(element -> visitor.consume(
                element.volume(),
                element.type(),
                element.position().x(),
                element.position().y(),
                element.position().z()
            ));
        });
    }

    @Override
    public void forEach(final Consumer<VolumeElement<V, T>> consumer) {
        this.stream.forEach(consumer);
    }

    private void startPhase(final Runnable runnable) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        try (final @Nullable PhaseContext<@NonNull ?> context = instance.getPhaseContext().isApplyingStreams()
            ? null
            : PluginPhase.State.VOLUME_STREAM_APPLICATION
            .createPhaseContext(instance)
            .setVolumeStream(this)
            .spawnType(() -> PhaseTracker.getCauseStackManager().context(EventContextKeys.SPAWN_TYPE).orElse(null))
        ) {
            if (context != null) {
                context.buildAndSwitch();
            }

            runnable.run();
        }
    }
}
