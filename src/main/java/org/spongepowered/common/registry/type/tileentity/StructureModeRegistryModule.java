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
package org.spongepowered.common.registry.type.tileentity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.StructureModes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.tileentity.StructureBlockTileEntity;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureModeRegistryModule implements CatalogRegistryModule<StructureMode> {

    @RegisterCatalog(StructureModes.class)
    public final Map<String, StructureMode> structureModes = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.structureModes.put("corner", (StructureMode) (Object) StructureBlockTileEntity.Mode.CORNER);
        this.structureModes.put("data", (StructureMode) (Object) StructureBlockTileEntity.Mode.DATA);
        this.structureModes.put("load", (StructureMode) (Object) StructureBlockTileEntity.Mode.LOAD);
        this.structureModes.put("save", (StructureMode) (Object) StructureBlockTileEntity.Mode.SAVE);
    }

    @Override
    public Optional<StructureMode> getById(String id) {
        return Optional.ofNullable(this.structureModes.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<StructureMode> getAll() {
        return ImmutableSet.copyOf(this.structureModes.values());
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (StructureBlockTileEntity.Mode mode : StructureBlockTileEntity.Mode.values()) {
            String name = mode.name().toLowerCase(Locale.ENGLISH);
            if (!this.structureModes.containsKey(name)) {
                this.structureModes.put(name, (StructureMode) (Object) mode);
            }
        }
    }

}
