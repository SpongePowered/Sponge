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
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DimensionRegistryModule implements SpongeAdditionalCatalogRegistryModule<DimensionType> {

    @RegisterCatalog(DimensionTypes.class)
    private final Map<String, DimensionType> dimensionTypeMappings = Maps.newHashMap();
    private final Map<Integer, SpongeConfig<SpongeConfig.DimensionConfig>> dimensionConfigs = Maps.newHashMap();
    private final Map<Integer, DimensionType> providerIdMappings = Maps.newHashMap();
    private final Map<Integer, String> worldFolderDimensionIdMappings = Maps.newHashMap();
    private final Map<UUID, String> worldFolderUniqueIdMappings = Maps.newHashMap();

    public static DimensionRegistryModule getInstance() {
        return Holder.instance;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(DimensionType dimType) {
        this.dimensionTypeMappings.put(dimType.getName().toLowerCase(), dimType);
        this.providerIdMappings.put(((SpongeDimensionType) dimType).getDimensionTypeId(), dimType);
    }

    public DimensionType fromProviderId(int id) {
        return this.providerIdMappings.get(id);
    }

    public void unregisterProvider(int id) {
        DimensionType dimType = this.providerIdMappings.remove(id);
        if (dimType != null) {
            this.dimensionTypeMappings.remove(dimType.getName().toLowerCase());
        }
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
    }

    @AdditionalRegistration
    public void reApplyDimensionTypes() {
        // Re-map fields in case mods have changed vanilla providers
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

    public boolean isConfigRegistered(int id) {
        return this.dimensionConfigs.containsKey(id);
    }

    public void registerConfig(int id, SpongeConfig<SpongeConfig.DimensionConfig> config) {
        this.dimensionConfigs.put(id, config);
    }

    public SpongeConfig<SpongeConfig.DimensionConfig> getConfig(int id) {
        return this.dimensionConfigs.get(id);
    }

    public void validateProvider(WorldProvider provider) {
        if (((IMixinWorldProvider) provider).getDimensionConfig() == null) {
            int providerId = DimensionManager.getProviderType(((IMixinWorldProvider) provider).getDimensionId());
            if (!isConfigRegistered(providerId)) {
                String providerName = provider.getDimensionType().getName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
                SpongeConfig<SpongeConfig.DimensionConfig> config = new SpongeConfig<>(SpongeConfig.Type.DIMENSION,
                        SpongeImpl.getSpongeConfigDir().resolve("worlds").resolve(providerName).resolve("dimension.conf"), SpongeImpl.ECOSYSTEM_ID);
                registerConfig(providerId, config);
            }
            ((IMixinWorldProvider) provider).setDimensionConfig(getConfig(providerId));
        }
    }

    DimensionRegistryModule() {
    }

    private static final class Holder {

        static final DimensionRegistryModule instance = new DimensionRegistryModule();
    }
}
