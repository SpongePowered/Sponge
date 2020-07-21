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
package org.spongepowered.vanilla.mixin.core.world.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin_Vanilla {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;getDirectory(Ljava/io/File;)Ljava/io/File;"))
    private File vanilla$useProvidedWorldDirectory(DimensionType dimensionType, File p_212679_1_) {
        return p_212679_1_;
    }

    // Vanilla's ChunkManager queries the DimensionType for the folder, we need to compensate by having it shift one directory up
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/server/ChunkManager"))
    private ChunkManager vanilla$useParentWorldDirectory(ServerWorld worldIn, File worldDirectory, DataFixer p_i51538_3_, TemplateManager p_i51538_4_,
        Executor p_i51538_5_, ThreadTaskExecutor<Runnable> mainThreadIn, IChunkLightProvider p_i51538_7_, ChunkGenerator<?> generatorIn,
        IChunkStatusListener p_i51538_9_, Supplier<DimensionSavedDataManager> p_i51538_10_, int p_i51538_11_) {

        final DimensionType type = worldIn.getDimension().getType();

        return new ChunkManager(worldIn, type == DimensionType.OVERWORLD ? worldDirectory : worldDirectory.getParentFile(), p_i51538_3_, p_i51538_4_, p_i51538_5_,
            mainThreadIn, (ServerChunkProvider) (Object) this, generatorIn, p_i51538_9_, p_i51538_10_, p_i51538_11_);
    }
}
