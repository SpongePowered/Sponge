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
package org.spongepowered.common.mixin.core.item.crafting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingResult;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.item.recipe.smelting.MatchSmeltingVanillaItemStack;
import org.spongepowered.common.item.recipe.smelting.SpongeSmeltingRecipe;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(FurnaceRecipes.class)
public abstract class FurnaceRecipesMixin implements SpongeAdditionalCatalogRegistryModule<SmeltingRecipe> {

    @Shadow @Final private Map<ItemStack, ItemStack> smeltingList;
    @Shadow @Final private Map<ItemStack, Float> experienceList;

    private final Map<String, SmeltingRecipe> recipesById = new HashMap<>();

    private final List<SmeltingRecipe> customRecipes = new ArrayList<>();
    private final List<SmeltingRecipe> notCustomRecipes = new ArrayList<>();
    // No IdentityHashBiMap implementation exists
    private final Map<ItemStack, SmeltingRecipe> nativeIngredientToCustomRecipe = new IdentityHashMap<>();

    @Shadow private boolean compareItemStacks(ItemStack stack1, ItemStack stack2) {
        throw new IllegalStateException("unreachable");
    }

    @Inject(method = "getSmeltingResult", at = @At("RETURN"), cancellable = true)
    private void spongeImpl$onGetSmeltingResult(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStackSnapshot ingredient = ItemStackUtil.snapshotOf(stack);
        Optional<SmeltingResult> result = getCustomResult(ingredient);

        if (result.isPresent()) {
            ItemStack nativeResult = ItemStackUtil.fromSnapshotToNative(result.get().getResult());
            cir.setReturnValue(nativeResult);
        } else {
            for (ItemStack nativeIngredient : this.nativeIngredientToCustomRecipe.keySet()) {
                if (this.compareItemStacks(nativeIngredient, stack)) {
                    cir.setReturnValue(ItemStack.field_190927_a);
                    return;
                }
            }
        }
    }

    @Inject(method = "getSmeltingExperience", at = @At("RETURN"), cancellable = true)
    private void spongeImpl$onGetSmeltingExperience(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        ItemStackSnapshot ingredient = ItemStackUtil.snapshotOf(stack);
        Optional<SmeltingResult> result = getCustomResult(ingredient);

        if (result.isPresent()) {
            float nativeResult = (float) result.get().getExperience();

            cir.setReturnValue(nativeResult);
        }
    }

    private Optional<SmeltingResult> getCustomResult(ItemStackSnapshot ingredient) {
        checkNotNull(ingredient, "ingredient");

        for (SmeltingRecipe recipe : this.customRecipes) {
            Optional<SmeltingResult> result = recipe.getResult(ingredient);

            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }


    @Inject(method = "addSmeltingRecipe(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;F)V", at = @At("RETURN"))
    private void spongeImpl$onAddSmeltingRecipe(ItemStack input, ItemStack stack, float experience, CallbackInfo ci) {
        final ItemStackSnapshot result = ItemStackUtil.snapshotOf(stack);
        final ItemStackSnapshot ingredient = ItemStackUtil.snapshotOf(input);

        final PluginContainer activeModContainer = SpongeImplHooks.getActiveModContainer();
        final String namespace = activeModContainer == null ? "minecraft" : activeModContainer.getId();

        // Some mods are doing things wrongly which causes their recipes to be broken,
        // just log the problem and don't register them. Crashing the server is not an
        // option either.
        if (result.isEmpty() || ingredient.isEmpty()) {
            SpongeImpl.getLogger().error("Invalid smelting recipe registration!\n" +
                            "The mod \"" + namespace + "\" is registering a smelting recipe wrongly which causes the resulting recipe to be " +
                            "invalid.\nA common cause which makes smelting recipes invalid occurs when registering those recipes before " +
                            "the used blocks are\nregistered, it's recommend to register them when the items are finished registering.\n",
                    new IllegalStateException("Invalid smelting recipe registered by the mod \"" + namespace + "\"!"));
            return;
        }

        final Predicate<ItemStackSnapshot> ingredientPredicate = new MatchSmeltingVanillaItemStack(ingredient);

        String resultId = result.getType().getId();
        int index = resultId.indexOf(':');
        resultId = index == -1 ? resultId : resultId.substring(index + 1);

        String ingredientId = ingredient.getType().getId();
        index = ingredientId.indexOf(':');
        ingredientId = index == -1 ? ingredientId : ingredientId.substring(index + 1);

        final String name = resultId + "_to_" + ingredientId;
        final String id = namespace + ':' + name;

        final SmeltingRecipe recipe = new SpongeSmeltingRecipe(id, new FixedTranslation(name),
                ingredient, ingredientPredicate, experience, result);
        this.recipesById.put(id, recipe);
        this.notCustomRecipes.add(recipe);
    }

    @Override
    public void registerAdditionalCatalog(SmeltingRecipe recipe) {
        checkNotNull(recipe, "recipe");
        checkArgument(!this.recipesById.containsKey(recipe.getId()),
                "Duplicate id: %s", recipe.getId());

        final ItemStackSnapshot exemplaryIngredient = recipe.getExemplaryIngredient();
        final ItemStack nativeExemplaryIngredient = ItemStackUtil.fromSnapshotToNative(exemplaryIngredient);
        final ItemStack nativeExemplaryResult = ItemStackUtil.fromSnapshotToNative(recipe.getExemplaryResult());
        final float nativeExemplaryExperience = (float) recipe.getResult(exemplaryIngredient)
                .orElseThrow(() -> new IllegalStateException("Could not get the result for the exemplary ingredient.")).getExperience();

        this.smeltingList.put(nativeExemplaryIngredient, nativeExemplaryResult);
        this.experienceList.put(nativeExemplaryResult, nativeExemplaryExperience);
        this.nativeIngredientToCustomRecipe.put(nativeExemplaryIngredient, recipe);
        this.customRecipes.add(recipe);
        this.recipesById.put(recipe.getId(), recipe);
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }
}
