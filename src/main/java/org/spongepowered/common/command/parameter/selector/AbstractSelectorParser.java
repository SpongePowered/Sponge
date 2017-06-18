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
package org.spongepowered.common.command.parameter.selector;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParser;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selector;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

abstract class AbstractSelectorParser implements CatalogedSelectorParser {

    private final String id;
    private final String name;
    private final Class<? extends Entity> entityTarget;

    AbstractSelectorParser(String id, String name, Class<? extends Entity> entityTarget) {
        this.id = id;
        this.name = name;
        this.entityTarget = entityTarget;
    }

    @Override
    public final Optional<?> parseSelector(Cause cause, String selector, CommandContext context, Function<Text, ArgumentParseException> errorFunction)
            throws ArgumentParseException {
        try {
            Set<Entity> entities;
            if (context.getLocation().isPresent()) {
                entities = Selector.parse(selector).resolve(context.getLocation().get());
            } else if (context.getCommandSource().isPresent()) {
                entities = Selector.parse(selector).resolve(context.getCommandSource().get());
            } else {
                throw errorFunction.apply(t("Cannot use selectors with this cause."));
            }

            if (!entities.stream().allMatch(this.entityTarget::isInstance)) {
                throw errorFunction.apply(t("The selector returned entities that are not valid for this argument."));
            }

            // No need to cast in this case.
            return Optional.of(entities);
        } catch (IllegalArgumentException ex) {
            throw errorFunction.apply(Text.of(ex.getMessage()));
        }
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
