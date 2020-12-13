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

import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.command.ICommandSourceBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

public final class SpongeCommandCauseFactory implements CommandCause.Factory {

    @Override
    @NonNull
    public CommandCause create() {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final Cause cause = frame.getCurrentCause();
            final ICommandSource iCommandSource =
                    cause.first(ICommandSource.class).orElseGet(() -> SpongeCommon.getGame().getSystemSubject());
            final CommandSource commandSource;
            if (iCommandSource instanceof CommandSourceProviderBridge) {
                // We know about this one so we can create it using the factory method on the source.
                commandSource = ((CommandSourceProviderBridge) iCommandSource).bridge$getCommandSource(cause);
            } else {
                // try to create a command cause from the given ICommandSource, but as Mojang did not see fit to
                // put any identifying characteristics on the object, we have to go it alone...
                final EventContext context = cause.getContext();
                final @Nullable Locatable locatable = iCommandSource instanceof Locatable ? (Locatable) iCommandSource : null;
                final ITextComponent displayName;
                if (iCommandSource instanceof Entity) {
                    displayName = ((Entity) iCommandSource).get(Keys.DISPLAY_NAME).map(SpongeAdventure::asVanilla)
                            .orElseGet(() -> new StringTextComponent(
                                    iCommandSource instanceof Nameable ? ((Nameable) iCommandSource).getName() :
                                            iCommandSource.getClass().getSimpleName()));
                } else {
                    displayName = new StringTextComponent(
                            iCommandSource instanceof Nameable ? ((Nameable) iCommandSource).getName() :
                                    iCommandSource.getClass().getSimpleName());
                }
                final String name = displayName.getString();
                commandSource = new CommandSource(
                        iCommandSource,
                        context.get(EventContextKeys.LOCATION).map(x -> VecHelper.toVanillaVector3d(x.getPosition()))
                                .orElseGet(() -> locatable == null ? Vector3d.ZERO : VecHelper.toVanillaVector3d(locatable.getLocation().getPosition())),
                        context.get(EventContextKeys.ROTATION)
                                .map(x -> new Vector2f((float) x.getX(), (float) x.getY()))
                                .orElse(Vector2f.ZERO),
                        context.get(EventContextKeys.LOCATION).map(x -> (ServerWorld) x.getWorld())
                                .orElseGet(() -> locatable == null ? SpongeCommon.getServer().getLevel(World.OVERWORLD) :
                                        (ServerWorld) locatable.getServerLocation().getWorld()),
                        4,
                        name,
                        displayName,
                        SpongeCommon.getServer(),
                        iCommandSource instanceof Entity ? (net.minecraft.entity.Entity) iCommandSource : null
                );
            }

            // We don't want the command source to have altered the cause here (unless there is the special case of the
            // server), so we reset it back to what it was (in the ctor of CommandSource, it will add the current source
            // to the cause - that's for if the source is created elsewhere, not here)
            ((CommandSourceBridge) commandSource).bridge$setCause(frame.getCurrentCause());
            return (CommandCause) commandSource;
        }
    }

}
