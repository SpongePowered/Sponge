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
package org.spongepowered.common.mixin.core.world.food;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.food.FoodDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataBridge {

    // @formatter: off
    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustionLevel;
    // @formatter: on

    private Player impl$player;

    @Override
    public void bridge$setPlayer(Player player) {
        this.impl$player = player;
    }

    @Redirect(method = "setFoodLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void impl$setFoodLevel(FoodData self, int value) {
        this.foodLevel = this.fireEventAndGetValue(Keys.FOOD_LEVEL, this.foodLevel, value);
    }

    @Redirect(method = "eat(IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void impl$eatSetFoodLevel(FoodData self, int value) {
        this.foodLevel = this.fireEventAndGetValue(Keys.FOOD_LEVEL, this.foodLevel, value);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void impl$tickDrainFoodLevel(FoodData self, int value) {
        this.foodLevel = this.fireEventAndGetValue(Keys.FOOD_LEVEL, this.foodLevel, value);
    }

    @Redirect(method = "eat(IF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;saturationLevel:F", opcode = Opcodes.PUTFIELD))
    private void impl$eatSetSaturationLevel(FoodData self, float value) {
        this.saturationLevel = this.fireEventAndGetValue(Keys.SATURATION, (double) this.saturationLevel, (double) value).floatValue();
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;saturationLevel:F", opcode = Opcodes.PUTFIELD))
    private void impl$tickDrainSaturationLevel(FoodData self, float value) {
        this.saturationLevel = this.fireEventAndGetValue(Keys.SATURATION, (double) this.saturationLevel, (double) value).floatValue();
    }

    @Redirect(method = "addExhaustion", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;exhaustionLevel:F", opcode = Opcodes.PUTFIELD))
    private void impl$addExhaustion(FoodData self, float value) {
        this.exhaustionLevel = this.fireEventAndGetValue(Keys.EXHAUSTION, (double) this.exhaustionLevel, (double) value).floatValue();
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;exhaustionLevel:F", opcode = Opcodes.PUTFIELD))
    private void impl$tickDrainExhaustion(FoodData self, float value) {
        this.exhaustionLevel = this.fireEventAndGetValue(Keys.EXHAUSTION, (double) this.exhaustionLevel, (double) value).floatValue();
    }

    private <E> E fireEventAndGetValue(Key<? extends Value<E>> key, E currentValue, E value) {
        final DataTransactionResult transaction = DataTransactionResult.builder()
                .replace(Value.immutableOf(key, currentValue))
                .success(Value.immutableOf(key, value))
                .result(DataTransactionResult.Type.SUCCESS)
                .build();

        final ChangeDataHolderEvent.ValueChange
                event =
                SpongeEventFactory.createChangeDataHolderEventValueChange(PhaseTracker.getCauseStackManager().currentCause(), transaction, (DataHolder.Mutable) this.impl$player);

        Sponge.eventManager().post(event);

        if (event.isCancelled()) {
            return currentValue;
        }

        for (final Value.Immutable<?> immutableValue : event.endResult().successfulData()) {
            if (immutableValue.key() == key) {
                return (E) immutableValue.get();
            }
        }

        return currentValue;
    }
}
