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
import org.spongepowered.api.datapack.DataPackEntry;
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
import java.util.concurrent.CompletableFuture;

public final class SpongeDataPackManager implements DataPackManager {

    private final MinecraftServer server;
    private final Path packDir;
    private boolean ignoreNext;

    public SpongeDataPackManager(final MinecraftServer server, final Path packDir) {
        this.server = server;
        this.packDir = packDir;
    }

    // TODO IntegratedServer call?
    public void init() {
        this.ignoreNext = false;
        final List<String> reloadablePacks = this.registerPacks();
        if (!reloadablePacks.isEmpty()) {
            SpongeCommon.logger().info("Reloading for plugin data packs... " + reloadablePacks.size());
            this.ignoreNext = true;
            this.server.reloadResources(this.discoverNewPacks());
        }
    }

    // see ReloadCommand#discoverNewPacks
    private List<String> discoverNewPacks() {
        final PackRepository packRepo = this.server.getPackRepository();
        final List<String> toReload = new ArrayList<>(packRepo.getSelectedIds());
        packRepo.reload();
        final List<String> disabled = this.server.getWorldData().getDataPackConfig().getDisabled();
        for (final String available : packRepo.getAvailableIds()) {
            if (!disabled.contains(available) && !toReload.contains(available)) {
                toReload.add(available);
            }
        }
        return toReload;
    }

    @Override
    public CompletableFuture<Void> reload() {
        this.ignoreNext = false;
        return this.server.reloadResources(this.discoverNewPacks());
    }

    public List<String> registerPacks() {
        // Ignore reload immediately after first call
        if (this.ignoreNext) {
            this.ignoreNext = false;
            return List.of();
        }

        SpongeIngredient.clearCache();
        IngredientResultUtil.clearCache();

        final List<String> reloadablePacks = new ArrayList<>();
        this.serialize(DataPackTypes.ADVANCEMENT, reloadablePacks);
        this.serialize(DataPackTypes.RECIPE, reloadablePacks);
        this.serialize(DataPackTypes.TAG, reloadablePacks);
        return reloadablePacks;
    }

    private <T extends DataPackEntry> List<T> callRegisterDataPackValueEvent(final DataPackType<T> type) {
        final RegisterDataPackValueEventImpl<T> event = new RegisterDataPackValueEventImpl<>(Cause.of(EventContext.empty(), SpongeCommon.game()), SpongeCommon.game(), type);
        SpongeCommon.post(event);
        return event.serializables();
    }

    @SuppressWarnings("unchecked")
    private <T extends DataPackEntry> void serialize(final DataPackType<T> type, final Collection<String> reloadablePacks) {
        final SpongeDataPackType implType = (SpongeDataPackType) type;
        final List<T> serializables = this.callRegisterDataPackValueEvent(type);
        if (serializables.isEmpty()) {
            return;
        }

        final List<DataPackSerializedObject> serialized = new ArrayList<>();
        for (final DataPackEntry serializable : serializables) {
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
    public CompletableFuture<Boolean> save(final DataPackEntry entry) {
        final SpongeDataPackType implType = (SpongeDataPackType) entry.type();
        final JsonElement json = implType.getObjectSerializer().serialize(entry, this.server.registryAccess());
        final DataPackSerializedObject obj = (DataPackSerializedObject) implType.getObjectFunction().apply(entry, json);
        this.serializePack(implType, new ArrayList<>(), List.of(obj), 1);
        return CompletableFuture.completedFuture(implType.reloadable());
    }
}
