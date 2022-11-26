package org.spongepowered.common.mixin.core.registries;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.item.recipe.cooking.SpongeRecipeSerializers;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {

    @Inject(method = "lambda$static$17", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/RecipeSerializer;SHAPELESS_RECIPE:Lnet/minecraft/world/item/crafting/RecipeSerializer;"))
    private static void impl$staticInitSpongeRecipeSerializers(final Registry $$0, final CallbackInfoReturnable<RecipeSerializer> cir)
    {
        final var serializer = SpongeRecipeSerializers.SPONGE_BLASTING;
    }

    @Inject(method = "lambda$static$6", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EntityType;PIG:Lnet/minecraft/world/entity/EntityType;"))
    private static void impl$staticInitSpongeEntityTypes(final Registry $$0, final CallbackInfoReturnable<EntityType> cir)
    {
        final var type = HumanEntity.TYPE;
    }

}
