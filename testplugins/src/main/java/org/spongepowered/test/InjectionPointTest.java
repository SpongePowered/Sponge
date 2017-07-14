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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.inject.InjectionPoint;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "injection_point_test", name = "Injection Point Test")
public class InjectionPointTest {

    @Inject private Injector injector;

    @Listener
    public void onInit(GamePreInitializationEvent event) {
        final Injector injector = this.injector.createChildInjector(new TestModule());
        injector.getInstance(TestObject.class);
    }

    public static class TestObject {

        @Inject
        public TestObject(
                @Named("Injection Test Logger A") Logger loggerA,
                Logger pluginLogger,
                @Named("Injection Test Logger B") Logger loggerB) {
            loggerA.info("This is the test logger A.");
            loggerB.info("This is the test logger B.");
            pluginLogger.info("This is the plugin logger.");
        }
    }

    public static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Logger.class).annotatedWith(Named.class).toProvider(NamedLoggerProvider.class);
        }

        public static class NamedLoggerProvider implements Provider<Logger> {

            @Inject private InjectionPoint point;

            @Override
            public Logger get() {
                return LoggerFactory.getLogger(this.point.getAnnotation(Named.class).value());
            }
        }
    }
}
