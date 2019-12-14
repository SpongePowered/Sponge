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
package org.spongepowered.common.data.manipulator.immutable.item;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableHideData;
import org.spongepowered.api.data.manipulator.mutable.item.HideData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeHideData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeHideData extends AbstractImmutableData<ImmutableHideData, HideData> implements ImmutableHideData {

    private final boolean enchantments;
    private final boolean attributes;
    private final boolean unbreakable;
    private final boolean canDestroy;
    private final boolean canPlace;
    private final boolean miscellaneous;
    private final Immutable<Boolean> enchantmentsValue;
    private final Immutable<Boolean> attributesValue;
    private final Immutable<Boolean> unbreakableValue;
    private final Immutable<Boolean> canDestroyValue;
    private final Immutable<Boolean> canPlaceValue;
    private final Immutable<Boolean> miscellaneousValue;

    public ImmutableSpongeHideData(boolean enchantments, boolean attributes, boolean unbreakable, boolean canDestroy, boolean canPlace,
            boolean miscellaneous) {
        super(ImmutableHideData.class);
        this.enchantments = enchantments;
        this.attributes = attributes;
        this.unbreakable = unbreakable;
        this.canDestroy = canDestroy;
        this.canPlace = canPlace;
        this.miscellaneous = miscellaneous;

        this.enchantmentsValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_ENCHANTMENTS, false, enchantments);
        this.attributesValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_ATTRIBUTES, false, attributes);
        this.unbreakableValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_UNBREAKABLE, false, unbreakable);
        this.canDestroyValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_CAN_DESTROY, false, canDestroy);
        this.canPlaceValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_CAN_PLACE, false, canPlace);
        this.miscellaneousValue = ImmutableSpongeValue.cachedOf(Keys.HIDE_MISCELLANEOUS, false, miscellaneous);

        this.registerGetters();
    }

    @Override
    public Immutable<Boolean> hideEnchantments() {
        return this.enchantmentsValue;
    }

    @Override
    public Immutable<Boolean> hideAttributes() {
        return this.attributesValue;
    }

    @Override
    public Immutable<Boolean> hideUnbreakable() {
        return this.unbreakableValue;
    }

    @Override
    public Immutable<Boolean> hideCanDestroy() {
        return this.canDestroyValue;
    }

    @Override
    public Immutable<Boolean> hideCanPlace() {
        return this.canPlaceValue;
    }

    @Override
    public Immutable<Boolean> hideMiscellaneous() {
        return this.miscellaneousValue;
    }

    @Override
    public HideData asMutable() {
        return new SpongeHideData(this.enchantments, this.attributes, this.unbreakable, this.canDestroy, this.canPlace, this.miscellaneous);
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
    protected void registerGetters() {
        this.registerFieldGetter(Keys.HIDE_ENCHANTMENTS, this::isEnchantments);
        this.registerKeyValue(Keys.HIDE_ENCHANTMENTS, this::hideEnchantments);

        this.registerFieldGetter(Keys.HIDE_ATTRIBUTES, this::isAttributes);
        this.registerKeyValue(Keys.HIDE_ATTRIBUTES, this::hideAttributes);

        this.registerFieldGetter(Keys.HIDE_UNBREAKABLE, this::isUnbreakable);
        this.registerKeyValue(Keys.HIDE_UNBREAKABLE, this::hideUnbreakable);

        this.registerFieldGetter(Keys.HIDE_CAN_DESTROY, this::isCanDestroy);
        this.registerKeyValue(Keys.HIDE_CAN_DESTROY, this::hideCanDestroy);

        this.registerFieldGetter(Keys.HIDE_CAN_PLACE, this::isCanPlace);
        this.registerKeyValue(Keys.HIDE_CAN_PLACE, this::hideCanPlace);

        this.registerFieldGetter(Keys.HIDE_MISCELLANEOUS, this::isMiscellaneous);
        this.registerKeyValue(Keys.HIDE_MISCELLANEOUS, this::hideMiscellaneous);
    }

    public boolean isMiscellaneous() {
        return this.miscellaneous;
    }

    public boolean isEnchantments() {
        return this.enchantments;
    }

    public boolean isAttributes() {
        return this.attributes;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public boolean isCanDestroy() {
        return this.canDestroy;
    }

    public boolean isCanPlace() {
        return this.canPlace;
    }
}
