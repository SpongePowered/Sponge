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
package org.spongepowered.common.mixin.api.mcp.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import net.minecraft.world.WorldType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.gen.GeneratorType;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.ServerWorldBridge;

import javax.annotation.Nullable;

@Mixin(WorldType.class)
public abstract class WorldTypeMixin_API implements GeneratorType {

    @Shadow @Final private String name;

    @Nullable private CatalogKey key;

    @Override
    public CatalogKey getKey() {
        if (this.key == null) {
            this.key = CatalogKey.of(SpongeImplHooks.getModIdFromClass(this.getClass()), CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                this.name));
        }

        return this.key;
    }

    @Override
    public DataContainer getDefaultGeneratorSettings() {
        // TODO 1.14 - Json settings/legacy settings -> DataContainer
        return null;
    }

    @Override
    public TerrainGenerator createGenerator(ServerWorld world) {
        checkNotNull(world);
        return ((ServerWorldBridge) world).bridge$createWorldGenerator(this.getDefaultGeneratorSettings());
    }
}
