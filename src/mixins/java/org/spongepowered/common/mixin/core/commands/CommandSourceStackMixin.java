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
package org.spongepowered.common.mixin.core.commands;

import com.mojang.brigadier.ResultConsumer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.commands.CommandSourceStackAccessor;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.bridge.commands.CommandSourceBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.service.server.permission.SpongePermissions;
import org.spongepowered.common.util.VecHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements CommandSourceStackBridge {

    private static final String PROTECTED_CTOR = "(Lnet/minecraft/commands/CommandSource;Lnet/minecraft/world/phys/Vec3;"
            + "Lnet/minecraft/world/phys/Vec2;Lnet/minecraft/server/level/ServerLevel;ILjava/lang/String;Lnet/minecraft/network/chat/Component;"
            + "Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/entity/Entity;ZLcom/mojang/brigadier/ResultConsumer;"
            + "Lnet/minecraft/commands/arguments/EntityAnchorArgument$Anchor;)";
    private static final String PROTECTED_CTOR_METHOD = "<init>" + CommandSourceStackMixin.PROTECTED_CTOR + "V";

    @Shadow @Final private CommandSource source;
    @Shadow @Final @Mutable private Vec3 worldPosition;
    @Shadow @Final @Mutable private Vec2 rotation;
    @Shadow @Final @Mutable private ServerLevel level;
    @Shadow @Final @Mutable private int permissionLevel;

    @Shadow @Final private Component displayName;
    @Shadow @Final private String textName;
    @Shadow @Final @Nullable private Entity entity;

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private boolean silent;
    @Shadow @Final private ResultConsumer<CommandSourceStack> consumer;
    @Shadow @Final private EntityAnchorArgument.Anchor anchor;
    private Cause impl$cause;
    @Nullable private Supplier<String> impl$potentialPermissionNode = null;

    @Inject(method = CommandSourceStackMixin.PROTECTED_CTOR_METHOD, at = @At("RETURN"))
    private void impl$setCauseOnConstruction(
            final CommandSource p_i49553_1_,
            final Vec3 p_i49553_2_,
            final Vec2 p_i49553_3_,
            final ServerLevel p_i49553_4_,
            final int p_i49553_5_,
            final String p_i49553_6_,
            final Component p_i49553_7_,
            final MinecraftServer p_i49553_8_,
            @Nullable final Entity p_i49553_9_,
            final boolean p_i49553_10_,
            final ResultConsumer<CommandSourceStack> p_i49553_11_,
            final EntityAnchorArgument.Anchor p_i49553_12_,
            final CallbackInfo ci
    ) {
        this.impl$cause = PhaseTracker.getCauseStackManager().currentCause();
        final EventContext context = this.impl$cause.context();

        context.get(EventContextKeys.LOCATION).ifPresent(x ->{
            this.worldPosition = VecHelper.toVanillaVector3d(x.position());
            this.level = (ServerLevel) x.world();
        });

        context.get(EventContextKeys.ROTATION).ifPresent(x -> this.rotation = new Vec2((float) x.x(), (float) x.y()));
        context.get(EventContextKeys.SUBJECT).ifPresent(x -> {
            if (x instanceof EntityAccessor) {
                this.permissionLevel = ((EntityAccessor) x).invoker$getPermissionLevel();
            } else if (x instanceof MinecraftServer && !((MinecraftServer) x).isSingleplayer()) {
                this.permissionLevel = 4;
            }
        });
    }

    /*
     * All the with* methods copy this CommandSource, so we need to do our own copy.
     * This method MUST be above all other with* return injections so that the cause copy happens
     * FIRST. That way, we don't overwrite any changes we then need to make.
     */
    @Inject(method = {
            "withEntity",
            "withPosition",
            "withRotation(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/commands/CommandSourceStack;",
            "withCallback(Lcom/mojang/brigadier/ResultConsumer;)Lnet/minecraft/commands/CommandSourceStack;",
            "withSuppressedOutput",
            "withPermission",
            "withMaximumPermission",
            "withAnchor",
            "withLevel"
    }, at = @At("RETURN"))
    private void impl$copyPermissionOnCopy(final CallbackInfoReturnable<CommandSourceStack> cir) {
        if (cir.getReturnValue() != (Object) this) {
            final CommandSourceStackBridge commandSourceStackBridge = ((CommandSourceStackBridge) cir.getReturnValue());
            commandSourceStackBridge.bridge$setPotentialPermissionNode(this.impl$potentialPermissionNode);
            commandSourceStackBridge.bridge$setCause(this.impl$cause);
        }
    }

    @Override
    public CommandCause bridge$withCurrentCause() {
        // Cause is set in ctor.
        return (CommandCause) CommandSourceStackAccessor.invoker$new(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel,
                this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    /*
     * A note on why we're doing this with the cause manually.
     *
     * When the object is first constructed, we get the cause from the stack manager. However, as the command processor
     * works through the nodes, this entire source may get replaced. We want to keep some of the changes in sync,
     * but the original cause may have gone by the time the source changes. Really, this command source is the analogue
     * of our Cause, NOT our CauseStackManager, so we just need to do `Cause.with(...)` along with their select `with*(...)`
     * methods.
     */

    @Inject(method = "withLevel", at = @At("RETURN"))
    private void impl$updateCauseOnWithWorld(final ServerLevel serverWorld, final CallbackInfoReturnable<CommandSourceStack> cir) {
        if (cir.getReturnValue() != (Object) this) {
            final ServerLocation location = this.impl$cause.context().get(EventContextKeys.LOCATION)
                    .map(x -> ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) serverWorld, x.position()))
                    .orElseGet(() -> ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) serverWorld,
                            VecHelper.toVector3d(cir.getReturnValue().getPosition())));
            ((CommandSourceStackBridge) cir.getReturnValue()).bridge$setCause(this.impl$applyToCause(EventContextKeys.LOCATION, location));
        }
    }

    @Inject(method = "withPosition", at = @At("RETURN"))
    private void impl$updateCauseOnWithPosition(final Vec3 pos, final CallbackInfoReturnable<CommandSourceStack> cir) {
        if (cir.getReturnValue() != (Object) this) {
            final org.spongepowered.math.vector.Vector3d position = VecHelper.toVector3d(pos);
            final ServerLocation location = this.impl$cause.context().get(EventContextKeys.LOCATION)
                    .map(x -> ServerLocation.of(x.world(), position))
                    .orElseGet(() -> ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) cir.getReturnValue().getLevel(), position));
            ((CommandSourceStackBridge) cir.getReturnValue()).bridge$setCause(this.impl$applyToCause(EventContextKeys.LOCATION, location));
        }
    }

    @Inject(method = "withRotation(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/commands/CommandSourceStack;", at = @At("RETURN"))
    private void impl$updateCauseOnWithRotation(final Vec2 rotation, final CallbackInfoReturnable<CommandSourceStack> cir) {
        if (cir.getReturnValue() != (Object) this) {
            final org.spongepowered.math.vector.Vector3d rot = new org.spongepowered.math.vector.Vector3d(rotation.x, rotation.y, 0); // no roll
            ((CommandSourceStackBridge) cir.getReturnValue()).bridge$setCause(this.impl$applyToCause(EventContextKeys.ROTATION, rot));
        }
    }

    @Inject(method = "hasPermission", at = @At(value = "HEAD"), cancellable = true)
    private void impl$checkPermission(final int opLevel, final CallbackInfoReturnable<Boolean> cir) {
        if (Sponge.isServerAvailable() && this.impl$potentialPermissionNode != null) {
            final String perm = this.impl$potentialPermissionNode.get();
            // This will register the permission with the first op level we retrieve.
            SpongePermissions.registerPermission(perm, opLevel);
            cir.setReturnValue(((CommandCause) this).hasPermission(perm));
        }
        // fall through to the op level check if we haven't set a permission node.
    }

    @Override
    public void bridge$setPotentialPermissionNode(final @Nullable Supplier<String> permission) {
        this.impl$potentialPermissionNode = permission;
    }

    @Override
    public void bridge$setCause(final Cause cause) {
        this.impl$cause = cause;
    }

    @Override
    public Cause bridge$getCause() {
        return this.impl$cause;
    }

    @Override
    public CommandSource bridge$getCommandSource() {
        return this.source;
    }

    @Override
    public void bridge$updateFrameFromCommandSource(final CauseStackManager.StackFrame frame) {
        ((CommandSourceBridge) this.source).bridge$addToCauseStack(frame);
    }

    @Override
    public CommandCause bridge$asCommandCause() {
        return (CommandCause) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Cause impl$applyToCause(final EventContextKey<T> key, final T value) {
        final EventContext.Builder builder = EventContext.builder();
        // We don't use from() here as we might need to replace a context
        // See https://github.com/SpongePowered/Sponge/issues/3495
        this.impl$cause.context().asMap().forEach((k, v) -> {
            if (!k.equals(key)) {
                builder.add((EventContextKey) k, v);
            }
        });
        builder.add(key, value);
        return Cause.builder().from(this.impl$cause).build(builder.build());
    }

}
