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
package org.spongepowered.common.data;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.persistence.datastore.SpongeDataStore;
import org.spongepowered.common.util.Constants;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;

public class DataUtil {

    public static void syncTagToData(Object dataHolder) {
        if (dataHolder instanceof SpongeDataHolderBridge && dataHolder instanceof DataCompoundHolder) {
            DataUtil.deserializeSpongeData((SpongeDataHolderBridge & DataCompoundHolder) dataHolder);
        }
    }

    public static boolean syncDataToTag(Object dataHolder) {
        if (dataHolder instanceof DataCompoundHolder) {
            return DataUtil.serializeSpongeData((DataCompoundHolder & SpongeDataHolderBridge) dataHolder);
        }
        return false;
    }

    public static <T extends SpongeDataHolderBridge & DataCompoundHolder> void deserializeSpongeData(final T dataHolder) {
        final CompoundTag compound = dataHolder.data$getCompound();
        if (compound == null) {
            return;
        }
        final DataContainer allData = NBTTranslator.INSTANCE.translate(compound);

        DataUtil.upgradeDataVersion(compound, allData); // Upgrade v2->v3

        // Run content-updaters and collect failed data
        final Class<? extends DataHolder> typeToken = dataHolder.getClass().asSubclass(DataHolder.class);
        allData.getView(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT).ifPresent(customData -> {
            for (DataQuery keyNamespace : customData.keys(false)) {
                final DataView keyedData = customData.getView(keyNamespace).get();
                for (DataQuery keyValue : keyedData.keys(false)) {
                    final ResourceKey dataStoreKey = ResourceKey.of(keyNamespace.asString("."), keyValue.asString("."));
                    final DataView dataStoreData = keyedData.getView(keyValue).get();
                    final Integer contentVersion = dataStoreData.getInt(Constants.Sponge.Data.V3.CONTENT_VERSION).orElse(1);
                    final Optional<DataStore> dataStore = SpongeDataManager.getDatastoreRegistry().getDataStore(dataStoreKey, typeToken);
                    if (dataStore.isPresent()) {
                        if (dataStore.get() instanceof SpongeDataStore) {
                            ((SpongeDataStore) dataStore.get()).getUpdaterFor(contentVersion).ifPresent(updater -> {
                                dataStoreData.set(Constants.Sponge.Data.V3.CONTENT, updater.update(dataStoreData.getView(Constants.Sponge.Data.V3.CONTENT).get()));
                                SpongeCommon.logger().info("Updated datastore {} from {} to {} ", dataStoreKey.asString(), contentVersion, ((SpongeDataStore) dataStore.get()).getVersion());
                            });
                        }
                    } else {
                        dataHolder.bridge$addFailedData(keyNamespace.then(keyValue), dataStoreData);
                    }
                }
            }
        });

        dataHolder.bridge$mergeDeserialized(DataManipulator.mutableOf()); // Initialize sponge data holder
        for (DataStore dataStore : SpongeDataManager.getDatastoreRegistry().getDataStoresForType(typeToken)) {
            // Deserialize to Manipulator
            final DataManipulator.Mutable deserialized = dataStore.deserialize(allData);
            try {
                // and set data in CustomDataHolderBridge
                dataHolder.bridge$mergeDeserialized(deserialized);
            } catch (Exception e) {
                SpongeCommon.logger().error("Could not merge data from datastore: {}", deserialized, e);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void upgradeDataVersion(CompoundTag compound, DataContainer allData) {
        // Check for v2 data
        allData.getView(Constants.Forge.FORGE_DATA_ROOT).flatMap(forgeData -> forgeData.getView(Constants.Sponge.Data.V2.SPONGE_DATA_ROOT))
                .ifPresent(spongeDataV2 -> DataUtil.upgradeV2CustomData(compound, allData, spongeDataV2));
    }

    @SuppressWarnings("deprecation")
    private static void upgradeV2CustomData(CompoundTag compound, DataContainer allData, DataView spongeDataV2) {
        // convert custom data v2 to v3
        final DataView spongeDataV3 = DataContainer.createNew();
        for (DataView customDataV2 : spongeDataV2.getViewList(Constants.Sponge.Data.V2.CUSTOM_MANIPULATOR_LIST).orElse(Collections.emptyList())) {
            try {
                // ForgeData/SpongeData/CustomManipulators[{ContentVersion|ManipulatorId|ManipulatorData}]
                final String id = customDataV2.getString(Constants.Sponge.Data.V2.MANIPULATOR_ID).get();
                final Object data = customDataV2.get(Constants.Sponge.Data.V2.MANIPULATOR_DATA).get();
                final Integer contentVersion = customDataV2.getInt(Queries.CONTENT_VERSION).get();

                // Fetch DataStore key for legacy registration or try to read it as a resouce key
                ResourceKey key = SpongeDataManager.INSTANCE.getLegacyRegistration(id).orElse(ResourceKey.resolve(id));

                // Build new custom data structure
                // sponeg-data/<datastore-namespace>/<datastore-value>/{version|content}
                final DataView dataStoreData = spongeDataV3.createView(DataQuery.of(key.namespace(), key.value()));
                dataStoreData.set(Constants.Sponge.Data.V3.CONTENT_VERSION, contentVersion);
                dataStoreData.set(Constants.Sponge.Data.V3.CONTENT, data);
                SpongeCommon.logger().info("Upgraded custom data for datastore: {}", key);
            } catch (Exception e) {
                SpongeCommon.logger().error("Error when upgrading V2 custom data", e);
            }
        }

        spongeDataV2.remove(Constants.Sponge.Data.V2.CUSTOM_MANIPULATOR_LIST);

        // convert sponge data v2 to v3
        for (DataQuery spongeDataKey : spongeDataV2.keys(false)) {
            final DataQuery query = SpongeDataManager.INSTANCE.legacySpongeDataQuery(spongeDataKey.toString());
            if (query == null) {
                SpongeCommon.logger().error("Missing legacy sponge data query mapping {}", spongeDataKey.toString());
            } else {
                final Object value = spongeDataV2.get(spongeDataKey).get();
                SpongeCommon.logger().info("Upgraded sponge data: {}->{} type {}", spongeDataKey.toString(), query.toString(), value.getClass().getSimpleName());
                spongeDataV3.set(query, value);
            }
        }

        // Setting upgraded v3 data
        allData.set(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT, spongeDataV3);

        // cleanup v2 data
        allData.getView(Constants.Forge.FORGE_DATA_ROOT).ifPresent(forgeData -> {
            forgeData.remove(Constants.Sponge.Data.V2.SPONGE_DATA_ROOT);
            if (forgeData.isEmpty()) {
                allData.remove(Constants.Forge.FORGE_DATA_ROOT);
            }
        });

        // cleanup v2 data on compound
        if (compound.contains(Constants.Forge.FORGE_DATA)) {
            final CompoundTag forgeData = compound.getCompound(Constants.Forge.FORGE_DATA);
            forgeData.remove(Constants.Sponge.Data.V2.SPONGE_DATA);
            if (forgeData.isEmpty()) {
                compound.remove(Constants.Forge.FORGE_DATA);
            }
        }
    }

    public static <T extends SpongeDataHolderBridge & DataCompoundHolder> boolean serializeSpongeData(final T dataHolder) {
        CompoundTag compound = dataHolder.data$getCompound();
        if (compound == null) {
            compound = new CompoundTag();
            dataHolder.data$setCompound(compound);
        }
        compound.remove(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT.asString(".")); // Remove all previous SpongeData

        final DataContainer allData = NBTTranslator.INSTANCE.translate(compound);

        // Clear old custom data root
        final DataView customDataRoot = allData.createView(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT);

        dataHolder.bridge$getFailedData().forEach(customDataRoot::set); // Add back failed data

        final DataManipulator.Mutable manipulator = dataHolder.bridge$getManipulator();
        final Type dataHolderType = dataHolder.getClass();
        manipulator.getKeys().stream()
                .map(key -> SpongeDataManager.getDatastoreRegistry().getDataStore(key, dataHolderType))
                .forEach(dataStore -> dataStore.serialize(manipulator, allData));

        // If data is still present after cleanup merge it back into nbt
        if (DataUtil.cleanupEmptySpongeData(allData)) {
            compound.merge(NBTTranslator.INSTANCE.translate(allData));
        }
        if (compound.isEmpty()) {
            dataHolder.data$setCompound(null);
            return false;
        }
        return true;
    }

    private static boolean cleanupEmptySpongeData(DataContainer allData) {
        return allData.getView(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT).map(spongeData -> {
                if (spongeData.isEmpty()) {
                    allData.remove(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT);
                    return false;
                }
                return true;
            }).orElse(false);
    }

    public static void setSpongeData(DataView allData, DataQuery dataStoreKey, DataView pluginData, int version) {
        final DataQuery dataStoreDataQuery = Constants.Sponge.Data.V3.SPONGE_DATA_ROOT.then(dataStoreKey);
        final DataView dataStoreDataView = allData.getView(dataStoreDataQuery).orElseGet(() -> allData.createView(dataStoreDataQuery));
        dataStoreDataView.set(Constants.Sponge.Data.V3.CONTENT_VERSION, version);
        dataStoreDataView.set(Constants.Sponge.Data.V3.CONTENT, pluginData);
    }

    public static Optional<DataView> getSpongeData(DataView allData, DataQuery dataStoreKey, int version) {
        return allData.getView(Constants.Sponge.Data.V3.SPONGE_DATA_ROOT
                .then(dataStoreKey)
                .then(Constants.Sponge.Data.V3.CONTENT));
    }
}
