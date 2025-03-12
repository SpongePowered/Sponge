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
package org.spongepowered.common.event.lifecycle;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterTagEvent;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.tag.SpongePluginTag;
import org.spongepowered.common.tag.SpongePluginTagModifier;
import org.spongepowered.common.tag.SpongePluginTagPredicate;
import org.spongepowered.common.tag.SpongePluginTags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class RegisterTagEventImpl extends AbstractLifecycleEvent implements RegisterTagEvent {

    private final Map<Tag<?>, SpongePluginTagModifier<?>> tags = new HashMap<>();

    public RegisterTagEventImpl(final Cause cause, final Game game) {
        super(cause, game);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TagStep<T> tag(final Tag<T> tag) {
        final SpongePluginTagModifier<?> modifier = this.tags.computeIfAbsent(tag, $ -> new SpongePluginTagModifier<>());
        return new TagStepImpl<>((SpongePluginTagModifier<T>) modifier, null);
    }

    public SpongePluginTags tags() {
        return new SpongePluginTags(this.tags.entrySet().stream().collect(
            Collectors.groupingBy(e -> e.getKey().registry(), Collectors.toMap(e -> e.getKey().key(), Map.Entry::getValue))));
    }

    private record TagStepImpl<T>(SpongePluginTagModifier<T> pluginTagModifier, @Nullable SpongePluginTagPredicate<T> predicate) implements TagStep<T> {

        @Override
        public TagStep<T> filter(final Predicate<DefaultedRegistryReference<T>> predicate) {
            this.pluginTagModifier.filter(SpongePluginTagPredicate.key(predicate));
            return this;
        }

        @Override
        public TagStep<T> filterTags(final Predicate<Tag<T>> predicate) {
            this.pluginTagModifier.filter(SpongePluginTagPredicate.tag(predicate));
            return this;
        }

        @Override
        public TagStep<T> filterTags(final BiPredicate<Tag<T>, DefaultedRegistryReference<T>> predicate) {
            this.pluginTagModifier.filter(SpongePluginTagPredicate.tagKey(predicate));
            return this;
        }

        @Override
        public TagStep<T> append(final RegistryKey<T> key) {
            this.pluginTagModifier.append(new SpongePluginTag(key.location(), false), this.predicate);
            return this;
        }

        @Override
        public TagStep<T> append(final Tag<T> tag) {
            this.pluginTagModifier.append(new SpongePluginTag(tag.key(), true), this.predicate);
            return this;
        }

        @Override
        public TagStep<T> test(final Predicate<DefaultedRegistryReference<T>> predicate, final Consumer<TagStep<T>> consumer) {
            consumer.accept(new TagStepImpl<>(this.pluginTagModifier, SpongePluginTagPredicate.key(predicate).and(this.predicate)));
            return this;
        }

        @Override
        public TagStep<T> testTags(final Predicate<Tag<T>> predicate, final Consumer<TagStep<T>> consumer) {
            consumer.accept(new TagStepImpl<>(this.pluginTagModifier, SpongePluginTagPredicate.tag(predicate).and(this.predicate)));
            return this;
        }

        @Override
        public TagStep<T> testTags(final BiPredicate<Tag<T>, DefaultedRegistryReference<T>> predicate, final Consumer<TagStep<T>> consumer) {
            consumer.accept(new TagStepImpl<>(this.pluginTagModifier, SpongePluginTagPredicate.tagKey(predicate).and(this.predicate)));
            return this;
        }
    }
}
