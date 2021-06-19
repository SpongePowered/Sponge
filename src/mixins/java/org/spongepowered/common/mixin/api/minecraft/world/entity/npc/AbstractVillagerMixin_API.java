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
package org.spongepowered.common.mixin.api.minecraft.world.entity.npc;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.trader.Trader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.world.entity.AgableMobMixin_API;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin_API extends AgableMobMixin_API implements Trader {

    // @formatter:off
    @Shadow public abstract void shadow$setTradingPlayer(Player player);
    @Shadow public abstract Player shadow$getTradingPlayer();
    // @formatter:on

    @Override
    public Optional<Humanoid> customer() {
        return Optional.ofNullable((Humanoid) this.shadow$getTradingPlayer());
    }

    @Override
    public void setCustomer(@Nullable Humanoid humanoid) {
        this.shadow$setTradingPlayer((Player) humanoid);
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
