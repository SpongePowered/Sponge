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

import org.spongepowered.test.myhomes.MyHomes;
import org.spongepowered.test.myhomes.data.home.Home;
import org.spongepowered.test.myhomes.data.home.HomeData;
import org.spongepowered.test.myhomes.data.home.ImmutableHomeData;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class HomeDataBuilder extends AbstractDataBuilder<HomeData> implements DataManipulatorBuilder<HomeData, ImmutableHomeData> {

    public static final int CONTENT_VERSION = 2;

    public HomeDataBuilder() {
        super(HomeData.class, CONTENT_VERSION);
    }

    @Override
    public HomeData create() {
        return new HomeDataImpl();
    }

    @Override
    public Optional<HomeData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<HomeData> buildContent(DataView container) throws InvalidDataException {
        if(!container.contains(MyHomes.HOMES)) return Optional.empty();

        HomeData data = new HomeDataImpl();

        container.getView(MyHomes.HOMES.getQuery())
                .get().getKeys(false).forEach(name -> data.homes().put(name.toString(), container.getSerializable(name, Home.class)
                .orElseThrow(InvalidDataException::new)));

        container.getSerializable(MyHomes.DEFAULT_HOME.getQuery(), Home.class).ifPresent(home -> {
            data.set(MyHomes.DEFAULT_HOME, home);
        });

        return Optional.of(data);
    }

    public static class HomesUpdater implements DataContentUpdater {

        @Override
        public int getInputVersion() {
            return 1;
        }

        @Override
        public int getOutputVersion() {
            return 2;
        }

        @Override
        public DataView update(DataView content) {
            content.set(MyHomes.HOMES, ImmutableMap.of());

            return content;
        }
    }
}
