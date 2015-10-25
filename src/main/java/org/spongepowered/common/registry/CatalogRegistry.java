package org.spongepowered.common.registry;

import org.spongepowered.api.CatalogType;

import java.util.Collection;
import java.util.Optional;

public interface CatalogRegistry<T extends CatalogType> extends BaseRegistry {

    Optional<T> getById(String id);

    Collection<T> getAll();

}
