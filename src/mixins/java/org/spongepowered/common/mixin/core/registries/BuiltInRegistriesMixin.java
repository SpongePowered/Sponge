package org.spongepowered.common.mixin.core.registries;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.item.recipe.cooking.SpongeRecipeSerializers;
import org.spongepowered.common.launch.Launch;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {

    // We hook in here if we extend existing vanilla BuiltInRegistries with our own
    // This methods should then be called during bootstrap

    @Shadow @Final private static Map<ResourceLocation, Supplier<?>> LOADERS;

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

    @Inject(method = "bootStrap", at = @At(value = "HEAD"))
    private static void impl$beforeCreateContents(CallbackInfo ci)
    {
        Launch.instance().lifecycle().establishEarlyGlobalRegistries();
    }

}
