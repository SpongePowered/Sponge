package org.spongepowered.common.registry;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public final class RegistryUtil {

    public static <T extends CatalogType, E> void generateRegistry(SpongeCatalogRegistry registry, CatalogKey key, Class<T> catalogClass, Stream<E> valueStream, boolean generateSuppliers) {
        registry.registerRegistry(catalogClass, key, () -> valueStream.map(value -> (T) value).collect(Collectors.toSet()), generateSuppliers);
    }

    public static <T extends CatalogType, E> void generateSuppliers(SpongeCatalogRegistry registry, Class<T> catalogClass, Function<E, String> suggestedNameSupplier, Stream<E> valueStream) {
        valueStream.forEach(value -> registry.registerSupplier(catalogClass, suggestedNameSupplier.apply(value), () -> (T) value));
    }
}
