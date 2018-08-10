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
package org.spongepowered.test.myhomes.data.home.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.test.myhomes.MyHomes;
import org.spongepowered.test.myhomes.data.home.Home;
import org.spongepowered.test.myhomes.data.home.HomeData;
import org.spongepowered.test.myhomes.data.home.ImmutableHomeData;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import java.util.Map;

public class ImmutableHomeDataImpl extends AbstractImmutableData<ImmutableHomeData, HomeData> implements ImmutableHomeData {

    private final Home defaultHome;
    private final ImmutableMap<String, Home> homes;

    private final ImmutableValue<Home> defaultHomeValue;
    private final ImmutableMapValue<String, Home> homesValue;

    public ImmutableHomeDataImpl() {
        this(null, ImmutableMap.of());
    }

    public ImmutableHomeDataImpl(Home defaultHome, Map<String, Home> homes) {
        this.defaultHome = checkNotNull(defaultHome);
        this.homes = ImmutableMap.copyOf(checkNotNull(homes));

        this.defaultHomeValue = Sponge.getRegistry().getValueFactory()
                .createValue(MyHomes.DEFAULT_HOME, defaultHome)
                .asImmutable();

        this.homesValue = Sponge.getRegistry().getValueFactory()
                .createMapValue(MyHomes.HOMES, homes, ImmutableMap.of())
                .asImmutable();
    }

    // Override if you have a separate interface
    @Override
    public ImmutableValue<Home> defaultHome() {
        return this.defaultHomeValue;
    }

    // Override if you have a separate interface
    @Override
    public ImmutableMapValue<String, Home> homes() {
        return this.homesValue;
    }

    private Home getDefaultHome() {
        return this.defaultHome;
    }

    private Map<String, Home> getHomes() {
        return this.homes;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(MyHomes.DEFAULT_HOME, this::defaultHome);
        registerKeyValue(MyHomes.HOMES, this::homes);

        registerFieldGetter(MyHomes.DEFAULT_HOME, this::getDefaultHome);
        registerFieldGetter(MyHomes.HOMES, this::getHomes);
    }

    @Override
    public int getContentVersion() {
        // Update whenever the serialization format changes
        return HomeDataBuilder.CONTENT_VERSION;
    }

    @Override
    public HomeDataImpl asMutable() {
        return new HomeDataImpl(this.defaultHome, this.homes);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        // This is the simplest, but use whatever structure you want!
        if(this.defaultHome != null) {
            container.set(MyHomes.DEFAULT_HOME, this.defaultHome);
        }
        container.set(MyHomes.HOMES, this.homes);

        return container;
    }
}
