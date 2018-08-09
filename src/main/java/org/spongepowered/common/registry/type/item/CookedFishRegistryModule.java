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
package org.spongepowered.common.registry.type.item;

import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.CookedFishes;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeCookedFish;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

@RegisterCatalog(CookedFishes.class)
public final class CookedFishRegistryModule extends AbstractCatalogRegistryModule<CookedFish>
    implements AlternateCatalogRegistryModule<CookedFish> {

    @Override
    protected String marshalFieldKey(String key) {
        return key.replace("cooked.", "");
    }

    @Override
    public void registerDefaults() {
        registerAdditional();
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            final CatalogKey key = CatalogKey.resolve(fishType.name());
            if (fishType.canCook() && !this.map.containsKey(key)) {
                CookedFish cooked = new SpongeCookedFish(key,
                        new SpongeTranslation("item.fish." + fishType.getUnlocalizedName() + ".cooked.name"), fishType);
                this.map.put(cooked.getKey(), cooked);
            }
        }
    }

}
