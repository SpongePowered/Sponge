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
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.world.server.SpongeDimensionTypes;

import java.nio.file.Path;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge {

    // @formatter:off
    // @formatter:on

    @Nullable private Boolean impl$createDragonFight;

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
    public DimensionType bridge$decorateData(final SpongeDimensionTypes.SpongeDataSection data) {
        this.impl$createDragonFight = data.createDragonFight();
        return (DimensionType) (Object) this;
    }

    @Override
    public SpongeDimensionTypes.SpongeDataSection bridge$createData() {
        return new SpongeDimensionTypes.SpongeDataSection(this.impl$createDragonFight != null && this.impl$createDragonFight);
    }

    @Override
    public Boolean bridge$createDragonFight() {
        return this.impl$createDragonFight;
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;catchDecoderException(Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;"))
    private static Codec<DimensionType> impl$onWrapCodec(final Codec<DimensionType> codec) {
        return ExtraCodecs.catchDecoderException(SpongeDimensionTypes.injectCodec(codec));
    }
}
