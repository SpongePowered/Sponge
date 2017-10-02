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

import net.minecraft.tileentity.TileEntityStructure;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.StructureModes;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Locale;

@RegisterCatalog(StructureModes.class)
public final class StructureModeRegistryModule extends AbstractCatalogRegistryModule<StructureMode> {

    @Override
    public void registerDefaults() {
        this.register(CatalogKey.minecraft("corner"), (StructureMode) (Object) TileEntityStructure.Mode.CORNER);
        this.register(CatalogKey.minecraft("data"), (StructureMode) (Object) TileEntityStructure.Mode.DATA);
        this.register(CatalogKey.minecraft("load"), (StructureMode) (Object) TileEntityStructure.Mode.LOAD);
        this.register(CatalogKey.minecraft("save"), (StructureMode) (Object) TileEntityStructure.Mode.SAVE);
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (TileEntityStructure.Mode mode : TileEntityStructure.Mode.values()) {
            String name = mode.name().toLowerCase(Locale.ENGLISH);
            if (!this.map.containsKey(name)) {
                this.map.put(CatalogKey.resolve(name), (StructureMode) (Object) mode);
            }
        }
    }

}
