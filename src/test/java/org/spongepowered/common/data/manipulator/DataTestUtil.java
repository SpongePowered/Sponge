package org.spongepowered.common.data.manipulator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.data.DataRegistrar;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DataTestUtil {

    private DataTestUtil() {}

    @SuppressWarnings("unchecked")
    static List<Object[]> generateManipulatorTestObjects() throws Exception {
        generateKeyMap();
        SpongeGame mockGame = mock(SpongeGame.class);

        when(mockGame.getDataManager()).thenReturn(SpongeDataManager.getInstance());
        DataRegistrar.setupSerialization(mockGame);
        final List<Object[]> list = new ArrayList<>();

        final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> manipulatorBuilderMap = getBuilderMap();
        final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> delegateMap = getDelegateMap();
        delegateMap.entrySet().stream().filter(entry -> isValidForTesting(entry.getKey())).forEach(entry -> {
            list.add(new Object[]{entry.getKey().getSimpleName(), entry.getKey(), manipulatorBuilderMap.get(entry.getKey())});
        });
        return list;
    }

    private static boolean isValidForTesting(Class<?> clazz) {
        return !Modifier.isInterface(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
               && clazz.getAnnotation(ImplementationRequiredForTest.class) == null;
    }

    @SuppressWarnings("unchecked")
    private static void generateKeyMap() throws Exception {
        Method mapGetter = KeyRegistry.class.getDeclaredMethod("getKeyMap");
        mapGetter.setAccessible(true);
        final Map<String, Key<?>> mapping = (Map<String, Key<?>>) mapGetter.invoke(null);
        for (Field field : Keys.class.getDeclaredFields()) {
            if (!mapping.containsKey(field.getName().toLowerCase())) {
                continue;
            }
            Field modifierField = Field.class.getDeclaredField("modifiers");
            modifierField.setAccessible(true);
            modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, mapping.get(field.getName().toLowerCase()));
        }
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
