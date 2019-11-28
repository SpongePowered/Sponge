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
package org.spongepowered.common.mixin.api.mcp.entity.effect;

import com.google.common.collect.Lists;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpirableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Collection;
import java.util.List;
import net.minecraft.entity.effect.LightningBoltEntity;

@Mixin(LightningBoltEntity.class)
public abstract class EntityLightningBoltMixin_API extends EntityWeatherEffectMixin_API implements Lightning {

    @Shadow private int lightningState;

    private final List<Entity> api$struckEntities = Lists.newArrayList();
    private final List<Transaction<BlockSnapshot>> api$struckBlocks = Lists.newArrayList();
    private boolean api$effect = false;

    @Override
    public boolean isEffect() {
        return this.api$effect;
    }

    @Override
    public void setEffect(boolean effect) {
        this.api$effect = effect;
        if (effect) {
            this.api$struckBlocks.clear();
            this.api$struckEntities.clear();
        }
    }

    @Override
    public ExpirableData getExpiringData() {
        return new SpongeExpirableData(this.lightningState, 2);
    }

    @Override
    public MutableBoundedValue<Integer> expireTicks() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPIRATION_TICKS)
                .minimum((int) Short.MIN_VALUE)
                .maximum(2)
                .defaultValue(2)
                .actualValue(this.lightningState)
                .build();
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getExpiringData());
    }
}
