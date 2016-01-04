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
package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableInvisibilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.InvisibilityData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeInvisibilityData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeInvisibilityData extends AbstractImmutableData<ImmutableInvisibilityData, InvisibilityData> implements ImmutableInvisibilityData {

    private final boolean invisible;
    private final boolean collision;
    private final boolean untargetable;

    public ImmutableSpongeInvisibilityData(boolean invisible, boolean collision, boolean untargetable) {
        super(ImmutableInvisibilityData.class);
        this.invisible = invisible;
        this.collision = collision;
        this.untargetable = untargetable;
    }

    @Override
    public ImmutableValue<Boolean> invisible() {
        return ImmutableSpongeValue.cachedOf(Keys.INVISIBLE, false, this.invisible);
    }

    @Override
    public ImmutableValue<Boolean> ignoresCollisionDetection() {
        return ImmutableSpongeValue.cachedOf(Keys.INVISIBILITY_IGNORES_COLLISION, false, this.collision);
    }

    @Override
    public ImmutableValue<Boolean> untargetable() {
        return ImmutableSpongeValue.cachedOf(Keys.INVISIBILITY_PREVENTS_TARGETING, false, this.untargetable);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.INVISIBLE, this::isInvisible);
        registerKeyValue(Keys.INVISIBLE, this::invisible);

        registerFieldGetter(Keys.INVISIBILITY_IGNORES_COLLISION, this::isCollision);
        registerKeyValue(Keys.INVISIBILITY_IGNORES_COLLISION, this::ignoresCollisionDetection);

        registerFieldGetter(Keys.INVISIBILITY_PREVENTS_TARGETING, this::isUntargetable);
        registerKeyValue(Keys.INVISIBILITY_PREVENTS_TARGETING, this::untargetable);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.INVISIBLE, this.invisible)
                .set(Keys.INVISIBILITY_IGNORES_COLLISION, this.collision)
                .set(Keys.INVISIBILITY_PREVENTS_TARGETING, this.untargetable);
    }

    private boolean isInvisible() {
        return this.invisible;
    }

    private boolean isCollision() {
        return this.collision;
    }

    private boolean isUntargetable() {
        return this.untargetable;
    }

    @Override
    public InvisibilityData asMutable() {
        return new SpongeInvisibilityData(this.invisible, this.collision, this.untargetable);
    }

    @Override
    public int compareTo(ImmutableInvisibilityData o) {
        return ComparisonChain.start()
                .compare(o.ignoresCollisionDetection().get(), this.collision)
                .compare(o.invisible().get(), this.invisible)
                .compare(o.untargetable().get(), this.untargetable)
                .result();
    }
}
