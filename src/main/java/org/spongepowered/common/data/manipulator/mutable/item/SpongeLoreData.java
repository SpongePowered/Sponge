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
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

import java.util.ArrayList;
import java.util.List;

public class SpongeLoreData extends AbstractData<LoreData, ImmutableLoreData> implements LoreData {

    private List<Text> lore;

    public SpongeLoreData() {
        this(new ArrayList<Text>());
    }

    public SpongeLoreData(List<Text> lore) {
        super(LoreData.class);
        this.lore = lore;
        registerGettersAndSetters();
    }

    @Override
    public ListValue<Text> lore() {
        return new SpongeListValue<Text>(Keys.ITEM_LORE, lore);
    }

    @Override
    public LoreData copy() {
        return new SpongeLoreData(lore);
    }

    @Override
    public ImmutableLoreData asImmutable() {
        return new ImmutableSpongeLoreData(lore);
    }

    @Override
    public int compareTo(LoreData o) {
        return Booleans.compare(o.lore().containsAll(this.lore),
                this.lore.containsAll(o.lore().get()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.ITEM_LORE.getQuery(), this.asJson(this.lore));
    }

    private List<String> asJson(List<Text> lore) {
        List<String> jsonLines = Lists.newArrayList();
        for (Text line : lore) {
            jsonLines.add(Texts.json().to(line));
        }
        return jsonLines;
    }

    public List<Text> getLore() {
        return this.lore;
    }

    public void setLore(List<Text> lore) {
        this.lore = lore;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ITEM_LORE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getLore();
            }
        });
        registerFieldSetter(Keys.ITEM_LORE, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setLore((List<Text>) value);
            }
        });
        registerKeyValue(Keys.ITEM_LORE, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return lore();
            }
        });
    }
}
