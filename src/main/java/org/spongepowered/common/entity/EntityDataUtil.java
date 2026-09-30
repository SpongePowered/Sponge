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
package org.spongepowered.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.DataUpdaterDelegate;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;

public final class EntityDataUtil {

    private static final ImmutableList<DataContentUpdater> ENTITY_UPDATERS = ImmutableList.of(new EntityDataVersionUpdater());
    private static final ImmutableList<DataContentUpdater> ARCHETYPE_UPDATERS = ImmutableList.of(new EntityArchetypeDataVersionUpdater());

    private EntityDataUtil() {
    }

    public static DataView upgradeEntityContainer(final DataView container) {
        return EntityDataUtil.upgrade(container, EntityDataUtil.ENTITY_UPDATERS, Constants.Entity.Data.CONTENT_VERSION,
            Constants.Entity.V2.DATA_VERSION, Constants.Entity.V2.DATA);
    }

    public static DataView upgradeEntityArchetypeContainer(final DataView container) {
        return EntityDataUtil.upgrade(container, EntityDataUtil.ARCHETYPE_UPDATERS, Constants.Sponge.EntityArchetype.Data.CONTENT_VERSION,
            Constants.Sponge.EntityArchetype.V2.DATA_VERSION, Constants.Sponge.EntityArchetype.V2.DATA);
    }

    private static DataView upgrade(final DataView container, final ImmutableList<DataContentUpdater> updaters, final int currentVersion,
            final DataQuery dataVersionQuery, final DataQuery dataQuery) {
        final int version = container.getInt(Queries.CONTENT_VERSION).orElse(1);
        final ImmutableList.Builder<DataContentUpdater> builder = ImmutableList.builder();
        int lastUpdaterVersion = version;
        for (final DataContentUpdater updater : updaters) {
            if (lastUpdaterVersion == updater.inputVersion()) {
                lastUpdaterVersion = updater.outputVersion();
                builder.add(updater);
            }
        }

        final DataUpdaterDelegate delegate = new DataUpdaterDelegate(builder.build(), version, currentVersion);
        final DataView updatedContainer = delegate.update(container);

        if (!updatedContainer.contains(dataVersionQuery, dataQuery)) {
            throw new InvalidDataException("Missing the entity data. Cannot re-construct an EntityArchetype!");
        }

        final CompoundTag stackData = updatedContainer.getView(dataQuery)
            .map(NBTTranslator.INSTANCE::translate)
            .orElseThrow(() -> new InvalidDataException("Unable retrieve item stack data"));

        final int dataVersion = updatedContainer.getInt(dataVersionQuery).get();
        final Dynamic<Tag> fixedData = DataFixers.getDataFixer().update(
            References.ENTITY,
            new Dynamic<>(NbtOps.INSTANCE, stackData),
            dataVersion,
            SharedConstants.getCurrentVersion().getDataVersion().getVersion()
        );

        return updatedContainer.set(dataQuery, NBTTranslator.INSTANCE.translate((CompoundTag) fixedData.getValue()));
    }

    public static EntityType<?> entityType(final CompoundTag entityData) throws InvalidDataException {
        if (!entityData.contains(Constants.Entity.ENTITY_TYPE_ID, Constants.NBT.TAG_STRING)) {
            throw new InvalidDataException("Missing entity type id in entity data");
        }
        final String entityTypeId = entityData.getString(Constants.Entity.ENTITY_TYPE_ID);
        return Sponge.game().registry(RegistryTypes.ENTITY_TYPE)
            .findValue(ResourceKey.resolve(entityTypeId))
            .orElseThrow(() -> new InvalidDataException("Could not deserialize an EntityType: " + entityTypeId));
    }

    private static final class EntityDataVersionUpdater implements DataContentUpdater {

        @Override
        public int inputVersion() {
            return Constants.Entity.Data.BASE_VERSION;
        }

        @Override
        public int outputVersion() {
            return Constants.Entity.Data.DATA_VERSIONED;
        }

        @Override
        public DataView update(final DataView content) {
            final DataContainer updated = content.copy();
            updated.remove(Constants.Sponge.UNSAFE_NBT);
            updated.remove(Constants.Entity.CLASS);
            updated.remove(Constants.Entity.TYPE);

            updated.set(Queries.CONTENT_VERSION, this.outputVersion());
            updated.set(Constants.Entity.V2.DATA_VERSION, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            updated.set(Constants.Entity.V2.DATA, content.getView(Constants.Sponge.UNSAFE_NBT)
                .map(DataView::copy)
                .orElseGet(() -> DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED))
                .set(Constants.Entity.ENTITY_TYPE_ID, content.getString(Constants.Entity.TYPE)
                    .orElseThrow(() -> new IllegalStateException("Missing entity type id in entity data"))));

            return updated;
        }
    }

    private static final class EntityArchetypeDataVersionUpdater implements DataContentUpdater {

        @Override
        public int inputVersion() {
            return Constants.Sponge.EntityArchetype.Data.BASE_VERSION;
        }

        @Override
        public int outputVersion() {
            return Constants.Sponge.EntityArchetype.Data.DATA_VERSIONED;
        }

        @Override
        public DataView update(final DataView content) {
            final DataContainer updated = content.copy();
            updated.remove(Constants.Sponge.EntityArchetype.V1.ENTITY_DATA);
            updated.remove(Constants.Sponge.EntityArchetype.V1.ENTITY_TYPE);

            updated.set(Queries.CONTENT_VERSION, this.outputVersion());
            updated.set(Constants.Sponge.EntityArchetype.V2.DATA_VERSION, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            updated.set(Constants.Sponge.EntityArchetype.V2.DATA,
                    content.getView(Constants.Sponge.EntityArchetype.V1.ENTITY_DATA)
                        .map(DataView::copy)
                        .orElseGet(() -> DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED))
                        .set(Constants.Entity.ENTITY_TYPE_ID, content.getString(Constants.Sponge.EntityArchetype.V1.ENTITY_TYPE)
                            .orElseThrow(() -> new IllegalStateException("Missing entity type id in archetype data"))));

            return updated;
        }
    }
}
