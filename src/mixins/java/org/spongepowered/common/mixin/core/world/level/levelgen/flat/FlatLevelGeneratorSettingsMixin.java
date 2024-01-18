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
package org.spongepowered.common.mixin.core.world.level.levelgen.flat;

import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(FlatLevelGeneratorSettings.class)
public abstract class FlatLevelGeneratorSettingsMixin {

    @Redirect(method = "adjustGenerationSettings", at = @At(value = "INVOKE", target = "Ljava/util/List;set(ILjava/lang/Object;)Ljava/lang/Object;"),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/Heightmap$Types;MOTION_BLOCKING:Lnet/minecraft/world/level/levelgen/Heightmap$Types;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/GenerationStep$Decoration;TOP_LAYER_MODIFICATION:Lnet/minecraft/world/level/levelgen/GenerationStep$Decoration;")
            )
    )
    private <E> E impl$preventLayerOverwrite(final List<?> instance, final int index, final E element) {
        //Not really sure what vanilla is trying to do here but this doesn't make much sense and,
        //we hit NPE on the next call to this method. This doesn't happen on vanilla as it always
        //creates a new instance.
        return null;
    }
}
