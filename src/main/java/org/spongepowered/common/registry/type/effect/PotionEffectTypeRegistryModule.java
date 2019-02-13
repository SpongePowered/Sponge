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
package org.spongepowered.common.registry.type.effect;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.ArrayList;
import java.util.List;

@RegisterCatalog(PotionEffectTypes.class)
public final class PotionEffectTypeRegistryModule extends AbstractCatalogRegistryModule<PotionEffectType>
    implements SpongeAdditionalCatalogRegistryModule<PotionEffectType>, AlternateCatalogRegistryModule<PotionEffectType> {

    public static PotionEffectTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final List<PotionEffectType> potionList = new ArrayList<>();

    @Override
    public void registerDefaults() {
        for (Potion potion : Potion.REGISTRY) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType) potion;
                this.potionList.add(potionEffectType);
                this.map.put((CatalogKey) (Object) Potion.REGISTRY.getKey(potion), potionEffectType);
            }
        }
    }

    @AdditionalRegistration
    public void additionalRegistration() { // I'm guessing that this should work very well.
        for (Potion potion : Potion.REGISTRY) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType) potion;
                if (!this.potionList.contains(potionEffectType)) {
                    this.potionList.add(potionEffectType);
                    this.map.put((CatalogKey) (Object) Potion.REGISTRY.getKey(potion), potionEffectType);
                }
            }
        }
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(PotionEffectType extraCatalog) {
    }

    public void registerFromGameData(ResourceLocation id, PotionEffectType itemType) {
        this.map.put((CatalogKey) (Object) id, itemType);
    }

    PotionEffectTypeRegistryModule() {

    }

    private static final class Holder {
        static final PotionEffectTypeRegistryModule INSTANCE = new PotionEffectTypeRegistryModule();
    }
}
