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
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
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
import net.minecraft.world.BossInfo;
import net.minecraft.world.GameType;
import net.minecraft.world.raid.Raid;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.api.data.type.ChestAttachmentType;
import org.spongepowered.api.data.type.ComparatorMode;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.FoxType;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.Hinge;
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
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.SlabPortion;
import org.spongepowered.api.data.type.SpellType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.registry.CatalogRegistry;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.api.registry.UnknownTypeException;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.registry.builtin.sponge.BanTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.BodyPartStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DamageTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DismountTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.DisplaySlotStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.DragonPhaseTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.FireworkShapeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalExecutorTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.GoalTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseColorStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.HorseStyleStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.LlamaTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MusicDiscStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.NotePitchStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.ParrotTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.RabbitTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.BiomeSupplier;
import org.spongepowered.common.registry.builtin.sponge.CatTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.ContainerTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.CriteriaTriggersSupplier;
import org.spongepowered.common.registry.builtin.vanilla.DimensionTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.EffectSupplier;
import org.spongepowered.common.registry.builtin.vanilla.EnchantmentSupplier;
import org.spongepowered.common.registry.builtin.vanilla.EntityTypeSupplier;
import org.spongepowered.common.registry.builtin.sponge.EventContextKeyStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.FluidSupplier;
import org.spongepowered.common.registry.builtin.vanilla.ItemSupplier;
import org.spongepowered.common.registry.builtin.vanilla.PaintingTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.ParticleTypeSupplier;
import org.spongepowered.common.registry.builtin.vanilla.SoundEventSupplier;
import org.spongepowered.common.registry.builtin.sponge.TextColorStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.TextStyleTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.SpawnTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.TileEntityTypeSupplier;
import org.spongepowered.common.registry.builtin.sponge.WoodTypeStreamGenerator;
import org.spongepowered.common.registry.builtin.vanilla.VillagerProfessionSupplier;
import org.spongepowered.common.registry.builtin.sponge.AccountDeletionResultTypeStreamGenerator;

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

    @SuppressWarnings("ConstantConditions")
    public <T extends CatalogType> SpongeCatalogRegistry registerRegistry(Class<T> catalogClass, CatalogKey key, @Nullable Supplier<Set<T>> defaultsSupplier, boolean generateSuppliers) {
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

    private <T extends CatalogType, U> SpongeCatalogRegistry registerMappedRegistry(Class<T> catalogClass, CatalogKey key,
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

    public <T extends CatalogType, R extends Registry<T>> @Nullable R getRegistry(Class<T> catalogClass) {
        checkNotNull(catalogClass);
        return (R) this.registriesByType.get(catalogClass);
    }

    public <T extends CatalogType, R extends Registry<T>> @NonNull R requireRegistry(Class<T> catalogClass) {
        final R registry = this.getRegistry(catalogClass);
        if (registry == null) {
            throw new IllegalArgumentException("No registry is registered for " + catalogClass);
        }
        return registry;
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

        // TODO 1.14 - Text stuff needs to be registered as soon as possible in the engine, needed by BossBarOverlay (as an example)
        this
            .generateMappedRegistry(TextColor.class, CatalogKey.minecraft("text_color"), TextColorStreamGenerator.stream(), true)
            .generateMappedRegistry(TextStyle.Type.class, CatalogKey.minecraft("text_style"), TextStyleTypeStreamGenerator.stream(), true);

        this
            .generateRegistry(AccountDeletionResultType.class, CatalogKey.sponge("account_deletion_result_type"), AccountDeletionResultTypeStreamGenerator.stream(), true)
            .registerRegistry(Advancement.class, CatalogKey.minecraft("advancement"))
            .registerRegistry(AdvancementTree.class, CatalogKey.minecraft("advancement_tree"))
            .generateRegistry(AdvancementType.class, CatalogKey.minecraft("advancement_type"), Arrays.stream(FrameType.values()), true)
            .generateRegistry(BanType.class, CatalogKey.minecraft("ban_type"), BanTypeStreamGenerator.stream(), true)
            .generateRegistry(BannerPatternShape.class, CatalogKey.minecraft("banner_pattern_shape"), Arrays.stream(BannerPattern.values()), true)
            .generateRegistry(BossBarOverlay.class, CatalogKey.minecraft("boss_bar_overlay"), Arrays.stream(BossInfo.Overlay.values()), true)
            .generateRegistry(BossBarColor.class, CatalogKey.minecraft("boss_bar_color"), Arrays.stream(BossInfo.Color.values()), true)
            .generateRegistry(BodyPart.class, CatalogKey.minecraft("body_part"), BodyPartStreamGenerator.stream(), true)
            .generateRegistry(ChatType.class, CatalogKey.minecraft("chat_type"), Arrays.stream(net.minecraft.util.text.ChatType.values()), true)
            .generateRegistry(ChatVisibility.class, CatalogKey.minecraft("chat_visibility"), Arrays.stream(net.minecraft.entity.player.ChatVisibility.values()), true)
            .generateRegistry(ChestAttachmentType.class, CatalogKey.minecraft("chest_attachment_type"), Arrays.stream(ChestType.values()), true)
            .generateRegistry(CollisionRule.class, CatalogKey.minecraft("collision_rule"), Arrays.stream(Team.CollisionRule.values()), true)
            .generateRegistry(ComparatorMode.class, CatalogKey.minecraft("comparator_mode"), Arrays.stream(net.minecraft.state.properties.ComparatorMode.values()), true)
            .generateRegistry(Difficulty.class, CatalogKey.minecraft("difficulty"), Arrays.stream(net.minecraft.world.Difficulty.values()), true)
            .generateRegistry(DismountType.class, CatalogKey.minecraft("dismount_type"), DismountTypeStreamGenerator.stream(), true)
            .generateRegistry(DisplaySlot.class, CatalogKey.minecraft("display_slot"), DisplaySlotStreamGenerator.stream(), true)
            .generateRegistry(DragonPhaseType.class, CatalogKey.minecraft("dragon_phase_type"), DragonPhaseTypeStreamGenerator.stream(), true)
            .generateRegistry(DyeColor.class, CatalogKey.minecraft("dye_color"), Arrays.stream(net.minecraft.item.DyeColor.values()), true)
            .generateRegistry(FoxType.class, CatalogKey.minecraft("fox_type"), Arrays.stream(FoxEntity.Type.values()), true)
            .generateRegistry(GameMode.class, CatalogKey.minecraft("game_mode"), Arrays.stream(GameType.values()), true)
            .generateRegistry(GoalExecutorType.class, CatalogKey.minecraft("goal_executor_type"), GoalExecutorTypeStreamGenerator.stream(), true)
            .generateRegistry(HandPreference.class, CatalogKey.minecraft("hand_preference"), Arrays.stream(HandSide.values()), true)
            .generateRegistry(HandType.class, CatalogKey.minecraft("hand_type"), Arrays.stream(Hand.values()), true)
            .generateRegistry(Hinge.class, CatalogKey.minecraft("hinge"), Arrays.stream(DoorHingeSide.values()), true)
            .generateRegistry(InstrumentType.class, CatalogKey.minecraft("instrument_type"), Arrays.stream(NoteBlockInstrument.values()), true)
            .generateRegistry(MooshroomType.class, CatalogKey.minecraft("mooshroom_type"), Arrays.stream(MooshroomEntity.Type.values()), true)
            .generateRegistry(MusicDisc.class, CatalogKey.minecraft("music_disc"), MusicDiscStreamGenerator.stream(), true)
            .generateRegistry(PandaGene.class, CatalogKey.minecraft("panda_gene"), Arrays.stream(PandaEntity.Type.values()), true)
            .generateRegistry(PhantomPhase.class, CatalogKey.minecraft("phantom_phase"), Arrays.stream(PhantomEntity.AttackPhase.values()), true)
            .generateRegistry(PickupRule.class, CatalogKey.minecraft("pickup_rule"), Arrays.stream(AbstractArrowEntity.PickupStatus.values()), true)
            .generateRegistry(PistonType.class, CatalogKey.minecraft("piston_type"), Arrays.stream(net.minecraft.state.properties.PistonType.values()), true)
            .generateRegistry(PortionType.class, CatalogKey.minecraft("portion_type"), Arrays.stream(Half.values()), true)
            .generateRegistry(RaidStatus.class, CatalogKey.minecraft("raid_status"), Arrays.stream(Raid.Status.values()), true)
            .generateRegistry(RailDirection.class, CatalogKey.minecraft("rail_direction"), Arrays.stream(RailShape.values()), true)
            .generateRegistry(SlabPortion.class, CatalogKey.minecraft("slab_portion"), Arrays.stream(SlabType.values()), true)
            .generateRegistry(SpellType.class, CatalogKey.minecraft("spell_type"), Arrays.stream(SpellcastingIllagerEntity.SpellType.values()), true)
            .generateRegistry(StairShape.class, CatalogKey.minecraft("stair_shape"), Arrays.stream(StairsShape.values()), true)
            .generateRegistry(StructureMode.class, CatalogKey.minecraft("structure_mode"), Arrays.stream(net.minecraft.state.properties.StructureMode.values()), true)
            .generateRegistry(ToolType.class, CatalogKey.minecraft("tool_type"), Arrays.stream(ItemTier.values()), true)
            .generateRegistry(TropicalFishShape.class, CatalogKey.minecraft("tropical_fish_shape"), Arrays.stream(TropicalFishEntity.Type.values()), true)
            .generateRegistry(WireAttachmentType.class, CatalogKey.minecraft("wire_attachment_type"), Arrays.stream(RedstoneSide.values()), true)
            .generateRegistry(WoodType.class, CatalogKey.minecraft("wood_type"), WoodTypeStreamGenerator.stream(), true)
            .generateRegistry(Visibility.class, CatalogKey.minecraft("visibility"), Arrays.stream(Team.Visible.values()), true)
            .generateRegistry(SpawnType.class, CatalogKey.sponge("spawn_type"), SpawnTypeStreamGenerator.stream(), true)
            .generateRegistry(EventContextKey.class, CatalogKey.sponge("event_context_key"), EventContextKeyStreamGenerator.stream(), true)
            .generateRegistry(DamageType.class, CatalogKey.sponge("damage_type"), DamageTypeStreamGenerator.stream(), true)
            .registerRegistry(Currency.class, CatalogKey.sponge("currency"))
        ;

        this
            .generateMappedRegistry(CatType.class, CatalogKey.minecraft("cat_type"), CatTypeStreamGenerator.stream(), true)
            .generateMappedRegistry(FireworkShape.class, CatalogKey.minecraft("firework_shape"), FireworkShapeStreamGenerator.stream(), true)
            .generateMappedRegistry(GoalType.class, CatalogKey.minecraft("goal_type"), GoalTypeStreamGenerator.stream(), true)
            .generateMappedRegistry(HorseColor.class, CatalogKey.minecraft("horse_color"), HorseColorStreamGenerator.stream(), true)
            .generateMappedRegistry(HorseStyle.class, CatalogKey.minecraft("horse_style"), HorseStyleStreamGenerator.stream(), true)
            .generateMappedRegistry(LlamaType.class, CatalogKey.minecraft("llama_type"), LlamaTypeStreamGenerator.stream(), true)
            .generateMappedRegistry(NotePitch.class, CatalogKey.minecraft("note_pitch"), NotePitchStreamGenerator.stream(), true)
            .generateMappedRegistry(ParrotType.class, CatalogKey.minecraft("parrot_type"), ParrotTypeStreamGenerator.stream(), true)
            .generateMappedRegistry(RabbitType.class, CatalogKey.minecraft("rabbit_type"), RabbitTypeStreamGenerator.stream(), true)
        ;
    }

    /**
     * Only specify lines of registries found in {@link Registry} as these are true Vanilla registries
     */
    private void registerDefaultSuppliers() {

        // TODO 1.14 - This is not right but I don't want this forgotten so here for now
        // TODO 1.14 - Stats are stupid, need to handle them manually

        // Class based/Likely for mods to override
        BiomeSupplier.registerSuppliers(this);
        ContainerTypeSupplier.registerSuppliers(this);
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
        VillagerProfessionSupplier.registerSuppliers(this);
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateRegistry(Class<T> catalogClass, CatalogKey key, Stream<E> valueStream, boolean generateSuppliers) {
        this.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) (Object) value).collect(Collectors.toSet()), generateSuppliers);
        return this;
    }

    private <T extends CatalogType, E> SpongeCatalogRegistry generateMappedRegistry(Class<T> catalogClass, CatalogKey key, Stream<Tuple<T, E>> valueStream, boolean generateSuppliers) {
        this.registerMappedRegistry(catalogClass, key, () -> valueStream.collect(Collectors.toSet()), generateSuppliers);
        return this;
    }
}
