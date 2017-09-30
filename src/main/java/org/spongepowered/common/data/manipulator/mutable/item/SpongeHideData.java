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
package org.spongepowered.common.data.manipulator.mutable.item;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableHideData;
import org.spongepowered.api.data.manipulator.mutable.item.HideData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeHideData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeHideData extends AbstractData<HideData, ImmutableHideData> implements HideData {

    private boolean enchantments;
    private boolean attributes;
    private boolean unbreakable;
    private boolean canDestroy;
    private boolean canPlace;
    private boolean miscellaneous;

    public SpongeHideData(boolean enchantments, boolean attributes, boolean unbreakable, boolean canDestroy, boolean canPlace,
            boolean miscellaneous) {
        super(HideData.class);

        this.enchantments = enchantments;
        this.attributes = attributes;
        this.unbreakable = unbreakable;
        this.canDestroy = canDestroy;
        this.canPlace = canPlace;
        this.miscellaneous = miscellaneous;

        registerGettersAndSetters();
    }

    public SpongeHideData() {
        this(false, false, false, false, false, false);
    }

    @Override
    public Value<Boolean> hideEnchantments() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_ENCHANTMENTS, this.enchantments, false);
    }

    @Override
    public Value<Boolean> hideAttributes() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_ATTRIBUTES, this.attributes, false);
    }

    @Override
    public Value<Boolean> hideUnbreakable() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_UNBREAKABLE, this.unbreakable, false);
    }

    @Override
    public Value<Boolean> hideCanDestroy() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_CAN_DESTROY, this.canDestroy, false);
    }

    @Override
    public Value<Boolean> hideCanPlace() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_CAN_PLACE, this.canPlace, false);
    }

    @Override
    public Value<Boolean> hideMiscellaneous() {
        return SpongeValueFactory.getInstance().createValue(Keys.HIDE_MISCELLANEOUS, this.miscellaneous, false);
    }

    @Override
    public HideData copy() {
        return new SpongeHideData(this.enchantments, this.attributes, this.unbreakable, this.canDestroy, this.canPlace, this.miscellaneous);
    }

    @Override
    public ImmutableHideData asImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeHideData.class, this.enchantments, this.attributes, this.unbreakable,
                this.canDestroy, this.canPlace, this.miscellaneous);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.HIDE_ENCHANTMENTS, this.enchantments)
                .set(Keys.HIDE_ATTRIBUTES, this.attributes)
                .set(Keys.HIDE_UNBREAKABLE, this.unbreakable)
                .set(Keys.HIDE_CAN_DESTROY, this.canDestroy)
                .set(Keys.HIDE_CAN_PLACE, this.canPlace)
                .set(Keys.HIDE_MISCELLANEOUS, this.miscellaneous);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.HIDE_ENCHANTMENTS, this::isEnchantments);
        registerFieldSetter(Keys.HIDE_ENCHANTMENTS, this::setEnchantments);
        registerKeyValue(Keys.HIDE_ENCHANTMENTS, this::hideEnchantments);

        registerFieldGetter(Keys.HIDE_ATTRIBUTES, this::isAttributes);
        registerFieldSetter(Keys.HIDE_ATTRIBUTES, this::setAttributes);
        registerKeyValue(Keys.HIDE_ATTRIBUTES, this::hideAttributes);

        registerFieldGetter(Keys.HIDE_UNBREAKABLE, this::isUnbreakable);
        registerFieldSetter(Keys.HIDE_UNBREAKABLE, this::setUnbreakable);
        registerKeyValue(Keys.HIDE_UNBREAKABLE, this::hideUnbreakable);

        registerFieldGetter(Keys.HIDE_CAN_DESTROY, this::isCanDestroy);
        registerFieldSetter(Keys.HIDE_CAN_DESTROY, this::setCanDestroy);
        registerKeyValue(Keys.HIDE_CAN_DESTROY, this::hideCanDestroy);

        registerFieldGetter(Keys.HIDE_CAN_PLACE, this::isCanPlace);
        registerFieldSetter(Keys.HIDE_CAN_PLACE, this::setCanPlace);
        registerKeyValue(Keys.HIDE_CAN_PLACE, this::hideCanPlace);

        registerFieldGetter(Keys.HIDE_MISCELLANEOUS, this::isMiscellaneous);
        registerFieldSetter(Keys.HIDE_MISCELLANEOUS, this::setMiscellaneous);
        registerKeyValue(Keys.HIDE_MISCELLANEOUS, this::hideMiscellaneous);
    }

    public boolean isEnchantments() {
        return this.enchantments;
    }

    public void setEnchantments(boolean enchantments) {
        this.enchantments = enchantments;
    }

    public boolean isAttributes() {
        return this.attributes;
    }

    public void setAttributes(boolean attributes) {
        this.attributes = attributes;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean isCanDestroy() {
        return this.canDestroy;
    }

    public void setCanDestroy(boolean canDestroy) {
        this.canDestroy = canDestroy;
    }

    public boolean isCanPlace() {
        return this.canPlace;
    }

    public void setCanPlace(boolean canPlace) {
        this.canPlace = canPlace;
    }

    public boolean isMiscellaneous() {
        return this.miscellaneous;
    }

    public void setMiscellaneous(boolean miscellaneous) {
        this.miscellaneous = miscellaneous;
    }
}
