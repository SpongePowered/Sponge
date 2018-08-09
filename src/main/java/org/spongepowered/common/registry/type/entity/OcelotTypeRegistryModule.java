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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.OcelotTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.entity.SpongeOcelotType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegisterCatalog(OcelotTypes.class)
public class OcelotTypeRegistryModule extends AbstractCatalogRegistryModule<OcelotType> implements CatalogRegistryModule<OcelotType> {

    public static final Map<String, OcelotType> OCELOT_TYPES = Maps.newHashMap();
    public static final Map<Integer, OcelotType> OCELOT_IDMAP = Maps.newHashMap();
    // ocelot types
    public static final SpongeOcelotType WILD_OCELOT = new SpongeOcelotType(0, "WILD_OCELOT");
    public static final SpongeOcelotType BLACK_CAT = new SpongeOcelotType(1, "BLACK_CAT");
    public static final SpongeOcelotType RED_CAT = new SpongeOcelotType(2, "RED_CAT");
    public static final SpongeOcelotType SIAMESE_CAT = new SpongeOcelotType(3, "SIAMESE_CAT");

    @Override
    public void registerDefaults() {
        OcelotTypeRegistryModule.OCELOT_TYPES.put("wild_ocelot", OcelotTypeRegistryModule.WILD_OCELOT);
        OcelotTypeRegistryModule.OCELOT_TYPES.put("black_cat", OcelotTypeRegistryModule.BLACK_CAT);
        OcelotTypeRegistryModule.OCELOT_TYPES.put("red_cat", OcelotTypeRegistryModule.RED_CAT);
        OcelotTypeRegistryModule.OCELOT_TYPES.put("siamese_cat", OcelotTypeRegistryModule.SIAMESE_CAT);

        OcelotTypeRegistryModule.OCELOT_IDMAP.put(0, OcelotTypeRegistryModule.WILD_OCELOT);
        OcelotTypeRegistryModule.OCELOT_IDMAP.put(1, OcelotTypeRegistryModule.BLACK_CAT);
        OcelotTypeRegistryModule.OCELOT_IDMAP.put(2, OcelotTypeRegistryModule.RED_CAT);
        OcelotTypeRegistryModule.OCELOT_IDMAP.put(3, OcelotTypeRegistryModule.SIAMESE_CAT);
        for (Map.Entry<String, OcelotType> entry : OCELOT_TYPES.entrySet()) {
            register(CatalogKey.resolve(entry.getKey()), entry.getValue());
        }

    }

}
