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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableInvisibilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.InvisibilityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeInvisibilityData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.util.EntityUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Map;
import java.util.Optional;

public class InvisibilityDataProcessor
        extends AbstractEntityDataProcessor<Entity, InvisibilityData, ImmutableInvisibilityData> {

    public InvisibilityDataProcessor() {
        super(Entity.class);
    }

    @Override
    protected InvisibilityData createManipulator() {
        return new SpongeInvisibilityData();
    }

    @Override
    protected boolean doesDataExist(Entity dataHolder) {
        return true;
    }

    @Override
    protected boolean set(Entity dataHolder, Map<Key<?>, Object> keyValues) {
        if (!dataHolder.worldObj.isRemote) {
            final boolean invis = (Boolean) keyValues.get(Keys.INVISIBLE);
            final boolean collision = (Boolean) keyValues.get(Keys.INVISIBILITY_IGNORES_COLLISION);
            final boolean untargetable = (Boolean) keyValues.get(Keys.INVISIBILITY_PREVENTS_TARGETING);
            dataHolder.setInvisible(invis);
            if (invis) {
                EntityUtil.toggleInvisibility(dataHolder, invis);
                ((IMixinEntity) dataHolder).setIgnoresCollision(collision);
                ((IMixinEntity) dataHolder).setUntargetable(untargetable);
            } else {
                return EntityUtil.toggleInvisibility(dataHolder, invis);
            }
            return true;
        }
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity dataHolder) {
        return ImmutableMap.of(Keys.INVISIBLE, ((IMixinEntity) dataHolder).isReallyREALLYInvisible(),
                Keys.INVISIBILITY_IGNORES_COLLISION, ((IMixinEntity) dataHolder).ignoresCollision(),
                Keys.INVISIBILITY_PREVENTS_TARGETING, ((IMixinEntity) dataHolder).isUntargetable());
    }

    @Override
    public Optional<InvisibilityData> fill(DataContainer container, InvisibilityData invisibilityData) {

        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
