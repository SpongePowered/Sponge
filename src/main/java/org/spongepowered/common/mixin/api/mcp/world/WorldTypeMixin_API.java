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

import com.google.common.base.MoreObjects;
import net.minecraft.world.WorldType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.gen.GeneratorType;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;

@Mixin(WorldType.class)
public abstract class WorldTypeMixin_API implements GeneratorType {

    @Nullable private CatalogKey api$key;

    @Inject(method = "<init>(ILjava/lang/String;Ljava/lang/String;I)V", at = @At("RETURN"))
    private void onConstructSpongeRegister(int id, String name, String serializedName, int version, CallbackInfo ci) {
        this.api$key = CatalogKey.of(SpongeImplHooks.getModIdFromClass(this.getClass()), name);
        // TODO - register this with our registry?
    }

    @Override
    public CatalogKey getKey() {
        return this.api$key;
    }

    @Override
    public DataContainer getDefaultGeneratorSettings() {
        // TODO 1.14 - Json settings/legacy settings -> DataContainer
        return null;
    }

    @Override
    public TerrainGenerator createGenerator(ServerWorld world) {
        checkNotNull(world);
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public int hashCode() {
        return this.api$key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldTypeMixin_API)) {
            return false;
        }

        final WorldTypeMixin_API other = (WorldTypeMixin_API) obj;
        return this.api$key.equals(other.api$key);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("key", this.api$key)
            .add("settings", ((GeneratorType) this).getDefaultGeneratorSettings())
            .toString();
    }
}
