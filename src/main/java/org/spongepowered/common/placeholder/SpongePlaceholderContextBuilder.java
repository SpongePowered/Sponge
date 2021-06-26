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
package org.spongepowered.common.placeholder;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;


public class SpongePlaceholderContextBuilder implements PlaceholderContext.Builder {

    private @Nullable Supplier<Object> associatedObjectSupplier;
    private @Nullable String argument = null;

    @Override
    @SuppressWarnings("unchecked")
    public PlaceholderContext.Builder associatedObject(final @Nullable Object associatedObject) {
        if (associatedObject == null) {
            this.associatedObjectSupplier = null;
        } else if (associatedObject instanceof Supplier) {
            return this.associatedObject((Supplier<Object>) associatedObject);
        } else if (associatedObject instanceof SystemSubject) {
            this.associatedObjectSupplier = Sponge::systemSubject;
        } else if (associatedObject instanceof Server) {
            this.associatedObjectSupplier = Sponge::server;
        } else if (associatedObject instanceof Player) {
            return this.associatedObject((Player) associatedObject);
        } else if (associatedObject instanceof ServerWorld) {
            final ResourceKey key = ((ServerWorld) associatedObject).key();
            this.associatedObjectSupplier = () -> SpongeCommon.game().server().worldManager().world(key).orElse(null);
        } else if (associatedObject instanceof Entity) {
            final Entity entity = ((Entity) associatedObject);
            final ResourceKey key = entity.serverLocation().world().key();
            final UUID entityUuid = ((Entity) associatedObject).uniqueId();
            this.associatedObjectSupplier =
                    () -> SpongeCommon.game().server().worldManager().world(key).flatMap(x -> x.entity(entityUuid)).orElse(null);
        } else {
            // We create a weak reference here so we don't hold on to game objects.
            final WeakReference<Object> objectWeakReference = new WeakReference<>(associatedObject);
            this.associatedObjectSupplier = objectWeakReference::get;
        }
        return this;
    }

    @Override
    public PlaceholderContext.Builder associatedObject(final @Nullable Supplier<Object> associatedObjectSupplier) {
        this.associatedObjectSupplier = associatedObjectSupplier;
        return this;
    }

    @Override
    public PlaceholderContext.Builder argumentString(final @Nullable String argument) {
        this.argument = argument == null || argument.isEmpty() ? null : argument;
        return this;
    }

    @Override
    public PlaceholderContext build() throws IllegalStateException {
        return new SpongePlaceholderContext(this.associatedObjectSupplier, this.argument);
    }

    @Override
    public PlaceholderContext.Builder reset() {
        this.associatedObjectSupplier = null;
        this.argument = null;
        return this;
    }

}
