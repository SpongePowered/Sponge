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
package org.spongepowered.common.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.lifecycle.RegisterDataPackValueEventImpl;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SpongeDataPackManager implements DataPackManager {

    private final MinecraftServer server;
    private final Path packDir;
    private boolean ignoreNext;

    public SpongeDataPackManager(final MinecraftServer server, final Path packDir) {
        this.server = server;
        this.packDir = packDir;
    }

    public void init() {
        final List<String> reloadablePacks = this.registerPacks(false);
        if (!reloadablePacks.isEmpty()) {
            SpongeCommon.logger().info("Reloading for plugin data packs... " + reloadablePacks.size());
            // see ReloadCommand#discoverNewPacks
            final PackRepository packRepo = this.server.getPackRepository();
            final List<String> toReload = new ArrayList<>(packRepo.getSelectedIds());
            packRepo.reload();
            final List<String> disabled = this.server.getWorldData().getDataPackConfig().getDisabled();
            for (final String available : packRepo.getAvailableIds()) {
                if (!disabled.contains(available) && !toReload.contains(available)) {
                    toReload.add(available);
                }
            }
            this.server.reloadResources(toReload);
        }
    }

    @Override
    public void reload() {
        this.ignoreNext = false;
        this.registerPacks(true);
    }

    // TODO IntegratedServer call?
    public List<String> registerPacks(final boolean isReload) {
        // Ignore reload immediately after first call
        if (isReload && this.ignoreNext) {
            this.ignoreNext = false;
            return List.of();
        }
        if (!isReload) {
            this.ignoreNext = true;
        }

        SpongeIngredient.clearCache();
        IngredientResultUtil.clearCache();

        final List<String> reloadablePacks = new ArrayList<>();
        this.serialize(DataPackTypes.ADVANCEMENT, reloadablePacks);
        this.serialize(DataPackTypes.RECIPE, reloadablePacks);
        this.serialize(DataPackTypes.TAG, reloadablePacks);
        return reloadablePacks;
    }

    private <T extends DataPack.Reloadable> List<T> callRegisterDataPackValueEvent(final DataPackType<T> type) {
        final RegisterDataPackValueEventImpl<T> event = new RegisterDataPackValueEventImpl<>(Cause.of(EventContext.empty(), SpongeCommon.game()), SpongeCommon.game(), type);
        SpongeCommon.post(event);
        return event.serializables();
    }

    @SuppressWarnings("unchecked")
    private <T extends DataPack.Reloadable> void serialize(final DataPackType<T> type, final Collection<String> reloadablePacks) {
        final SpongeDataPackType implType = (SpongeDataPackType) type;
        final List<T> serializables = this.callRegisterDataPackValueEvent(type);
        if (serializables.isEmpty()) {
            return;
        }

        final List<DataPackSerializedObject> serialized = new ArrayList<>();
        for (final DataPack serializable : serializables) {
            final JsonObject o = (JsonObject) implType.getObjectSerializer().serialize(serializable, this.server.registryAccess());
            serialized.add((DataPackSerializedObject) implType.getObjectFunction().apply(serializable, o));
        }

        // Serialize the pack itself now - objects later
        this.serializePack(implType, reloadablePacks, serialized, serializables.size());
    }

    private void serializePack(final SpongeDataPackType implType, final Collection<String> reloadablePacks,
            final List<DataPackSerializedObject> serialized, final int count) {
        try {
            final boolean success = implType.getPackSerializer().serialize(implType, this.packDir, serialized, count);
            if (success && implType.reloadable()) {
                reloadablePacks.add("file/" + implType.getPackSerializer().getPackName());
            } else {
                reloadablePacks.remove("file/" + implType.getPackSerializer().getPackName());
            }
        } catch (final IOException e) {
            reloadablePacks.remove("file/" + implType.getPackSerializer().getPackName());
            SpongeCommon.logger().error(e);
        }
    }

    @Override
    public void save(final DataPack.Persistent pack) {
        final SpongeDataPackType implType = (SpongeDataPackType) pack.type();
        final JsonElement json = implType.getObjectSerializer().serialize(pack, this.server.registryAccess());
        final DataPackSerializedObject obj = (DataPackSerializedObject) implType.getObjectFunction().apply(pack, json);
        this.serializePack(implType, new ArrayList<>(), List.of(obj), 1);
    }
}
