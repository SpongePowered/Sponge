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
package org.spongepowered.common.mixin.core.command;

import com.mojang.brigadier.ResultConsumer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.accessor.command.CommandSourceAccessor;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

@Mixin(CommandSource.class)
public abstract class CommandSourceMixin implements CommandSourceBridge {

    @Shadow @Final private ICommandSource source;
    @Shadow @Final private Vec3d pos;
    @Shadow @Final private Vec2f rotation;
    @Shadow @Final private ServerWorld world;
    @Shadow @Final private int permissionLevel;
    @Shadow @Final private String name;
    @Shadow @Final private ITextComponent displayName;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final @javax.annotation.Nullable private Entity entity;
    @Shadow @Final private boolean feedbackDisabled;
    @Shadow @Final private ResultConsumer<CommandSource> resultConsumer;
    @Shadow @Final private EntityAnchorArgument.Type entityAnchorType;
    private boolean impl$causeHasBeenUsedInConstruction = false;
    private Cause impl$cause = PhaseTracker.getCauseStackManager().getCurrentCause();

    @Override
    public Cause bridge$getCause() {
        return this.impl$cause;
    }

    @Override
    public CommandSource bridge$createFromThisSource() {
        if (this.impl$causeHasBeenUsedInConstruction) {
            return (CommandSource) (Object) this;
        }
        return this.bridge$createFromCauseAndThisSource(this.impl$cause);
    }

    @Override
    public CommandSource bridge$createFromCauseAndThisSource(final Cause cause) {
        Cause currentCause = cause;

        // First, we check to see if the command source is the root cause. If not, we want to add it.
        if (!cause.root().equals(this.source)) {
            currentCause = cause.with(this.source);
        }

        // Now, the following EventContextKeys will alter certain parts of the source.
        // EventContextKeys.MESSAGE_CHANNEL
        // EventContextKeys.LOCATION
        // EventContextKeys.ROTATION
        // EventContextKeys.BLOCK_TARGET
        final EventContext eventContext = cause.getContext();
        if (eventContext.containsKey(EventContextKeys.MESSAGE_CHANNEL) ||
                eventContext.containsKey(EventContextKeys.LOCATION) ||
                eventContext.containsKey(EventContextKeys.ROTATION) ||
                eventContext.containsKey(EventContextKeys.BLOCK_TARGET)) {
            return this.impl$withCause(currentCause);
        }

        this.impl$cause = currentCause;
        this.impl$causeHasBeenUsedInConstruction = true;
        return (CommandSource) (Object) this;
    }

    @Override
    public ICommandSource bridge$getICommandSource() {
        return this.source;
    }

    @Override
    public CommandCause bridge$asCommandCause() {
        return (CommandCause) this;
    }

    private CommandSource impl$withCause(final Cause cause) {
        final EventContext context = cause.getContext();
        return CommandSourceAccessor.accessor$createInstance(
                this.source,
                context.get(EventContextKeys.LOCATION).map(x -> VecHelper.toVec3d(x.getPosition())).orElse(this.pos),
                context.get(EventContextKeys.ROTATION).map(x -> new Vec2f((float) x.getX(), (float) x.getZ())).orElse(this.rotation),
                context.get(EventContextKeys.LOCATION).map(x -> (ServerWorld) x.getWorld()).orElse(this.world),
                this.permissionLevel,
                this.name,
                this.displayName,
                this.server,
                this.entity,
                this.feedbackDisabled,
                this.resultConsumer, // TODO: Message Channel stuff should go here, right?
                this.entityAnchorType
        );
    }

}
