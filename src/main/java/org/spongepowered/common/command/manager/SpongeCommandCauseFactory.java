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
package org.spongepowered.common.command.manager;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

public final class SpongeCommandCauseFactory implements CommandCause.Factory {

    @Override
    public @NonNull CommandCause create() {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final Cause cause = frame.currentCause();
            final CommandSource iCommandSource =
                    cause.first(CommandSource.class).orElseGet(() -> SpongeCommon.game().systemSubject());
            final CommandSourceStack commandSource;
            if (iCommandSource instanceof CommandSourceProviderBridge) {
                // We know about this one so we can create it using the factory method on the source.
                commandSource = ((CommandSourceProviderBridge) iCommandSource).bridge$getCommandSource(cause);
            } else {
                // try to create a command cause from the given ICommandSource, but as Mojang did not see fit to
                // put any identifying characteristics on the object, we have to go it alone...
                final EventContext context = cause.context();
                final @Nullable Locatable locatable = iCommandSource instanceof Locatable ? (Locatable) iCommandSource : null;
                final Component displayName;
                if (iCommandSource instanceof Entity) {
                    displayName = ((Entity) iCommandSource).get(Keys.DISPLAY_NAME).map(SpongeAdventure::asVanilla)
                            .orElseGet(() -> new TextComponent(
                                    iCommandSource instanceof Nameable ? ((Nameable) iCommandSource).name() :
                                            iCommandSource.getClass().getSimpleName()));
                } else {
                    displayName = new TextComponent(
                            iCommandSource instanceof Nameable ? ((Nameable) iCommandSource).name() :
                                    iCommandSource.getClass().getSimpleName());
                }
                final String name = displayName.getString();
                commandSource = new CommandSourceStack(
                        iCommandSource,
                        context.get(EventContextKeys.LOCATION).map(x -> VecHelper.toVanillaVector3d(x.position()))
                                .orElseGet(() -> locatable == null ? Vec3.ZERO : VecHelper.toVanillaVector3d(locatable.location().position())),
                        context.get(EventContextKeys.ROTATION)
                                .map(rot -> new Vec2((float) rot.x(), (float) rot.y()))
                                .orElse(Vec2.ZERO),
                        context.get(EventContextKeys.LOCATION).map(x -> (ServerLevel) x.world())
                                .orElseGet(() -> locatable == null ? SpongeCommon.server().getLevel(Level.OVERWORLD) :
                                        (ServerLevel) locatable.serverLocation().world()),
                        4,
                        name,
                        displayName,
                        SpongeCommon.server(),
                        iCommandSource instanceof Entity ? (net.minecraft.world.entity.Entity) iCommandSource : null
                );
            }

            // We don't want the command source to have altered the cause here (unless there is the special case of the
            // server), so we reset it back to what it was (in the ctor of CommandSource, it will add the current source
            // to the cause - that's for if the source is created elsewhere, not here)
            ((CommandSourceStackBridge) commandSource).bridge$setCause(frame.currentCause());
            return (CommandCause) commandSource;
        }
    }

}
