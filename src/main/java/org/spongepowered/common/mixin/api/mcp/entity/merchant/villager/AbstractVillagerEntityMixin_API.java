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
package org.spongepowered.common.mixin.api.mcp.entity.merchant.villager;

import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.trader.Trader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.mcp.entity.AgeableEntityMixin_API;

import java.util.Optional;
import java.util.Set;

@Mixin(AbstractVillagerEntity.class)
public abstract class AbstractVillagerEntityMixin_API extends AgeableEntityMixin_API implements Trader {

    @Shadow public abstract void shadow$setCustomer(PlayerEntity player);
    @Shadow public abstract PlayerEntity shadow$getCustomer();

    public Optional<Humanoid> getCustomer() {
        return Optional.ofNullable((Humanoid) this.shadow$getCustomer());
    }

    public void setCustomer(@Nullable Humanoid humanoid) {
        this.shadow$setCustomer((PlayerEntity) humanoid);
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Merchant
        values.add(this.tradeOffers().asImmutable());

        values.add(this.trading().asImmutable());

        return values;
    }

}
