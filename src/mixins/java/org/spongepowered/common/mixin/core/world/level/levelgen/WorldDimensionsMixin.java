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
package org.spongepowered.common.mixin.core.world.level.levelgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;

import java.util.Map;

@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsMixin {

    @Redirect(method = "<init>(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object findOverworldStem(final Map instance, final Object key) {
        var val = instance.get(key);
        // Yes this looks strange. The issue is Sponge gives all levels a PrimaryLevelData. Vanilla reads that file and reads the
        // "worldgensettings" tag section which is really where LevelStems are retained. It then creates a view of the stems, wherein called
        // Registry<LevelStem> and uses that to lookup this data. If Vanilla checked if Registries has that object already populated and just
        // continues to use it...there would be no issues. But why would they? Vanilla only has one level.dat "PrimaryLevelData". As a consequence,
        // this Registry<LevelStem> object is re-created each time we ask the PrimaryLevelData to parse its data tag. This is not an issue in
        // Vanilla because, again, there is only one PrimaryLevelData.
        //
        // Therefore, the easiest fix is to defer back to the stem registry already stored in the server should we hit a null here. This
        // ends up being a double lookup for all worlds but the overworld but I feel it is a small price to pay vs. a very complicated series
        // of mixins to remove retrieving the level stem settings solely for all worlds but the overworld
        if (val == null) {
            return SpongeCommon.vanillaRegistry(Registries.LEVEL_STEM).get((ResourceKey<LevelStem>) key);
        }

        return val;
    }
}
