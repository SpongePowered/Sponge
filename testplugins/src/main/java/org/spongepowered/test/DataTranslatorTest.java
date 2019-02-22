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
package org.spongepowered.test;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = "data_translator_test", name = "Data Translator Test", description = "test custom DataTranslator", version = "0.0.0")
public class DataTranslatorTest {

    @Inject private Logger logger;

    @Listener
    public void onRegisterDataTranslators(GameRegistryEvent.Register<DataTranslator<?>> event) {
        event.register(new MyObjectDataTranslator());
        this.logger.info("Registered MyObjectDataTranslator");
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        final DataQuery query = DataQuery.of("Test");

        final DataContainer container = DataContainer.createNew();
        container.set(query, new MyObject("MyValue"));

        if (container.getObject(query, MyObject.class).get().getValue().equals("MyValue")) {
            this.logger.info("MyObjectDataTranslator: OK");
        } else {
            this.logger.info("MyObjectDataTranslator: FAIL");
        }
    }

    public static class MyObject {

        private final String value;

        public MyObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class MyObjectDataTranslator implements DataTranslator<MyObject> {

        @Override
        public TypeToken<MyObject> getToken() {
            return TypeToken.of(MyObject.class);
        }

        @Override
        public MyObject translate(DataView view) throws InvalidDataException {
            return new MyObject(view.getString(DataQuery.of("value")).get());
        }

        @Override
        public DataContainer translate(MyObject obj) throws InvalidDataException {
            return DataContainer.createNew().set(DataQuery.of("value"), obj.getValue());
        }

        @Override
        public String getId() {
            return "data_translator_test:my_object";
        }

        @Override
        public String getName() {
            return "my_object";
        }
    }
}
