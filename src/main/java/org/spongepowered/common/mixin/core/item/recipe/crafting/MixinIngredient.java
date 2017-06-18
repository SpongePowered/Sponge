package org.spongepowered.common.mixin.core.item.recipe.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(Ingredient.class)
@Implements(@Interface(iface = org.spongepowered.api.item.recipe.crafting.Ingredient.class, prefix = "ingredient$"))
public abstract class MixinIngredient {

    @Shadow @Final protected ItemStack[] matchingStacks;
    @Shadow public abstract boolean apply(ItemStack p_apply_1_);

    @Intrinsic
    public List<org.spongepowered.api.item.inventory.ItemStackSnapshot> ingredient$displayedItems() {
        return Arrays.stream(matchingStacks).map(ItemStackUtil::snapshotOf).collect(Collectors.toList());
    }

    @Intrinsic
    public boolean ingredient$test(org.spongepowered.api.item.inventory.ItemStack itemStack) {
        return this.apply(ItemStackUtil.toNative(itemStack));
    }
}
