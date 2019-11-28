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
package org.spongepowered.common.mixin.api.mcp.entity.passive;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.EntityVillagerBridge;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.api.mcp.entity.EntityAgeableMixin_API;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(VillagerEntity.class)
@Implements(@Interface(iface = Villager.class, prefix = "apiVillager$"))
public abstract class EntityVillagerMixin_API extends EntityAgeableMixin_API implements Villager, CarriedInventory<Villager> {

    @Shadow public abstract void setProfession(int professionId);
    @Shadow public abstract void setCustomer(PlayerEntity player);
    @Shadow public abstract boolean shadow$isTrading();
    @Shadow @Nullable public abstract PlayerEntity shadow$getCustomer();


    @Intrinsic
    public boolean apiVillager$isTrading() {
        return this.shadow$isTrading();
    }

    @Override
    public Optional<Humanoid> getCustomer() {
        return Optional.ofNullable((Humanoid) this.shadow$getCustomer());
    }

    @Override
    public void setCustomer(@Nullable Humanoid humanoid) {
        this.setCustomer((PlayerEntity) humanoid);
    }

    // Data delegated methods


    @Override
    public CareerData getCareerData() {
        return new SpongeCareerData(((EntityVillagerBridge) this).bridge$getCareer());
    }

    @Override
    public Value<Career> career() {
        return new SpongeValue<>(Keys.CAREER, Constants.Catalog.CAREER_DEFAULT, ((EntityVillagerBridge) this).bridge$getCareer());
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getCareerData());
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return this;
    }

    @Override
    public Optional<Villager> getCarrier() {
        return Optional.of(this);
    }
}
