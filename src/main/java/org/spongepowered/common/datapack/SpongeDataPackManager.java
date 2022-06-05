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

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.lifecycle.RegisterDataPackValueEventImpl;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SpongeDataPackManager {

    private static boolean ignoreNext;

    // TODO IntegratedServer call?
    public static List<String> registerPacks(final Path packDir, final boolean isReload) {
        // Ignore reload immediately after first call
        if (isReload && SpongeDataPackManager.ignoreNext) {
            SpongeDataPackManager.ignoreNext = false;
            return List.of();
        }
        if (!isReload) {
            SpongeDataPackManager.ignoreNext = true;
        }

        SpongeIngredient.clearCache();
        IngredientResultUtil.clearCache();

        final List<String> reloadablePacks = new ArrayList<>();
        SpongeDataPackManager.serialize(DataPackTypes.ADVANCEMENT, packDir, reloadablePacks);
        SpongeDataPackManager.serialize(DataPackTypes.RECIPE, packDir, reloadablePacks);
        SpongeDataPackManager.serialize(DataPackTypes.TAG, packDir, reloadablePacks);
        SpongeDataPackManager.serialize(DataPackTypes.BIOME, packDir, reloadablePacks);
        SpongeDataPackManager.serialize(DataPackTypes.WORLD_TYPE, packDir, reloadablePacks);
        SpongeDataPackManager.serialize(DataPackTypes.WORLD, packDir, reloadablePacks);
        return reloadablePacks;
    }

    private static <T extends DataPackSerializable> List<T> callRegisterDataPackValueEvent(final DataPackType<T> type) {
        final RegisterDataPackValueEventImpl<T> event = new RegisterDataPackValueEventImpl<>(Cause.of(EventContext.empty(), SpongeCommon.game()), SpongeCommon.game(), type);
        SpongeCommon.post(event);
        return event.serializables();
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataPackSerializable> void serialize(final DataPackType<T> type, final Path packDir, final Collection<String> reloadablePacks) {
        final RegistryAccess registryAccess = SpongeCommon.server().registryAccess();
        final List<T> serializables = SpongeDataPackManager.callRegisterDataPackValueEvent(type);
        if (serializables.isEmpty()) {
            return;
        }

        final SpongeDataPackType implType = (SpongeDataPackType) type;
        final List<DataPackSerializedObject> serialized = new ArrayList<>();
        for (final DataPackSerializable serializable : serializables) {
            final JsonObject o = (JsonObject) implType.getObjectSerializer().serialize(serializable, registryAccess);
            serialized.add((DataPackSerializedObject) implType.getObjectFunction().apply(serializable, o));
        }

        // Serialize the pack itself now - objects later
        SpongeDataPackManager.serializePack(implType, packDir, reloadablePacks, serialized, serializables.size());
    }

    private static void serializePack(final SpongeDataPackType implType, final Path packDir, final Collection<String> reloadablePacks,
            final List<DataPackSerializedObject> serialized, final int count) {
        try {
            final boolean success = implType.getPackSerializer().serialize(implType, packDir, serialized, count);
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
}
