package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(SlotCrafting.class)
public abstract class MixinSlotCrafting extends Slot {

    @Shadow @Final private EntityPlayer thePlayer;

    public MixinSlotCrafting(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void putStack(@Nullable ItemStack stack) {
        super.putStack(stack);
        if (this.thePlayer instanceof EntityPlayerMP) {
            ((EntityPlayerMP) this.thePlayer).connection.sendPacket(new SPacketSetSlot(0, 0, stack));
        }
    }
}
