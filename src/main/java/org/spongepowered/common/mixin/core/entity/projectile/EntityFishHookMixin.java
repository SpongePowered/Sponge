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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.mixin.core.entity.EntityMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(EntityFishHook.class)
public abstract class EntityFishHookMixin extends EntityMixin {

    @Shadow @Nullable private EntityPlayer angler;
    @Shadow @Nullable public net.minecraft.entity.Entity caughtEntity;

    @Shadow protected abstract void bringInHookedEntity();

    @Shadow private int ticksCatchable;
    @Shadow private int luck;
    @Shadow private boolean inGround;

    @Nullable private ProjectileSource projectileSource;

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void onSetHookedEntity(CallbackInfo ci) {
        if (SpongeImpl.postEvent(SpongeEventFactory.createFishingEventHookEntity(Sponge.getCauseStackManager().getCurrentCause(), ((FishHook) this),
                (Entity) this.caughtEntity))) {
            this.caughtEntity = null;
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
    public int handleHookRetraction() {
        if (!this.world.field_72995_K && this.angler != null) {
            int i = 0;

            // Sponge start
            List<Transaction<ItemStackSnapshot>> transactions;
            if (this.ticksCatchable > 0) {
                // Moved from below
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.world);
                lootcontext$builder.func_186469_a(this.luck + this.angler.func_184817_da());
                transactions = this.world.func_184146_ak().func_186521_a(LootTableList.field_186387_al)
                        .func_186462_a(this.rand, lootcontext$builder.func_186471_a())
                        .stream()
                        .map(s -> {
                            ItemStackSnapshot snapshot = ((org.spongepowered.api.item.inventory.ItemStack) s).createSnapshot();
                            return new Transaction<>(snapshot, snapshot);
                        })
                        .collect(Collectors.toList());
            } else {
                transactions = new ArrayList<>();
            }
            Sponge.getCauseStackManager().pushCause(this.angler);

            if (SpongeImpl.postEvent(SpongeEventFactory.createFishingEventStop(Sponge.getCauseStackManager().getCurrentCause(), ((FishHook) this), transactions))) {
                // Event is cancelled
                return -1;
            }

            if (this.caughtEntity != null) {
                this.bringInHookedEntity();
                this.world.func_72960_a((net.minecraft.entity.Entity) (Object) this, (byte) 31);
                i = this.caughtEntity instanceof EntityItem ? 3 : 5;
            } // Sponge: Remove else

            // Sponge start - Moved up to event call
            if (!transactions.isEmpty()) { // Sponge: Check if we have any transactions instead
                //LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.world);
                //lootcontext$builder.withLuck((float) this.field_191518_aw + this.angler.getLuck());

                // Use transactions
                for (Transaction<ItemStackSnapshot> transaction : transactions) {
                    if (!transaction.isValid()) {
                        continue;
                    }
                    ItemStack itemstack = (ItemStack) transaction.getFinal().createStack();
                    // Sponge end

                    EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
                    double d0 = this.angler.field_70165_t - this.posX;
                    double d1 = this.angler.field_70163_u - this.posY;
                    double d2 = this.angler.field_70161_v - this.posZ;
                    double d3 = MathHelper.func_76133_a(d0 * d0 + d1 * d1 + d2 * d2);
                    //double d4 = 0.1D;
                    entityitem.field_70159_w = d0 * 0.1D;
                    entityitem.field_70181_x = d1 * 0.1D + MathHelper.func_76133_a(d3) * 0.08D;
                    entityitem.field_70179_y = d2 * 0.1D;
                    this.world.func_72838_d(entityitem);
                    this.angler.field_70170_p.func_72838_d(new EntityXPOrb(this.angler.field_70170_p, this.angler.field_70165_t, this.angler.field_70163_u + 0.5D, this.angler.field_70161_v + 0.5D,
                            this.rand.nextInt(6) + 1));
                    Item item = itemstack.func_77973_b();

                    if (item == Items.field_151115_aP || item == Items.field_179566_aV) {
                        this.angler.func_71064_a(StatList.field_188071_E, 1);
                    }
                }
                Sponge.getCauseStackManager().popCause();

                i = Math.max(i, 1); // Sponge: Don't lower damage if we've also caught an entity
            }

            if (this.inGround) {
                i = 2;
            }

            this.setDead();
            return i;
        } else {
            return 0;
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, ((FishHook) this));
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, ((FishHook) this).getShooter(), this.angler);
    }
}
