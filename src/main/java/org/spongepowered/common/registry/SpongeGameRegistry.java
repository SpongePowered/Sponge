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

import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.AbstractAITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.PluginProvidedRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.EntityStatistic;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.statistic.TeamStatistic;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.serializer.TextSerializerFactory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.extent.ExtentBufferFactory;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.CatalogTypeTypeSerializer;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.network.status.SpongeFavicon;
import org.spongepowered.common.registry.type.block.RotationRegistryModule;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.registry.util.RegistryModuleLoader;
import org.spongepowered.common.text.selector.SpongeSelectorFactory;
import org.spongepowered.common.text.serializer.SpongeTextSerializerFactory;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.LanguageUtil;
import org.spongepowered.common.util.graph.DirectedGraph;
import org.spongepowered.common.util.graph.TopologicalOrder;
import org.spongepowered.common.world.extent.SpongeExtentBufferFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class SpongeGameRegistry implements GameRegistry {

    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(CatalogType.class), new CatalogTypeTypeSerializer());
    }

    public final RegistrationPhase getPhase() {
        return this.phase;
    }

    private RegistrationPhase phase = RegistrationPhase.PRE_REGISTRY; // Needed for module phase registrations

    protected final Map<Class<? extends CatalogType>, CatalogRegistryModule<?>> catalogRegistryMap = new IdentityHashMap<>();
    private List<Class<? extends RegistryModule>> orderedModules = new ArrayList<>();
    final Map<Class<? extends RegistryModule>, RegistryModule> classMap = new IdentityHashMap<>();
    private final Map<Class<?>, Supplier<?>> builderSupplierMap = new IdentityHashMap<>();
    private final Set<RegistryModule> registryModules = new HashSet<>();

    public SpongeGameRegistry() {
    }

    public void preRegistryInit() {
        CommonModuleRegistry.getInstance().registerDefaultModules();
        final DirectedGraph<Class<? extends RegistryModule>> graph = new DirectedGraph<>();
        for (RegistryModule module : this.registryModules) {
            this.classMap.put(module.getClass(), module);
            addToGraph(module, graph);
        }
        // Now we need ot do the catalog ones
        for (CatalogRegistryModule<?> module : this.catalogRegistryMap.values()) {
            this.classMap.put(module.getClass(), module);
            addToGraph(module, graph);
        }

        this.orderedModules.addAll(TopologicalOrder.createOrderedLoad(graph));

        registerModulePhase();
        SpongeVillagerRegistry.registerVanillaTrades();
        DataRegistrar.setupSerialization(SpongeImpl.getGame());
    }


    @Override
    public <T extends CatalogType> SpongeGameRegistry registerModule(Class<T> catalogClass, CatalogRegistryModule<T> registryModule) {
        checkArgument(!this.catalogRegistryMap.containsKey(catalogClass), "Already registered a registry module!");
        this.catalogRegistryMap.put(catalogClass, registryModule);
        if (this.phase != RegistrationPhase.PRE_REGISTRY) {
            if (catalogClass.getName().contains("org.spongepowered.api") && catalogClass.getAnnotation(PluginProvidedRegistryModule.class) == null) {
                throw new UnsupportedOperationException("Cannot register a module for an API defined class! That's the implementation's job!");
            }
            syncModules();
        }
        return this;
    }

    @Override
    public SpongeGameRegistry registerModule(RegistryModule module) {
        checkArgument(!this.registryModules.contains(module));
        this.registryModules.add(checkNotNull(module));
        if (this.phase != RegistrationPhase.PRE_REGISTRY) {
            syncModules();
        }
        return this;
    }

    private void syncModules() {
        final DirectedGraph<Class<? extends RegistryModule>> graph = new DirectedGraph<>();
        for (RegistryModule aModule : this.registryModules) {
            if (!this.classMap.containsKey(aModule.getClass())) {
                this.classMap.put(aModule.getClass(), aModule);
            }
            addToGraph(aModule, graph);
        }
        // Now we need ot do the catalog ones
        for (CatalogRegistryModule<?> aModule : this.catalogRegistryMap.values()) {
            if (!this.classMap.containsKey(aModule.getClass())) {
                this.classMap.put(aModule.getClass(), aModule);
            }
            addToGraph(aModule, graph);
        }
        this.orderedModules.clear();
        this.orderedModules.addAll(TopologicalOrder.createOrderedLoad(graph));
    }

    @Override
    public <T> SpongeGameRegistry registerBuilderSupplier(Class<T> builderClass, Supplier<? extends T> supplier) {
        checkArgument(!this.builderSupplierMap.containsKey(builderClass), "Already registered a builder supplier!");
        this.builderSupplierMap.put(builderClass, supplier);
        return this;
    }

    /**
     * Gets the desired {@link CatalogRegistryModule} for the desired {@link CatalogType} class.
     *
     * @param catalogClass The catalog class
     * @param <T> The type of catalog type
     * @return The catalog registry module
     */
    @SuppressWarnings("unchecked")
    public <T extends CatalogType> Optional<CatalogRegistryModule<T>> getRegistryModuleFor(Class<T> catalogClass) {
        checkNotNull(catalogClass);
        return Optional.of((CatalogRegistryModule<T>) this.catalogRegistryMap.get(catalogClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <TUnknown, T extends CatalogType> boolean isAdditionalRegistered(Class<TUnknown> clazz, Class<T> catalogType) {
        CatalogRegistryModule<T> module = getRegistryModuleFor(catalogType).orElse(null);
        checkArgument(module instanceof ExtraClassCatalogRegistryModule);
        ExtraClassCatalogRegistryModule<T, ?> classModule = (ExtraClassCatalogRegistryModule<T, ?>) module;
        return classModule.hasRegistrationFor((Class) clazz);
    }

    public <TUnknown, T extends CatalogType> T getTranslated(Class<TUnknown> clazz, Class<T> catalogClazz) {
        CatalogRegistryModule<T> module = getRegistryModuleFor(catalogClazz).orElse(null);
        checkArgument(module instanceof ExtraClassCatalogRegistryModule);
        ExtraClassCatalogRegistryModule<T, TUnknown> classModule = (ExtraClassCatalogRegistryModule<T, TUnknown>) module;
        return classModule.getForClass(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> Optional<T> getType(Class<T> typeClass, String id) {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(typeClass).orElse(null);
        if (registryModule == null) {
            return Optional.empty();
        } else {
            return registryModule.getById(id.toLowerCase());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> Collection<T> getAllOf(Class<T> typeClass) {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(typeClass).orElse(null);
        if (registryModule == null) {
            return Collections.emptyList();
        } else {
            return registryModule.getAll();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ResettableBuilder<?, ? super T>> T createBuilder(Class<T> builderClass) {
        checkNotNull(builderClass, "Builder class was null!");
        checkArgument(this.builderSupplierMap.containsKey(builderClass), "Could not find a Supplier for the provided class: " + builderClass.getCanonicalName());
        return (T) this.builderSupplierMap.get(builderClass).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> void register(Class<T> type, T obj) throws IllegalArgumentException, UnsupportedOperationException {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(type).orElse(null);
        if (registryModule == null) {
            throw new UnsupportedOperationException("Failed to find a RegistryModule for that type");
        } else {
            if (registryModule instanceof SpongeAdditionalCatalogRegistryModule) {
                if(((SpongeAdditionalCatalogRegistryModule<T>) registryModule).allowsApiRegistration()) {
                    ((SpongeAdditionalCatalogRegistryModule<T>) registryModule).registerAdditionalCatalog(obj);
                    return;
                }
            } else if (registryModule instanceof AdditionalCatalogRegistryModule) {
                ((AdditionalCatalogRegistryModule<T>) registryModule).registerAdditionalCatalog(obj);
                return;
            }
            throw new UnsupportedOperationException("This catalog type does not support additional registration");
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
    public AITaskType registerAITaskType(Object plugin, String id, String name, Class<? extends AbstractAITask<? extends Agent>> aiClass) {

        return AITaskTypeModule.getInstance().createAITaskType(plugin, id, name, aiClass);
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
    public Favicon loadFavicon(String raw) throws IOException {
        return SpongeFavicon.load(raw);
    }

    @Override
    public Favicon loadFavicon(Path path) throws IOException {
        return SpongeFavicon.load(path);
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
    public Optional<Translation> getTranslationById(String id) {
        return Optional.<Translation>of(new SpongeTranslation(id));
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
    public Optional<ResourcePack> getResourcePackById(String id) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<DisplaySlot> getDisplaySlotForColor(TextColor color) {
        return Optional.ofNullable(DisplaySlotRegistryModule.getInstance().displaySlotMappings.get(color.getId()));
    }

    @Override
    public ExtentBufferFactory getExtentBufferFactory() {
        return SpongeExtentBufferFactory.INSTANCE;
    }

    @Override
    public ValueFactory getValueFactory() {
        return SpongeValueFactory.getInstance();
    }

    @Override
    public VillagerRegistry getVillagerRegistry() {
        return SpongeVillagerRegistry.getInstance();
    }

    @Override
    public TextSerializerFactory getTextSerializerFactory() {
        return SpongeTextSerializerFactory.INSTANCE;
    }

    @Override
    public SpongeSelectorFactory getSelectorFactory() {
        return SpongeSelectorFactory.INSTANCE;
    }

    @Override
    public Locale getLocale(String locale) {
        return LanguageUtil.LOCALE_CACHE.getUnchecked(locale);
    }

    private void registerModulePhase() {
        for (Class<? extends RegistryModule> moduleClass : this.orderedModules) {
            if (!this.classMap.containsKey(moduleClass)) {
                throw new IllegalStateException("Something funky happened!");
            }
            final RegistryModule module = this.classMap.get(moduleClass);
            RegistryModuleLoader.tryModulePhaseRegistration(module);
        }
        registerAdditionalPhase();
    }

    private void registerAdditionalPhase() {
        for (Class<? extends RegistryModule> moduleClass : this.orderedModules) {
            final RegistryModule module = this.classMap.get(moduleClass);
            RegistryModuleLoader.tryAdditionalRegistration(module);
        }
    }

    private void addToGraph(RegistryModule module, DirectedGraph<Class<? extends RegistryModule>> graph) {
        graph.add(module.getClass());
        RegistrationDependency dependency = module.getClass().getAnnotation(RegistrationDependency.class);
        if (dependency != null) {
            for (Class<? extends RegistryModule> dependent : dependency.value()) {
                graph.addEdge(checkNotNull(module.getClass(), "Dependency class was null!"), dependent);
            }
        }
    }

    public void preInit() {
        this.phase = RegistrationPhase.PRE_INIT;
        registerModulePhase();
    }

    public void init() {
        this.phase = RegistrationPhase.INIT;
        registerModulePhase();
    }

    public void postInit() {
        this.phase = RegistrationPhase.POST_INIT;
        registerModulePhase();
        SpongePropertyRegistry.completeRegistration();
        SpongeDataManager.finalizeRegistration();
        this.phase = RegistrationPhase.LOADED;
    }

    public void registerAdditionals() {
        registerAdditionalPhase();
    }
}
