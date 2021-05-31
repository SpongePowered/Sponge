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
package org.spongepowered.common.mixin.core.world.level.border;

import net.minecraft.world.level.border.WorldBorder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ChangeWorldBorderEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.world.border.SpongeWorldBorderBuilder;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin implements WorldBorderBridge {

    private ResourceKey impl$associatedWorld;
    private boolean impl$fireEvent = true;

    @Override
    public void bridge$setAssociatedWorld(final ResourceKey associatedWorld) {
        this.impl$associatedWorld = associatedWorld;
    }

    @Inject(method = "setCenter", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetCenter(final double x, final double z, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .center(x, z)
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setSize", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetSize(final double size, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .targetDiameter(size)
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "lerpSizeBetween", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onLerping(final double initial, final double target, final long milliseconds, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .initialDiameter(initial)
                            .targetDiameter(target)
                            .timeToTargetDiameter(Duration.ofMillis(milliseconds))
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setWarningBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetWarningBlocks(final int distance, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .warningDistance(distance)
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setDamageSafeZone", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetDamageSafeZone(final double size, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .safeZone(size)
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setDamagePerBlock", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetDamagePerBlock(final double damagePerBlock, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .damagePerBlock(damagePerBlock)
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setWarningTime", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSetWarningTime(final int warningTime, final CallbackInfo ci) {
        if (this.impl$fireEvent) {
            final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed =
                    () -> new SpongeWorldBorderBuilder().from(this)
                            .warningTime(Duration.ofSeconds(warningTime))
                            .build();
            if (this.impl$suppressOriginalAction(proposed)) {
                ci.cancel();
            }
        }
    }

    private boolean impl$suppressOriginalAction(final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed) {
        final ChangeWorldBorderEvent.World result = this.impl$fireWorldBorderEvent(proposed);
        return result != null && (result.isCancelled() || result.originalNewBorder().orElse(null) != result.newBorder().orElse(null));
    }

    private ChangeWorldBorderEvent.@Nullable World impl$fireWorldBorderEvent(final Supplier<org.spongepowered.api.world.border.WorldBorder> proposed) {
        if (this.impl$associatedWorld != null && SpongeCommon.getGame().isServerAvailable()) {
            final Optional<ServerWorld> world = Sponge.server().worldManager().world(this.impl$associatedWorld);
            if (world.isPresent()) {
                final ChangeWorldBorderEvent.World event = SpongeEventFactory.createChangeWorldBorderEventWorld(
                        PhaseTracker.getCauseStackManager().currentCause(),
                        Optional.of(proposed.get()),
                        Optional.of(proposed.get()),
                        Optional.of(this.bridge$asImmutable()),
                        world.get()
                );
                final boolean isCancelled = Sponge.eventManager().post(event);
                if (!isCancelled) {
                    final org.spongepowered.api.world.border.WorldBorder toSet =
                            event.newBorder().orElse((org.spongepowered.api.world.border.WorldBorder) WorldBorder.DEFAULT_SETTINGS);
                    if (proposed.get() != toSet) {
                        // set the values, suppress this call.
                        this.impl$fireEvent = false;
                        this.bridge$applyFrom(toSet);
                        this.impl$fireEvent = true;
                    }
                }
                return event;
            }
        }
        return null;
    }

    @Override
    public org.spongepowered.api.world.border.WorldBorder bridge$asImmutable() {
        return (org.spongepowered.api.world.border.WorldBorder) ((WorldBorder) (Object) this).createSettings();
    }

    @Override
    public org.spongepowered.api.world.border.@Nullable WorldBorder bridge$applyFrom(final org.spongepowered.api.world.border.WorldBorder worldBorder) {
        final ChangeWorldBorderEvent.World event = this.impl$fireWorldBorderEvent(() -> worldBorder);
        final org.spongepowered.api.world.border.WorldBorder toSet;
        if (event != null) {
            if (event.isCancelled()) {
                return this.bridge$asImmutable();
            }

            final org.spongepowered.api.world.border.WorldBorder defaultSettings =
                    (org.spongepowered.api.world.border.WorldBorder) WorldBorder.DEFAULT_SETTINGS;
            toSet = event.newBorder().orElse(defaultSettings);
        } else {
            toSet = worldBorder;
        }

        this.impl$fireEvent = false;
        ((WorldBorder) (Object) this).applySettings((WorldBorder.Settings) toSet);
        this.impl$fireEvent = true;
        return toSet;
    }

}
