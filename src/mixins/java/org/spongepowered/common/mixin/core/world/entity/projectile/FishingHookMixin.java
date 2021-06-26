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

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.FishingBobber;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends EntityMixin {

    // @formatter:off
    @Shadow @Nullable private net.minecraft.world.entity.Entity hookedIn;
    @Shadow private int nibble;
    @Shadow @Final private int luck;

    @Shadow @Nullable public abstract Player shadow$getPlayerOwner();
    // @formatter:on

    @Shadow protected abstract void bringInHookedEntity();


    @Nullable private ProjectileSource impl$projectileSource;

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void onSetHookedEntity(CallbackInfo ci) {
        if (SpongeCommon
            .post(SpongeEventFactory.createFishingEventHookEntity(PhaseTracker.getCauseStackManager().currentCause(), (Entity) this.hookedIn, (FishingBobber) this))) {
            this.hookedIn = null;
            ci.cancel();
        }
    }

    /**
     * @author Aaron1011 - February 6th, 2015
     * @author Minecrell - December 24th, 2016 (Updated to Minecraft 1.11.2)
     * @author Minecrell - June 14th, 2017 (Rewritten to handle cases where no items are dropped)
     * @reason This needs to handle for both cases where a fish and/or an entity is being caught.
     */
    @Overwrite
    public int retrieve(ItemStack stack) {
        final Player playerEntity = this.shadow$getPlayerOwner();

        if (!this.level.isClientSide && playerEntity != null) {
            int i = 0;

            // Sponge start
            final List<Transaction<ItemStackSnapshot>> transactions;
            if (this.nibble > 0) {
                // Moved from below
                final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel)this.level)
                        .withParameter(LootContextParams.ORIGIN, this.shadow$position())
                        .withParameter(LootContextParams.TOOL, stack)
                        .withParameter(LootContextParams.THIS_ENTITY, (net.minecraft.world.entity.Entity) (Object) this)
                        .withRandom(this.random)
                        .withLuck((float)this.luck + playerEntity.getLuck());
                final LootTable lootTable = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                final List<ItemStack> list = lootTable.getRandomItems(lootcontext$builder.create(LootContextParamSets.FISHING));
                transactions = list.stream().map(ItemStackUtil::snapshotOf)
                        .map(snapshot -> new Transaction<>(snapshot, snapshot))
                        .collect(Collectors.toList());
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)playerEntity, stack, (FishingHook) (Object) this, list);
            } else {
                transactions = new ArrayList<>();
            }
            PhaseTracker.getCauseStackManager().pushCause(playerEntity);

            if (SpongeCommon.post(SpongeEventFactory.createFishingEventStop(PhaseTracker.getCauseStackManager().currentCause(), ((FishingBobber) this), transactions))) {
                // Event is cancelled
                return 0;
            }
            // Sponge end

            if (this.hookedIn != null) {
                this.bringInHookedEntity();
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) playerEntity, stack, (FishingHook) (Object) this, Collections.emptyList());
                this.level.broadcastEntityEvent((net.minecraft.world.entity.Entity) (Object) this, (byte) 31);
                i = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } // Sponge: Remove else

            // Sponge start - Moved up to event call
            if (!transactions.isEmpty()) { // Sponge: Check if we have any transactions instead
                //LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.world);
                //lootcontext$builder.withLuck((float) this.field_191518_aw + playerEntity.getLuck());

                // Use transactions
                for (Transaction<ItemStackSnapshot> transaction : transactions) {
                    if (!transaction.isValid()) {
                        continue;
                    }
                    ItemStack itemstack = (ItemStack) (Object) transaction.finalReplacement().createStack();
                    // Sponge end

                    ItemEntity entityitem = new ItemEntity(this.level, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), itemstack);
                    double d0 = playerEntity.getX() - this.shadow$getX();
                    double d1 = playerEntity.getY() - this.shadow$getY();
                    double d2 = playerEntity.getZ() - this.shadow$getZ();
                    double d3 = Mth.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    //double d4 = 0.1D;
                    entityitem.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Mth.sqrt(d3) * 0.08D, d2 * 0.1D);
                    this.level.addFreshEntity(entityitem);
                    playerEntity.level.addFreshEntity(new ExperienceOrb(playerEntity.level, playerEntity.getX(), playerEntity.getY() + 0.5D,
                            playerEntity.getZ() + 0.5D,
                            this.random.nextInt(6) + 1));
                    Item item = itemstack.getItem();

                    if (item.is(ItemTags.FISHES)) {
                        playerEntity.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }
                PhaseTracker.getCauseStackManager().popCause();

                i = Math.max(i, 1); // Sponge: Don't lower damage if we've also caught an entity
            }

            if (this.onGround) {
                i = 2;
            }

            this.shadow$remove();
            return i;
        } else {
            return 0;
        }
    }

    @Inject(method = "checkCollision", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void impl$callCollideImpactEvent(final CallbackInfo ci, final HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.MISS || ((WorldBridge) this.level).bridge$isFake()) {
            return;
        }

        if (SpongeCommonEventFactory.handleCollideImpactEvent((FishingHook) (Object) this,
                ((Projectile) this).get(Keys.SHOOTER).orElse(null), hitResult)) {
            this.shadow$remove();
            ci.cancel();
        }
    }
}
