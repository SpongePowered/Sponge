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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemTier;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.GameType;
import net.minecraft.world.raid.Raid;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.type.ArmorMaterial;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.ChestAttachmentType;
import org.spongepowered.api.data.type.ComparatorMode;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.FoxType;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.DoorHinge;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.LlamaType;
import org.spongepowered.api.data.type.MooshroomType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.PandaGene;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.data.type.PhantomPhase;
import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.ProfessionType;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.SlabPortion;
import org.spongepowered.api.data.type.SpellType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroup;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.registry.CatalogRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.data.persistence.DataSerializers;
import org.spongepowered.common.event.lifecycle.RegisterCatalogEventImpl;
import org.spongepowered.common.registry.builtin.sponge.QueryTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.AccountDeletionResultTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.BanTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.BodyPartStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.CatTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.CatalogedValueParameterStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ClientCompletionTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.CommandRegistrarStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DamageTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DismountTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DisplaySlotStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.EventContextKeyStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalExecutorTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseColorStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseStyleStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.LlamaTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MusicDiscStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.NotePitchStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ParrotTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.PortalAgentTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.RabbitTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SpawnTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.WoodTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.IAttributeTypeRegistrar;
import org.spongepowered.common.registry.builtin.vanilla.BiomeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.BlockSupplier;
import org.spongepowered.common.registry.builtin.vanilla.ContainerTypeSupplier;
import org.spongepowered.common.registry.builtin.sponge.CriteriaTriggersRegistrar;
import org.spongepowered.common.registry.builtin.vanilla.DimensionTypeSupplier;
import org.spongepowered.common.registry.builtin.sponge.PhaseTypeRegistrar;
import org.spongepowered.common.registry.builtin.vanilla.EffectSupplier;
import org.spongepowered.common.registry.builtin.vanilla.EnchantmentSupplier;
import org.spongepowered.common.registry.builtin.vanilla.EntityTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.FluidSupplier;
import org.spongepowered.common.registry.builtin.vanilla.ItemSupplier;
import org.spongepowered.common.registry.builtin.vanilla.PaintingTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.ParticleTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.SoundEventSupplier;
import org.spongepowered.common.registry.builtin.vanilla.TileEntityTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.VillagerProfessionSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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

    public SpongeCatalogRegistry() {
        this.suppliers = new IdentityHashMap<>();
        this.registries = new Object2ObjectOpenHashMap<>();
        this.registriesByType = new IdentityHashMap<>();
        this.dynamicCatalogs = new ArrayList<>();

        this.registerVanillaRegistries();
    }

    @Override
    public <T extends CatalogType, E extends T> Supplier<E> provideSupplier(final Class<T> catalogClass, final String suggestedId) {
        Preconditions.checkNotNull(suggestedId);

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
    public <T extends CatalogType> Optional<T> get(final Class<T> typeClass, final ResourceKey key) {
        Preconditions.checkNotNull(key);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Optional.empty();
        }

        return (Optional<T>) registry.getValue((ResourceLocation) (Object) key);
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        if (registry == null) {
            return Collections.emptyList();
        }
        return ImmutableSet.<T>copyOf((Set<T>) (Object) (((SimpleRegistryAccessor) registry).accessor$getRegistryObjects().values()));
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllOf(final Class<T> typeClass) {
        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final Stream<T> stream;
        if (registry == null) {
            stream = Stream.empty();
        } else {
            stream = (Stream<T>) (Object) ((SimpleRegistryAccessor) registry).accessor$getRegistryObjects().values().stream();
        }

        return stream;
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllFor(final Class<T> typeClass, final String namespace) {
        Preconditions.checkNotNull(namespace);

        final Registry<CatalogType> registry = this.registriesByType.get(typeClass);
        final List<T> types = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, Object> entry : ((SimpleRegistryAccessor) registry).accessor$getRegistryObjects().entrySet()) {
            if (entry.getKey().getNamespace().equals(namespace)) {
                types.add((T) entry.getValue());
            }
        }

        return types;
    }

    @Override
    public <T extends CatalogType> Stream<T> streamAllFor(final Class<T> typeClass, final String namespace) {
        Preconditions.checkNotNull(namespace);

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

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerCatalogAndSupplier(final Class<E> catalogClass, final String suggestedId, Supplier<E> supplier) {
        Preconditions.checkNotNull(supplier);

        // Typically this isn't safe but for fake vanilla registries we can do it
        final E value = supplier.get();

        final Registry<CatalogType> registry = this.registriesByType.get(catalogClass);
        ((SimpleRegistry) registry).register((ResourceLocation) (Object) ((CatalogType) value).getKey(), value);

        return this.registerSupplier(catalogClass, suggestedId, supplier);
    }

    public <T extends CatalogType, E extends T> SpongeCatalogRegistry registerSupplier(final Class<E> catalogClass, final String suggestedId, Supplier<E> supplier) {
        Preconditions.checkNotNull(supplier);

        final Map<String, Supplier<CatalogType>> catalogSuppliers = this.suppliers.computeIfAbsent((Class<CatalogType>) (Object) catalogClass, k -> new Object2ObjectArrayMap<>());
        if (catalogSuppliers.containsKey(suggestedId)) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' with id '%s' has a supplier already registered!", catalogClass, suggestedId));
        }

        catalogSuppliers.put(suggestedId, (Supplier<CatalogType>) (Object) supplier);
        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, final boolean isDynamic) {
        Preconditions.checkNotNull(key);

        return this.registerRegistry(catalogClass, key, null, false, isDynamic);
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key,
            @Nullable final Supplier<Set<T>> defaultsSupplier, final boolean generateSuppliers, final boolean isDynamic) {
        Preconditions.checkNotNull(key);

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
            this.dynamicCatalogs.add(catalogClass);
        }

        return this;
    }

    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(final Class<T> catalogClass, final ResourceKey key, final Registry<T> registry) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(registry);

        this.registries.put(key, (Registry<CatalogType>) registry);
        this.registriesByType.put((Class<CatalogType>) catalogClass, (Registry<CatalogType>) registry);
        return this;
    }

    private <T extends CatalogType> SpongeCatalogRegistry generateCallbackRegistry(Class<T> catalogClass, ResourceKey key, BiConsumer<ResourceLocation, T> callback) {
        Preconditions.checkNotNull(key);

        final Registry<CatalogType> registry = this.registries.putIfAbsent(key, (Registry<CatalogType>) new CallbackRegistry<>(callback));
        if (registry != null) {
            throw new DuplicateRegistrationException(String.format("Catalog '%s' already has a registry registered for '%s'!", catalogClass, key));
        }
        this.registriesByType.put((Class<CatalogType>) catalogClass, registry);

        return this;
    }

    private <T extends CatalogType, U> SpongeCatalogRegistry registerMappedRegistry(final Class<T> catalogClass, final ResourceKey key,
            @Nullable final Supplier<Set<Tuple<T, U>>> defaultsSupplier, final boolean generateSuppliers, final boolean isDynamic) {
        Preconditions.checkNotNull(key);

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
                    SpongeCatalogRegistry.this.suppliers.computeIfAbsent((Class<CatalogType>) catalogClass, k -> new HashMap<>()).put(key.getValue(), kv::getFirst);
                }
            });
        }

        if (isDynamic) {
            this.dynamicCatalogs.add(catalogClass);
        }

        return this;
    }

    public <T extends CatalogType, R extends Registry<T>> @Nullable R getRegistry(Class<T> catalogClass) {
        return (R) this.registriesByType.get(catalogClass);
    }

    public <T extends CatalogType, R extends Registry<T>> @NonNull R requireRegistry(Class<T> catalogClass) {
        final R registry = this.getRegistry(catalogClass);
        if (registry == null) {
            throw new IllegalArgumentException("No registry is registered for " + catalogClass);
        }
        return registry;
    }

    public <C extends CatalogType> C registerCatalog(C catalogType) {
        Preconditions.checkNotNull(catalogType);

        final Registry<C> registry = (Registry<C>) this.registriesByType.get(catalogType.getClass());
        if (registry == null) {
            throw new UnknownTypeException(String.format("Catalog '%s' with id '%s' has no registry registered!", catalogType.getClass(), catalogType.getKey()));
        }

        return ((SimpleRegistry<C>) registry).register((ResourceLocation) (Object) catalogType.getKey(), catalogType);
    }

    public void callRegisterCatalogEvents(final Cause cause, final Game game) {
        for (final Class<? extends CatalogType> dynamicCatalog : this.dynamicCatalogs) {
            final TypeToken<? extends CatalogType> token = TypeToken.of(dynamicCatalog);
            game.getEventManager().post(new RegisterCatalogEventImpl<>(cause, game, (TypeToken<CatalogType>) token));
        }
    }

    private void registerVanillaRegistries() {
        this.registerRegistry(BiomeType.class, ResourceKey.minecraft("biome_type"), (Registry<BiomeType>) (Object) Registry.BIOME);
        this.registerRegistry(BlockType.class, ResourceKey.minecraft("block_type"), (Registry<BlockType>) (Object) Registry.BLOCK);
        this.registerRegistry(ContainerType.class, ResourceKey.minecraft("container_type"), (Registry<ContainerType>) (Object) Registry.MENU);
        this.registerRegistry(DimensionType.class, ResourceKey.minecraft("dimension_type"), (Registry<DimensionType>) (Object) Registry.DIMENSION_TYPE);
        this.registerRegistry(PotionEffectType.class, ResourceKey.minecraft("potion_effect_type"), (Registry<PotionEffectType>) (Object) Registry.EFFECTS);
        this.registerRegistry(EnchantmentType.class, ResourceKey.minecraft("enchantment_type"), (Registry<EnchantmentType>) (Object) Registry.ENCHANTMENT);
        this.registerRegistry(EntityType.class, ResourceKey.minecraft("entity_type"), (Registry<EntityType>) (Object) Registry.ENTITY_TYPE);
        this.registerRegistry(FluidType.class, ResourceKey.minecraft("fluid_type"), (Registry<FluidType>) (Object) Registry.FLUID);
        this.registerRegistry(ArtType.class, ResourceKey.minecraft("art_type"), (Registry<ArtType>) (Object) Registry.MOTIVE);
        this.registerRegistry(ParticleType.class, ResourceKey.minecraft("particle_type"), (Registry<ParticleType>) (Object) Registry.PARTICLE_TYPE);
        this.registerRegistry(SoundType.class, ResourceKey.minecraft("sound_type"), (Registry<SoundType>) (Object) Registry.SOUND_EVENT);
        this.registerRegistry(BlockEntityType.class, ResourceKey.minecraft("block_entity_type"), (Registry<BlockEntityType>) (Object) Registry.BLOCK_ENTITY_TYPE);
        this.registerRegistry(ProfessionType.class, ResourceKey.minecraft("profession_type"), (Registry<ProfessionType>) (Object) Registry.VILLAGER_PROFESSION);
        this.registerRegistry(VillagerType.class, ResourceKey.minecraft("villager_type"), (Registry<VillagerType>) (Object) Registry.VILLAGER_TYPE);

        // Que the "I'm Vanilla and I'm fucking stupid" music
        CriteriaTriggersRegistrar.registerRegistry(this);
        IAttributeTypeRegistrar.registerRegistry(this);
        PhaseTypeRegistrar.registerRegistry(this);
    }

    /**
     * Only specify lines of registries that are not found in {@link Registry}.
     */
    public void registerDefaultRegistries() {

        // TODO 1.14 - We'll take on a case by case basis if any mods are extending/replacing Enum values and therefore breaks this. Otherwise it will
        // TODO 1.14 - get to the point of insanity if literally every enum in the game becomes hardcoded lines that we have to map out...

        // TODO 1.14 - Text stuff needs to be registered as soon as possible in the engine, needed by BossBarOverlay (as an example)
//        this
//            .generateMappedRegistry(TextColor.class, ResourceKey.minecraft("text_color"), TextColorStreamGenerator.stream(), true)
//            .generateMappedRegistry(TextStyle.Type.class, ResourceKey.minecraft("text_style"), TextStyleTypeStreamGenerator.stream(), true)
//            .generateRegistry(TextSerializer.class, ResourceKey.minecraft("text_serializer"), TextSerializerStreamGenerator.stream(), true)
//        ;

        this
            .generateRegistry(AccountDeletionResultType.class, ResourceKey.sponge("account_deletion_result_type"), AccountDeletionResultTypeStreamGenerator.stream(), true, false)
            .registerRegistry(Advancement.class, ResourceKey.minecraft("advancement"), false)
            .registerRegistry(AdvancementTree.class, ResourceKey.minecraft("advancement_tree"), false)
//            .generateRegistry(AdvancementType.class, ResourceKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true)
            .generateRegistry(ArmorMaterial.class, ResourceKey.minecraft("armor_material"), Arrays.stream(net.minecraft.item.ArmorMaterial.values()), true, false)
            .generateRegistry(AttributeOperation.class, ResourceKey.minecraft("attribute_operation"), Arrays.stream(AttributeModifier.Operation.values()), true, false)
            .generateRegistry(BanType.class, ResourceKey.minecraft("ban_type"), BanTypeStreamGenerator.stream(), true, false)
            .generateRegistry(BannerPatternShape.class, ResourceKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true, false)
//            .generateRegistry(BossBarOverlay.class, ResourceKey.minecraft("boss_bar_overlay"), Arrays.stream(BossInfo.Overlay.values()), true)
//            .generateRegistry(BossBarColor.class, ResourceKey.minecraft("boss_bar_color"), Arrays.stream(BossInfo.Color.values()), true)
            .generateRegistry(BodyPart.class, ResourceKey.minecraft("body_part"), BodyPartStreamGenerator.stream(), true, false)
            .generateRegistry(ClientCompletionType.class, ResourceKey.sponge("client_completion"), ClientCompletionTypeStreamGenerator.stream(), true, false)
//            .generateRegistry(ChatType.class, ResourceKey.minecraft("chat_type"), Arrays.stream(net.minecraft.util.text.ChatType.values()), true)
            .generateRegistry(ChatVisibility.class, ResourceKey.minecraft("chat_visibility"), Arrays.stream(net.minecraft.entity.player.ChatVisibility.values()), true, false)
            .generateRegistry(ChestAttachmentType.class, ResourceKey.minecraft("chest_attachment_type"), Arrays.stream(ChestType.values()), true, false)
            .generateRegistry(CollisionRule.class, ResourceKey.minecraft("collision_rule"), Arrays.stream(Team.CollisionRule.values()), true, false)
            .generateRegistry(ComparatorMode.class, ResourceKey.minecraft("comparator_mode"), Arrays.stream(net.minecraft.state.properties.ComparatorMode.values()), true, false)
            .registerRegistry(Currency.class, ResourceKey.sponge("currency"), true)
            .generateRegistry(DamageType.class, ResourceKey.sponge("damage_type"), DamageTypeStreamGenerator.stream(), true, true)
            .generateRegistry(Difficulty.class, ResourceKey.minecraft("difficulty"), Arrays.stream(net.minecraft.world.Difficulty.values()), true, false)
            .generateRegistry(DismountType.class, ResourceKey.minecraft("dismount_type"), DismountTypeStreamGenerator.stream(), true, false)
            .generateRegistry(DyeColor.class, ResourceKey.minecraft("dye_color"), Arrays.stream(net.minecraft.item.DyeColor.values()), true, false)
            .generateRegistry(CatalogedValueParameter.class, ResourceKey.sponge("value_parameter"), CatalogedValueParameterStreamGenerator.stream(), true, true)
            .generateRegistry(CommandRegistrar.class, ResourceKey.sponge("command_registrar"), CommandRegistrarStreamGenerator.stream(), true, true)
            .generateRegistry(EquipmentGroup.class, ResourceKey.minecraft("equipment_group"), Arrays.stream(EquipmentSlotType.Group.values()), true, false)
            .generateRegistry(EquipmentType.class, ResourceKey.minecraft("equipment_type"), Arrays.stream(EquipmentSlotType.values()), true, false)
            .generateRegistry(EventContextKey.class, ResourceKey.sponge("event_context_key"), EventContextKeyStreamGenerator.stream(), true, true)
            .generateRegistry(FoxType.class, ResourceKey.minecraft("fox_type"), Arrays.stream(FoxEntity.Type.values()), true, false)
            .generateRegistry(GameMode.class, ResourceKey.minecraft("game_mode"), Arrays.stream(GameType.values()), true, false)
            .generateRegistry(GoalExecutorType.class, ResourceKey.minecraft("goal_executor_type"), GoalExecutorTypeStreamGenerator.stream(), true, false)
            .generateRegistry(HandPreference.class, ResourceKey.minecraft("hand_preference"), Arrays.stream(HandSide.values()), true, false)
            .generateRegistry(HandType.class, ResourceKey.minecraft("hand_type"), Arrays.stream(Hand.values()), true, false)
            .generateRegistry(DoorHinge.class, ResourceKey.minecraft("door_hinge"), Arrays.stream(DoorHingeSide.values()), true, false)
            .generateRegistry(InstrumentType.class, ResourceKey.minecraft("instrument_type"), Arrays.stream(NoteBlockInstrument.values()), true, false)
            .generateRegistry(MooshroomType.class, ResourceKey.minecraft("mooshroom_type"), Arrays.stream(MooshroomEntity.Type.values()), true, false)
            .generateRegistry(MusicDisc.class, ResourceKey.minecraft("music_disc"), MusicDiscStreamGenerator.stream(), true, false)
            .generateRegistry(PandaGene.class, ResourceKey.minecraft("panda_gene"), Arrays.stream(PandaEntity.Type.values()), true, false)
            .generateRegistry(PhantomPhase.class, ResourceKey.minecraft("phantom_phase"), Arrays.stream(PhantomEntity.AttackPhase.values()), true, false)
            .generateRegistry(PickupRule.class, ResourceKey.minecraft("pickup_rule"), Arrays.stream(AbstractArrowEntity.PickupStatus.values()), true, false)
            .generateRegistry(PistonType.class, ResourceKey.minecraft("piston_type"), Arrays.stream(net.minecraft.state.properties.PistonType.values()), true, false)
            .generateRegistry(PortalAgentType.class, ResourceKey.minecraft("portal_agent_type"), PortalAgentTypeStreamGenerator.stream(), true, false)
            .generateRegistry(PortionType.class, ResourceKey.minecraft("portion_type"), Arrays.stream(Half.values()), true, false)
            .generateRegistry(QueryType.class, ResourceKey.sponge("query_type"), QueryTypeStreamGenerator.stream(), true, true)
            .generateRegistry(RaidStatus.class, ResourceKey.minecraft("raid_status"), Arrays.stream(Raid.Status.values()), true, false)
            .generateRegistry(RailDirection.class, ResourceKey.minecraft("rail_direction"), Arrays.stream(RailShape.values()), true, false)
            .generateRegistry(SlabPortion.class, ResourceKey.minecraft("slab_portion"), Arrays.stream(SlabType.values()), true, false)
            .generateRegistry(SpawnType.class, ResourceKey.sponge("spawn_type"), SpawnTypeStreamGenerator.stream(), true, true)
            .generateRegistry(SpellType.class, ResourceKey.minecraft("spell_type"), Arrays.stream(SpellcastingIllagerEntity.SpellType.values()), true, false)
            .generateRegistry(StairShape.class, ResourceKey.minecraft("stair_shape"), Arrays.stream(StairsShape.values()), true, false)
            .generateRegistry(StructureMode.class, ResourceKey.minecraft("structure_mode"), Arrays.stream(net.minecraft.state.properties.StructureMode.values()), true, false)
            .generateRegistry(ToolType.class, ResourceKey.minecraft("tool_type"), Arrays.stream(ItemTier.values()), true, false)
            .generateRegistry(TropicalFishShape.class, ResourceKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true, false)
            .generateRegistry(WireAttachmentType.class, ResourceKey.minecraft("wire_attachment_type"), Arrays.stream(RedstoneSide.values()), true, false)
            .generateRegistry(WoodType.class, ResourceKey.minecraft("wood_type"), WoodTypeStreamGenerator.stream(), true, false)
            .generateRegistry(Visibility.class, ResourceKey.minecraft("visibility"), Arrays.stream(Team.Visible.values()), true, false)
        ;

        this
            .generateMappedRegistry(CatType.class, ResourceKey.minecraft("cat_type"), CatTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(GoalType.class, ResourceKey.minecraft("goal_type"), GoalTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(HorseColor.class, ResourceKey.minecraft("horse_color"), HorseColorStreamGenerator.stream(), true, false)
            .generateMappedRegistry(HorseStyle.class, ResourceKey.minecraft("horse_style"), HorseStyleStreamGenerator.stream(), true, false)
            .generateMappedRegistry(LlamaType.class, ResourceKey.minecraft("llama_type"), LlamaTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(NotePitch.class, ResourceKey.minecraft("note_pitch"), NotePitchStreamGenerator.stream(), true, false)
            .generateMappedRegistry(ParrotType.class, ResourceKey.minecraft("parrot_type"), ParrotTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(RabbitType.class, ResourceKey.minecraft("rabbit_type"), RabbitTypeStreamGenerator.stream(), true, false)
            .generateMappedRegistry(DataTranslator.class, ResourceKey.sponge("data_translator"), DataSerializers.stream(), true, false)
            .generateMappedRegistry(DisplaySlot.class, ResourceKey.minecraft("display_slot"), DisplaySlotStreamGenerator.stream(), true, false)
        ;
//
//        this.generateCallbackRegistry(DataRegistration.class, ResourceKey.sponge("data_registration"), (key, value) -> SpongeDataManager.getInstance().registerDataRegistration((SpongeDataRegistration) value));
    }

    /**
     * Only specify lines of registries found in {@link Registry} as these are true Vanilla registries
     */
    public void registerDefaultSuppliers() {

        // TODO 1.14 - Stats are stupid, need to handle them manually

        // Class based/Likely for mods to override
        BiomeSupplier.registerSuppliers(this);
        BlockSupplier.registerSuppliers(this);
        ContainerTypeSupplier.registerSuppliers(this);
        CriteriaTriggersRegistrar.registerSuppliers(this);
        DimensionTypeSupplier.registerSuppliers(this);
        EffectSupplier.registerSuppliers(this);
        EnchantmentSupplier.registerSuppliers(this);
        EntityTypeSupplier.registerSuppliers(this);
        FluidSupplier.registerSuppliers(this);
        IAttributeTypeRegistrar.registerSuppliers(this);
        ItemSupplier.registerSuppliers(this);
        PaintingTypeSupplier.registerSuppliers(this);
        ParticleTypeSupplier.registerSuppliers(this);
        PhaseTypeRegistrar.registerSuppliers(this);
        SoundEventSupplier.registerSuppliers(this);
        TileEntityTypeSupplier.registerSuppliers(this);
        VillagerProfessionSupplier.registerSuppliers(this);
    }

    public <T extends CatalogType, E> SpongeCatalogRegistry generateRegistry(final Class<T> catalogClass, final ResourceKey key, final Stream<E> valueStream, final boolean generateSuppliers, final boolean isDynamic) {
        this.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), generateSuppliers, isDynamic);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateMappedRegistry(final Class<T> catalogClass, final ResourceKey key, final Stream<Tuple<T, E>> valueStream, final boolean generateSuppliers, final boolean isDynamic) {
        this.registerMappedRegistry(catalogClass, key, () -> valueStream.collect(Collectors.toSet()), generateSuppliers, isDynamic);
        return this;
    }
}
