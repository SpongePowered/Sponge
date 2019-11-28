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
package org.spongepowered.common.data.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.TileEntityTrackerCategory;
import org.spongepowered.common.config.category.TileEntityTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;

public class SpongeTileEntityType extends SpongeCatalogType implements BlockEntityType {

    private final String name;
    private final String modId;
    private final Class<? extends BlockEntity> clazz;
    private final boolean canTick;
    // Used by tracker config
    public boolean allowsBlockBulkCapture = true;
    public boolean allowsEntityBulkCapture = true;
    public boolean allowsBlockEventCreation = true;
    public boolean allowsEntityEventCreation = true;

    public SpongeTileEntityType(Class<? extends BlockEntity> clazz, String name, String id, boolean canTick, String modId) {
        super(id);
        this.name = checkNotNull(name, "name");
        this.clazz = checkNotNull(clazz, "clazz");
        this.canTick = canTick;
        this.modId = modId;
        this.initializeTrackerState();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getModId() {
        return this.modId;
    }

    public boolean canTick() {
        return this.canTick;
    }

    public void initializeTrackerState() {
        final SpongeConfig<TrackerConfig> trackerConfigAdapter = SpongeImpl.getTrackerConfigAdapter();
        final TileEntityTrackerCategory tileEntityTracker = trackerConfigAdapter.getConfig().getTileEntityTracker();
        final String modId = this.modId;
        final String name = this.name;

        TileEntityTrackerModCategory modCapturing = tileEntityTracker.getModMappings().get(modId);

        if (modCapturing == null) {
            modCapturing = new TileEntityTrackerModCategory();
            tileEntityTracker.getModMappings().put(modId, modCapturing);
        }

        if (!modCapturing.isEnabled()) {
            this.allowsBlockBulkCapture = false;
            this.allowsEntityBulkCapture = false;
            this.allowsBlockEventCreation = false;
            this.allowsEntityEventCreation = false;
            modCapturing.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockBulkCapture);
            modCapturing.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityBulkCapture);
            modCapturing.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsBlockEventCreation);
            modCapturing.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = modCapturing.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityBulkCapture = modCapturing.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsBlockEventCreation = modCapturing.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
            this.allowsEntityEventCreation = modCapturing.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(), k -> true);
        }

        if (tileEntityTracker.autoPopulateData()) {
            trackerConfigAdapter.save();
        }
    }

    @Override
    public Class<? extends BlockEntity> getTileEntityType() {
        return this.clazz;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("TileEntityClass", this.clazz);
    }

}
