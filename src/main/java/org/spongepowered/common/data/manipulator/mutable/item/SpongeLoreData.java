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

import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.List;

public class SpongeLoreData extends AbstractData<LoreData, ImmutableLoreData> implements LoreData {

    private List<Text> lore;

    public SpongeLoreData() {
        this(new ArrayList<>());
    }

    public SpongeLoreData(List<Text> lore) {
        super(LoreData.class);
        this.lore = Lists.newArrayList(lore);
        registerGettersAndSetters();
    }

    @Override
    public ListValue<Text> lore() {
        return new SpongeListValue<>(Keys.ITEM_LORE, this.lore);
    }

    @Override
    public LoreData copy() {
        return new SpongeLoreData(this.lore);
    }

    @Override
    public ImmutableLoreData asImmutable() {
        return new ImmutableSpongeLoreData(this.lore);
    }

    @Override
    public int compareTo(LoreData o) {
        return Booleans.compare(o.lore().containsAll(this.lore),
                this.lore.containsAll(o.lore().get()));
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.ITEM_LORE.getQuery(), SpongeTexts.asJson(this.lore));
    }

    public List<Text> getLore() {
        return this.lore;
    }

    public void setLore(List<Text> lore) {
        this.lore = Lists.newArrayList(lore);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ITEM_LORE, SpongeLoreData.this::getLore);
        registerFieldSetter(Keys.ITEM_LORE, SpongeLoreData.this::setLore);
        registerKeyValue(Keys.ITEM_LORE, SpongeLoreData.this::lore);
    }
}
