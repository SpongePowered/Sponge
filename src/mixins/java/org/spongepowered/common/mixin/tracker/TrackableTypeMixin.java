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
package org.spongepowered.common.mixin.tracker;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.TrackableBridge;

@Mixin({Block.class, EntityType.class, BlockEntityType.class})
public abstract class TrackableTypeMixin implements TrackableBridge {

    private boolean tracker$allowsBlockBulkCaptures = true;
    private boolean tracker$allowsBlockEventCreation = true;
    private boolean tracker$allowsEntityBulkCaptures = true;
    private boolean tracker$allowsEntityEventCreation = true;

    @Override
    public boolean bridge$shouldTick() {
        return true;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.tracker$allowsBlockBulkCaptures;
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {
        this.tracker$allowsBlockBulkCaptures = allowsBlockBulkCaptures;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.tracker$allowsBlockEventCreation;
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {
        this.tracker$allowsBlockEventCreation = allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.tracker$allowsEntityBulkCaptures;
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {
        this.tracker$allowsEntityBulkCaptures = allowsEntityBulkCaptures;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.tracker$allowsEntityEventCreation;
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {
        this.tracker$allowsEntityEventCreation = allowsEntityEventCreation;
    }

}
