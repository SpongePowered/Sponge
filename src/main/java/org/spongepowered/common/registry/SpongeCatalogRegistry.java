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
package org.spongepowered.common.registry;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.LlamaType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.TypeNotFoundException;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.SpongeDataRegistration;
import org.spongepowered.common.data.persistence.DataSerializers;
import org.spongepowered.common.registry.builtin.sponge.CatTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DisplaySlotStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseColorStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseStyleStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.LlamaTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.NotePitchStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ParrotTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.RabbitTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.FireworkShapeStreamGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeCatalogRegistry implements CatalogRegistry {

    private final Map<Class<CatalogType>, Map<String, Supplier<CatalogType>>> suppliers;
    private final Map<ResourceKey, Registry<CatalogType>> registries;
    private final Map<Class<CatalogType>, Registry<CatalogType>> registriesByType;

    private final List<Class<? extends CatalogType>> dynamicCatalogs;
    private final List<Class<? extends CatalogType>> datapackCatalogues;

    public SpongeCatalogRegistry() {
        this.suppliers = new IdentityHashMap<>();
        this.registries = new Object2ObjectOpenHashMap<>();
        this.registriesByType = new IdentityHashMap<>();
        this.dynamicCatalogs = new ArrayList<>();
        this.datapackCatalogues = new ArrayList<>();
    }

    @Override
    public <T extends CatalogType, E extends T> Supplier<E> provideSupplier(final Class<T> catalogClass, final String suggestedId) {
        Objects.requireNonNull(suggestedId);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.get(catalogClass);

        if (catalogSuppliers == null) {
            final String message = String.format("No suppliers found for type '%s'!", catalogClass);
            System.err.println(message);
            throw new TypeNotFoundException(message);
        }

        final Supplier<CatalogType> catalogSupplier = catalogSuppliers.get(suggestedId.toLowerCase());
        if (catalogSupplier == null) {
            final String message = String.format("Supplier for type '%s' with id '%s' has not been registered!", catalogClass, suggestedId);
            System.err.println(message);
            throw new TypeNotFoundException(message);
        }

        return (Supplier<E>) (Object) catalogSupplier;
    }

    @Override
    public <T extends CatalogType> Optional<T> get(final Class<T> typeClass, final net.kyori.adventure.key.Key key) {
        Objects.requireNonNull(key);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Optional.empty();
        }

        return (Optional<T>) registry.getValue(SpongeAdventure.asVanilla(key));
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Collections.emptyList();
        }
        return ImmutableSet.<T>copyOf((Set<T>) (Object) (((SimpleRegistryAccessor) registry).accessor$storage().values()));
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) ((SimpleRegistryAccessor) registry).accessor$storage().values().stream();
        }

        return stream;
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllFor(final Class<T> typeClass, final String namespace) {
        Objects.requireNonNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final List<T> types = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, Object> entry : ((SimpleRegistryAccessor) registry).accessor$storage().entrySet()) {
            if (entry.getKey().getNamespace().equals(namespace)) {
                types.add((T) entry.getValue());
            }
        }

        return types;
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllFor(final Class<T> typeClass, final String namespace) {
        Objects.requireNonNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) ((SimpleRegistryAccessor) registry).accessor$storage()
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().getNamespace().equals(namespace))
                .map(Map.Entry::getValue);
        }

        return stream;
    }

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerCatalogAndSupplier(final Class<E> catalogClass, final String suggestedId, Supplier<E> supplier) {
        Objects.requireNonNull(supplier);

        // Typically this isn't safe but for fake vanilla registries we can do it
        final E value = supplier.get();

        final Registry<CatalogType> registry = this.registriesByType.get(catalogClass);
        ((SimpleRegistry) registry).register((ResourceLocation) (Object) ((CatalogType) value).getKey(), value);

        return this.registerSupplier(catalogClass, suggestedId, supplier);
    }

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerSupplier(final Class<E> catalogClass, final String suggestedId, final Supplier<E> supplier) {
        Objects.requireNonNull(supplier);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.computeIfAbsent((Class<CatalogType>) (Object) catalogClass, k -> new Object2ObjectArrayMap<>());
        if (catalogSuppliers.containsKey(suggestedId)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' with id '%s' has a supplier already registered!", catalogClass, suggestedId));
        }

        catalogSuppliers.put(suggestedId, (Supplier<CatalogType>) (Object) supplier);
        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, final boolean isDynamic, final boolean isDataPack) {
        return this.registerRegistry(catalogClass, key, null, false, isDynamic, isDataPack);
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, final boolean isDynamic) {
        return this.registerRegistry(catalogClass, key, null, false, isDynamic, false);
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key,
            @Nullable final Supplier<Set<T>> defaultsSupplier, final boolean generateSuppliers, final boolean isDynamic, final boolean isDataPack) {
        Objects.requireNonNull(key);

        if (isDataPack) {
            this.registries.remove(key);
            this.registriesByType.remove(catalogClass);
        }
        if (this.registries.get(key) != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered for '%s!", catalogClass, key));
        }

        final SimpleRegistry<T> registry = new SimpleRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(catalogType -> {
                registry.register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(catalogType.getKey().getValue(), () -> catalogType);
                }
            });
        }

        if (isDynamic) {
            if (isDataPack) {
                this.datapackCatalogues.add(catalogClass);
            } else {
                this.dynamicCatalogs.add(catalogClass);
            }
        }

        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, final Registry<T> registry) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(registry);

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);
        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerCallbackRegistry(final Class<T> catalogClass, final ResourceKey key,
            @Nullable final Supplier<Set<T>> defaultsSupplier, final BiConsumer<ResourceLocation, T> callback, final boolean generateSuppliers,
            final boolean isDynamic) {
        Objects.requireNonNull(key);

        if (this.registries.containsKey(key)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered for '%s!", catalogClass, key));
        }

        final CallbackRegistry<CatalogType> registry = new CallbackRegistry<>((BiConsumer<ResourceLocation, CatalogType>) callback);
        this.registries.put(key, registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(v -> {
                registry.register((ResourceLocation) (Object) v.getKey(), v);
                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(v.getKey().getValue(), () -> v);
                }
            });
        }

        if (isDynamic) {
            this.dynamicCatalogs.add(catalogClass);
        }

        return this;
    }

    private <T extends CatalogType, U> SpongeCatalogRegistry registerMappedRegistry(final Class<T> catalogClass, final ResourceKey key,
            @Nullable final Supplier<Set<Tuple<T, U>>> defaultsSupplier, final boolean generateSuppliers, final boolean isDynamic) {
        Objects.requireNonNull(key);

        if (this.registries.containsKey(key)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered for '%s!", catalogClass, key));
        }

        final MappedRegistry<T, U> registry = new MappedRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(kv -> {
                registry.register((ResourceLocation) (Object) kv.getFirst().getKey(), kv.getFirst());
                registry.registerMapping(kv.getFirst(), kv.getSecond());

                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(kv.getFirst()
                            .getKey().getValue(), kv::getFirst);
                }
            });
        }

        if (isDynamic) {
            this.dynamicCatalogs.add(catalogClass);
        }

        return this;
    }

    public <T extends CatalogType, R extends Registry<T>> @Nullable R getRegistry(final Class<T> catalogClass) {
        return (R) this.registriesByType.get(catalogClass);
    }

    public <T extends CatalogType, R extends Registry<T>> @NonNull R requireRegistry(final Class<T> catalogClass) {
        final R registry = this.getRegistry(catalogClass);
        if (registry == null) {
            throw new IllegalArgumentException("No registry is registered for " + catalogClass);
        }
        return registry;
    }

    public <C extends CatalogType> C registerCatalog(final TypeToken<C> catalog, final C catalogType) {
        Objects.requireNonNull(catalogType);

        final Registry<C> registry = (Registry<C>) this.registriesByType.get(GenericTypeReflector.erase(catalog.getType()));
        if (registry == null) {
            throw new TypeNotFoundException(String.format("Catalog '%s' with id '%s' has no registry registered!", catalogType.getClass(), catalogType.getKey()));
        }

        return ((SimpleRegistry<C>) registry).register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
    }

    public void callRegisterCatalogEvents(final Cause cause, final Game game) {
        this.callRegisterCatalogEvents(cause, game, this.dynamicCatalogs);
    }

    public void callDataPackRegisterCatalogEvents(Cause cause, Game game) {
        this.callRegisterCatalogEvents(cause, game, this.datapackCatalogues);
    }

    private void callRegisterCatalogEvents(final Cause cause, final Game game, List<Class<? extends CatalogType>> catalogs) {
        for (final Class<? extends CatalogType> dynamicCatalog : catalogs) {
            final TypeToken<? extends CatalogType> token = TypeToken.get(dynamicCatalog);
            game.getEventManager().post(new RegisterCatalogEventImpl<>(cause, game, (TypeToken<CatalogType>) token));
        }
    }

    /**
     * Only specify lines of registries that are not found in {@link Registry}.
     */
    public void registerDefaultRegistries() {

        // ORDER MATTERS


        this
            .generateMappedRegistry(CatType.class, ResourceKey.minecraft("cat_type"), CatTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(FireworkShape.class, ResourceKey.minecraft("firework_shape"), FireworkShapeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(GoalType.class, ResourceKey.minecraft("goal_type"), GoalTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(HorseColor.class, ResourceKey.minecraft("horse_color"), HorseColorStreamGenerator.stream(), true, false)
            .generateMappedRegistry(HorseStyle.class, ResourceKey.minecraft("horse_style"), HorseStyleStreamGenerator.stream(), true, false)
            .generateMappedRegistry(LlamaType.class, ResourceKey.minecraft("llama_type"), LlamaTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(NotePitch.class, ResourceKey.minecraft("note_pitch"), NotePitchStreamGenerator.stream(), true, false)
            .generateMappedRegistry(ParrotType.class, ResourceKey.minecraft("parrot_type"), ParrotTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(RabbitType.class, ResourceKey.minecraft("rabbit_type"), RabbitTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(DataTranslator.class, ResourceKey.sponge("data_translator"), DataSerializers.stream(), true, false)
            .generateMappedRegistry(DisplaySlot.class, ResourceKey.sponge("display_slot"), DisplaySlotStreamGenerator.stream(), true, false)
        ;

        this.registerRegistry(SpongeDataRegistration.class, ResourceKey.sponge("data_registration"), (Registry) this.getRegistry(DataRegistration.class));

        this.registerDatapackCatalogues();
    }

    public void registerDatapackCatalogues() {
        this.datapackCatalogues.clear();
        this
            .registerRegistry(RecipeRegistration.class, ResourceKey.sponge("recipe"), true, true)
            .registerRegistry(Advancement.class, ResourceKey.minecraft("advancement"), true, true)
        ;
    }

    public <T extends CatalogType, E> SpongeCatalogRegistry generateRegistry(final Class<T> catalogClass, final ResourceKey key, final Stream<E> valueStream, final boolean generateSuppliers, final boolean isDynamic) {
        this.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), generateSuppliers, isDynamic, false);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateCallbackRegistry(final Class<T> catalogClass, final ResourceKey key, final Stream<E> valueStream, final BiConsumer<ResourceLocation, T> callback, final boolean generateSuppliers, final boolean isDynamic) {
        this.registerCallbackRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), callback, generateSuppliers, isDynamic);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateMappedRegistry(final Class<T> catalogClass, final ResourceKey key, final Stream<Tuple<T, E>> valueStream, final boolean generateSuppliers, final boolean isDynamic) {
        this.registerMappedRegistry(catalogClass, key, () -> valueStream.collect(Collectors.toSet()), generateSuppliers, isDynamic);
        return this;
    }
}
