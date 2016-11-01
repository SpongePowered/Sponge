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
import org.spongepowered.api.keyboard.KeyContext;
import org.spongepowered.api.keyboard.KeyContexts;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.keyboard.SpongeKeyContext;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class KeyContextRegistryModule implements AlternateCatalogRegistryModule<KeyContext>,
        SpongeAdditionalCatalogRegistryModule<KeyContext> {

    public static KeyContextRegistryModule get() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(KeyContexts.class) private final Map<String, KeyContext> keyContexts = new HashMap<>();
    private final Int2ObjectMap<KeyContext> keyContextsByInternalId = new Int2ObjectOpenHashMap<>();

    private int internalIdCounter = 5;

    public boolean isRegistered(KeyContext keyContext) {
        return this.keyContexts.containsValue(keyContext);
    }

    public Optional<KeyContext> getByInternalId(int internalId) {
        return Optional.ofNullable(this.keyContextsByInternalId.get(internalId));
    }

    @Override
    public Map<String, KeyContext> provideCatalogMap() {
        final Map<String, KeyContext> mappings = new HashMap<>();
        for (Map.Entry<String, KeyContext> entry : this.keyContexts.entrySet()) {
            final int index = entry.getKey().indexOf(':');
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
    public void registerAdditionalCatalog(KeyContext keyContext) {
        checkNotNull(keyContext, "keyContext");
        checkArgument(!this.keyContexts.containsValue(keyContext), "The key context %s is already registered", keyContext.getId());
        checkArgument(!this.keyContexts.containsKey(keyContext.getId().toLowerCase(Locale.ENGLISH)),
                "The key context id %s is already used", keyContext.getId());

        final SpongeKeyContext keyContext1 = (SpongeKeyContext) keyContext;
        keyContext1.setInternalId(this.internalIdCounter++);
        registerAdditionalBinding(keyContext1);
    }

    private void registerAdditionalBinding(SpongeKeyContext keyContext) {
        this.keyContexts.put(keyContext.getId(), keyContext);
        this.keyContextsByInternalId.put(keyContext.getInternalId(), keyContext);
    }

    @Override
    public Optional<KeyContext> getById(String id) {
        return Optional.ofNullable(this.keyContexts.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<KeyContext> getAll() {
        return ImmutableSet.copyOf(this.keyContexts.values());
    }

    @Override
    public void registerDefaults() {
        registerAdditionalBinding(new SpongeKeyContext(SpongeImpl.getMinecraftPlugin(), "universal",
                player -> true, context -> true).setInternalId(0));
        registerAdditionalBinding(new SpongeKeyContext(SpongeImpl.getMinecraftPlugin(), "in_game",
                player -> !((IMixinEntityPlayerMP) player).isGuiOpen(), context -> true).setInternalId(1));
        registerAdditionalBinding(new SpongeKeyContext(SpongeImpl.getMinecraftPlugin(), "gui",
                player -> ((IMixinEntityPlayerMP) player).isGuiOpen(), context -> true).setInternalId(2));
        registerAdditionalBinding(new SpongeKeyContext(SpongeImpl.getMinecraftPlugin(), "inventory",
                player -> player.getOpenInventory().isPresent(), context -> true).setInternalId(3));
    }

    private KeyContextRegistryModule() {
    }

    private static final class Holder {

        static final KeyContextRegistryModule INSTANCE = new KeyContextRegistryModule();
    }
}
