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
package org.spongepowered.common.registry.type;

import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.block.BlastResistanceProperty;
import org.spongepowered.api.data.property.block.GravityAffectedProperty;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.HardnessProperty;
import org.spongepowered.api.data.property.block.HeldItemProperty;
import org.spongepowered.api.data.property.block.IndirectlyPoweredProperty;
import org.spongepowered.api.data.property.block.LightEmissionProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.data.property.block.StatisticsTrackedProperty;
import org.spongepowered.api.data.property.block.TemperatureProperty;
import org.spongepowered.api.data.property.block.UnbreakableProperty;
import org.spongepowered.api.data.property.entity.EyeHeightProperty;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.data.property.item.ApplicableEffectProperty;
import org.spongepowered.api.data.property.item.BurningFuelProperty;
import org.spongepowered.api.data.property.item.DamageAbsorptionProperty;
import org.spongepowered.api.data.property.item.EfficiencyProperty;
import org.spongepowered.api.data.property.item.EquipmentProperty;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.property.item.SaturationProperty;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.property.store.block.BlastResistancePropertyStore;
import org.spongepowered.common.data.property.store.block.GravityAffectedPropertyStore;
import org.spongepowered.common.data.property.store.block.GroundLuminancePropertyStore;
import org.spongepowered.common.data.property.store.block.HardnessPropertyStore;
import org.spongepowered.common.data.property.store.block.HeldItemPropertyStore;
import org.spongepowered.common.data.property.store.block.IndirectlyPoweredPropertyStore;
import org.spongepowered.common.data.property.store.block.LightEmissionPropertyStore;
import org.spongepowered.common.data.property.store.block.MatterPropertyStore;
import org.spongepowered.common.data.property.store.block.PassablePropertyStore;
import org.spongepowered.common.data.property.store.block.PoweredPropertyStore;
import org.spongepowered.common.data.property.store.block.ReplaceablePropertyStore;
import org.spongepowered.common.data.property.store.block.SkyLuminancePropertyStore;
import org.spongepowered.common.data.property.store.block.StatisticsTrackedPropertyStore;
import org.spongepowered.common.data.property.store.block.TemperaturePropertyStore;
import org.spongepowered.common.data.property.store.block.UnbreakablePropertyStore;
import org.spongepowered.common.data.property.store.entity.EyeHeightPropertyStore;
import org.spongepowered.common.data.property.store.entity.EyeLocationPropertyStore;
import org.spongepowered.common.data.property.store.item.ApplicableEffectPropertyStore;
import org.spongepowered.common.data.property.store.item.BurningFuelPropertyStore;
import org.spongepowered.common.data.property.store.item.DamageAbsorptionPropertyStore;
import org.spongepowered.common.data.property.store.item.EfficiencyPropertyStore;
import org.spongepowered.common.data.property.store.item.EquipmentPropertyStore;
import org.spongepowered.common.data.property.store.item.FoodRestorationPropertyStore;
import org.spongepowered.common.data.property.store.item.HarvestingPropertyStore;
import org.spongepowered.common.data.property.store.item.SaturationPropertyStore;
import org.spongepowered.common.data.property.store.item.UseLimitPropertyStore;
import org.spongepowered.common.registry.RegistryModule;

public final class PropertyRegistryModule implements RegistryModule {

    @Override
    public void registerDefaults() {
        final PropertyRegistry propertyRegistry = SpongePropertyRegistry.getInstance();

        // Blocks
        propertyRegistry.register(BlastResistanceProperty.class, new BlastResistancePropertyStore());
        propertyRegistry.register(GravityAffectedProperty.class, new GravityAffectedPropertyStore());
        propertyRegistry.register(GroundLuminanceProperty.class, new GroundLuminancePropertyStore());
        propertyRegistry.register(HardnessProperty.class, new HardnessPropertyStore());
        propertyRegistry.register(HeldItemProperty.class, new HeldItemPropertyStore());
        propertyRegistry.register(IndirectlyPoweredProperty.class, new IndirectlyPoweredPropertyStore());
        propertyRegistry.register(LightEmissionProperty.class, new LightEmissionPropertyStore());
        propertyRegistry.register(MatterProperty.class, new MatterPropertyStore());
        propertyRegistry.register(PassableProperty.class, new PassablePropertyStore());
        propertyRegistry.register(PoweredProperty.class, new PoweredPropertyStore());
        propertyRegistry.register(ReplaceableProperty.class, new ReplaceablePropertyStore());
        propertyRegistry.register(SkyLuminanceProperty.class, new SkyLuminancePropertyStore());
        propertyRegistry.register(StatisticsTrackedProperty.class, new StatisticsTrackedPropertyStore());
        propertyRegistry.register(TemperatureProperty.class, new TemperaturePropertyStore());
        propertyRegistry.register(UnbreakableProperty.class, new UnbreakablePropertyStore());

        // Items
        propertyRegistry.register(ApplicableEffectProperty.class, new ApplicableEffectPropertyStore());
        propertyRegistry.register(BurningFuelProperty.class, new BurningFuelPropertyStore());
        propertyRegistry.register(DamageAbsorptionProperty.class, new DamageAbsorptionPropertyStore());
        propertyRegistry.register(EfficiencyProperty.class, new EfficiencyPropertyStore());
        propertyRegistry.register(EquipmentProperty.class, new EquipmentPropertyStore());
        propertyRegistry.register(FoodRestorationProperty.class, new FoodRestorationPropertyStore());
        propertyRegistry.register(HarvestingProperty.class, new HarvestingPropertyStore());
        propertyRegistry.register(SaturationProperty.class, new SaturationPropertyStore());
        propertyRegistry.register(UseLimitProperty.class, new UseLimitPropertyStore());

        // Entities
        propertyRegistry.register(EyeLocationProperty.class, new EyeLocationPropertyStore());
        propertyRegistry.register(EyeHeightProperty.class, new EyeHeightPropertyStore());
    }
}
