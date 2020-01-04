package org.spongepowered.common.registry.builtin;

import net.minecraft.entity.passive.CatEntity;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.CatType;
import org.spongepowered.common.data.type.SpongeCatType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Collectors;

public final class CatTypeRegistry {

    private CatTypeRegistry() {
    }

    public static void generateRegistry(SpongeCatalogRegistry registry) {
        registry
            .registerRegistry(CatType.class, CatalogKey.minecraft("cat_type"), () -> {
                // Meowzers
                return CatEntity.field_213425_bD.entrySet()
                    .stream()
                    .map(kv -> {
                        final String value = kv.getValue().getPath();

                        return new SpongeCatType(CatalogKey.minecraft(value.substring(value.lastIndexOf("."), value.lastIndexOf("/") + 1)), kv.getKey());
                    })
                    .collect(Collectors.toSet());
            }, true);
    }
}
