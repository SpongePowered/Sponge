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
package org.spongepowered.neoforge.mixin.core.world.level.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.block.entity.AbstractFurnaceBlockEntityBridge;



@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin_Neo implements AbstractFurnaceBlockEntityBridge {

    // @formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow int cookingProgress;

    /*
    @Shadow private static boolean shadow$canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, NonNullList<ItemStack> slots, int maxStackSize) {
        throw new IllegalStateException("Mixin failed to shadow canBurn");
    }
    // @formatter:on

    private boolean forgeImpl$filledWaterBucket;

    // Tick up and Start
    @Redirect(method = "serverTick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/core/NonNullList;I)Z",
            ordinal = 1))
    private static boolean forgeImpl$checkIfCanSmelt(final AbstractFurnaceBlockEntity entity,
                                                     final RegistryAccess registryAccess,
                                                     final RecipeHolder<?> recipe,
                                                     final NonNullList<ItemStack> slots,
                                                     final int maxStackSize) {
        final var $this = (AbstractFurnaceBlockEntityMixin_Forge) (Object) (entity);
        if (!shadow$canBurn(registryAccess, recipe, slots, maxStackSize)) {
            return false;
        }

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(slots.get(1));

        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        if ($this.cookingProgress == 0) { // Start
            final CookingEvent.Start event = SpongeEventFactory.createCookingEventStart(cause, (FurnaceBlockEntity) entity, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe.value()), Optional.of((ResourceKey) (Object) recipe.id()));
            SpongeCommon.post(event);
            return !event.isCancelled();
        } else { // Tick up
            final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf($this.items.get(0));
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entity, cooking, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe.value()), Optional.of((ResourceKey) (Object) recipe.id()));
            SpongeCommon.post(event);
            return !event.isCancelled();
        }
    }

    // Tick down
    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private static int forgeImpl$resetCookTimeIfCancelled(final int newCookTime, final int zero, final int totalCookTime,
                                                          final Level level, final BlockPos entityPos, final BlockState state, final AbstractFurnaceBlockEntity entity) {
        final int clampedCookTime = Mth.clamp(newCookTime, zero, totalCookTime);
        final var thisEntity = (AbstractFurnaceBlockEntityMixin_Forge) (Object) entity;
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(thisEntity.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final var recipe = thisEntity.bridge$getCurrentRecipe();
        final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf(thisEntity.items.get(0));
        final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entity, cooking, Optional.of(fuel),
                recipe.map(r -> (CookingRecipe) r.value()), recipe.map(r -> (ResourceKey) (Object) r.id()));
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            return thisEntity.cookingProgress; // dont tick down
        }

        return clampedCookTime;
    }

    // Finish
    @Inject(method = "burn", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;WET_SPONGE:Lnet/minecraft/world/level/block/Block;", opcode = Opcodes.GETSTATIC)
        ))
    private void forgeImpl$captureBucketFill(final RegistryAccess $$0, final RecipeHolder<?> $$1, final NonNullList<ItemStack> $$2, final int $$3, final CallbackInfoReturnable<Boolean> cir) {
        final AbstractFurnaceBlockEntityMixin_Forge mixinSelf = MixinTargetHelper.cast(this);
        mixinSelf.forgeImpl$filledWaterBucket = true;
    }

    @Inject(
        method = "burn",
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void forgeImpl$afterSmeltItem(
        final RegistryAccess registryAccess, final RecipeHolder<?> recipe,
        final NonNullList<ItemStack> slots, final int var2,
        final CallbackInfoReturnable<Boolean> cir,
        final ItemStack itemIn, final ItemStack recipeResult, final ItemStack itemOut
    ) {
        final AbstractFurnaceBlockEntityMixin_Forge mixinSelf = MixinTargetHelper.cast(this);
        final FurnaceBlockEntity entity = (FurnaceBlockEntity) this;

        final List<SlotTransaction> transactions = new ArrayList<>();
        itemIn.grow(1);
        final ItemStackSnapshot originalSmeltItem = ItemStackUtil.snapshotOf(itemIn);
        itemIn.shrink(1);
        transactions.add(new SlotTransaction(entity.inventory().slot(0).get(), originalSmeltItem, ItemStackUtil.snapshotOf(itemIn)));

        final boolean hasFuel = !mixinSelf.forgeImpl$filledWaterBucket;
        if (mixinSelf.forgeImpl$filledWaterBucket) {
            transactions.add(new SlotTransaction(entity.inventory().slot(1).get(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(slots.get(1))));
        }
        mixinSelf.forgeImpl$filledWaterBucket = false;

        if (itemOut.isEmpty()) {
            transactions.add(new SlotTransaction(entity.inventory().slot(2).get(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(recipeResult)));
        } else if (ItemStack.isSameItemSameComponents(itemOut, recipeResult)) {
            itemOut.shrink(1);
            final ItemStackSnapshot originalResult = ItemStackUtil.snapshotOf(itemOut);
            itemOut.grow(1);
            transactions.add(new SlotTransaction(entity.inventory().slot(2).get(), originalResult, ItemStackUtil.snapshotOf(itemOut)));
        }
        final Optional<ItemStackSnapshot> fuel = hasFuel && !slots.get(1).isEmpty() ? Optional.of(ItemStackUtil.snapshotOf(slots.get(1))) : Optional.empty();
        final CookingEvent.Finish event = SpongeEventFactory.createCookingEventFinish(PhaseTracker.getCauseStackManager().currentCause(), entity,
            fuel, Optional.of((CookingRecipe) recipe.value()), Optional.of((ResourceKey) (Object) recipe.id()), Collections.unmodifiableList(transactions));
        SpongeCommon.post(event);

        for (final SlotTransaction transaction : transactions) {
            transaction.custom().ifPresent(item -> slots.set(((SlotAdapter) transaction.slot()).getOrdinal(), ItemStackUtil.fromSnapshotToNative(item)));
        }
    }*/
}
