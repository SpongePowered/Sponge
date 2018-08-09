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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.TileEntityTrackerCategory;
import org.spongepowered.common.config.category.TileEntityTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;

import java.util.Locale;

public class SpongeTileEntityType extends SpongeCatalogType implements TileEntityType {

    private final Class<? extends TileEntity> clazz;
    private final CatalogKey key;
    private final boolean canTick;
    // Used by tracker config
    public boolean allowsBlockBulkCapture = true;
    public boolean allowsEntityBulkCapture = true;
    public boolean allowsBlockEventCreation = true;
    public boolean allowsEntityEventCreation = true;

    public SpongeTileEntityType(Class<? extends TileEntity> clazz, CatalogKey key, boolean canTick) {
        super(key, key.getValue());
        this.clazz = checkNotNull(clazz, "clazz");
        this.canTick = canTick;
        this.key = key;
        this.initializeTrackerState();
    }

    public String getModId() {
        return this.getKey().getNamespace();
    }

    public boolean canTick() {
        return this.canTick;
    }

    public void initializeTrackerState() {
        SpongeConfig<TrackerConfig> trackerConfig = SpongeImpl.getTrackerConfig();
        TileEntityTrackerCategory tileEntityTracker = trackerConfig.getConfig().getTileEntityTracker();
        final String modId = this.key.getNamespace();

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
            modCapturing.getBlockBulkCaptureMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> this.allowsBlockBulkCapture);
            modCapturing.getEntityBulkCaptureMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> this.allowsEntityBulkCapture);
            modCapturing.getBlockEventCreationMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> this.allowsBlockEventCreation);
            modCapturing.getEntityEventCreationMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = modCapturing.getBlockBulkCaptureMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsEntityBulkCapture = modCapturing.getEntityBulkCaptureMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsBlockEventCreation = modCapturing.getBlockEventCreationMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsEntityEventCreation = modCapturing.getEntityEventCreationMap().computeIfAbsent(this.key.getValue().toLowerCase(Locale.ENGLISH), k -> true);
        }

        if (tileEntityTracker.autoPopulateData()) {
            trackerConfig.save();
        }
    }

    @Override
    public Class<? extends TileEntity> getTileEntityType() {
        return this.clazz;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("TileEntityClass", this.clazz);
    }

}
