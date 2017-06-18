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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehavior;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehaviors;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.command.parameter.flag.behaviors.AcceptNonValueBehavior;
import org.spongepowered.common.command.parameter.flag.behaviors.AcceptValueBehavior;
import org.spongepowered.common.command.parameter.flag.behaviors.ErrorBehavior;
import org.spongepowered.common.command.parameter.flag.behaviors.IgnoreBehavior;
import org.spongepowered.common.command.parameter.flag.behaviors.SkipBehavior;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class UnknownFlagBehaviorRegistryModule implements CatalogRegistryModule<UnknownFlagBehavior> {

    @RegisterCatalog(UnknownFlagBehaviors.class)
    private final Map<String, UnknownFlagBehavior> parserModifierMappings = Maps.newHashMap();

    @Override
    public Optional<UnknownFlagBehavior> getById(String id) {
        return this.parserModifierMappings.values().stream().filter(x -> x.getId().equalsIgnoreCase(id)).findFirst();
    }

    @Override
    public Collection<UnknownFlagBehavior> getAll() {
        return ImmutableSet.copyOf(this.parserModifierMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.parserModifierMappings.put("accept_nonvalue", new AcceptNonValueBehavior());
        this.parserModifierMappings.put("accept_value", new AcceptValueBehavior());
        this.parserModifierMappings.put("error", new ErrorBehavior());
        this.parserModifierMappings.put("ignore", new IgnoreBehavior());
        this.parserModifierMappings.put("skip", new SkipBehavior());
    }

}
