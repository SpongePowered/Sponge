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
package org.spongepowered.common.mixin.core.world;

import com.google.common.base.MoreObjects;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldTypeBridge;
import org.spongepowered.common.registry.type.world.GeneratorTypeRegistryModule;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(WorldType.class)
public abstract class WorldTypeMixin implements WorldTypeBridge {

    @Shadow @Final private String name;
    @Shadow @Final private int id;

    @Nullable private Function<World, BiomeProvider> impl$biomeProvider;
    @Nullable private BiFunction<World, String, ChunkGenerator> impl$chunkGenerator;


    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void onConstructSpongeRegister(final int id, final String name, final CallbackInfo callbackInfo) {
        // Ensures that new world types are automatically registered with the registry module
        GeneratorTypeRegistryModule.getInstance().registerAdditionalCatalog((GeneratorType) this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.name.hashCode();
        result = prime * result + this.id;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WorldType)) {
            return false;
        }

        final WorldType other = (WorldType) obj;
        return this.name.equals(other.func_77127_a()) && this.id == other.func_82747_f();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((GeneratorType) this).getId())
                .add("name", this.name)
                .add("settings", ((GeneratorType) this).getGeneratorSettings())
                .toString();
    }

    @Override
    public Optional<Function<World, BiomeProvider>> bridge$getBiomeProvider() {
        return Optional.ofNullable(this.impl$biomeProvider);
    }

    @Override
    public Optional<BiFunction<World, String, ChunkGenerator>> bridge$getChunkGenerator() {
        return Optional.ofNullable(this.impl$chunkGenerator);
    }

    @Override
    public void bridge$setChunkGenerator(final BiFunction<World, String, ChunkGenerator> function) {
        this.impl$chunkGenerator = function;
    }

    @Override
    public void bridge$setBiomeProvider(final Function<World, BiomeProvider> function) {
        this.impl$biomeProvider = function;
    }
}
