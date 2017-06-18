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

import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.function.Supplier;

public class CatalogableTextValueParameter extends TextValueParameter implements CatalogedValueParameter {

    public static CatalogableTextValueParameter FORMATTING_CODE = new CatalogableTextValueParameter(
            () -> TextSerializers.FORMATTING_CODE, false, "sponge:formatting_code_text", "Formatting code text parameter"
    );

    public static CatalogableTextValueParameter FORMATTING_CODE_ALL = new CatalogableTextValueParameter(
            () -> TextSerializers.FORMATTING_CODE, true, "sponge:formatting_code_text_all", "Formatting code all text parameter"
    );

    public static CatalogableTextValueParameter JSON = new CatalogableTextValueParameter(
            () -> TextSerializers.JSON, false, "sponge:json_text", "Json text parameter"
    );

    public static CatalogableTextValueParameter JSON_ALL = new CatalogableTextValueParameter(
            () -> TextSerializers.JSON, true, "sponge:json_text_all", "Json text all text parameter"
    );

    private final String id;
    private final String name;

    private CatalogableTextValueParameter(Supplier<TextSerializer> serializerSupplier, boolean allRemaining, String id, String name) {
        super(serializerSupplier, allRemaining);
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
