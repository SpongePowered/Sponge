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
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.SpongeImplHooks;

public class SpongeTileEntityType extends SpongeCatalogType implements TileEntityType {

    private final String name;
    private final String modId;
    private final Class<? extends TileEntity> clazz;
    private final boolean canTick;

    public SpongeTileEntityType(Class<? extends TileEntity> clazz, String name, String id, boolean canTick, String modId) {
        super(id);
        this.name = checkNotNull(name, "name");
        this.clazz = checkNotNull(clazz, "clazz");
        this.canTick = canTick;
        this.modId = modId;
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
