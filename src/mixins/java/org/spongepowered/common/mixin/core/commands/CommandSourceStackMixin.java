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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParseContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.ValidationWrapper;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TaskChainer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.Validatable;
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
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.commands.CommandSourceStackAccessor;
import org.spongepowered.common.accessor.world.entity.player.PlayerAccessor;
import org.spongepowered.common.bridge.commands.CommandSourceBridge;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.service.server.permission.SpongePermissions;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements CommandSourceStackBridge, SuggestionContext {


    //@formatter:off
    @Shadow @Final private CommandSource source;
    @Shadow @Final @Mutable private Vec3 worldPosition;
    @Shadow @Final @Mutable private Vec2 rotation;
    @Shadow @Final @Mutable private ServerLevel level;
    @Shadow @Final @Mutable private PermissionSet permissions;

    @Shadow @Final private CommandSourceStack.NamesProvider namesProvider;
    @Shadow @Final @Nullable private Entity entity;

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private boolean silent;
    @Shadow @Final private CommandResultCallback resultCallback;
    @Shadow @Final private EntityAnchorArgument.Anchor anchor;
    @Shadow @Final private CommandSigningContext signingContext;

    @Shadow @Final private TaskChainer chatMessageChainer;
    //@formatter:on

    private Cause impl$cause;
    @Nullable private Supplier<String> impl$potentialPermissionNode = null;
    private @Nullable PermissionService impl$permissionService;
    private @Nullable ParseContext impl$parseContext;

    @Inject(method = "<init>(Lnet/minecraft/commands/CommandSource;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec2;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/permissions/PermissionSet;Lnet/minecraft/commands/CommandSourceStack$NamesProvider;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/entity/Entity;ZLnet/minecraft/commands/CommandResultCallback;Lnet/minecraft/commands/arguments/EntityAnchorArgument$Anchor;Lnet/minecraft/commands/CommandSigningContext;Lnet/minecraft/util/TaskChainer;)V",
        at = @At("RETURN"))
    private void impl$setCauseOnConstruction(
        final CommandSource source,
        final Vec3 position,
        final Vec2 rotation,
        final ServerLevel level,
        final PermissionSet permissions,
        final CommandSourceStack.NamesProvider namesProvider,
        final MinecraftServer server,
        final @Nullable Entity entity,
        final boolean silent,
        final CommandResultCallback resultCallback,
        final EntityAnchorArgument.Anchor anchor,
        final CommandSigningContext signingContext,
        final TaskChainer chatMessageChainer,
        final CallbackInfo ci
    ) {
        this.impl$cause = PhaseTracker.getInstance().currentCause();
        final EventContext context = this.impl$cause.context();

        context.get(EventContextKeys.LOCATION).ifPresent(x -> {
            this.worldPosition = VecHelper.toVanillaVector3d(x.position());
            this.level = (ServerLevel) x.world();
        });

        context.get(EventContextKeys.ROTATION).ifPresent(x -> this.rotation = new Vec2((float) x.x(), (float) x.y()));
        context.get(EventContextKeys.SUBJECT).ifPresent(x -> {
            if (x instanceof PlayerAccessor pa) {
                this.permissions = pa.invoker$permissions();
            } else if (x instanceof MinecraftServer ms && !ms.isSingleplayer()) {
                this.permissions = PermissionSet.ALL_PERMISSIONS;
            }
        });
        this.permissions = (incoming) -> {
            if (this.impl$potentialPermissionNode != null) {
                final var opLevel = SpongeCommon.server().operatorUserPermissions().level();
                final String perm = this.impl$potentialPermissionNode.get();
                // This will register the permission with the first op level we retrieve.
                if (this.impl$permissionService != null) {
                    SpongePermissions.registerPermission(this.impl$permissionService, perm, opLevel);
                } else if (Sponge.isServerAvailable()) {
                    SpongePermissions.registerPermission(Sponge.server().serviceProvider().permissionService(), perm, opLevel);
                }
                return ((CommandCause) this).hasPermission(perm);
            }
            return permissions.hasPermission(incoming);
        };
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
        "withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;",
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
            commandSourceStackBridge.bridge$permissionService(this.impl$permissionService);
            commandSourceStackBridge.bridge$parseContext(this.impl$parseContext);
        }
    }

    @Override
    public CommandCause bridge$withCurrentCause() {
        // Cause is set in ctor.
        final CommandCause instance = (CommandCause) CommandSourceStackAccessor.invoker$new(this.source, this.worldPosition, this.rotation, this.level, this.permissions,
            this.namesProvider, this.server, this.entity, this.silent, this.resultCallback, this.anchor, this.signingContext, this.chatMessageChainer);
        ((CommandSourceStackBridge) instance).bridge$permissionService(this.impl$permissionService);
        ((CommandSourceStackBridge) instance).bridge$parseContext(this.impl$parseContext);
        return instance;
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

    @Override
    public void bridge$permissionService(final PermissionService permissionService) {
        this.impl$permissionService = permissionService;
    }

    @Override
    public void bridge$parseContext(final @Nullable ParseContext context) {
        this.impl$parseContext = context;
    }

    /*
     * Vanilla parses commands and suggests for them with contexts of their own rather than with the source.
     * Sponge's dispatcher works on the source throughout, so the source is those contexts too. One that only
     * stands in for a context it was given, such as while a function is compiled, has no server or level to
     * answer from and passes the questions on.
     */

    @Inject(method = "holderGetters", at = @At("HEAD"), cancellable = true)
    private void impl$holderGettersOfParseContext(final CallbackInfoReturnable<HolderGetter.Provider> cir) {
        if (this.impl$parseContext != null) {
            cir.setReturnValue(this.impl$parseContext.holderGetters());
        }
    }

    @Inject(method = "enabledFeatures", at = @At("HEAD"), cancellable = true)
    private void impl$enabledFeaturesOfParseContext(final CallbackInfoReturnable<FeatureFlagSet> cir) {
        if (this.impl$parseContext != null) {
            cir.setReturnValue(this.impl$parseContext.enabledFeatures());
        }
    }

    @Override
    public boolean allowsSelectors() {
        if (this.impl$parseContext != null) {
            return this.impl$parseContext.allowsSelectors();
        }
        return ((CommandCause) this).hasPermission(Constants.Permissions.SELECTOR_PERMISSION)
            || this.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS);
    }

    @Override
    public <T extends Validatable> ValidationWrapper<T> getOrCreateValidationWrapper(final Holder<T> element, final ContextKeySet contextKeySet) throws CommandSyntaxException {
        if (this.impl$parseContext != null) {
            return this.impl$parseContext.getOrCreateValidationWrapper(element, contextKeySet);
        }
        // As for a command that is about to run
        return ValidationWrapper.Provider.IMMEDIATE.getOrCreate(element, contextKeySet);
    }

    @Override
    public Collection<String> onlinePlayerNames() {
        return List.of(this.server.getPlayerNames());
    }

    @Override
    public Collection<String> allTeams() {
        return this.server.getScoreboard().getTeamNames();
    }

    @Override
    public Stream<Identifier> availableSounds() {
        return BuiltInRegistries.SOUND_EVENT.stream().map(SoundEvent::location);
    }

    @Override
    public Stream<Identifier> availablePostEffects() {
        return Stream.empty();
    }

    @Override
    public @Nullable MinecraftServer server() {
        return this.server;
    }

    @Override
    public @Nullable Level level() {
        return this.level;
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return this.server.levelKeys();
    }

    @Override
    public @Nullable Entity entity() {
        return this.entity;
    }

    @Override
    public Vec3 position() {
        return this.worldPosition;
    }

    @Override
    public CompletableFuture<Suggestions> suggestObjectiveNames(final CommandContext<?> context, final SuggestionsBuilder builder) {
        return SuggestionContext.suggest(this.server.getScoreboard().getObjectiveNames(), builder);
    }

    @Override
    public CompletableFuture<Suggestions> customSuggestion(final CommandContext<?> context) {
        return Suggestions.empty();
    }

    @Override
    public <E> CompletableFuture<Suggestions> suggestRegistryElements(
        final CommandContext<?> context,
        final ResourceKey<? extends Registry<? extends E>> key,
        final SuggestionContext.ElementSuggestionType elements,
        final SuggestionsBuilder builder,
        final Predicate<E> filter
    ) {
        return this.holderLookups().lookup(key).map(registry -> {
            this.suggestRegistryElements(registry, elements, builder, filter);
            return builder.buildFuture();
        }).orElseGet(Suggestions::empty);
    }
}
