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
package org.spongepowered.common.mixin.api.mcp.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
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
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

@Mixin(CommandSource.class)
public abstract class CommandSourceMixin_API implements CommandCause {

    @Override
    public Cause getCause() {
        return ((CommandSourceBridge) this).bridge$getCause();
    }

    @Override
    public Subject getSubject() {
        return this.getCause().getContext()
                .get(EventContextKeys.SUBJECT)
                .orElseGet(() -> this.getCause().first(Subject.class).orElseGet(Sponge::getSystemSubject));
    }

    @Override
    public Audience getAudience() {
        return this.getCause().getContext()
                .get(EventContextKeys.AUDIENCE)
                .orElseGet(() -> this.getCause().first(Audience.class).orElseGet(Sponge::getSystemSubject));
    }

    @Override
    public Optional<ServerLocation> getLocation() {
        final Cause cause = this.getCause();
        final EventContext eventContext = cause.getContext();
        if (eventContext.containsKey(EventContextKeys.LOCATION)) {
            return eventContext.get(EventContextKeys.LOCATION);
        }

        final Optional<ServerLocation> optionalLocation = this.getTargetBlock().flatMap(BlockSnapshot::getLocation);
        if (optionalLocation.isPresent()) {
            return optionalLocation;
        }

        return cause.first(Locatable.class).map(Locatable::getServerLocation);
    }

    @Override
    public Optional<Vector3d> getRotation() {
        final Cause cause = this.getCause();
        final EventContext eventContext = cause.getContext();
        if (eventContext.containsKey(EventContextKeys.ROTATION)) {
            return eventContext.get(EventContextKeys.ROTATION);
        }

        return cause.first(Entity.class).map(Entity::getRotation);
    }

    @Override
    public Optional<BlockSnapshot> getTargetBlock() {
        return Optional.ofNullable(this.getCause().getContext().get(EventContextKeys.BLOCK_TARGET)
                .orElseGet(() -> this.getCause().first(BlockSnapshot.class).orElse(null)));
    }

    @Override
    public void sendMessage(@NonNull final Identified identity, @NonNull final Component message) {
        this.getAudience().sendMessage(identity, message);
    }

    @Override
    public void sendMessage(@NonNull final Identity identity, @NonNull final Component message) {
        this.getAudience().sendMessage(identity, message);
    }

}
