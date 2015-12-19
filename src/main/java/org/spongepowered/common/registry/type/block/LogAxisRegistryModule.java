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
package org.spongepowered.common.registry.type.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLog;
import org.spongepowered.api.data.type.LogAxes;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Optional;

public final class LogAxisRegistryModule implements CatalogRegistryModule<LogAxis> {

    @RegisterCatalog(LogAxes.class)
    private final BiMap<String, LogAxis> logAxisMappings = HashBiMap.create();

    @Override
    public Optional<LogAxis> getById(String id) {
        return Optional.ofNullable(this.logAxisMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<LogAxis> getAll() {
        return ImmutableList.copyOf((LogAxis[]) (Object[]) BlockLog.EnumAxis.values());
    }

    @Override
    public void registerDefaults() {
        this.logAxisMappings.put("x", (LogAxis) (Object) BlockLog.EnumAxis.X);
        this.logAxisMappings.put("y", (LogAxis) (Object) BlockLog.EnumAxis.Y);
        this.logAxisMappings.put("z", (LogAxis) (Object) BlockLog.EnumAxis.Z);
        this.logAxisMappings.put("none", (LogAxis) (Object) BlockLog.EnumAxis.NONE);
    }

}
