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
package org.spongepowered.common.mixin.core.item.recipe.smelting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipeRegistry;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(FurnaceRecipe.class)
public abstract class MixinFurnaceRecipes implements SmeltingRecipeRegistry, SpongeAdditionalCatalogRegistryModule<SmeltingRecipe> {


    private final Map<CatalogKey, SmeltingRecipe> recipesByKey = new HashMap<>();

    private final List<SmeltingRecipe> customRecipes = new ArrayList<>();
    // No IdentityHashBiMap implementation exists
    private final Map<ItemStack, SmeltingRecipe> nativeIngredientToCustomRecipe = new IdentityHashMap<>();

    @Shadow private boolean compareItemStacks(ItemStack stack1, ItemStack stack2) {
        throw new IllegalStateException("unreachable");
    }

    @Shadow @Final private Ingredient input;

    @Inject(method = "getCraftingResult", at = @At("RETURN"), cancellable = true)
    private void onGetSmeltingResult(CallbackInfoReturnable<ItemStack> cir) {
        Optional<SmeltingResult> result = getCustomResult(this.input);

        if (result.isPresent()) {
            ItemStack nativeResult = ItemStackUtil.fromSnapshotToNative(result.get().getResult());
            cir.setReturnValue(nativeResult);
        } else {
            for (ItemStack nativeIngredient : this.nativeIngredientToCustomRecipe.keySet()) {
                // displayedItems is exact for Minecraft ingredients
                for (ItemStackSnapshot snapshot: ((org.spongepowered.api.item.recipe.crafting.Ingredient) (Object) this.input).displayedItems())
                    if (this.compareItemStacks(nativeIngredient, ItemStackUtil.toNative(snapshot.createStack()))) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
            }
        }
    }

    @Inject(method = "getExperience", at = @At("RETURN"), cancellable = true)
    private void onGetSmeltingExperience(CallbackInfoReturnable<Float> cir) {
        Optional<SmeltingResult> result = getCustomResult(this.input);

        if (result.isPresent()) {
            float nativeResult = (float) result.get().getExperience();

            cir.setReturnValue(nativeResult);
        }
    }

    private Optional<SmeltingResult> getCustomResult(Ingredient ingredient) {
        checkNotNull(ingredient, "ingredient");

        for (SmeltingRecipe recipe : this.customRecipes) {
            for (ItemStackSnapshot snapshot: ((org.spongepowered.api.item.recipe.crafting.Ingredient) (Object) ingredient).displayedItems()) {
                Optional<SmeltingResult> result = recipe.getResult(snapshot);

                if (result.isPresent()) {
                    return result;
                }
            }

        }

        return Optional.empty();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onAddSmeltingRecipe(ResourceLocation p_i48715_1_, String p_i48715_2_, Ingredient p_i48715_3_, ItemStack output, float p_i48715_5_, int p_i48715_6_, CallbackInfo ci) {
        final ItemStackSnapshot result = ItemStackUtil.snapshotOf(output);
        final ItemStackSnapshot ingredient = ItemStackUtil.snapshotOf(input);

        final PluginContainer activeModContainer = SpongeImplHooks.getActiveModContainer();
        final String namespace = activeModContainer == null ? CatalogKey.MINECRAFT_NAMESPACE : activeModContainer.getId();

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


        final CatalogKey key = CatalogKey.of(namespace,
                ingredient.getType().getKey().getValue() + "_to_" + result.getType().getKey().getValue());

        final SmeltingRecipe recipe = new SpongeSmeltingRecipe(key, new FixedTranslation(key.getValue()),
                ingredient, ingredientPredicate, experience, result);
        this.recipesByKey.put(key, recipe);
    }

    @Override
    public Optional<SmeltingRecipe> findMatchingRecipe(ItemStackSnapshot ingredient) {
        checkNotNull(ingredient, "ingredient");

        for (SmeltingRecipe customRecipe : this.recipesByKey.values()) {
            if (customRecipe.isValid(ingredient)) {
                return Optional.of(customRecipe);
            }
        }

        return Optional.empty();
    }

    @Override
    public void registerAdditionalCatalog(SmeltingRecipe recipe) {
        checkNotNull(recipe, "recipe");
        checkArgument(!this.recipesByKey.containsKey(recipe.getKey()),
                "Duplicate key: %s", recipe.getKey());

        final ItemStackSnapshot exemplaryIngredient = recipe.getExemplaryIngredient();
        final ItemStack nativeExemplaryIngredient = ItemStackUtil.fromSnapshotToNative(exemplaryIngredient);
        final ItemStack nativeExemplaryResult = ItemStackUtil.fromSnapshotToNative(recipe.getExemplaryResult());
        final float nativeExemplaryExperience = (float) recipe.getResult(exemplaryIngredient)
                .orElseThrow(() -> new IllegalStateException("Could not get the result for the exemplary ingredient.")).getExperience();

        this.smeltingList.put(nativeExemplaryIngredient, nativeExemplaryResult);
        this.experienceList.put(nativeExemplaryResult, nativeExemplaryExperience);
        this.nativeIngredientToCustomRecipe.put(nativeExemplaryIngredient, recipe);
        this.customRecipes.add(recipe);
        this.recipesByKey.put(recipe.getKey(), recipe);
    }

    @Override
    public Optional<SmeltingRecipe> get(CatalogKey key) {
        checkNotNull(key, "key");
        return Optional.ofNullable(this.recipesByKey.get(key));
    }

    @Override
    public Collection<SmeltingRecipe> getAll() {
        return ImmutableList.copyOf(this.recipesByKey.values());
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }
}
