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
package org.spongepowered.common.mixin.core.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

import java.nio.file.Path;
import java.util.function.Function;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    // @formatter:off
    @Shadow @Final @Mutable private boolean createDragonFight;
    // @formatter:on

    /**
     * @author zidane
     * @reason Compensate for our per-world level save adapters
     */
    @Overwrite
    public static Path getStorageFolder(ResourceKey<Level> worldKey, Path defaultLevelDirectory) {
        // Sponge Start - The directory is already set to be at this location
        return defaultLevelDirectory;
    }

    @Override
    public DimensionType bridge$decorateData(final SpongeWorldTypeTemplate.SpongeDataSection data) {
        this.createDragonFight = data.createDragonFight;
        return (DimensionType) (Object) this;
    }

    @Override
    public SpongeWorldTypeTemplate.SpongeDataSection bridge$createData() {
        return new SpongeWorldTypeTemplate.SpongeDataSection(this.createDragonFight);
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;comapFlatMap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<Object> impl$captureActualCodecBeforeWrap(final Codec<Object> codec, final Function<? super Object, ? extends DataResult<?
            extends Object>> to, final Function<? super Object, ? extends Object> from) {
        SpongeWorldTypeTemplate.internalCodec((Codec<DimensionType>) (Object) codec);
        return codec.comapFlatMap(to, from);
    }
}
