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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.FrameType;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.raid.Raid;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.ChestAttachmentType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.registry.CatalogRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.mixin.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.registry.builtin.BiomeSupplier;
import org.spongepowered.common.registry.builtin.CatTypeRegistry;
import org.spongepowered.common.registry.builtin.CriteriaTriggersSupplier;
import org.spongepowered.common.registry.builtin.DimensionTypeSupplier;
import org.spongepowered.common.registry.builtin.EffectSupplier;
import org.spongepowered.common.registry.builtin.EnchantmentSupplier;
import org.spongepowered.common.registry.builtin.EntityTypeSupplier;
import org.spongepowered.common.registry.builtin.FluidSupplier;
import org.spongepowered.common.registry.builtin.ItemSupplier;
import org.spongepowered.common.registry.builtin.PaintingTypeSupplier;
import org.spongepowered.common.registry.builtin.ParticleTypeSupplier;
import org.spongepowered.common.registry.builtin.SoundEventSupplier;
import org.spongepowered.common.registry.builtin.TextColorRegistry;
import org.spongepowered.common.registry.builtin.TextStyleRegistry;
import org.spongepowered.common.registry.builtin.TileEntityTypeSupplier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@SuppressWarnings("unchecked")
public final class SpongeCatalogRegistry implements CatalogRegistry {

    private final Map<Class<CatalogType>, Map<String, Supplier<CatalogType>>> suppliers;
    private final Map<CatalogKey, Registry<CatalogType>> registries;
    private final Map<Class<CatalogType>, Registry<CatalogType>> registriesByType;

    public SpongeCatalogRegistry() {
        this.suppliers = new IdentityHashMap<>();
        this.registries = new Object2ObjectOpenHashMap<>();
        this.registriesByType = new IdentityHashMap<>();
    }

    @Override
    public <T extends CatalogType, E extends T> Supplier<E> provideSupplier(Class<T> catalogClass, String suggestedId) {
        checkNotNull(catalogClass);
        checkNotNull(suggestedId);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.get(catalogClass);
        if (catalogSuppliers == null) {
            throw new UnknownTypeException(String.format("Supplier for type '%s' has not been registered!", catalogClass));
        }

        final Supplier<CatalogType> catalogSupplier = catalogSuppliers.get(suggestedId.toLowerCase());
        if (catalogSupplier == null) {
            throw new UnknownTypeException(String.format("Supplier for type '%s' with id '%s' has not been registered!", catalogClass, suggestedId));
        }

        return (Supplier<E>) (Object) catalogSupplier;
    }

    @Override
    public <T extends CatalogType> Optional<T> get(Class<T> typeClass, CatalogKey key) {
        final Registry<CatalogType> registry = this.registries.get(typeClass);
        if (registry == null) {
            return Optional.empty();
        }

        return (Optional<T>) registry.getValue((ResourceLocation) (Object) key);
    }

    @Override
    public <T extends CatalogType> Stream<T> getAllOf(Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registries.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) Arrays.stream(((SimpleRegistryAccessor) registry).accessor$getValues());
        }

        return stream;
    }

    @Override
    public <T extends CatalogType> Stream<T> getAllFor(Class<T> typeClass, String namespace) {
        checkNotNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) ((SimpleRegistryAccessor) registry).accessor$getRegistryObjects()
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().getNamespace().equals(namespace))
                .map(Map.Entry::getValue);
        }

        return stream;
    }

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerSupplier(Class<E> catalogClass, String suggestedId,
        Supplier<E> supplier) {
        checkNotNull(catalogClass);
        checkNotNull(supplier);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.computeIfAbsent((Class<CatalogType>) (Object) catalogClass, k -> new Object2ObjectArrayMap<>());
        if (catalogSuppliers.containsKey(suggestedId)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' with id '%s' has a supplier already registered!", catalogClass,
                suggestedId));
        }

        catalogSuppliers.put(suggestedId, (Supplier<CatalogType>) (Object) supplier);
        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(Class<T> catalogClass, CatalogKey key) {
        checkNotNull(catalogClass);
        checkNotNull(key);

        return this.registerRegistry(catalogClass, key, null, false);
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(Class<T> catalogClass, CatalogKey key,
        @Nullable Supplier<Set<T>> defaultsSupplier, boolean generateSuppliers) {
        checkNotNull(catalogClass);
        checkNotNull(key);

        if (this.registries.get(key) != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered!", catalogClass));
        }

        final SimpleRegistry<T> registry = new SimpleRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(catalogType -> {
                registry.register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(key.getValue(), () -> catalogType);
                }
            });
        }
        return this;
    }

    public SpongeCatalogRegistry registerRegistry(Class<CatalogType> catalogClass, CatalogKey key, Registry<CatalogType> registry) {
        checkNotNull(key);
        checkNotNull(registry);

        this.registries.put(key, registry);
        this.registriesByType.put(catalogClass, registry);
        return this;
    }

    public <T extends CatalogType, U> SpongeCatalogRegistry registerMappedRegistry(Class<T> catalogClass, CatalogKey key,
        @Nullable Supplier<Set<Tuple<T, U>>> defaultsSupplier, boolean generateSuppliers) {

        checkNotNull(catalogClass);
        checkNotNull(key);

        if (this.registries.get(key) != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered!", catalogClass));
        }

        final MappedRegistry<T, U> registry = new MappedRegistry<>();

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);

        if (defaultsSupplier != null) {
            defaultsSupplier.get().forEach(kv -> {
                registry.register((ResourceLocation) (Object) kv.getFirst().getKey(), kv.getFirst());
                registry.registerMapping(kv.getFirst(), kv.getSecond());

                if (generateSuppliers) {
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(key.getValue(), kv::getFirst);
                }
            });
        }
        return this;
    }

    public <T extends CatalogType> SimpleRegistry<T> getRegistry(Class<T> catalogClass) {
        checkNotNull(catalogClass);

        return (SimpleRegistry<T>) (Object) this.registriesByType.get(catalogClass);
    }

    public SpongeCatalogRegistry registerCatalog(CatalogType catalogType) {
        checkNotNull(catalogType);

        final Registry<CatalogType> registry = this.registriesByType.get(catalogType.getClass());
        if (registry == null) {
            throw new UnknownTypeException(String.format("Catalog '%s' with id '%s' has no registry registered!", catalogType.getClass(), catalogType.getKey()));
        }

        ((SimpleRegistry<CatalogType>) registry).register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
        return this;
    }

    /**
     * Only specify lines of registries that are not found in {@link Registry}.
     */
    public void registerDefaultRegistries() {

        // TODO 1.14 - We'll take on a case by case basis if any mods are extending/replacing Enum values and therefore breaks this. Otherwise it will
        // TODO 1.14 - get to the point of insanity if literally every enum in the game becomes hardcoded lines that we have to map out...

        this
            .registerRegistry(Advancement.class, CatalogKey.minecraft("advancement"))
            .registerRegistry(AdvancementTree.class, CatalogKey.minecraft("advancement_tree"))
            .generateRegistry(AdvancementType.class, CatalogKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true)
            .generateRegistry(ChestAttachmentType.class, CatalogKey.minecraft("chest_attachment_type"), Arrays.stream(ChestType.values()), true)
            .generateRegistry(CollisionRule.class, CatalogKey.minecraft("collision_rule"), Arrays.stream(Team.CollisionRule.values()), true)
            .generateRegistry(DyeColor.class, CatalogKey.minecraft("dye_color"), Arrays.stream(net.minecraft.item.DyeColor.values()), true)
            .generateRegistry(RaidStatus.class, CatalogKey.minecraft("raid_status"), Arrays.stream(Raid.Status.values()), true)
            .generateRegistry(Visibility.class, CatalogKey.minecraft("visibility"), Arrays.stream(Team.Visible.values()), true)
        ;

        // Oddballs
        CatTypeRegistry.generateRegistry(this);
        TextColorRegistry.generateRegistry(this);
        TextStyleRegistry.generateRegistry(this);
    }

    /**
     * Only specify lines of registries found in {@link Registry} as these are true Vanilla registries
     */
    private void registerDefaultSuppliers() {

        // TODO 1.14 - This is not right but I don't want this forgotten so here for now
        // TODO 1.14 - Stats are stupid, need to handle them manually

        // TODO 1.14 - We'll take on a case by case basis if any mods are extending/replacing Enum values and therefore breaks this. Otherwise it will
        // TODO 1.14 - get to the point of insanity if literally every enum in the game becomes hardcoded lines that we have to map out...

        this
            .generateSuppliers(BannerPatternShape.class, Arrays.stream(BannerPattern.values()))
            .generateSuppliers(InstrumentType.class, Arrays.stream(NoteBlockInstrument.values()))
        ;

        // Class based/Likely for mods to override
        BiomeSupplier.registerSuppliers(this);
        BiomeSupplier.registerSuppliers(this);
        CriteriaTriggersSupplier.registerSuppliers(this);
        DimensionTypeSupplier.registerSuppliers(this);
        EffectSupplier.registerSuppliers(this);
        EnchantmentSupplier.registerSuppliers(this);
        EntityTypeSupplier.registerSuppliers(this);
        FluidSupplier.registerSuppliers(this);
        ItemSupplier.registerSuppliers(this);
        PaintingTypeSupplier.registerSuppliers(this);
        ParticleTypeSupplier.registerSuppliers(this);
        SoundEventSupplier.registerSuppliers(this);
        TileEntityTypeSupplier.registerSuppliers(this);
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateRegistry(Class<T> catalogClass, CatalogKey key, Stream<E> valueStream, boolean generateSuppliers) {
        this.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) (Object) value).collect(Collectors.toSet()), generateSuppliers);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateSuppliers(Class<T> catalogClass, Stream<E> valueStream) {
        valueStream.map(value -> (T) value).forEach(value -> this.registerSupplier(catalogClass, value.getKey().getValue(), () -> value));
        return this;
    }
}
