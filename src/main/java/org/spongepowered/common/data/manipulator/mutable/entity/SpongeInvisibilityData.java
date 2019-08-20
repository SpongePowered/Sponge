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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableInvisibilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.InvisibilityData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeInvisibilityData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

public class SpongeInvisibilityData extends AbstractData<InvisibilityData, ImmutableInvisibilityData> implements InvisibilityData {

    private boolean vanish;
    private boolean collision;
    private boolean untargetable;
    private boolean invisible;

    public SpongeInvisibilityData() {
        this(false, false, false, false);
    }

    public SpongeInvisibilityData(boolean vanish, boolean collision, boolean untargetable, boolean invisible) {
        super(InvisibilityData.class);
        this.vanish = vanish;
        this.collision = collision;
        this.untargetable = untargetable;
        this.invisible = invisible;
        this.registerGettersAndSetters();
    }

    @Override
    public Value<Boolean> invisible() {
        return new SpongeValue<>(Keys.INVISIBLE, false, this.invisible);
    }

    @Override
    public Value<Boolean> vanish() {
        return new SpongeValue<>(Keys.VANISH, false, this.vanish);
    }

    @Override
    public Value<Boolean> ignoresCollisionDetection() {
        return new SpongeValue<>(Keys.VANISH_IGNORES_COLLISION, false, this.collision);
    }

    @Override
    public Value<Boolean> untargetable() {
        return new SpongeValue<>(Keys.VANISH_PREVENTS_TARGETING, false, this.untargetable);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.VANISH, this::isVanish);
        registerFieldSetter(Keys.VANISH, (value) -> this.vanish = value);
        registerKeyValue(Keys.VANISH, this::vanish);

        registerFieldGetter(Keys.VANISH_IGNORES_COLLISION, this::isCollision);
        registerFieldSetter(Keys.VANISH_IGNORES_COLLISION, (value) -> this.collision = value);
        registerKeyValue(Keys.VANISH_IGNORES_COLLISION, this::ignoresCollisionDetection);

        registerFieldGetter(Keys.VANISH_PREVENTS_TARGETING, this::isUntargetable);
        registerFieldSetter(Keys.VANISH_PREVENTS_TARGETING, (value) -> this.untargetable = value);
        registerKeyValue(Keys.VANISH_PREVENTS_TARGETING, this::untargetable);

        registerFieldGetter(Keys.INVISIBLE, () -> this.invisible);
        registerFieldSetter(Keys.INVISIBLE, (value) -> this.invisible = value);
        registerKeyValue(Keys.INVISIBLE, this::invisible);
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.InvisibilityData.INVISIBILITY_DATA_WITH_VANISH;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.INVISIBLE, this.invisible)
                .set(Keys.VANISH, this.vanish)
                .set(Keys.VANISH_IGNORES_COLLISION, this.collision)
                .set(Keys.VANISH_PREVENTS_TARGETING, this.untargetable);
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
    public InvisibilityData copy() {
        return new SpongeInvisibilityData(this.vanish, this.collision, this.untargetable, this.invisible);
    }

    @Override
    public ImmutableInvisibilityData asImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeInvisibilityData.class, this.vanish, this.collision, this.untargetable, this.invisible);
    }

}
