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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.util.Tristate;

import java.util.Map;
import java.util.function.Supplier;

public class CatalogableChoicesValueParameter extends ChoicesValueParameter implements CatalogedValueParameter {

    public static final CatalogableChoicesValueParameter BOOLEAN = new CatalogableChoicesValueParameter(
            "sponge:boolean",
            "Boolean",
            ImmutableMap.<String, Supplier<?>>builder()
                    .put("true", () -> true)
                    .put("t", () -> true)
                    .put("yes", () -> true)
                    .put("y", () -> true)
                    .put("verymuchso", () -> true)
                    .put("false", () -> false)
                    .put("f", () -> false)
                    .put("no", () -> false)
                    .put("n", () -> false)
                    .put("notatall", () -> false)
                    .build(),
            Tristate.TRUE);

    private final String id;
    private final String name;

    public CatalogableChoicesValueParameter(String id, String name, Map<String, Supplier<?>> choicesSupplier, Tristate includeChoicesInUsage) {
        super(choicesSupplier, includeChoicesInUsage);
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
