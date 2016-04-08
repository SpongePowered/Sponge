package org.spongepowered.common.mixin.optimization;

import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;

// This mixin needs to come before common's MixinEntity so that the event hook can be maintained
// for checking entity pre-construct events
@Mixin(value = Entity.class, priority = 998)
public abstract class MixinEntity_Item_Pre_Merge {

    @Shadow public World worldObj;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;

    @Shadow public abstract UUID getUniqueID();

    @Overwrite
    @Nullable
    public EntityItem entityDropItem(net.minecraft.item.ItemStack itemStackIn, float offsetY) {
        if (itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
            // Sponge Start - track the items for pre-merging
            if (offsetY == 0.0F && this.worldObj instanceof WorldServer) {
                final IMixinWorldServer mixinWorld = (IMixinWorldServer) this.worldObj;
                final PhaseData currentPhase = mixinWorld.getCauseTracker().getStack().peek();
                final IPhaseState currentState = currentPhase.getState();
                if (currentState.tracksEntitySpecificDrops()) {
                    final PhaseContext context = currentPhase.getContext();
                    final Multimap<UUID, ItemStack> multimap = context.getCapturedEntityDropSupplier()
                            .orElseThrow(PhaseUtil.throwWithContext("Was intending to capture entity item drops, but couldn't find multimap!",
                                    currentPhase.getContext()))
                            .get();
                    final Collection<ItemStack> itemStacks = multimap.get(this.getUniqueID());
                    SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemStackUtil.fromNative(itemStackIn));
                    return null;
                }
            }
            // Sponge End
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double) offsetY, this.posZ, itemStackIn);
            entityitem.setDefaultPickupDelay();
            this.worldObj.spawnEntityInWorld(entityitem);
            return entityitem;
        } else {
            return null;
        }
    }

}
