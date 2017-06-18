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
import org.spongepowered.api.command.format.CommandMessageFormat;
import org.spongepowered.api.command.format.CommandMessageFormats;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.command.format.SpongeColorReplacingCommandMessageFormat;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CommandMessageFormatRegistryModule implements AdditionalCatalogRegistryModule<CommandMessageFormat> {

    @RegisterCatalog(CommandMessageFormats.class)
    private final Map<String, CommandMessageFormat> formatMappings = Maps.newHashMap();
    private final Map<String, CommandMessageFormat> idMappings = Maps.newHashMap();

    @Override
    public void registerAdditionalCatalog(CommandMessageFormat extraCatalog) {
        Preconditions.checkArgument(!idMappings.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)),
                "That ID has already been registered.");

        this.idMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public Optional<CommandMessageFormat> getById(String id) {
        return Optional.ofNullable(this.idMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CommandMessageFormat> getAll() {
        return ImmutableSet.copyOf(this.idMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.formatMappings.put("debug",
                new SpongeColorReplacingCommandMessageFormat("sponge:debug", "Debug message formatting", TextColors.GRAY));
        this.formatMappings.put("error",
                new SpongeColorReplacingCommandMessageFormat("sponge:error", "Error message formatting", TextColors.RED));
        this.formatMappings.put("success",
                new SpongeColorReplacingCommandMessageFormat("sponge:success", "Success message formatting", TextColors.GREEN));
        this.formatMappings.put("system",
                new SpongeColorReplacingCommandMessageFormat("sponge:system", "System message formatting", TextColors.YELLOW));

        this.formatMappings.forEach((k, v) -> this.idMappings.put(v.getId().toLowerCase(Locale.ENGLISH), v));
    }

}
