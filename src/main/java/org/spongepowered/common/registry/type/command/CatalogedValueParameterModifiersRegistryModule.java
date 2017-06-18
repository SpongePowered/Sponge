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
import com.google.common.collect.Maps;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameterModifiers;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.command.parameter.modifier.AllOfModifier;
import org.spongepowered.common.command.parameter.modifier.CatalogableOrEntityTargetModifier;
import org.spongepowered.common.command.parameter.modifier.CatalogableOrSourceDefaultValueModifier;
import org.spongepowered.common.command.parameter.modifier.NowDateTimeModifier;
import org.spongepowered.common.command.parameter.modifier.OnlyOneModifier;
import org.spongepowered.common.command.parameter.modifier.OptionalModifier;
import org.spongepowered.common.command.parameter.modifier.OptionalWeakModifier;
import org.spongepowered.common.command.parameter.modifier.OrSourceIPModifier;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CatalogedValueParameterModifiersRegistryModule implements AdditionalCatalogRegistryModule<CatalogedValueParameterModifier> {

    @RegisterCatalog(CatalogedValueParameterModifiers.class)
    private final Map<String, CatalogedValueParameterModifier> parserModifierMappings = Maps.newHashMap();
    private final Map<String, CatalogedValueParameterModifier> idMappings = Maps.newHashMap();

    @Override
    public void registerAdditionalCatalog(CatalogedValueParameterModifier extraCatalog) {
        Preconditions.checkArgument(!this.idMappings.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)),
                "That ID has already been registered.");

        this.idMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public Optional<CatalogedValueParameterModifier> getById(String id) {
        return Optional.ofNullable(this.idMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CatalogedValueParameterModifier> getAll() {
        return ImmutableSet.copyOf(this.idMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.parserModifierMappings.put("all_of", new AllOfModifier());
        this.parserModifierMappings.put("only_one", new OnlyOneModifier());
        this.parserModifierMappings.put("optional", new OptionalModifier());
        this.parserModifierMappings.put("optional_weak", new OptionalWeakModifier());
        this.parserModifierMappings.put("or_current_date_time", new NowDateTimeModifier());
        this.parserModifierMappings.put("or_source", CatalogableOrSourceDefaultValueModifier.OR_SOURCE);
        this.parserModifierMappings.put("or_source_ip", new OrSourceIPModifier());
        this.parserModifierMappings.put("or_entity_source", CatalogableOrSourceDefaultValueModifier.OR_ENTITY_SOURCE);
        this.parserModifierMappings.put("or_entity_target", CatalogableOrEntityTargetModifier.OR_ENTITY_TARGET);
        this.parserModifierMappings.put("or_player_source", CatalogableOrSourceDefaultValueModifier.OR_PLAYER_SOURCE);
        this.parserModifierMappings.put("or_player_target", CatalogableOrEntityTargetModifier.OR_PLAYER_TARGET);

        this.parserModifierMappings.forEach((k, v) -> this.idMappings.put(v.getId().toLowerCase(Locale.ENGLISH), v));
    }

}
