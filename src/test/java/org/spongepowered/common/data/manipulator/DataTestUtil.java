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
package org.spongepowered.common.data.manipulator;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import net.minecraft.init.Bootstrap;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeGameRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class DataTestUtil {

    private DataTestUtil() {}

    private static void initializeEnvironment() {
        Bootstrap.register();

        Game game = mock(SpongeGame.class);
        RegistryHelper.setFinalStatic(Sponge.class, "game", game);

        SpongeGameRegistry registry = new SpongeGameRegistry();
        when(game.getRegistry()).thenReturn(registry);
        when(game.getDataManager()).thenCallRealMethod();

        // Initialize plugin manager
        PluginManager manager = mock(PluginManager.class);
        when(manager.getPlugin(anyString())).thenReturn(Optional.of(mock(PluginContainer.class)));
        when(game.getPluginManager()).thenReturn(manager);

        // Initialize platform
        Platform platform = new SpongePlatform(manager, SpongeImpl.MINECRAFT_VERSION);
        when(game.getPlatform()).thenReturn(platform);

        new SpongeImpl(mock(Injector.class), game, manager);

        registry.preRegistryInit();
        registry.preInit();
        registry.init();
        //registry.postInit();
    }

    @SuppressWarnings("unchecked")
    static List<Object[]> generateManipulatorTestObjects() throws Exception {
        initializeEnvironment();

        final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap = getBuilderMap();
        final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap = getDelegateMap();
        return delegateMap.entrySet().stream()
                .filter(entry -> isValidForTesting(entry.getKey()))
                .map(entry -> new Object[]{entry.getKey().getSimpleName(), entry.getKey(), manipulatorBuilderMap.get(entry.getKey())})
                .collect(Collectors.toList());
    }

    private static boolean isValidForTesting(Class<?> clazz) {
        return !Modifier.isInterface(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
               /*&& clazz.getAnnotation(ImplementationRequiredForTest.class) == null*/;
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> getDelegateMap() throws Exception {
        final Field delegateField = SpongeDataManager.class.getDeclaredField("processorMap");
        delegateField.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>>) delegateField.get(SpongeDataManager.getInstance());

    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> getBuilderMap() throws Exception {
        final Field builderMap = SpongeDataManager.class.getDeclaredField("builderMap");
        builderMap.setAccessible(true);
        return (Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>>) builderMap.get(SpongeDataManager.getInstance());
    }

}
