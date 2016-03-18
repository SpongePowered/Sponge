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
package org.spongepowered.common.data.processor.data.tileentity;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.TileEntityEndGateway;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableEndGatewayData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.EndGatewayData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeEndGatewayData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntityEndGateway;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class EndGatewayDataProcessor extends AbstractTileEntityDataProcessor<TileEntityEndGateway, EndGatewayData, ImmutableEndGatewayData> {

    public EndGatewayDataProcessor() {
        super(TileEntityEndGateway.class);
    }

    @Override
    protected boolean doesDataExist(TileEntityEndGateway container) {
        return true;
    }

    @Override
    protected boolean set(TileEntityEndGateway container, Map<Key<?>, Object> map) {
        if (map.containsKey(Keys.EXIT_PORTAL)) {
            @Nullable Vector3i exitPortal = (Vector3i) map.get(Keys.EXIT_PORTAL);
            if (exitPortal != null) {
                ((IMixinTileEntityEndGateway) container).setExitPortal(exitPortal);
                container.markDirty();
            }
        }

        if (map.containsKey(Keys.EXACT_TELEPORT)) {
            @Nullable Boolean exactTeleport = (Boolean) map.get(Keys.EXACT_TELEPORT);
            if (exactTeleport != null) {
                ((IMixinTileEntityEndGateway) container).setExactTeleport(exactTeleport);
            }
        }

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(TileEntityEndGateway container) {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        builder.put(Keys.EXIT_PORTAL, ((IMixinTileEntityEndGateway) container).getExitPortal());
        builder.put(Keys.EXACT_TELEPORT, ((IMixinTileEntityEndGateway) container).isExactTeleport());
        return builder.build();
    }

    @Override
    protected EndGatewayData createManipulator() {
        return new SpongeEndGatewayData();
    }

    @Override
    public Optional<EndGatewayData> fill(DataContainer container, EndGatewayData data) {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
