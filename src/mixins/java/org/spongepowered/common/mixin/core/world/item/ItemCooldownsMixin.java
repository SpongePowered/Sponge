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
package org.spongepowered.common.mixin.core.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.world.item.ItemCooldowns_CooldownInstanceAccessor;
import org.spongepowered.common.bridge.world.item.ItemCooldownsBridge;
import org.spongepowered.common.util.Constants;

import java.util.Map;

@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin implements ItemCooldownsBridge {

    // @formatter:off
    @Shadow @Final private Map<Item, ?> cooldowns;
    @Shadow public abstract ResourceLocation shadow$getCooldownGroup(final ItemStack $$0);

    // @formatter:on


    private int impl$lastSetCooldownResult;

    @Inject(
            method = "addCooldown(Lnet/minecraft/resources/ResourceLocation;I)V",
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void impl$throwEventOnSetAndTrackResult(final ResourceLocation group, final int ticks, final CallbackInfo ci) {
        this.impl$lastSetCooldownResult = this.impl$throwSetCooldownEvent(group, ticks);
        if (this.impl$lastSetCooldownResult == Constants.Sponge.Entity.Player.ITEM_COOLDOWN_CANCELLED) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "addCooldown(Lnet/minecraft/resources/ResourceLocation;I)V",
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            argsOnly = true
    )
    private int impl$setResultOfEvent(int ticks) {
        return this.impl$lastSetCooldownResult;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;", remap = false))
    private Object onTick(final Map.Entry<ResourceLocation, ?> entry) {
        this.impl$throwEndCooldownEvent(entry.getKey());
        return entry.getKey();
    }

    protected int impl$throwSetCooldownEvent(final ResourceLocation group, final int ticks) {
        return 0;
    }

    protected void impl$throwEndCooldownEvent(final ResourceLocation group) {

    }

    @Override
    public boolean bridge$getSetCooldownResult() {
        return this.impl$lastSetCooldownResult != Constants.Sponge.Entity.Player.ITEM_COOLDOWN_CANCELLED;
    }

    @Inject(method = "getCooldownPercent", at = @At("HEAD"), cancellable = true)
    private void impl$getCooldownPercentInfiniteCooldown(final ItemStack $$0, final float $$1, final CallbackInfoReturnable<Float> cir) {
        ResourceLocation $$2 = this.shadow$getCooldownGroup($$0);
        final ItemCooldowns_CooldownInstanceAccessor cooldown = (ItemCooldowns_CooldownInstanceAccessor) this.cooldowns.get($$0);
        if (cooldown != null && cooldown.accessor$endTime() == cooldown.accessor$startTime() - 1) {
            cir.setReturnValue(1.0F);
        }
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemCooldowns$CooldownInstance;endTime:I"))
    private int impl$dontRemoveInfiniteCooldown(final @Coerce ItemCooldowns_CooldownInstanceAccessor instance) {
        if (instance.accessor$endTime() != instance.accessor$startTime() - 1) {
            return instance.accessor$endTime();
        }
        return Integer.MAX_VALUE;
    }
}
