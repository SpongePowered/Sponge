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
package org.spongepowered.common.text.placeholder;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongePlaceholderContextBuilder implements PlaceholderContext.Builder {

    @Nullable private Supplier<Object> associatedObjectSupplier;
    @Nullable private String argument = null;

    @Override
    @SuppressWarnings("unchecked")
    public PlaceholderContext.Builder setAssociatedObject(@Nullable Object associatedObject) {
        if (associatedObject == null) {
            this.associatedObjectSupplier = null;
        } else if (associatedObject instanceof Supplier) {
            return setAssociatedObject((Supplier<Object>) associatedObject);
        } else if (associatedObject instanceof ConsoleSource) {
            this.associatedObjectSupplier = () -> (ConsoleSource) SpongeImpl.getServer();
        } else if (associatedObject instanceof Player) {
            return this.setAssociatedObject((Player) associatedObject);
        } else if (associatedObject instanceof World) {
            final UUID uuid = ((World) associatedObject).getUniqueId();
            this.associatedObjectSupplier = () -> SpongeImpl.getGame().getServer().getWorld(uuid).orElse(null);
        } else if (associatedObject instanceof Entity) {
            final Entity entity = ((Entity) associatedObject);
            final UUID worldUuid = entity.getWorld().getUniqueId();
            final UUID entityUuid = ((Entity) associatedObject).getUniqueId();
            this.associatedObjectSupplier =
                    () -> SpongeImpl.getGame().getServer().getWorld(worldUuid).flatMap(x -> x.getEntity(entityUuid)).orElse(null);
        } else {
            // We create a weak reference here so we don't hold on to game objects.
            final WeakReference<Object> objectWeakReference = new WeakReference<>(associatedObject);
            this.associatedObjectSupplier = objectWeakReference::get;
        }
        return this;
    }

    @Override
    public PlaceholderContext.Builder setAssociatedObject(@Nullable Supplier<Object> associatedObjectSupplier) {
        this.associatedObjectSupplier = associatedObjectSupplier;
        return this;
    }

    @Override
    public PlaceholderContext.Builder setArgumentString(@Nullable String argument) {
        this.argument = argument == null || argument.isEmpty() ? null : argument;
        return this;
    }


    @Override
    public PlaceholderContext build() throws IllegalStateException {
        return new SpongePlaceholderContext(this.associatedObjectSupplier, this.argument);
    }

    @Override
    public PlaceholderContext.Builder from(PlaceholderContext value) {
        Preconditions.checkArgument(value instanceof SpongePlaceholderContext, "Must be a SpongePlaceholderContext");
        this.argument = value.getArgumentString().orElse(null);
        this.associatedObjectSupplier = ((SpongePlaceholderContext) value).getAssociatedObjectSupplier();
        return this;
    }

    @Override
    public PlaceholderContext.Builder reset() {
        this.associatedObjectSupplier = null;
        this.argument = null;
        return this;
    }

}
