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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.ElderData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.monster.Guardian;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeElderData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.List;
import java.util.Optional;

@Mixin(EntityGuardian.class)
public abstract class MixinEntityGuardian extends MixinEntityMob implements Guardian {

    @Shadow @Final private static DataParameter<Integer> TARGET_ENTITY;
    @Shadow public abstract boolean isElder();
    @Shadow private void setTargetedEntity(int entityId) { }

    @Override
    public ElderData getElderData() {
        return new SpongeElderData(isElder());
    }

    @Override
    public Value<Boolean> elder() {
        return new SpongeValue<>(Keys.ELDER_GUARDIAN, false, this.isElder());
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getElderData());
    }

    @Override
    public Optional<Living> getBeamTarget() {
        return Optional.ofNullable((Living) this.worldObj.getEntityByID(this.dataManager.get(TARGET_ENTITY)));
    }

    @Override
    public void setBeamTarget(Living entity) {
        if (entity == null) {
            this.setTargetedEntity(0);
        } else {
            this.setTargetedEntity(((EntityLivingBase) entity).getEntityId());
        }
    }
}
