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
package org.spongepowered.common.registry.type.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.common.command.parameter.value.CatalogableCatalogTypeValueParameter;
import org.spongepowered.common.command.parameter.value.CatalogableChoicesValueParameter;
import org.spongepowered.common.command.parameter.value.CatalogableNumberValueParameter;
import org.spongepowered.common.command.parameter.value.CatalogableTextValueParameter;
import org.spongepowered.common.command.parameter.value.ColorValueParameter;
import org.spongepowered.common.command.parameter.value.DataContainerValueParameter;
import org.spongepowered.common.command.parameter.value.DateTimeValueParameter;
import org.spongepowered.common.command.parameter.value.DurationValueParameter;
import org.spongepowered.common.command.parameter.value.EntityValueParameter;
import org.spongepowered.common.command.parameter.value.IPValueParameter;
import org.spongepowered.common.command.parameter.value.JoinedStringValueParameter;
import org.spongepowered.common.command.parameter.value.LocationValueParameter;
import org.spongepowered.common.command.parameter.value.NoneValueParameter;
import org.spongepowered.common.command.parameter.value.PlayerValueParameter;
import org.spongepowered.common.command.parameter.value.PluginContainerValueParameter;
import org.spongepowered.common.command.parameter.value.RawJoinedStringValueParameter;
import org.spongepowered.common.command.parameter.value.StringValueParameter;
import org.spongepowered.common.command.parameter.value.URLValueParameter;
import org.spongepowered.common.command.parameter.value.UUIDValueParameter;
import org.spongepowered.common.command.parameter.value.UserValueParameter;
import org.spongepowered.common.command.parameter.value.Vector3dValueParameter;
import org.spongepowered.common.command.parameter.value.WorldPropertiesValueParameter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class CatalogedValueParametersRegistryModule implements AdditionalCatalogRegistryModule<CatalogedValueParameter> {

    @RegisterCatalog(CatalogedValueParameters.class)
    private final Map<String, CatalogedValueParameter> parserMappings = Maps.newHashMap();
    private final Map<String, CatalogedValueParameter> idMappings = Maps.newHashMap();

    @Override
    public void registerAdditionalCatalog(CatalogedValueParameter extraCatalog) {
        Preconditions.checkArgument(!this.idMappings.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)), "That ID has already been "
                + "registered.");

        this.idMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public Optional<CatalogedValueParameter> getById(String id) {
        return Optional.ofNullable(this.idMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CatalogedValueParameter> getAll() {
        return ImmutableSet.copyOf(this.idMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.parserMappings.put("big_decimal",
                new CatalogableNumberValueParameter("sponge:big_decimal", "Big Decimal parameter", "a big decimal", BigDecimal::new));
        this.parserMappings.put("big_integer",
                new CatalogableNumberValueParameter("sponge:big_integer", "Big Integer parameter", "a big integer", BigInteger::new));
        this.parserMappings.put("boolean", CatalogableChoicesValueParameter.BOOLEAN);
        this.parserMappings.put("color", new ColorValueParameter());
        this.parserMappings.put("data_container", new DataContainerValueParameter());
        this.parserMappings.put("date_time", new DateTimeValueParameter());

        CatalogableCatalogTypeValueParameter<DimensionType> ccdt = new CatalogableCatalogTypeValueParameter<>(
                "sponge:dimension_catalog_type",
                "Dimension Catalog Type",
                DimensionType.class,
                Lists.newArrayList("minecraft", "sponge")
        );
        this.parserMappings.put("dimension", ccdt);

        this.parserMappings.put("duration", new DurationValueParameter());

        this.parserMappings.put("double",
                new CatalogableNumberValueParameter("sponge:double", "Double parameter", "a double", Double::parseDouble));
        this.parserMappings.put("integer",
                new CatalogableNumberValueParameter("sponge:integer", "Integer parameter", "an integer", Integer::parseInt));
        this.parserMappings.put("ip", new IPValueParameter());
        this.parserMappings.put("long",
                new CatalogableNumberValueParameter("sponge:long", "Long parameter", "a long integer", Integer::parseInt));

        this.parserMappings.put("none", new NoneValueParameter());

        this.parserMappings.put("entity", new EntityValueParameter("sponge:entity", "Entity parameter"));

        PlayerValueParameter parameter = new PlayerValueParameter("sponge:player", "Player parameter");
        this.parserMappings.put("player", parameter);

        this.parserMappings.put("plugin", new PluginContainerValueParameter());

        this.parserMappings.put("remaining_joined_strings", new JoinedStringValueParameter());
        this.parserMappings.put("remaining_raw_joined_strings", new RawJoinedStringValueParameter());

        this.parserMappings.put("string", new StringValueParameter());

        this.parserMappings.put("text_formatting_code", CatalogableTextValueParameter.FORMATTING_CODE);
        this.parserMappings.put("text_formatting_code_all", CatalogableTextValueParameter.FORMATTING_CODE_ALL);
        this.parserMappings.put("text_json", CatalogableTextValueParameter.JSON);
        this.parserMappings.put("text_json_all", CatalogableTextValueParameter.JSON_ALL);

        this.parserMappings.put("url", new URLValueParameter());
        this.parserMappings.put("user", new UserValueParameter("sponge:user", "User parameter", parameter));
        this.parserMappings.put("uuid", new UUIDValueParameter());

        Vector3dValueParameter vector3dValueParameter = new Vector3dValueParameter();
        this.parserMappings.put("vector3d", new Vector3dValueParameter());

        WorldPropertiesValueParameter worldPropertiesValueParameter = new WorldPropertiesValueParameter(ccdt);
        this.parserMappings.put("world_properties", worldPropertiesValueParameter);

        this.parserMappings.put("location", new LocationValueParameter(worldPropertiesValueParameter, vector3dValueParameter));

        this.parserMappings.forEach((k, v) -> this.idMappings.put(v.getId().toLowerCase(Locale.ENGLISH), v));
    }

}
