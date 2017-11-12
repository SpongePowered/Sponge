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
package org.spongepowered.common.mixin.core.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinFoodStats;

@Mixin(FoodStats.class)
public abstract class MixinFoodStats implements IMixinFoodStats {

    @Shadow private int foodLevel;
    private Player player; // Null when on client

    @Override
    public void setPlayer(EntityPlayer player) {
        this.player = (Player) player;
    }

    @Inject(method = "addStats(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "HEAD"))
    private void onStatsItemStart(ItemFood food, ItemStack stack, CallbackInfo ci) {
        if (this.player == null) {
            return;
        }

        Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) stack).createSnapshot());
    }

    @Inject(method = "addStats(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "RETURN"))
    private void onStatsItemReturn(CallbackInfo ci) {
        if (this.player == null) {
            return;
        }

        Sponge.getCauseStackManager().removeContext(EventContextKeys.USED_ITEM);
    }

    @Redirect(method = "addStats(IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/util/FoodStats;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void onAddStatsSetFoodLevel(FoodStats this$0, int newLevel) {
        this.changeFoodLevelImpl(newLevel);
    }

    @Redirect(method = "setFoodLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/util/FoodStats;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void onSetFoodLevelMethod(FoodStats this$0, int newLevel) {
        this.changeFoodLevelImpl(newLevel);
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/util/FoodStats;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void onOnUpdateSetFoodLevel(FoodStats this$0, int newLevel) {
        this.changeFoodLevelImpl(newLevel);
    }

    private void changeFoodLevelImpl(int newLevel) {
        if (this.player == null) {
            this.foodLevel = newLevel;
            return;
        }

        Sponge.getCauseStackManager().pushCause(this.player);
        this.player.offerWithEvent(Keys.FOOD_LEVEL, newLevel, Sponge.getCauseStackManager().getCurrentCause());
        Sponge.getCauseStackManager().popCause();

        /*Sponge.getCauseStackManager().pushCause(this.player);
        ImmutableValue<Integer> oldVal = this.player.getValue(Keys.FOOD_LEVEL).get().asImmutable();
        ImmutableValue<Integer> newVal = oldVal.with(newLevel);

        DataTransactionResult result = DataTransactionResult.successReplaceResult(newVal, oldVal);
        ChangeDataHolderEvent.ValueChange event = SpongeEventFactory.createChangeDataHolderEventValueChange(Sponge.getCauseStackManager().getCurrentCause(), result, this.player);
        if (!SpongeImpl.postEvent(event)) {
            this.foodLevel = ((Optional<ImmutableValue<Integer>>) (Optional) event.getEndResult().get(DataTransactionResult.DataCategory.SUCCESSFUL, Keys.FOOD_LEVEL)).map(BaseValue::get).orElse(this.foodLevel);
        }*/

    }

    @Override
    public void setFoodLevelDirect(int foodLevel) {
        this.foodLevel = foodLevel;
    }
}
