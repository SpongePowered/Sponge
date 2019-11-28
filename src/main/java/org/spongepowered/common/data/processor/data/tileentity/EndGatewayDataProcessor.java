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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableEndGatewayData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.EndGatewayData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeEndGatewayData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.mixin.core.tileentity.TileEntityEndGatewayAccessor;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class EndGatewayDataProcessor extends AbstractTileEntityDataProcessor<EndGatewayTileEntity, EndGatewayData, ImmutableEndGatewayData> {

    public EndGatewayDataProcessor() {
        super(EndGatewayTileEntity.class);
    }

    @Override
    protected boolean doesDataExist(final EndGatewayTileEntity container) {
        return true;
    }

    @Override
    protected boolean set(final EndGatewayTileEntity container, final Map<Key<?>, Object> map) {
        @Nullable final Vector3i exitPortal = (Vector3i) map.get(Keys.EXIT_POSITION);
        if (exitPortal != null) {
            ((TileEntityEndGatewayAccessor) container).accessor$SetExit(new BlockPos(exitPortal.getX(), exitPortal.getY(), exitPortal.getZ()));
        }

        ((TileEntityEndGatewayAccessor) container).accessor$setExactTeleport((Boolean) map.get(Keys.EXACT_TELEPORT));

        @Nullable final Long age = (Long) map.get(Keys.END_GATEWAY_AGE);
        if (age != null) {
            ((TileEntityEndGatewayAccessor) container).accessor$setAge(age);
        }

        @Nullable final Integer teleportCooldown = (Integer) map.get(Keys.END_GATEWAY_TELEPORT_COOLDOWN);
        if (teleportCooldown != null) {
            ((TileEntityEndGatewayAccessor) container).accessor$setTeleportCooldown(teleportCooldown);
        }

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final EndGatewayTileEntity container) {
        final ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        builder.put(Keys.EXIT_POSITION, VecHelper.toVector3i(((TileEntityEndGatewayAccessor) container).accessor$getExitPortal()));
        builder.put(Keys.EXACT_TELEPORT, ((TileEntityEndGatewayAccessor) container).accessor$getExactTeleport());
        builder.put(Keys.END_GATEWAY_AGE, ((TileEntityEndGatewayAccessor) container).accessor$getAge());
        builder.put(Keys.END_GATEWAY_TELEPORT_COOLDOWN, ((TileEntityEndGatewayAccessor) container).accessor$getTeleportCooldown());
        return builder.build();
    }

    @Override
    protected EndGatewayData createManipulator() {
        return new SpongeEndGatewayData();
    }

    @Override
    public Optional<EndGatewayData> fill(final DataContainer container, EndGatewayData data) {
        checkNotNull(data, "data");

        final Optional<Vector3i> exitPosition = container.getObject(Keys.EXIT_POSITION.getQuery(), Vector3i.class);
        if (exitPosition.isPresent()) {
            data = data.set(Keys.EXIT_POSITION, exitPosition.get());
        }

        final Optional<Boolean> exactTeleport = container.getBoolean(Keys.EXACT_TELEPORT.getQuery());
        if (exactTeleport.isPresent()) {
            data = data.set(Keys.EXACT_TELEPORT, exactTeleport.get());
        }

        final Optional<Long> age = container.getLong(Keys.END_GATEWAY_AGE.getQuery());
        if (age.isPresent()) {
            data = data.set(Keys.END_GATEWAY_AGE, age.get());
        }

        final Optional<Integer> teleportCooldown = container.getInt(Keys.END_GATEWAY_TELEPORT_COOLDOWN.getQuery());
        if (teleportCooldown.isPresent()) {
            data = data.set(Keys.END_GATEWAY_TELEPORT_COOLDOWN, teleportCooldown.get());
        }

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(final DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
