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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.entity.SpongeRabbitType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class RabbitTypeRegistryModule implements CatalogRegistryModule<RabbitType> {

    public static final Map<String, RabbitType> RABBIT_TYPES = Maps.newHashMap();
    public static final Int2ObjectMap<RabbitType> RABBIT_IDMAP = new Int2ObjectOpenHashMap<>();
    // rabbit types
    public static final SpongeRabbitType BROWN_RABBIT = new SpongeRabbitType(0, "BROWN");
    public static final RabbitType WHITE_RABBIT = new SpongeRabbitType(1, "WHITE");
    public static final RabbitType BLACK_RABBIT = new SpongeRabbitType(2, "BLACK");
    public static final RabbitType BLACK_AND_WHITE_RABBIT = new SpongeRabbitType(3, "BLACK_AND_WHITE");
    public static final RabbitType GOLD_RABBIT = new SpongeRabbitType(4, "GOLD");
    public static final RabbitType SALT_AND_PEPPER_RABBIT = new SpongeRabbitType(5, "SALT_AND_PEPPER");
    public static final RabbitType KILLER_RABBIT = new SpongeRabbitType(99, "KILLER");
    @RegisterCatalog(RabbitTypes.class)
    private final Map<String, RabbitType> rabbitTypeMap = new HashMap<>();

    @Override
    public Optional<RabbitType> getById(String id) {
        return Optional.ofNullable(this.rabbitTypeMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<RabbitType> getAll() {
        return ImmutableList.copyOf(this.rabbitTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        RabbitTypeRegistryModule.RABBIT_TYPES.put("brown", RabbitTypeRegistryModule.BROWN_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("white", RabbitTypeRegistryModule.WHITE_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("black", RabbitTypeRegistryModule.BLACK_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("black_and_white", RabbitTypeRegistryModule.BLACK_AND_WHITE_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("gold", RabbitTypeRegistryModule.GOLD_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("salt_and_pepper", RabbitTypeRegistryModule.SALT_AND_PEPPER_RABBIT);
        RabbitTypeRegistryModule.RABBIT_TYPES.put("killer", RabbitTypeRegistryModule.KILLER_RABBIT);

        RabbitTypeRegistryModule.RABBIT_IDMAP.put(0, RabbitTypeRegistryModule.BROWN_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(1, RabbitTypeRegistryModule.WHITE_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(2, RabbitTypeRegistryModule.BLACK_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(3, RabbitTypeRegistryModule.BLACK_AND_WHITE_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(4, RabbitTypeRegistryModule.GOLD_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(5, RabbitTypeRegistryModule.SALT_AND_PEPPER_RABBIT);
        RabbitTypeRegistryModule.RABBIT_IDMAP.put(99, RabbitTypeRegistryModule.KILLER_RABBIT);
        this.rabbitTypeMap.putAll(RABBIT_TYPES);

    }

}
