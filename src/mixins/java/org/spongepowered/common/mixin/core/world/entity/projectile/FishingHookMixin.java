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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.projectile.FishingBobber;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends ProjectileMixin {

    // @formatter:off
    @Shadow @Nullable private Entity hookedIn;
    @Shadow private int nibble;
    // @formatter:on

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void impl$onSetHookedEntity(final @Nullable Entity hookedIn, final CallbackInfo ci) {
        if (hookedIn != null && SpongeCommon.post(SpongeEventFactory.createFishingEventHookEntity(PhaseTracker.getInstance().currentCause(), (org.spongepowered.api.entity.Entity) hookedIn, (FishingBobber) this))) {
            this.hookedIn = null;
            ci.cancel();
        }
    }

    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    private void impl$onRetrieveHookedEntity(final ItemStack tool, final CallbackInfoReturnable<Integer> cir) {
        if (SpongeCommon.post(SpongeEventFactory.createFishingEventStop(PhaseTracker.getInstance().currentCause(), ((FishingBobber) this), List.of()))) {
            cir.setReturnValue(0);
        }
    }

    @WrapOperation(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> impl$onRetrieveLootTable(final LootTable lootTable, final LootParams lootParams, final Operation<ObjectArrayList<ItemStack>> original, final @Cancellable CallbackInfoReturnable<Integer> cir) {
        final List<Transaction<@NotNull ItemStackSnapshot>> transactions = original.call(lootTable, lootParams)
            .stream().map(ItemStackUtil::snapshotOf).map(snapshot -> new Transaction<>(snapshot, snapshot)).toList();

        if (SpongeCommon.post(SpongeEventFactory.createFishingEventStop(PhaseTracker.getInstance().currentCause(), ((FishingBobber) this), transactions))) {
            cir.setReturnValue(0);
            return ObjectArrayList.of();
        }

        return transactions.stream().filter(Transaction::isValid)
            .map(t -> (ItemStack) (Object) t.finalReplacement().asMutable()).collect(ObjectArrayList.toList());
    }

    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onGround()Z"), cancellable = true)
    private void impl$onRetrieveEmpty(final ItemStack tool, final CallbackInfoReturnable<Integer> cir) {
        if (this.hookedIn == null && this.nibble <= 0
            && SpongeCommon.post(SpongeEventFactory.createFishingEventStop(PhaseTracker.getInstance().currentCause(), ((FishingBobber) this), List.of()))) {
            cir.setReturnValue(0);
        }
    }

    @WrapMethod(method = "retrieve")
    private int impl$wrapRetrieveWithCause(final ItemStack tool, final Operation<Integer> original) {
        final Entity owner = this.shadow$getOwner();

        if (owner == null) {
            return original.call(tool);
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(owner);
            return original.call(tool);
        }
    }

    @Inject(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;discard()V"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;shouldStopFishing(Lnet/minecraft/world/entity/player/Player;)Z"),
            to = @At(value = "TAIL")
        )
    )
    private void impl$expireFishingHookOnLand(final CallbackInfo ci) {
        this.impl$callExpireEntityEvent();
    }

}
