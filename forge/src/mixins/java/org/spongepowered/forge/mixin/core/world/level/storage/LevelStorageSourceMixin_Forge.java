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
package org.spongepowered.forge.mixin.core.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceMixin_Forge {

    private static boolean forge$skipAdditionalFixupCalls = false;

    @Redirect(method = "readWorldGenSettings", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;fixUpDimensionsData"
            + "(Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/Dynamic;", remap = false))
    private static Dynamic<?> forge$skipForgeDimensionsFixup(final Dynamic<?> data, final Dynamic<?> data2, DataFixer param1, int param2) {
        if (!LevelStorageSourceMixin_Forge.forge$skipAdditionalFixupCalls) {
            LevelStorageSourceMixin_Forge.forge$skipAdditionalFixupCalls = true;
            return ForgeHooks.fixUpDimensionsData(data);
        }

        return data;
    }
}
