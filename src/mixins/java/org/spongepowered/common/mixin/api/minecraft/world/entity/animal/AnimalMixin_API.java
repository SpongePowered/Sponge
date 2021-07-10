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
package org.spongepowered.common.mixin.api.minecraft.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.world.entity.AgableMobMixin_API;
import java.util.Set;

@Mixin(net.minecraft.world.entity.animal.Animal.class)
public abstract class AnimalMixin_API extends AgableMobMixin_API implements Animal {

    // @formatter: off
    @Shadow public abstract boolean shadow$isFood(net.minecraft.world.item.ItemStack var1);
    @Shadow public abstract boolean shadow$canMate(net.minecraft.world.entity.animal.Animal var1);
    @Shadow public abstract void shadow$spawnChildFromBreeding(ServerLevel var1, net.minecraft.world.entity.animal.Animal var2);
    // @formatter: on

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.breedingCooldown().asImmutable());
        values.add(this.canBreed().asImmutable());

        this.breeder().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return this.shadow$isFood((net.minecraft.world.item.ItemStack) (Object) stack);
    }

    @Override
    public boolean canBreedWith(final Animal other) {
        return this.shadow$canMate((net.minecraft.world.entity.animal.Animal) other);
    }

    @Override
    public void breedWith(final Animal animal) {
        this.shadow$spawnChildFromBreeding((ServerLevel) this.world(), (net.minecraft.world.entity.animal.Animal) animal);
    }

}
