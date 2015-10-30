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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockSnapshotBuilder;
import org.spongepowered.api.block.BlockStateBuilder;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.ImmutableDataRegistry;
import org.spongepowered.api.data.manipulator.DataManipulatorRegistry;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.api.data.value.ValueBuilder;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.EntitySnapshotBuilder;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSourceBuilder;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.item.FireworkEffectBuilder;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.ScoreboardBuilder;
import org.spongepowered.api.scoreboard.TeamBuilder;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.ObjectiveBuilder;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.EntityStatistic;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticBuilder;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.statistic.TeamStatistic;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.WorldBuilder;
import org.spongepowered.api.world.explosion.ExplosionBuilder;
import org.spongepowered.api.world.extent.ExtentBufferFactory;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.gen.PopulatorFactory;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.configuration.CatalogTypeTypeSerializer;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.SpongeImmutableRegistry;
import org.spongepowered.common.data.SpongeSerializationRegistry;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.value.SpongeValueBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleType;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeEntityMeta;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.event.cause.entity.damage.SpongeBlockDamageSourceBuilder;
import org.spongepowered.common.item.SpongeFireworkBuilder;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.potion.SpongePotionBuilder;
import org.spongepowered.common.registry.type.RotationRegistryModule;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeVisibility;
import org.spongepowered.common.scoreboard.builder.SpongeObjectiveBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeScoreboardBuilder;
import org.spongepowered.common.scoreboard.builder.SpongeTeamBuilder;
import org.spongepowered.common.status.SpongeFavicon;
import org.spongepowered.common.text.format.SpongeTextColor;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.world.SpongeExplosionBuilder;
import org.spongepowered.common.world.SpongeWorldBuilder;
import org.spongepowered.common.world.extent.SpongeExtentBufferFactory;
import org.spongepowered.common.world.gen.SpongePopulatorType;
import org.spongepowered.common.world.gen.WorldGeneratorRegistry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SpongeGameRegistry implements GameRegistry {

    private static final Map<Class<? extends CatalogType>, CatalogRegistryModule<?>> catalogRegistries = new MapMaker().concurrencyLevel(4).makeMap();
    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(CatalogType.class), new CatalogTypeTypeSerializer());
    }

    public static final Item NONE_ITEM = new Item().setUnlocalizedName("none").setMaxDamage(0).setMaxStackSize(1);
    public static final ItemStack NONE = (ItemStack)new net.minecraft.item.ItemStack(NONE_ITEM);

    public static net.minecraft.util.DamageSource DAMAGESOURCE_POISON;
    public static net.minecraft.util.DamageSource DAMAGESOURCE_MELTING;

    public static final Map<Class<? extends WorldProvider>, SpongeConfig<SpongeConfig.DimensionConfig>> dimensionConfigs = Maps.newHashMap();

    public static final Map<String, TextColor> textColorMappings = Maps.newHashMap();
    public static final Map<EnumChatFormatting, SpongeTextColor> enumChatColor = Maps.newEnumMap(EnumChatFormatting.class);

    public static final Map<String, Visibility> visibilityMappings = Maps.newHashMap();
    public static final Map<Team.EnumVisible, SpongeVisibility> enumVisible = Maps.newEnumMap(Team.EnumVisible.class);
    public static final Map<String, DamageType> damageSourceToTypeMappings = Maps.newHashMap();

    private static final ImmutableMap<String, ObjectiveDisplayMode> objectiveDisplayModeMappings =
            new ImmutableMap.Builder<String, ObjectiveDisplayMode>()
                    .put("integer", (ObjectiveDisplayMode) (Object) IScoreObjectiveCriteria.EnumRenderType.INTEGER)
                    .put("hearts", (ObjectiveDisplayMode) (Object) IScoreObjectiveCriteria.EnumRenderType.HEARTS)
                    .build();

    public final Map<Class<? extends net.minecraft.world.gen.feature.WorldGenerator>, PopulatorType> populatorClassToTypeMappings = Maps.newHashMap();
    public final Map<Class<? extends Entity>, EntityType> entityClassToTypeMappings = Maps.newHashMap();
    public final Map<Class<? extends TileEntity>, TileEntityType> tileClassToTypeMappings = Maps.newHashMap();
    private final Map<String, Career> careerMappings = Maps.newHashMap();
    private final Map<String, Profession> professionMappings = Maps.newHashMap();
    private final Map<Integer, List<Career>> professionToCareerMappings = Maps.newHashMap();
    private final Map<String, DimensionType> dimensionTypeMappings = Maps.newHashMap();
    public final Map<Class<? extends Dimension>, DimensionType> dimensionClassMappings = Maps.newHashMap();
    private final WorldGeneratorRegistry worldGeneratorRegistry = new WorldGeneratorRegistry();
    private final Hashtable<Class<? extends WorldProvider>, Integer> classToProviders = new Hashtable<>();
    private final Map<UUID, WorldProperties> worldPropertiesUuidMappings = Maps.newHashMap();
    private final Map<String, WorldProperties> worldPropertiesNameMappings = Maps.newHashMap();
    private final Map<Integer, String> worldFolderDimensionIdMappings = Maps.newHashMap();
    public final Map<UUID, String> worldFolderUniqueIdMappings = Maps.newHashMap();
    public final Map<String, SpongeDisplaySlot> displaySlotMappings = Maps.newLinkedHashMap();

    protected final Map<String, PopulatorType> populatorTypeMappings = Maps.newHashMap();
    public final Map<String, TileEntityType> tileEntityTypeMappings = Maps.newHashMap();

    public static final Map<String, BlockType> blockTypeMappings = Maps.newHashMap();
    public static final Map<String, ItemType> itemTypeMappings = Maps.newHashMap();

    protected final Map<Class<? extends CatalogType>, CatalogRegistryModule<?>> catalogRegistryMap = new IdentityHashMap<>();

    private final Map<Class<?>, Supplier<?>> builderSupplierMap = new IdentityHashMap<>();

    public SpongeGameRegistry() {
        registerBuilderSupplier(ItemStackBuilder.class, SpongeItemStackBuilder::new);
        registerBuilderSupplier(TradeOfferBuilder.class, SpongeTradeOfferBuilder::new);
        registerBuilderSupplier(FireworkEffectBuilder.class, SpongeFireworkBuilder::new);
        registerBuilderSupplier(PotionEffectBuilder.class, SpongePotionBuilder::new);
        registerBuilderSupplier(ObjectiveBuilder.class, SpongeObjectiveBuilder::new);
        registerBuilderSupplier(TeamBuilder.class, SpongeTeamBuilder::new);
        registerBuilderSupplier(ScoreboardBuilder.class, SpongeScoreboardBuilder::new);
        registerBuilderSupplier(StatisticBuilder.class, () -> {throw new UnsupportedOperationException();});
        registerBuilderSupplier(WorldBuilder.class, SpongeWorldBuilder::new);
        registerBuilderSupplier(ExplosionBuilder.class, SpongeExplosionBuilder::new);
        registerBuilderSupplier(ValueBuilder.class, SpongeValueBuilder::new);
        registerBuilderSupplier(BlockStateBuilder.class, SpongeBlockStateBuilder::new);
        registerBuilderSupplier(BlockSnapshotBuilder.class, SpongeBlockSnapshotBuilder::new);
        registerBuilderSupplier(EntitySnapshotBuilder.class, SpongeEntitySnapshotBuilder::new);
        registerBuilderSupplier(BlockDamageSourceBuilder.class, SpongeBlockDamageSourceBuilder::new);

    }

    /**
     * Registers the {@link CatalogRegistryModule} for handling the registry stuffs.
     *
     * @param catalogClass
     * @param registryModule
     * @param <T>
     */
    public <T extends CatalogType> void registerModule(Class<T> catalogClass, CatalogRegistryModule<T> registryModule) {
        checkArgument(!this.catalogRegistryMap.containsKey(catalogClass), "Already registered a registry module!");
        this.catalogRegistryMap.put(catalogClass, registryModule);
    }

    public <T> SpongeGameRegistry registerBuilderSupplier(Class<T> builderClass, Supplier<? extends T> supplier) {
        checkArgument(!this.builderSupplierMap.containsKey(builderClass), "Already registered a builder supplier!");
        this.builderSupplierMap.put(builderClass, supplier);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> Optional<T> getType(Class<T> typeClass, String id) {
        CatalogRegistryModule<T> registryModule = (CatalogRegistryModule<T>) this.catalogRegistryMap.get(typeClass);
        if (registryModule == null) {
            return Optional.empty();
        } else {
            return registryModule.getById(id);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> Collection<T> getAllOf(Class<T> typeClass) {
        CatalogRegistryModule<T> registryModule = (CatalogRegistryModule<T>) this.catalogRegistryMap.get(typeClass);
        if (registryModule == null) {
            return Collections.emptyList();
        } else {
            return registryModule.getAll();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> createBuilderOfType(Class<T> builderClass) {
        if (!this.builderSupplierMap.containsKey(builderClass)) {
            return Optional.empty();
        } else {
            return Optional.of((T) this.builderSupplierMap.get(builderClass).get());
        }
    }

    @Override
    public ParticleEffectBuilder createParticleEffectBuilder(ParticleType particle) {
        checkNotNull(particle);

        if (particle instanceof SpongeParticleType.Colorable) {
            return new SpongeParticleEffectBuilder.BuilderColorable((SpongeParticleType.Colorable) particle);
        } else if (particle instanceof SpongeParticleType.Resizable) {
            return new SpongeParticleEffectBuilder.BuilderResizable((SpongeParticleType.Resizable) particle);
        } else if (particle instanceof SpongeParticleType.Note) {
            return new SpongeParticleEffectBuilder.BuilderNote((SpongeParticleType.Note) particle);
        } else if (particle instanceof SpongeParticleType.Material) {
            return new SpongeParticleEffectBuilder.BuilderMaterial((SpongeParticleType.Material) particle);
        } else {
            return new SpongeParticleEffectBuilder((SpongeParticleType) particle);
        }
    }

    @Override
    public List<String> getDefaultGameRules() {

        List<String> gameruleList = new ArrayList<>();
        for (Field f : DefaultGameRules.class.getFields()) {
            try {
                gameruleList.add((String) f.get(null));
            } catch (Exception e) {
                // Ignoring error
            }
        }
        return gameruleList;
    }

    @Override
    public List<Career> getCareers(Profession profession) {
        return this.professionToCareerMappings.get(((SpongeEntityMeta) profession).type);
    }

    public void registerDimensionType(DimensionType type) {
        this.dimensionTypeMappings.put(type.getName(), type);
        this.dimensionClassMappings.put(type.getDimensionClass(), type);
    }

    public void registerWorldProperties(WorldProperties properties) {
        this.worldPropertiesUuidMappings.put(properties.getUniqueId(), properties);
        this.worldPropertiesNameMappings.put(properties.getWorldName(), properties);
    }

    public void registerWorldDimensionId(int dim, String folderName) {
        this.worldFolderDimensionIdMappings.put(dim, folderName);
    }

    public void registerWorldUniqueId(UUID uuid, String folderName) {
        this.worldFolderUniqueIdMappings.put(uuid, folderName);
    }

    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return Optional.ofNullable(this.worldPropertiesNameMappings.get(worldName));
    }

    public Collection<WorldProperties> getAllWorldProperties() {
        return Collections.unmodifiableCollection(this.worldPropertiesNameMappings.values());
    }

    public String getWorldFolder(int dim) {
        return this.worldFolderDimensionIdMappings.get(dim);
    }

    public String getWorldFolder(UUID uuid) {
        return this.worldFolderUniqueIdMappings.get(uuid);
    }

    public int getProviderType(Class<? extends WorldProvider> provider) {
        return this.classToProviders.get(provider);
    }

    public WorldSettings.GameType getGameType(GameMode mode) {
        // TODO: This is client-only
        //return WorldSettings.GameType.getByName(mode.getTranslation().getId());
        throw new UnsupportedOperationException();
    }

    public Optional<WorldProperties> getWorldProperties(UUID uuid) {
        return Optional.ofNullable(this.worldPropertiesUuidMappings.get(uuid));
    }

    @Override
    public void registerWorldGeneratorModifier(WorldGeneratorModifier modifier) {
        this.worldGeneratorRegistry.registerModifier(modifier);
    }

    public WorldGeneratorRegistry getWorldGeneratorRegistry() {
        return this.worldGeneratorRegistry;
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        for (Rotation rotation : RotationRegistryModule.rotationMap.values()) {
            if (rotation.getAngle() == degrees) {
                return Optional.of(rotation);
            }
        }
        return Optional.empty();
    }

    @Override
    public GameProfile createGameProfile(UUID uuid, String name) {
        return (GameProfile) new com.mojang.authlib.GameProfile(uuid, name);
    }

    @Override
    public Favicon loadFavicon(String raw) throws IOException {
        return SpongeFavicon.load(raw);
    }

    @Override
    public Favicon loadFavicon(File file) throws IOException {
        return SpongeFavicon.load(file);
    }

    @Override
    public Favicon loadFavicon(URL url) throws IOException {
        return SpongeFavicon.load(url);
    }

    @Override
    public Favicon loadFavicon(InputStream in) throws IOException {
        return SpongeFavicon.load(in);
    }

    @Override
    public Favicon loadFavicon(BufferedImage image) throws IOException {
        return SpongeFavicon.load(image);
    }

    @Override
    public RecipeRegistry getRecipeRegistry() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public DataManipulatorRegistry getManipulatorRegistry() {
        return SpongeDataRegistry.getInstance();
    }

    @Override
    public ImmutableDataRegistry getImmutableDataRegistry() {
        return SpongeImmutableRegistry.getInstance();
    }

    @Override
    public Optional<Translation> getTranslationById(String id) {
        return Optional.<Translation>of(new SpongeTranslation(id));
    }

    private void setCareersAndProfessions() {
        try {
            Professions.class.getDeclaredField("FARMER").set(null, new SpongeProfession(0, "farmer"));
            Careers.class.getDeclaredField("FARMER").set(null, new SpongeCareer(0, "farmer", Professions.FARMER));
            Careers.class.getDeclaredField("FISHERMAN").set(null, new SpongeCareer(1, "fisherman", Professions.FARMER));
            Careers.class.getDeclaredField("SHEPHERD").set(null, new SpongeCareer(2, "shepherd", Professions.FARMER));
            Careers.class.getDeclaredField("FLETCHER").set(null, new SpongeCareer(3, "fletcher", Professions.FARMER));

            Professions.class.getDeclaredField("LIBRARIAN").set(null, new SpongeProfession(1, "librarian"));
            Careers.class.getDeclaredField("LIBRARIAN").set(null, new SpongeCareer(0, "librarian", Professions.LIBRARIAN));

            Professions.class.getDeclaredField("PRIEST").set(null, new SpongeProfession(2, "priest"));
            Careers.class.getDeclaredField("CLERIC").set(null, new SpongeCareer(0, "cleric", Professions.PRIEST));

            Professions.class.getDeclaredField("BLACKSMITH").set(null, new SpongeProfession(3, "blacksmith"));
            Careers.class.getDeclaredField("ARMORER").set(null, new SpongeCareer(0, "armor", Professions.BLACKSMITH));
            Careers.class.getDeclaredField("WEAPON_SMITH").set(null, new SpongeCareer(1, "weapon", Professions.BLACKSMITH));
            Careers.class.getDeclaredField("TOOL_SMITH").set(null, new SpongeCareer(2, "tool", Professions.BLACKSMITH));

            Professions.class.getDeclaredField("BUTCHER").set(null, new SpongeProfession(4, "butcher"));
            Careers.class.getDeclaredField("BUTCHER").set(null, new SpongeCareer(0, "butcher", Professions.BUTCHER));
            Careers.class.getDeclaredField("LEATHERWORKER").set(null, new SpongeCareer(1, "leatherworker", Professions.BUTCHER));

            this.professionMappings.put(Professions.FARMER.getName().toLowerCase(), Professions.FARMER);
            this.professionMappings.put(Professions.LIBRARIAN.getName().toLowerCase(), Professions.LIBRARIAN);
            this.professionMappings.put(Professions.PRIEST.getName().toLowerCase(), Professions.PRIEST);
            this.professionMappings.put(Professions.BLACKSMITH.getName().toLowerCase(), Professions.BLACKSMITH);
            this.professionMappings.put(Professions.BUTCHER.getName().toLowerCase(), Professions.BUTCHER);
            this.careerMappings.put(Careers.FARMER.getName().toLowerCase(), Careers.FARMER);
            this.careerMappings.put(Careers.FISHERMAN.getName().toLowerCase(), Careers.FISHERMAN);
            this.careerMappings.put(Careers.SHEPHERD.getName().toLowerCase(), Careers.SHEPHERD);
            this.careerMappings.put(Careers.FLETCHER.getName().toLowerCase(), Careers.FLETCHER);
            this.careerMappings.put(Careers.LIBRARIAN.getName().toLowerCase(), Careers.LIBRARIAN);
            this.careerMappings.put(Careers.CLERIC.getName().toLowerCase(), Careers.CLERIC);
            this.careerMappings.put(Careers.ARMORER.getName().toLowerCase(), Careers.ARMORER);
            this.careerMappings.put(Careers.WEAPON_SMITH.getName().toLowerCase(), Careers.WEAPON_SMITH);
            this.careerMappings.put(Careers.TOOL_SMITH.getName().toLowerCase(), Careers.TOOL_SMITH);
            this.careerMappings.put(Careers.BUTCHER.getName().toLowerCase(), Careers.BUTCHER);
            this.careerMappings.put(Careers.LEATHERWORKER.getName().toLowerCase(), Careers.LEATHERWORKER);
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.FARMER).type,
                    Arrays.asList(Careers.FARMER, Careers.FISHERMAN, Careers.SHEPHERD, Careers.FLETCHER));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.LIBRARIAN).type, Arrays.asList(Careers.LIBRARIAN));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.PRIEST).type, Arrays.asList(Careers.CLERIC));
            this.professionToCareerMappings
                    .put(((SpongeEntityMeta) Professions.BLACKSMITH).type, Arrays.asList(Careers.ARMORER, Careers.WEAPON_SMITH, Careers.TOOL_SMITH));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.BUTCHER).type, Arrays.asList(Careers.BUTCHER, Careers.LEATHERWORKER));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitleFactory() {
        // RegistryHelper.setFactory(Titles.class, new SpongeTitleFactory());
    }

    private void setDisplaySlots() {
        this.displaySlotMappings.put("list", new SpongeDisplaySlot("list", null, 0));
        this.displaySlotMappings.put("sidebar", new SpongeDisplaySlot("sidebar", null, 1));
        this.displaySlotMappings.put("below_name", new SpongeDisplaySlot("below_name", null, 2));

        RegistryHelper.mapFields(DisplaySlots.class, this.displaySlotMappings);

        for (Map.Entry<EnumChatFormatting, SpongeTextColor> entry : SpongeGameRegistry.enumChatColor.entrySet()) {
            this.displaySlotMappings.put(entry.getValue().getId(), new SpongeDisplaySlot(entry.getValue().getId(), entry.getValue(), entry.getKey()
                    .getColorIndex() + 3));
        }
    }

    private void addVisibility(String name, Team.EnumVisible handle) {
        SpongeVisibility visibility = new SpongeVisibility(handle);
        SpongeGameRegistry.visibilityMappings.put(name, visibility);
        SpongeGameRegistry.enumVisible.put(handle, visibility);
    }

    private void setVisibilities() {
        this.addVisibility("all", Team.EnumVisible.ALWAYS);
        this.addVisibility("own_team", Team.EnumVisible.HIDE_FOR_OTHER_TEAMS);
        this.addVisibility("other_teams", Team.EnumVisible.HIDE_FOR_OWN_TEAM);
        this.addVisibility("none", Team.EnumVisible.NEVER);

        RegistryHelper.mapFields(Visibilities.class, SpongeGameRegistry.visibilityMappings);
    }

    private void setObjectiveDisplayModes() {
        RegistryHelper.mapFields(ObjectiveDisplayModes.class, SpongeGameRegistry.objectiveDisplayModeMappings);
    }

    private void setPopulatorTypes() {
        this.populatorTypeMappings.put("big_mushroom", new SpongePopulatorType("big_mushroom", net.minecraft.world.gen.feature.WorldGenBigMushroom.class));
        this.populatorTypeMappings.put("big_tree", new SpongePopulatorType("big_tree", net.minecraft.world.gen.feature.WorldGenBigTree.class));
        this.populatorTypeMappings.put("birch_tree", new SpongePopulatorType("birch_tree", net.minecraft.world.gen.feature.WorldGenForest.class));
        this.populatorTypeMappings.put("block_blob", new SpongePopulatorType("block_blob", net.minecraft.world.gen.feature.WorldGenBlockBlob.class));
        this.populatorTypeMappings.put("bonus_chest", new SpongePopulatorType("bonus_chest", net.minecraft.world.gen.feature.WorldGeneratorBonusChest.class));
        this.populatorTypeMappings.put("bush", new SpongePopulatorType("bush", net.minecraft.world.gen.GeneratorBushFeature.class));
        this.populatorTypeMappings.put("cactus", new SpongePopulatorType("cactus", net.minecraft.world.gen.feature.WorldGenCactus.class));
        this.populatorTypeMappings.put("canopy_tree", new SpongePopulatorType("canopy_tree", net.minecraft.world.gen.feature.WorldGenCanopyTree.class));
        this.populatorTypeMappings.put("clay", new SpongePopulatorType("clay", net.minecraft.world.gen.feature.WorldGenClay.class));
        this.populatorTypeMappings.put("dead_bush", new SpongePopulatorType("dead_bush", net.minecraft.world.gen.feature.WorldGenDeadBush.class));
        this.populatorTypeMappings.put("desert_well", new SpongePopulatorType("desert_well", net.minecraft.world.gen.feature.WorldGenDesertWells.class));
        this.populatorTypeMappings.put("double_plant", new SpongePopulatorType("double_plant", net.minecraft.world.gen.feature.WorldGenBigMushroom.class));
        this.populatorTypeMappings.put("dungeon", new SpongePopulatorType("dungeon", net.minecraft.world.gen.feature.WorldGenDungeons.class));
        this.populatorTypeMappings.put("ender_crystal_platform", new SpongePopulatorType("ender_crystal_platform", net.minecraft.world.gen.feature.WorldGenSpikes.class));
        this.populatorTypeMappings.put("fire", new SpongePopulatorType("fire", net.minecraft.world.gen.feature.WorldGenFire.class));
        this.populatorTypeMappings.put("flower", new SpongePopulatorType("flower", net.minecraft.world.gen.feature.WorldGenFlowers.class));
        this.populatorTypeMappings.put("glowstone", new SpongePopulatorType("glowstone", net.minecraft.world.gen.feature.WorldGenGlowStone1.class));
        this.populatorTypeMappings.put("glowstone2", new SpongePopulatorType("glowstone2", net.minecraft.world.gen.feature.WorldGenGlowStone2.class));
        this.populatorTypeMappings.put("ice_path", new SpongePopulatorType("ice_path", net.minecraft.world.gen.feature.WorldGenIcePath.class));
        this.populatorTypeMappings.put("ice_spike", new SpongePopulatorType("ice_spike", net.minecraft.world.gen.feature.WorldGenIceSpike.class));
        this.populatorTypeMappings.put("jungle_bush_tree", new SpongePopulatorType("jungle_bush_tree", net.minecraft.world.gen.feature.WorldGenShrub.class));
        this.populatorTypeMappings.put("lake", new SpongePopulatorType("lake", net.minecraft.world.gen.feature.WorldGenLakes.class));
        this.populatorTypeMappings.put("lava", new SpongePopulatorType("lava", net.minecraft.world.gen.feature.WorldGenHellLava.class));
        this.populatorTypeMappings.put("liquid", new SpongePopulatorType("liquid", net.minecraft.world.gen.feature.WorldGenLiquids.class));
        this.populatorTypeMappings.put("mega_jungle_tree", new SpongePopulatorType("mega_jungle_tree", net.minecraft.world.gen.feature.WorldGenMegaJungle.class));
        this.populatorTypeMappings.put("mega_pine_tree", new SpongePopulatorType("mega_pinge_tree", net.minecraft.world.gen.feature.WorldGenMegaPineTree.class));
        this.populatorTypeMappings.put("melon", new SpongePopulatorType("melon", net.minecraft.world.gen.feature.WorldGenMelon.class));
        this.populatorTypeMappings.put("ore", new SpongePopulatorType("ore", net.minecraft.world.gen.feature.WorldGenMinable.class));
        this.populatorTypeMappings.put("pointy_taiga_tree", new SpongePopulatorType("pointy_taiga_tree", net.minecraft.world.gen.feature.WorldGenTaiga1.class));
        this.populatorTypeMappings.put("pumpkin", new SpongePopulatorType("pumpkin", net.minecraft.world.gen.feature.WorldGenPumpkin.class));
        this.populatorTypeMappings.put("reed", new SpongePopulatorType("reed", net.minecraft.world.gen.feature.WorldGenReed.class));
        this.populatorTypeMappings.put("sand", new SpongePopulatorType("sand", net.minecraft.world.gen.feature.WorldGenSand.class));
        this.populatorTypeMappings.put("savanna_tree", new SpongePopulatorType("savanna_tree", net.minecraft.world.gen.feature.WorldGenSavannaTree.class));
        this.populatorTypeMappings.put("shrub", new SpongePopulatorType("shrub", net.minecraft.world.gen.feature.WorldGenTallGrass.class));
        this.populatorTypeMappings.put("swamp_tree", new SpongePopulatorType("swamp_tree", net.minecraft.world.gen.feature.WorldGenSwamp.class));
        this.populatorTypeMappings.put("tall_taiga_tree", new SpongePopulatorType("tall_taiga_tree", net.minecraft.world.gen.feature.WorldGenTaiga2.class));
        this.populatorTypeMappings.put("tree", new SpongePopulatorType("tree", net.minecraft.world.gen.feature.WorldGenTrees.class));
        this.populatorTypeMappings.put("vine", new SpongePopulatorType("vine", net.minecraft.world.gen.feature.WorldGenVines.class));
        this.populatorTypeMappings.put("water_lily", new SpongePopulatorType("water_lily", net.minecraft.world.gen.feature.WorldGenWaterlily.class));

        RegistryHelper.mapFields(PopulatorTypes.class, new Function<String, PopulatorType>() {

            @Override
            public PopulatorType apply(String fieldName) {
                PopulatorType populatorType = SpongeGameRegistry.this.populatorTypeMappings.get(fieldName.toLowerCase());
                SpongeGameRegistry.this.populatorClassToTypeMappings
                        .put(((SpongePopulatorType) populatorType).populatorClass, populatorType);
                // remove old mapping
                SpongeGameRegistry.this.populatorTypeMappings.remove(fieldName.toLowerCase());
                // add new mapping with minecraft id
                SpongeGameRegistry.this.populatorTypeMappings.put(((SpongePopulatorType) populatorType).getId(), populatorType);
                return populatorType;
            }
        });
    }

    public void setDamageTypes() {
        damageSourceToTypeMappings.put("anvil", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("arrow", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("cactus", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("drown", DamageTypes.DROWN);
        damageSourceToTypeMappings.put("fall", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("fallingblock", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("generic", DamageTypes.GENERIC);
        damageSourceToTypeMappings.put("indirectmagic", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("infire", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("inwall", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("lava", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("lightningbolt", DamageTypes.PROJECTILE);
        damageSourceToTypeMappings.put("magic", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("mob", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("onfire", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("outofworld", DamageTypes.VOID);
        damageSourceToTypeMappings.put("player", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("starve", DamageTypes.HUNGER);
        damageSourceToTypeMappings.put("thorns", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("thrown", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("wither", DamageTypes.MAGIC);
    }

    public void setDamageSources() {
        try {
            DAMAGESOURCE_POISON = (new net.minecraft.util.DamageSource("poison")).setDamageBypassesArmor().setMagicDamage();
            DAMAGESOURCE_MELTING = (new net.minecraft.util.DamageSource("melting")).setDamageBypassesArmor().setFireDamage();
            DamageSources.class.getDeclaredField("DROWNING").set(null, (DamageSource) net.minecraft.util.DamageSource.drown);
            DamageSources.class.getDeclaredField("FALLING").set(null, (DamageSource) net.minecraft.util.DamageSource.fall);
            DamageSources.class.getDeclaredField("FIRE_TICK").set(null, (DamageSource) net.minecraft.util.DamageSource.onFire);
            DamageSources.class.getDeclaredField("GENERIC").set(null, (DamageSource) net.minecraft.util.DamageSource.generic);
            DamageSources.class.getDeclaredField("IN_FIRE").set(null, (DamageSource) net.minecraft.util.DamageSource.inFire);
            DamageSources.class.getDeclaredField("MAGIC").set(null, (DamageSource) net.minecraft.util.DamageSource.magic);
            DamageSources.class.getDeclaredField("MELTING").set(null, DAMAGESOURCE_MELTING);
            DamageSources.class.getDeclaredField("POISON").set(null, DAMAGESOURCE_POISON);
            DamageSources.class.getDeclaredField("STARVATION").set(null, (DamageSource) net.minecraft.util.DamageSource.starve);
            DamageSources.class.getDeclaredField("WITHER").set(null, (DamageSource) net.minecraft.util.DamageSource.wither);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setItemNone() {
        try {
            RegistryHelper.setFinalStatic(ItemStackSnapshot.class, "NONE", NONE.createSnapshot());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public Optional<EntityStatistic> getEntityStatistic(StatisticGroup statisticGroup, EntityType entityType) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<ItemStatistic> getItemStatistic(StatisticGroup statisticGroup, ItemType itemType) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<BlockStatistic> getBlockStatistic(StatisticGroup statisticGroup, BlockType blockType) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<TeamStatistic> getTeamStatistic(StatisticGroup statisticGroup, TextColor teamColor) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Collection<Statistic> getStatistics(StatisticGroup statisticGroup) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void registerStatistic(Statistic stat) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<ResourcePack> getById(String id) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<DisplaySlot> getDisplaySlotForColor(TextColor color) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PopulatorFactory getPopulatorFactory() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public ExtentBufferFactory getExtentBufferFactory() {
        return SpongeExtentBufferFactory.INSTANCE;
    }

    @Override
    public GameDictionary getGameDictionary() {
        throw new UnsupportedOperationException();
    }

    private void setTimingsFactory() {
        SpongeTimingsFactory factory = new SpongeTimingsFactory();
        RegistryHelper.setFactory(Timings.class, factory);
        factory.init();
    }

    public void preInit() {
        SpongeSerializationRegistry.setupSerialization(Sponge.getGame());
    }

    public void init() {
        setCareersAndProfessions();
        setTitleFactory();
        setDisplaySlots();
        setVisibilities();
        setObjectiveDisplayModes();
        setDamageTypes();
        setDamageSources();
        setTimingsFactory();
    }

    public void postInit() {
        setPopulatorTypes();
        setItemNone();
        SpongePropertyRegistry.completeRegistration();
        SpongeDataRegistry.finalizeRegistration();
    }
}
