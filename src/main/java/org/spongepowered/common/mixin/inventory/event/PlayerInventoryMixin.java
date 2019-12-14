package org.spongepowered.common.mixin.inventory.event;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public abstract class PlayerInventoryMixin implements TrackedInventoryBridge {

    @Shadow public static int shadow$getHotbarSize() {
        throw new AbstractMethodError("Shadow");
    }
    @Shadow public abstract ItemStack shadow$getStackInSlot(int index);
    @Shadow protected abstract int shadow$addResource(int p_191973_1_, ItemStack p_191973_2_);


    private Slot impl$getSpongeSlotByIndex(int index) {
        if (index < shadow$getHotbarSize()) {
            return ((PlayerInventory) this).getPrimary().getHotbar().getSlot(index).get();
        }
        index -= shadow$getHotbarSize();
        return ((PlayerInventory) this).getPrimary().getStorage().getSlot(index).get();
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void impl$ifCaptureDoTransactions(final int index, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        if (this.bridge$capturingInventory()) {
            // Capture "damaged" items picked up
            final Slot slot = this.impl$getSpongeSlotByIndex(index);
            this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(stack)));
        }
    }

    @Redirect(method = "storePartialItemStack", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/player/PlayerInventory;addResource(ILnet/minecraft/item/ItemStack;)I"))
    private int impl$ifCaptureDoTransactions(final net.minecraft.entity.player.PlayerInventory inv, final int index, final ItemStack stack) {
        if (this.bridge$capturingInventory()) {
            // Capture items getting picked up
            final Slot slot = index == 40 ? ((PlayerInventory) this).getOffhand() : this.impl$getSpongeSlotByIndex(index);
            final ItemStackSnapshot original = ItemStackUtil.snapshotOf(this.shadow$getStackInSlot(index));
            final int result = this.shadow$addResource(index, stack);
            final ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(this.shadow$getStackInSlot(index));
            this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, original, replacement));
            return result;
        }
        return this.shadow$addResource(index, stack);

    }
}
