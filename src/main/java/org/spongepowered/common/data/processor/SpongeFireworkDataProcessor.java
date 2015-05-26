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
package org.spongepowered.common.data.processor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.FireworkData;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.SpongeFireworkData;
import org.spongepowered.common.data.util.DataUtil;

import java.util.List;

public class SpongeFireworkDataProcessor implements SpongeDataProcessor<FireworkData> {

    @Override
    public Optional<FireworkData> getFrom(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<FireworkData> fillData(DataHolder dataHolder, FireworkData manipulator, DataPriority priority) {
        return null;
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, FireworkData manipulator, DataPriority priority) {
        if (checkNotNull(dataHolder) instanceof EntityFireworkRocket) {
            final ItemStack firework = ((EntityFireworkRocket) dataHolder).getDataWatcher().getWatchableObjectItemStack(8);
        }
        return null;
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<FireworkData> build(DataView container) throws InvalidDataException {
        final int modifier = getData(container, SpongeFireworkData.FLIGHT_MODIFIER, Integer.TYPE);
        checkDataExists(container, SpongeFireworkData.FIREWORK_EFFECTS);
        final List<FireworkEffect> effects = container.getSerializableList(SpongeFireworkData.FIREWORK_EFFECTS, FireworkEffect.class, Sponge
                .getGame().getServiceManager().provide(SerializationService.class).get()).get();
        return Optional.of(create().set(effects).setFlightModifier(modifier));
    }

    @Override
    public FireworkData create() {
        return new SpongeFireworkData();
    }

    @Override
    public Optional<FireworkData> createFrom(DataHolder dataHolder) {
        return null;
    }
}
