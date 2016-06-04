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
package org.spongepowered.common.mixin.optimization;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

// This mixin needs to come before common's MixinEntity so that the event hook can be maintained
// for checking entity pre-construct events
@Mixin(value = Entity.class, priority = 1002)
public abstract class MixinEntity_Item_Pre_Merge implements org.spongepowered.api.entity.Entity {

    @Shadow public World worldObj;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;

    @Shadow public abstract UUID getUniqueID();

    @Nullable private ItemStackSnapshot custom;

    /**
     * @author gabizou
     * @reason Track the items for pre-merging.
     *
     * Author's note: There is a redirect that will throw a pre-event if and only if
     * the item is already handled.
     */
    @Overwrite
    @Nullable
    public EntityItem entityDropItem(net.minecraft.item.ItemStack itemStackIn, float offsetY) {
        // Gotta stick with the client side handling things
        if (this.worldObj.isRemote) {
            if (itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
                EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double)offsetY, this.posZ, itemStackIn);
                entityitem.setDefaultPickupDelay();
                this.worldObj.spawnEntityInWorld(entityitem);
                return entityitem;
            } else {
                return null;
            }
        }

        // Now the real fun begins.
        final net.minecraft.item.ItemStack item;
        if (itemStackIn.getItem() != null) {
            // FIRST we want to throw the DropItemEvent.PRE
            ItemStackSnapshot snapshot = ((ItemStack) itemStackIn).createSnapshot();
            List<ItemStackSnapshot> original = new ArrayList<>();
            original.add(snapshot);
            DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(Cause.of(NamedCause.source(this)),
                    ImmutableList.of(snapshot), original);
            if (dropEvent.isCancelled()) {
                return null;
            }

            // SECOND throw the ConstructEntityEvent
            Transform<org.spongepowered.api.world.World>
                    suggested = new Transform<>(this.getWorld(), new Vector3d(this.posX, this.posY + (double) offsetY, this.posZ));
            SpawnCause cause = EntitySpawnCause.builder().entity(this).type(SpawnTypes.DROPPED_ITEM).build();
            ConstructEntityEvent.Pre event = SpongeEventFactory
                    .createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), EntityTypes.ITEM, suggested);
            SpongeImpl.postEvent(event);
            item = event.isCancelled() ? null : ItemStackUtil.toNative(dropEvent.getDroppedItems().get(0).createStack());
        } else {
            return null;
        }
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) this.worldObj;
        final PhaseData peek = mixinWorldServer.getCauseTracker().getStack().peek();
        final IPhaseState currentState = peek.getState();
        final PhaseContext phaseContext = peek.getContext();
        if (item != null && item.stackSize != 0 && item.getItem() != null) {
            // THIRD - Check if we can pre-merge the itemstacks - This is a combination of items that have a normal
            // offset of 0F, and if it is a normal offset, that means the mod that is calling this method is not expecting
            // an entity back.
            if (offsetY == 0.0F && this.worldObj instanceof WorldServer) {
//
//                if (currentState.tracksEntitySpecificDrops()) {
//                    final Multimap<UUID, ItemDropData> multimap = phaseContext.getCapturedEntityDropSupplier().get();
//                    final Collection<ItemDropData> itemStacks = multimap.get(this.getUniqueID());
//                    SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemStackUtil.fromNative(item));
//                    return null;
//                } else {
//                    final List<ItemStack> itemStacks = phaseContext.getCapturedItemStackSupplier().get();
//                    SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemStackUtil.fromNative(item));
//                    return null;
//                }
            }
            // FOURTH - Spawn the entity finally, this time to either capture or pass through
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double) offsetY, this.posZ, itemStackIn);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (currentState.getPhase().doesCaptureEntityDrops(currentState)) {
                if (currentState.tracksEntitySpecificDrops()) {
                    // We are capturing per entity drop
                    phaseContext.getCapturedEntityItemDropSupplier().get().put(this.getUniqueID(), entityitem);
                } else {
                    // We are adding to a general list - usually for EntityPhase.State.DEATH
                    phaseContext.getCapturedItemsSupplier().get().add(entityitem);
                }
                // Return the item, even if it wasn't spawned in the world.
                return entityitem;
            }
            // FINALLY - Spawn the entity in the world if all else didn't fail
            this.worldObj.spawnEntityInWorld(entityitem);
            return entityitem;
        }
        return null;
    }

}
