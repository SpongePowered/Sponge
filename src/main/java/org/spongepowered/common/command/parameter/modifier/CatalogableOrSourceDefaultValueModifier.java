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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameterModifier;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class CatalogableOrSourceDefaultValueModifier extends DefaultValueModifier implements CatalogedValueParameterModifier {

    public static final CatalogableOrSourceDefaultValueModifier OR_SOURCE =
            new CatalogableOrSourceDefaultValueModifier("sponge:or_source", "Or Source", CommandSource.class);

    public static final CatalogableOrSourceDefaultValueModifier OR_PLAYER_SOURCE =
            new CatalogableOrSourceDefaultValueModifier("sponge:or_player_source", "Or Player Source", Player.class);

    public static final CatalogableOrSourceDefaultValueModifier OR_ENTITY_SOURCE =
            new CatalogableOrSourceDefaultValueModifier("sponge:or_entity_source", "Or Entity Source", Entity.class);

    private final String id;
    private final String name;

    public CatalogableOrSourceDefaultValueModifier(String id, String name, Class<?> sourceType) {
        super(source -> {
            if (sourceType.isInstance(source)) {
                return Optional.of(source);
            }

            return Optional.empty();
        });
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
