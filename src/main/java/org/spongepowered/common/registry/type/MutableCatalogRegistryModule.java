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

package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class MutableCatalogRegistryModule<T extends CatalogType> implements AlternateCatalogRegistryModule<T> {

    private final Map<String, T> mapping;
    private final Function<String, String> keyTransformer;

    protected MutableCatalogRegistryModule() {
        this(Maps.newHashMap());
    }

    protected MutableCatalogRegistryModule(Map<String, T> mapping) {
        this.mapping = checkNotNull(mapping, "mapping");
        this.keyTransformer = (input) -> input;
    }

    protected MutableCatalogRegistryModule(Function<String, String> keyTransformer) {
        this(Maps.newHashMap(), keyTransformer);
    }

    protected MutableCatalogRegistryModule(Map<String, T> mapping, Function<String, String> keyTransformer) {
        this.mapping = checkNotNull(mapping, "mapping");
        this.keyTransformer = checkNotNull(keyTransformer, "keyTransformer");
    }

    @Override
    public final Optional<T> getById(String id) {
        return Optional.ofNullable(this.mapping.get(mapId(checkNotNull(id, "id"))));
    }

    private String mapId(String id) {
        return this.keyTransformer.apply(id.toLowerCase());
    }

    @Override
    public final Collection<T> getAll() {
        return ImmutableSet.copyOf(this.mapping.values());
    }

    @Override
    public Map<String, T> provideCatalogMap() {
        return ImmutableMap.copyOf(this.mapping);
    }

    /**
     * Generic version of {@link #register(CatalogType, String...)} to avoid
     * unnecessary casting for classes affected by mixins. This method will do
     * the required casting and then forward to
     * {@link #register(CatalogType, String...)}.
     *
     * @param type The catalog type instance to register
     * @param aliases The aliases to register for this catalog type instance
     * @see #register(CatalogType, String...) if yousing catalog types directly
     */
    @SuppressWarnings("unchecked")
    protected final void registerUnsafe(Object type, String... aliases) {
        register((T) type, aliases);
    }

    /**
     * Registers the given type with its id and the given aliases. This method
     * will overwrite any previous mappings. Due to this fact it should never be
     * exposed publicly use {@link #tryRegister(CatalogType, String...)} in this
     * case instead (i.e.
     * {@link AdditionalCatalogRegistryModule#registerAdditionalCatalog(CatalogType)}
     * ).
     *
     * @param type The catalog type instance to register
     * @param aliases The aliases to register for this catalog type instance
     * @see #registerUnsafe(Object, String...) to avoid the explicit casting
     * @see #tryRegister(CatalogType, String...)
     */
    protected void register(T type, String... aliases) {
        checkNotNull(type, "type");
        checkNotNull(aliases, "aliases");
        final String id = checkNotNull(type.getId(), "id");
        checkArgument(!id.isEmpty(), "Passed type's id is empty!");
        this.mapping.put(id.toLowerCase(), type);
        for (String alias : aliases) {
            this.mapping.put(alias.toLowerCase(), type);
        }
    }

    /**
     * Generic version of {@link #tryRegisterUnsafe(Object, String...)} to avoid
     * unnecessary casting for classes affected by mixins. This method will do
     * the required casting and then forward to
     * {@link #tryRegisterUnsafe(Object, String...)}.
     *
     * @param type The catalog type instance to register
     * @param aliases The aliases to register for this catalog type instance
     * @see #tryRegisterUnsafe(Object, String...) if yousing catalog types
     *      directly
     */
    @SuppressWarnings("unchecked")
    protected final boolean tryRegisterUnsafe(Object type, String... aliases) {
        return tryRegister((T) type, aliases);
    }

    /**
     * Tries to register the given type with its id and the given aliases. This
     * method will do nothing if the given catalog type's id is already mapped
     * to a different {@link CatalogType} and that {@link CatalogType} is mainly
     * referenced by the same key. This method will return false in that case.
     * It will return true if the main mapping was successful. Due to this fact
     * it should be used for late registration (i.e.
     * {@link AdditionalCatalogRegistryModule#registerAdditionalCatalog(CatalogType)}
     * ).
     *
     * @param type The catalog type instance to try to register
     * @param aliases The aliases to register for this catalog type instance
     * @return True if the catalog type was successfully registered. False
     *         otherwise
     * @see #tryRegisterUnsafe(Object, String...) to avoid the explicit casting
     * @see #register(CatalogType, String...)
     */
    protected boolean tryRegister(T type, String... aliases) {
        checkNotNull(type, "type");
        checkNotNull(aliases, "aliases");
        String id = checkNotNull(type.getId(), "id").toLowerCase();
        checkArgument(!id.isEmpty(), "Passed type's id is empty!");

        final Optional<T> optCurrent = getById(id);
        if (optCurrent.isPresent()) {
            final T current = optCurrent.get();
            if (type != current && checkIdClash(id, current)) {
                return false;
            }
        }
        this.mapping.put(id.toLowerCase(), type);
        for (String alias : aliases) {
            alias = alias.toLowerCase();
            final Optional<T> optCurrentAlias = getById(alias);
            if (optCurrentAlias.isPresent()) {
                final T current = optCurrentAlias.get();
                // Only overwrite if this is not the main id
                if (type != current && !checkIdClash(alias, current)) {
                    this.mapping.put(alias, type);
                }
            } else {
                this.mapping.put(alias, type);
            }
        }
        return true;
    }

    /**
     * Checks whether the given id and the new id will map to the same key in
     * the mapping and thus will conflict with the {@link CatalogType}
     * specifications. This method will return false if the current entry is
     * only mapped via an alias to the given {@link CatalogType}. And true if
     * the given id and the given {@link CatalogType}'s id match or map to the
     * same key in the mapping.
     *
     * @param newId The new catalog types id to check
     * @param currentEntry The currently registered entry to check for conflicts
     * @return True if the ids collide. False if it is only an alias
     */
    private boolean checkIdClash(String newId, T currentEntry) {
        // Check whether this is only an alias that can be overwritten or a
        // registered type's main id
        final String newMapped = mapId(newId);
        final String currentId = currentEntry.getId();
        final String currentMapped = mapId(currentId);
        return newId.equalsIgnoreCase(currentId) || newMapped.equals(currentMapped);
    }

}
