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
package org.spongepowered.common.registry.type.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.keyboard.KeyCategories;
import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.keyboard.SpongeKeyCategory;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class KeyCategoryRegistryModule implements AlternateCatalogRegistryModule<KeyCategory>,
        SpongeAdditionalCatalogRegistryModule<KeyCategory> {

    public static KeyCategoryRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(KeyCategories.class) private final Map<String, KeyCategory> keyCategories = new HashMap<>();
    private final Int2ObjectMap<KeyCategory> keyCategoriesByInternalId = new Int2ObjectOpenHashMap<>();

    private int internalIdCounter = 20;

    public boolean isRegistered(KeyCategory keyCategory) {
        return this.keyCategories.containsValue(keyCategory);
    }

    public Optional<KeyCategory> getByInternalId(int internalId) {
        return Optional.ofNullable(this.keyCategoriesByInternalId.get(internalId));
    }

    @Override
    public Map<String, KeyCategory> provideCatalogMap() {
        Map<String, KeyCategory> mappings = new HashMap<>();
        for (Map.Entry<String, KeyCategory> entry : this.keyCategories.entrySet()) {
            int index = entry.getKey().indexOf(':');
            if (index != -1) {
                mappings.put(entry.getKey().substring(index + 1), entry.getValue());
            }
        }
        return mappings;
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(KeyCategory keyCategory) {
        checkNotNull(keyCategory, "keyCategory");
        checkArgument(!this.keyCategories.containsValue(keyCategory), "The key category %s is already registered", keyCategory.getId());
        checkArgument(!this.keyCategories.containsKey(keyCategory.getId().toLowerCase(Locale.ENGLISH)),
                "The key category id %s is already used", keyCategory.getId());

        registerAdditionalCategory((SpongeKeyCategory) keyCategory, this.internalIdCounter++);
    }

    private void registerAdditionalCategory(SpongeKeyCategory keyCategory, int internalId) {
        keyCategory.setInternalId(internalId);
        this.keyCategories.put(keyCategory.getId(), keyCategory);
        this.keyCategoriesByInternalId.put(internalId, keyCategory);
    }

    private void registerDefaultCategory(String name, int internalId) {
        Text title = Sponge.getRegistry().getTranslationById("key.categories." + name).map(translation -> (Text) Text.of(translation))
                .orElseGet(() -> Text.of(name));
        registerAdditionalCategory(new SpongeKeyCategory("minecraft", name, title, true), internalId);
    }

    @Override
    public Optional<KeyCategory> getById(String id) {
        return Optional.ofNullable(this.keyCategories.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<KeyCategory> getAll() {
        return ImmutableSet.copyOf(this.keyCategories.values());
    }

    @Override
    public void registerDefaults() {
        registerDefaultCategory("movement", 0);
        registerDefaultCategory("inventory", 1);
        registerDefaultCategory("gameplay", 2);
        registerDefaultCategory("multiplayer", 3);
        registerDefaultCategory("misc", 4);
    }

    KeyCategoryRegistryModule() {}

    private static final class Holder {

        static final KeyCategoryRegistryModule INSTANCE = new KeyCategoryRegistryModule();
    }
}
