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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.WorldType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.world.type.SpongeWorldTypeEnd;
import org.spongepowered.common.world.type.SpongeWorldTypeNether;
import org.spongepowered.common.world.type.SpongeWorldTypeOverworld;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RegisterCatalog(GeneratorTypes.class)
public final class GeneratorTypeRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<GeneratorType>
    implements SpongeAdditionalCatalogRegistryModule<GeneratorType>, AlternateCatalogRegistryModule<GeneratorType> {

    public static GeneratorTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        for (WorldType worldType : WorldType.WORLD_TYPES) {
            if (worldType != null) {
                this.registerAdditionalCatalog((GeneratorType) worldType);
            }
        }
        this.registerAdditionalCatalog((GeneratorType) new SpongeWorldTypeEnd());
        this.registerAdditionalCatalog((GeneratorType) new SpongeWorldTypeNether());
        this.registerAdditionalCatalog((GeneratorType) new SpongeWorldTypeOverworld());
    }

    @AdditionalRegistration(RegistrationPhase.PRE_REGISTRY)
    public void registerAdditional() {
        for (WorldType worldType : WorldType.WORLD_TYPES) {
            if (worldType != null && !this.map.values().contains(worldType)) {
                this.map.put(CatalogKey.resolve(worldType.getName().toLowerCase(Locale.ENGLISH)), (GeneratorType) worldType);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(GeneratorTypes.class, this.provideCatalogMap());
    }

    @Override
    public Map<String, GeneratorType> provideCatalogMap() {
        final HashMap<String, GeneratorType> map = new HashMap<>();
        for (Map.Entry<CatalogKey, GeneratorType> entry : this.map.entrySet()) {
            final String value = entry.getKey().getValue().replace("debug_all_block_states", "debug");
            map.put(value, entry.getValue());
        }
        return map;
    }

    GeneratorTypeRegistryModule() {
        super("minecraft",
            new String[] {"minecraft", "sponge"},
            id -> id.replace("debug_all_block_states", "debug")
            );
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(GeneratorType extraCatalog) {
        checkNotNull(extraCatalog);
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    private static final class Holder {
        static final GeneratorTypeRegistryModule INSTANCE = new GeneratorTypeRegistryModule();
    }
}
