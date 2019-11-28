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
import org.spongepowered.api.data.manipulator.mutable.entity.OcelotData;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.Ocelot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeOcelotData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSittingData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.registry.type.entity.OcelotTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import net.minecraft.entity.passive.OcelotEntity;

@Mixin(OcelotEntity.class)
public abstract class EntityOcelotMixin_API extends EntityTameableMixin_API implements Ocelot {

    @Shadow public abstract int getTameSkin();

    @Override
    public OcelotData getOcelotData() {
        return new SpongeOcelotData(OcelotTypeRegistryModule.OCELOT_IDMAP.get(this.getTameSkin()));
    }

    @Override
    public Value<OcelotType> variant() {
        return new SpongeValue<>(Keys.OCELOT_TYPE, Constants.Entity.Ocelot.DEFAULT_TYPE, OcelotTypeRegistryModule.OCELOT_IDMAP.get(this.getTameSkin()));
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(new SpongeSittingData(this.shadow$isSitting()));
        manipulators.add(getOcelotData());
    }

    @Override
    public Translation getTranslation() {
        if (shadow$isTamed()) {
            return new SpongeTranslation("entity.Cat.name");
        }
        return super.getTranslation();
    }

}
