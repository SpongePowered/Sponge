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
package org.spongepowered.common.mixin.core.map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.map.MapIdTrackerBridge;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(MapIndex.class)
public abstract class MapIdTrackerMixin implements MapIdTrackerBridge {

	@Shadow @Final private Object2IntMap<String> usedAuxIds;

	@Override
	public void bridge$setHighestMapId(int id) {
		this.usedAuxIds.put(Constants.Map.ID_COUNTS_KEY, id);
	}

	@Override
	public Optional<Integer> bridge$getHighestMapId() {
		int id = usedAuxIds.getInt(Constants.Map.ID_COUNTS_KEY);
		if (id == usedAuxIds.defaultReturnValue()) {
			return Optional.empty(); // Default return value is -1
		}
		return Optional.of(id);
	}
}
