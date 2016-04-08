package org.spongepowered.common.mixin.optimization;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Collection;

@Mixin(SpongeImplHooks.class)
public class MixinSpongeImplHooks_Item_Pre_Merge {

    /**
     * author gabizou - April 7th, 2016
     *
     * Iterates over the collection to find possible matches for any merges that can take place.
     *
     * @param itemStacks The collection of item stacks to add on to
     * @param itemStack The item stack being merged in
     */
    @Overwrite
    public static void addItemStackToListForSpawning(Collection<ItemStack> itemStacks, ItemStack itemStack) {
        boolean addToList = true;
        final net.minecraft.item.ItemStack addingMinecraftStack = ItemStackUtil.toNative(itemStack);
        if (addingMinecraftStack == null) {
            return;
        }
        for (ItemStack existing : itemStacks) {
            final net.minecraft.item.ItemStack existingMinecraftStack = ItemStackUtil.toNative(existing);
            if (existingMinecraftStack == null) {
                continue;
            }

            if (existing.getItem() != itemStack.getItem()) {
                continue;
            } else if (existingMinecraftStack.hasTagCompound() ^ addingMinecraftStack.hasTagCompound()) {
                continue;
            } else if (existingMinecraftStack.hasTagCompound() && !existingMinecraftStack.getTagCompound().equals(addingMinecraftStack.getTagCompound())) {
                continue;
            } else if (existingMinecraftStack.getItem() == null) {
                continue;
            } else if (existingMinecraftStack.getItem().getHasSubtypes() && existingMinecraftStack.getMetadata() != addingMinecraftStack.getMetadata()) {
                continue;
            }
            // now to actually merge the itemstacks
            final int existingStackSize = existingMinecraftStack.stackSize;
            final int addingStackSize = addingMinecraftStack.stackSize;
            final int existingMaxStackSize = existingMinecraftStack.getMaxStackSize();
            final int proposedStackSize = existingStackSize + addingStackSize;
            if (existingMaxStackSize < proposedStackSize) {
                existingMinecraftStack.stackSize = existingMaxStackSize;
                addingMinecraftStack.stackSize = proposedStackSize - existingMaxStackSize;
                addToList = true;
                // Basically, if we are overflowing the current existing stack, we can delegate to the
                // next "equals" item stack to potentially merge into that stack as well
            } else {
                existingMinecraftStack.stackSize = proposedStackSize;
                addingMinecraftStack.stackSize = 0;
                addToList = false;
                break;
            }
        }
        if (addToList) {
            if (addingMinecraftStack.getItem() != null || addingMinecraftStack.stackSize > 0) {
                itemStacks.add(itemStack);
            }
        }
    }

}
