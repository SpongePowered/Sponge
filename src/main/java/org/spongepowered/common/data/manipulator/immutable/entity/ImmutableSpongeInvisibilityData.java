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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableInvisibilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.InvisibilityData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeInvisibilityData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;

public class ImmutableSpongeInvisibilityData extends AbstractImmutableData<ImmutableInvisibilityData, InvisibilityData> implements ImmutableInvisibilityData {

    private final boolean vanish;
    private final boolean collision;
    private final boolean untargetable;
    private final boolean invisible;
    private ImmutableValue<Boolean> invisibleValue;
    private ImmutableValue<Boolean> vanishValue;
    private ImmutableValue<Boolean> collisionValue;
    private ImmutableValue<Boolean> untargetableValue;

    public ImmutableSpongeInvisibilityData(boolean vanish, boolean collision, boolean untargetable, boolean invisible) {
        super(ImmutableInvisibilityData.class);
        this.vanish = vanish;
        this.collision = collision;
        this.untargetable = untargetable;
        this.invisible = invisible;
        this.invisibleValue = ImmutableSpongeValue.cachedOf(Keys.INVISIBLE, false, this.invisible);
        this.vanishValue = ImmutableSpongeValue.cachedOf(Keys.VANISH, false, this.vanish);
        this.collisionValue = ImmutableSpongeValue.cachedOf(Keys.VANISH_IGNORES_COLLISION, false, this.collision);
        this.untargetableValue = ImmutableSpongeValue.cachedOf(Keys.VANISH_PREVENTS_TARGETING, false, this.untargetable);
        this.registerGetters();
    }

    @Override
    public ImmutableValue<Boolean> invisible() {
        return this.invisibleValue;
    }

    @Override
    public ImmutableValue<Boolean> vanish() {
        return this.vanishValue;
    }

    @Override
    public ImmutableValue<Boolean> ignoresCollisionDetection() {
        return this.collisionValue;
    }

    @Override
    public ImmutableValue<Boolean> untargetable() {
        return this.untargetableValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.VANISH, this::isVanish);
        registerKeyValue(Keys.VANISH, this::vanish);

        registerFieldGetter(Keys.VANISH_IGNORES_COLLISION, this::isCollision);
        registerKeyValue(Keys.VANISH_IGNORES_COLLISION, this::ignoresCollisionDetection);

        registerFieldGetter(Keys.VANISH_PREVENTS_TARGETING, this::isUntargetable);
        registerKeyValue(Keys.VANISH_PREVENTS_TARGETING, this::untargetable);

        registerFieldGetter(Keys.INVISIBLE, () -> this.invisible);
        registerKeyValue(Keys.INVISIBLE, this::invisible);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.INVISIBLE, this.invisible)
                .set(Keys.VANISH, this.vanish)
                .set(Keys.VANISH_IGNORES_COLLISION, this.collision)
                .set(Keys.VANISH_PREVENTS_TARGETING, this.untargetable);
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.InvisibilityData.INVISIBILITY_DATA_WITH_VANISH;
    }

    private boolean isVanish() {
        return this.vanish;
    }

    private boolean isCollision() {
        return this.collision;
    }

    private boolean isUntargetable() {
        return this.untargetable;
    }

    @Override
    public InvisibilityData asMutable() {
        return new SpongeInvisibilityData(this.vanish, this.collision, this.untargetable, this.invisible);
    }

}
