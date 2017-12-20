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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.AbstractAITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipeRegistry;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.RegistryModuleAlreadyRegisteredException;
import org.spongepowered.api.registry.util.PluginProvidedRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.EntityStatistic;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.statistic.StatisticTypes;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.serializer.BookViewDataBuilder;
import org.spongepowered.api.text.serializer.TextConfigSerializer;
import org.spongepowered.api.text.serializer.TextSerializerFactory;
import org.spongepowered.api.text.serializer.TextTemplateConfigSerializer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.extent.ExtentBufferFactory;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.CatalogTypeTypeSerializer;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.event.registry.SpongeGameRegistryRegisterEvent;
import org.spongepowered.common.item.recipe.crafting.SpongeCraftingRecipeRegistry;
import org.spongepowered.common.network.status.SpongeFavicon;
import org.spongepowered.common.registry.type.block.RotationRegistryModule;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;
import org.spongepowered.common.registry.type.scoreboard.DisplaySlotRegistryModule;
import org.spongepowered.common.registry.util.RegistryModuleLoader;
import org.spongepowered.common.text.selector.SpongeSelectorFactory;
import org.spongepowered.common.text.serializer.SpongeTextSerializerFactory;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.graph.CyclicGraphException;
import org.spongepowered.common.util.graph.DirectedGraph;
import org.spongepowered.common.util.graph.TopologicalOrder;
import org.spongepowered.common.util.graph.DirectedGraph.DataNode;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@Singleton
public class SpongeGameRegistry implements GameRegistry {

    public static final boolean PRINT_CATALOG_TYPES = Boolean.parseBoolean(System.getProperty("sponge.print_all_catalog_types"));

    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(CatalogType.class), new CatalogTypeTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Text.class), new TextConfigSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(BookView.class), new BookViewDataBuilder());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TextTemplate.class), new TextTemplateConfigSerializer());
    }

    private final SpongePropertyRegistry propertyRegistry;

    private RegistrationPhase phase = RegistrationPhase.PRE_REGISTRY; // Needed for module phase registrations

    protected final Map<Class<? extends CatalogType>, CatalogRegistryModule<?>> catalogRegistryMap = new IdentityHashMap<>();
    private List<Class<? extends RegistryModule>> orderedModules = new ArrayList<>();
    final Map<Class<? extends RegistryModule>, RegistryModule> classMap = new IdentityHashMap<>();
    private final Map<Class<?>, Supplier<?>> builderSupplierMap = new IdentityHashMap<>();
    private final Set<RegistryModule> registryModules = new HashSet<>();

    @Inject
    public SpongeGameRegistry(SpongePropertyRegistry propertyRegistry) {
        this.propertyRegistry = propertyRegistry;
    }

    public final RegistrationPhase getPhase() {
        return this.phase;
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

        try {
            this.orderedModules.addAll(TopologicalOrder.createOrderedLoad(graph));
        } catch (CyclicGraphException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Registry module dependencies are cyclical!\n");
            msg.append("Dependency loops are:\n");
            for (DataNode<?>[] cycle : e.getCycles()) {
                msg.append("[");
                for (DataNode<?> node : cycle) {
                    msg.append(node.getData().toString()).append(" ");
                }
                msg.append("]\n");
            }
            SpongeImpl.getLogger().fatal(msg.toString());
            throw new RuntimeException("Registry modules dependencies error.");
        }

        registerModulePhase();
        SpongeVillagerRegistry.registerVanillaTrades();
        DataRegistrar.setupSerialization();
        final List<Tuple<Class<? extends CatalogType>, CatalogRegistryModule<?>>> modules = new ArrayList<>();
        for (Map.Entry<Class<? extends CatalogType>, CatalogRegistryModule<?>> entry : this.catalogRegistryMap.entrySet()) {
            modules.add(new Tuple<>(entry.getKey(), entry.getValue()));
        }
        modules.sort(Comparator.comparing(tuple -> tuple.getFirst().getSimpleName()));
        if (PRINT_CATALOG_TYPES) { // Lol... this gets spammy really fast.... Probably at some point should be put to file.
            final PrettyPrinter printer = new PrettyPrinter(100).add("Printing all Catalogs and their ID's").centre().hr()
                .addWrapped(
                    "This is a test to print out all registered catalogs during initialization for their mapping, id's, and objects themselves.");
            for (Tuple<Class<? extends CatalogType>, CatalogRegistryModule<?>> module : modules) {
                printer.add(" %s : %s", "CatalogType", module.getFirst().getSimpleName());

                final Collection<? extends CatalogType> all = module.getSecond().getAll();
                final List<CatalogType> catalogTypes = new ArrayList<>();
                catalogTypes.addAll(all);
                catalogTypes.sort(Comparator.comparing(CatalogType::getId));
                for (CatalogType catalogType : catalogTypes) {
                    printer.add("  -%s", catalogType.getId());
                }
                printer.hr();
            }
            printer.trace(System.err, SpongeImpl.getLogger(), Level.DEBUG);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends CatalogType> SpongeGameRegistry registerModule(Class<T> catalogClass, CatalogRegistryModule<T> registryModule) {
        @Nullable final CatalogRegistryModule<T> existingModule = (CatalogRegistryModule<T>) this.catalogRegistryMap.get(catalogClass);
        if (existingModule != null) {
            throw new RegistryModuleAlreadyRegisteredException("Already registered a registry module!", existingModule);
        }

        this.catalogRegistryMap.put(catalogClass, registryModule);
        if (!this.orderedModules.isEmpty()) {
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
        if (!this.orderedModules.isEmpty()) {
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
        try {
            this.orderedModules.addAll(TopologicalOrder.createOrderedLoad(graph));
        } catch (CyclicGraphException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Registry module dependencies are cyclical!\n");
            msg.append("Dependency loops are:\n");
            for (DataNode<?>[] cycle : e.getCycles()) {
                msg.append("[");
                for (DataNode<?> node : cycle) {
                    msg.append(node.getData().toString()).append(" ");
                }
                msg.append("]\n");
            }
            SpongeImpl.getLogger().fatal(msg.toString());
            throw new RuntimeException("Registry modules dependencies error.");
        }
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
        return Optional.ofNullable((CatalogRegistryModule<T>) this.catalogRegistryMap.get(catalogClass));
    }

    @SuppressWarnings("unchecked")
    public <TUnknown, T extends CatalogType> T getTranslated(Class<TUnknown> clazz, Class<T> catalogClazz) {
        CatalogRegistryModule<T> module = getRegistryModuleFor(catalogClazz).orElse(null);
        checkArgument(module instanceof ExtraClassCatalogRegistryModule);
        ExtraClassCatalogRegistryModule<T, TUnknown> classModule = (ExtraClassCatalogRegistryModule<T, TUnknown>) module;
        return classModule.getForClass(clazz);
    }

    @Override
    public <T extends CatalogType> Optional<T> getType(Class<T> typeClass, String id) {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(typeClass).orElse(null);
        if (registryModule == null) {
            return Optional.empty();
        }
        return registryModule.getById(id.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllOf(Class<T> typeClass) {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(typeClass).orElse(null);
        if (registryModule == null) {
            return Collections.emptyList();
        }
        return registryModule.getAll();
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllFor(String pluginId, Class<T> typeClass) {
        checkNotNull(pluginId);
        final CatalogRegistryModule<T> registryModule = getRegistryModuleFor(typeClass).orElse(null);
        if (registryModule == null) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        registryModule.getAll()
                .stream()
                .filter(type -> pluginId.equals(type.getId().split(":")[0]))
                .forEach(builder::add);

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ResettableBuilder<?, ? super T>> T createBuilder(Class<T> builderClass) {
        checkNotNull(builderClass, "Builder class was null!");
        final Supplier<?> supplier = this.builderSupplierMap.get(builderClass);
        if (supplier == null) {
            throw new IllegalArgumentException("Could not find a Supplier for the provided builder class: " + builderClass.getCanonicalName());
        }
        return (T) supplier.get();
    }

    @Override
    public <T extends CatalogType> T register(Class<T> type, T obj) throws IllegalArgumentException, UnsupportedOperationException {
        CatalogRegistryModule<T> registryModule = getRegistryModuleFor(type).orElse(null);
        if (registryModule == null) {
            throw new UnsupportedOperationException("Failed to find a RegistryModule for that type");
        }
        if (registryModule instanceof SpongeAdditionalCatalogRegistryModule) {
            if(((SpongeAdditionalCatalogRegistryModule<T>) registryModule).allowsApiRegistration()) {
                ((SpongeAdditionalCatalogRegistryModule<T>) registryModule).registerAdditionalCatalog(obj);
                return obj;
            }
        } else if (registryModule instanceof AdditionalCatalogRegistryModule) {
            ((AdditionalCatalogRegistryModule<T>) registryModule).registerAdditionalCatalog(obj);
            return obj;
        }
        throw new UnsupportedOperationException("This catalog type does not support additional registration");
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
    public Optional<EntityStatistic> getEntityStatistic(StatisticType statType, EntityType entityType) {
        checkNotNull(statType, "null stat type");
        checkNotNull(entityType, "null entity type");
        EntityList.EntityEggInfo eggInfo = EntityList.ENTITY_EGGS.get(new ResourceLocation(entityType.getId()));
        if (statType.equals(StatisticTypes.ENTITIES_KILLED)) {
            return Optional.of((EntityStatistic) eggInfo.killEntityStat);
        } else if (statType.equals(StatisticTypes.KILLED_BY_ENTITY)) {
            return Optional.of((EntityStatistic) eggInfo.entityKilledByStat);
        }
        throw new IllegalArgumentException("invalid entity stat type");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<ItemStatistic> getItemStatistic(StatisticType statType, ItemType itemType) {
        checkNotNull(statType, "null stat type");
        checkNotNull(itemType, "null item type");
        Item item = (Item) itemType;
        if (statType.equals(StatisticTypes.ITEMS_CRAFTED)) {
            return Optional.of((ItemStatistic) StatList.getCraftStats(item));
        } else if (statType.equals(StatisticTypes.ITEMS_USED)) {
            return Optional.of((ItemStatistic) StatList.getObjectUseStats(item));
        } else if (statType.equals(StatisticTypes.ITEMS_BROKEN)) {
            return Optional.of((ItemStatistic) StatList.getObjectBreakStats(item));
        } else if (statType.equals(StatisticTypes.ITEMS_PICKED_UP)) {
            return Optional.of((ItemStatistic) StatList.getObjectsPickedUpStats(item));
        } else if (statType.equals(StatisticTypes.ITEMS_DROPPED)) {
            return Optional.of((ItemStatistic) StatList.getDroppedObjectStats(item));
        }
        throw new IllegalArgumentException("invalid item stat type");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockStatistic> getBlockStatistic(StatisticType statType, BlockType blockType) {
        if (!statType.equals(StatisticTypes.BLOCKS_BROKEN)) {
            throw new IllegalArgumentException("invalid block stat type");
        }
        return Optional.of((BlockStatistic) StatList.getBlockStats((Block) blockType));
    }

    @Override
    public AITaskType registerAITaskType(Object plugin, String id, String name, Class<? extends AbstractAITask<? extends Agent>> aiClass) {

        return AITaskTypeModule.getInstance().createAITaskType(plugin, id, name, aiClass);
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        return RotationRegistryModule.getInstance().getRotationFromDegree(degrees);
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

    @SuppressWarnings("unchecked")
    @Override
    public CraftingRecipeRegistry getCraftingRecipeRegistry() {
        return SpongeCraftingRecipeRegistry.getInstance();
    }

    @Override
    public SmeltingRecipeRegistry getSmeltingRecipeRegistry() {
        return (SmeltingRecipeRegistry) FurnaceRecipes.instance();
    }

    @Override
    public Optional<Translation> getTranslationById(String id) {
        return Optional.of(new SpongeTranslation(id));
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

    @SuppressWarnings("deprecation")
    @Override
    public TextSerializerFactory getTextSerializerFactory() {
        return SpongeTextSerializerFactory.INSTANCE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpongeSelectorFactory getSelectorFactory() {
        return SpongeSelectorFactory.INSTANCE;
    }

    @Override
    public Locale getLocale(String locale) {
        return LocaleCache.getLocale(locale);
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

    @SuppressWarnings("unchecked")
    public void init() {
        this.phase = RegistrationPhase.INIT;
        registerModulePhase();
        // Throw the registry module events for the registries that should be loaded once
        for (Map.Entry<Class<? extends CatalogType>, CatalogRegistryModule<?>> entry : this.catalogRegistryMap.entrySet()) {
            final CatalogRegistryModule module = entry.getValue();
            if (module instanceof AdditionalCatalogRegistryModule && (!(module instanceof SpongeAdditionalCatalogRegistryModule) ||
                    ((SpongeAdditionalCatalogRegistryModule) module).allowsApiRegistration()) &&
                    module.getClass().getAnnotation(CustomRegistrationPhase.class) == null) {
                SpongeImpl.postEvent(new SpongeGameRegistryRegisterEvent(Cause.of(EventContext.empty(), this),
                        entry.getKey(), (AdditionalCatalogRegistryModule) module));
            }
        }
    }

    public void postInit() {
        this.phase = RegistrationPhase.POST_INIT;
        registerModulePhase();
        this.propertyRegistry.completeRegistration();
        SpongeDataManager.finalizeRegistration();
        this.phase = RegistrationPhase.LOADED;
    }

    public void registerAdditionals() {
        registerAdditionalPhase();
    }
}
