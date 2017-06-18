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
package org.spongepowered.common.command.parameter.modifier;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ParsingContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameterModifier;
import org.spongepowered.api.command.parameter.token.CommandArgs;

public class OnlyOneModifier implements CatalogedValueParameterModifier {

    private static final String KEY = "key";
    private static final TextTemplate ERROR_TEMPLATE = TextTemplate.of(Text.of("Argument "),
            TextTemplate.arg(KEY).build(),
            Text.of(" may only have one value!"));

    @Override
    public String getId() {
        return "sponge:only_one";
    }

    @Override
    public String getName() {
        return "Only One modifier";
    }

    @Override
    public void onParse(Text key, Cause cause, CommandArgs args, CommandContext context, ParsingContext parsingContext)
            throws ArgumentParseException {
        parsingContext.next();
        if (context.getAll(key).size() > 1) {
            throw args.createError(ERROR_TEMPLATE.apply(ImmutableMap.of(KEY, key)).build());
        }
    }
}
