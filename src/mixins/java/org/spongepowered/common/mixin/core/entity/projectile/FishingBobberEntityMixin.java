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
package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
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
import org.spongepowered.common.mixin.core.entity.EntityMixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends EntityMixin {

    // @formatter:off
    @Shadow @Nullable private net.minecraft.entity.Entity hookedIn;
    @Shadow private int nibble;
    @Shadow @Final private int luck;

    @Shadow @Nullable public abstract PlayerEntity shadow$getPlayerOwner();
    // @formatter:on

    @Shadow protected abstract void bringInHookedEntity();


    @Nullable private ProjectileSource impl$projectileSource;

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void onSetHookedEntity(CallbackInfo ci) {
        if (SpongeCommon
            .postEvent(SpongeEventFactory.createFishingEventHookEntity(PhaseTracker.getCauseStackManager().getCurrentCause(), (Entity) this.hookedIn, (FishingBobber) this))) {
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
        final PlayerEntity playerEntity = this.shadow$getPlayerOwner();

        if (!this.level.isClientSide && playerEntity != null) {
            int i = 0;

            // Sponge start
            final List<Transaction<ItemStackSnapshot>> transactions;
            if (this.nibble > 0) {
                // Moved from below
                final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld)this.level)
                        .withParameter(LootParameters.ORIGIN, this.shadow$position())
                        .withParameter(LootParameters.TOOL, stack)
                        .withParameter(LootParameters.THIS_ENTITY, (net.minecraft.entity.Entity) (Object) this)
                        .withRandom(this.random)
                        .withLuck((float)this.luck + playerEntity.getLuck());
                final LootTable lootTable = this.level.getServer().getLootTables().get(LootTables.FISHING);
                final List<ItemStack> list = lootTable.getRandomItems(lootcontext$builder.create(LootParameterSets.FISHING));
                transactions = list.stream().map(ItemStackUtil::snapshotOf)
                        .map(snapshot -> new Transaction<>(snapshot, snapshot))
                        .collect(Collectors.toList());
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, stack, (FishingBobberEntity) (Object) this, list);
            } else {
                transactions = new ArrayList<>();
            }
            PhaseTracker.getCauseStackManager().pushCause(playerEntity);

            if (SpongeCommon.postEvent(SpongeEventFactory.createFishingEventStop(PhaseTracker.getCauseStackManager().getCurrentCause(), ((FishingBobber) this), transactions))) {
                // Event is cancelled
                return 0;
            }
            // Sponge end

            if (this.hookedIn != null) {
                this.bringInHookedEntity();
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) playerEntity, stack, (FishingBobberEntity) (Object) this, Collections.emptyList());
                this.level.broadcastEntityEvent((net.minecraft.entity.Entity) (Object) this, (byte) 31);
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
                    ItemStack itemstack = (ItemStack) (Object) transaction.getFinal().createStack();
                    // Sponge end

                    ItemEntity entityitem = new ItemEntity(this.level, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), itemstack);
                    double d0 = playerEntity.getX() - this.shadow$getX();
                    double d1 = playerEntity.getY() - this.shadow$getY();
                    double d2 = playerEntity.getZ() - this.shadow$getZ();
                    double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    //double d4 = 0.1D;
                    entityitem.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + MathHelper.sqrt(d3) * 0.08D, d2 * 0.1D);
                    this.level.addFreshEntity(entityitem);
                    playerEntity.level.addFreshEntity(new ExperienceOrbEntity(playerEntity.level, playerEntity.getX(), playerEntity.getY() + 0.5D,
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;onHit(Lnet/minecraft/util/math/RayTraceResult;)V"))
    private void impl$callCollideImpactEvent(final CallbackInfo ci, final RayTraceResult hitResult) {
        if (hitResult.getType() == RayTraceResult.Type.MISS || ((WorldBridge) this.level).bridge$isFake()) {
            return;
        }

        if (SpongeCommonEventFactory.handleCollideImpactEvent((FishingBobberEntity) (Object) this,
                ((Projectile) this).get(Keys.SHOOTER).orElse(null), hitResult)) {
            this.shadow$remove();
            ci.cancel();
        }
    }
}
