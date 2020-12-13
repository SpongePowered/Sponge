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
package org.spongepowered.common.hooks;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.structure.LegacyStructureDataUtil;
import net.minecraft.world.storage.DimensionSavedDataManager;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.List;

public interface WorldGenerationHooks {

    default LegacyStructureDataUtil createLegacyStructureDataUtil(final DimensionType dimensionType, final DimensionSavedDataManager saveData) {
        final SpongeDimensionType spongeDimensionType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();
        if (spongeDimensionType == DimensionTypes.OVERWORLD.get()) {
            return new LegacyStructureDataUtil(saveData, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
        } else if (spongeDimensionType == DimensionTypes.THE_NETHER.get()) {
            List<String> list1 = ImmutableList.of("Fortress");
            return new LegacyStructureDataUtil(saveData, list1, list1);
        } else if (spongeDimensionType == DimensionTypes.THE_END.get()) {
            List<String> list = ImmutableList.of("EndCity");
            return new LegacyStructureDataUtil(saveData, list, list);
        } else {
            throw new RuntimeException(String.format("Unknown dimension type : %s", dimensionType));
        }
    }
}
