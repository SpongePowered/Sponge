package org.spongepowered.neoforge.accessor.world.level.block.entity;


import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedInvokerError;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor_Neo {

    @Invoker("canBurn")
    static boolean invoker$canBurn(final RegistryAccess registryAccess, @Nullable final RecipeHolder<?> var0, final SingleRecipeInput input, final NonNullList<ItemStack> var1, final int var2) {
        throw new UntransformedInvokerError();
    }
}

