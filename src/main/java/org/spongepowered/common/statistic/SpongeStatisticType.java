package org.spongepowered.common.statistic;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.statistic.Statistic;

public final class SpongeStatisticType<T extends CatalogType> extends SpongeStatistic implements Statistic.ForCatalog<T> {

    @Override
    public T getType() {
        return null;
    }
}
