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
package org.spongepowered.common.mixin.api.minecraft.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin_API implements CommandCause {

    @Override
    public Cause cause() {
        return ((CommandSourceStackBridge) this).bridge$getCause();
    }

    @Override
    public Subject subject() {
        return this.cause().context()
                .get(EventContextKeys.SUBJECT)
                .orElseGet(() -> this.cause().first(Subject.class).orElseGet(Sponge::systemSubject));
    }

    @Override
    public Audience audience() {
        return this.cause().context()
                .get(EventContextKeys.AUDIENCE)
                .orElseGet(() -> this.cause().first(Audience.class).orElseGet(Sponge::systemSubject));
    }

    @Override
    public Optional<ServerLocation> location() {
        final Cause cause = this.cause();
        final EventContext eventContext = cause.context();
        if (eventContext.containsKey(EventContextKeys.LOCATION)) {
            return eventContext.get(EventContextKeys.LOCATION);
        }

        final Optional<ServerLocation> optionalLocation = this.targetBlock().flatMap(BlockSnapshot::location);
        if (optionalLocation.isPresent()) {
            return optionalLocation;
        }

        return cause.first(Locatable.class).map(Locatable::serverLocation);
    }

    @Override
    public Optional<Vector3d> rotation() {
        final Cause cause = this.cause();
        final EventContext eventContext = cause.context();
        if (eventContext.containsKey(EventContextKeys.ROTATION)) {
            return eventContext.get(EventContextKeys.ROTATION);
        }

        return cause.first(Entity.class).map(Entity::rotation);
    }

    @Override
    public Optional<BlockSnapshot> targetBlock() {
        return Optional.ofNullable(this.cause().context().get(EventContextKeys.BLOCK_TARGET)
                .orElseGet(() -> this.cause().first(BlockSnapshot.class).orElse(null)));
    }

    @Override
    public void sendMessage(@NonNull final Identified identity, @NonNull final Component message) {
        this.audience().sendMessage(identity, message);
    }

    @Override
    public void sendMessage(@NonNull final Identity identity, @NonNull final Component message) {
        this.audience().sendMessage(identity, message);
    }

}
