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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import net.minecraft.world.WorldProvider;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.world.DimensionManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DimensionRegistryModule implements ExtraClassCatalogRegistryModule<DimensionType, Dimension> {

    @RegisterCatalog(DimensionTypes.class)
    private final Map<String, DimensionType> dimensionTypeMappings = Maps.newHashMap();
    private final Map<Class<? extends WorldProvider>, SpongeConfig<SpongeConfig.DimensionConfig>> dimensionConfigs = Maps.newHashMap();
    public final Map<Class<? extends Dimension>, DimensionType> dimensionClassMappings = Maps.newHashMap();
    private final Map<Integer, String> worldFolderDimensionIdMappings = Maps.newHashMap();
    public final Map<UUID, String> worldFolderUniqueIdMappings = Maps.newHashMap();


    public static DimensionRegistryModule getInstance() {
        return Holder.instance;
    }

    @Override
    public void registerAdditionalCatalog(DimensionType extraCatalog) {
        this.dimensionTypeMappings.put(extraCatalog.getName().toLowerCase(), extraCatalog);
        this.dimensionClassMappings.put(extraCatalog.getDimensionClass(), extraCatalog);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends Dimension> mappedClass) {
        return this.dimensionClassMappings.containsKey(mappedClass);
    }

    @Override
    public DimensionType getForClass(Class<? extends Dimension> clazz) {
        return this.dimensionClassMappings.get(checkNotNull(clazz));
    }

    @Override
    public Optional<DimensionType> getById(String id) {
        return Optional.ofNullable(this.dimensionTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DimensionType> getAll() {
        return Collections.unmodifiableCollection(this.dimensionTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        DimensionManager.init();
        RegistryHelper.mapFields(DimensionTypes.class, this.dimensionTypeMappings);
    }

    public String getWorldFolder(UUID uuid) {
        return this.worldFolderUniqueIdMappings.get(uuid);
    }

    public String getWorldFolder(int dim) {
        return this.worldFolderDimensionIdMappings.get(dim);
    }

    public void registerWorldDimensionId(int dim, String folderName) {
        this.worldFolderDimensionIdMappings.put(dim, folderName);
    }

    public void registerWorldUniqueId(UUID uuid, String folderName) {
        this.worldFolderUniqueIdMappings.put(uuid, folderName);
    }

    public boolean isConfigRegistered(Class<? extends WorldProvider> clazz) {
        return this.dimensionConfigs.containsKey(clazz);
    }

    public void registerConfig(Class<? extends WorldProvider> dimension, SpongeConfig<SpongeConfig.DimensionConfig> config) {
        this.dimensionConfigs.put(dimension, config);
    }

    public SpongeConfig<SpongeConfig.DimensionConfig> getConfig(Class<? extends WorldProvider> aClass) {
        return this.dimensionConfigs.get(aClass);
    }

    public void validateProvider(WorldProvider provider) {
        if (((IMixinWorldProvider) provider).getDimensionConfig() == null) {
            if (!isConfigRegistered(provider.getClass())) {
                String providerName = provider.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
                SpongeConfig<SpongeConfig.DimensionConfig> config = new SpongeConfig<>(SpongeConfig.Type.DIMENSION,
                    Sponge.getSpongeConfigDir().resolve("worlds").resolve(providerName).resolve("dimension.conf"), "sponge");
                registerConfig(provider.getClass(), config);
            }
            ((IMixinWorldProvider) provider).setDimensionConfig(getConfig(provider.getClass()));
        }
    }

    private DimensionRegistryModule() { }

    private static final class Holder {

        private static final DimensionRegistryModule instance = new DimensionRegistryModule();
    }
}
