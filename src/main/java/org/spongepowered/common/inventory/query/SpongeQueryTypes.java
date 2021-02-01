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
package org.spongepowered.common.inventory.query;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import org.spongepowered.common.inventory.lens.Lens;

import java.util.Set;

@SuppressWarnings("unused")
@RegistryScopes(scopes = RegistryScope.GAME)
public final class SpongeQueryTypes {

    // @formatter:off

    // SORTFIELDS:ON

    public static final DefaultedRegistryReference<QueryType.OneParam<Lens>> LENS = SpongeQueryTypes.oneParamKey(ResourceKey.sponge("lens"));

    public static final DefaultedRegistryReference<QueryType.OneParam<Set<Inventory>>> SLOT_LENS = SpongeQueryTypes.oneParamKey(ResourceKey.sponge("slot_lens"));

    public static final DefaultedRegistryReference<QueryType.OneParam<Inventory>> UNION = SpongeQueryTypes.oneParamKey(ResourceKey.sponge("union"));

    // SORTFIELDS:OFF

    // @formatter:on

    private SpongeQueryTypes() {
    }

    private static DefaultedRegistryReference<QueryType.NoParam> noParamKey(final ResourceKey location) {
        return RegistryKey.of(RegistryTypes.QUERY_TYPE, location).asDefaultedReference(() -> Sponge.game().registries());
    }

    private static <T1> DefaultedRegistryReference<QueryType.OneParam<T1>> oneParamKey(final ResourceKey location) {
        return RegistryKey.of(RegistryTypes.QUERY_TYPE, location).asDefaultedReference(() -> Sponge.game().registries());
    }

    private static <T1, T2> DefaultedRegistryReference<QueryType.TwoParam<T1, T2>> twoParamKey(final ResourceKey location) {
        return RegistryKey.of(RegistryTypes.QUERY_TYPE, location).asDefaultedReference(() -> Sponge.game().registries());
    }
}
