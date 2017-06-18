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
package org.spongepowered.common.command.parameter.value;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.managed.impl.PatternMatchingValueParameter;
import org.spongepowered.api.event.cause.Cause;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogTypeValueParameter<T extends CatalogType> extends PatternMatchingValueParameter {

    private final Class<T> catalogType;
    private final Iterable<String> prefixes;

    public CatalogTypeValueParameter(Class<T> catalogType, Iterable<String> prefixes) {
        this.catalogType = catalogType;
        this.prefixes = Iterables.transform(prefixes, x -> x.toLowerCase(Locale.ENGLISH));
    }

    @Override
    protected Iterable<String> getChoices(Cause cause) {
        GameRegistry gr = Sponge.getRegistry();
        final Collection<String> choices = gr.getAllOf(this.catalogType)
                .stream()
                .map(CatalogType::getId)
                .collect(Collectors.toList());
        final List<String> ret = Lists.newArrayList(choices);
        for (String prefix : prefixes) {
            ret.addAll(choices.stream().filter(x -> x.startsWith(prefix))
                    .map(x -> x.replace(prefix + ":", "")).collect(Collectors.toList()));
        }

        return ret;
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        final Optional<T> ret = Sponge.getGame().getRegistry().getType(this.catalogType, choice);
        if (!ret.isPresent()) {
            throw new IllegalArgumentException("Invalid input " + choice + " was found");
        }
        return ret.get();
    }

    // Below is used for resettable builder

    public Class<T> getCatalogType() {
        return this.catalogType;
    }

    public Iterable<String> getPrefixes() {
        return this.prefixes;
    }

}
