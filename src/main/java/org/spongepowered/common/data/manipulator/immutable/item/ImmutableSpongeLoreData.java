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

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.GetterFunction;

import java.util.List;

public class ImmutableSpongeLoreData extends AbstractImmutableData<ImmutableLoreData, LoreData> implements ImmutableLoreData {

    private final ImmutableList<Text> lore;

    public ImmutableSpongeLoreData() {
        this(ImmutableList.of(Texts.of()));
    }

    public ImmutableSpongeLoreData(List<Text> lore) {
        super(ImmutableLoreData.class);
        this.lore = ImmutableList.copyOf(lore);
        registerGetters();
    }

    @Override
    public ImmutableListValue<Text> lore() {
        return new ImmutableSpongeListValue<>(Keys.ITEM_LORE, this.lore);
    }

    @Override
    public LoreData asMutable() {
        return new SpongeLoreData(this.lore);
    }

    @Override
    public int compareTo(ImmutableLoreData o) {
        return Booleans.compare(o.lore().containsAll(this.lore),
                this.lore.containsAll(o.lore().get()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.ITEM_LORE.getQuery(), SpongeTexts.asJson(this.lore));
    }

    public List<Text> getLore() {
        return this.lore;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ITEM_LORE, ImmutableSpongeLoreData.this::getLore);
        registerKeyValue(Keys.ITEM_LORE, ImmutableSpongeLoreData.this::lore);
    }
}
