package org.spongepowered.common.statistic;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticCategory;

public final class SpongeStatisticCategoryType<T extends CatalogType> extends SpongeStatisticCategory implements StatisticCategory.ForCatalog<T> {

    @Override
    public TypeToken<T> getType() {
        return null;
    }

    @Override
    public Statistic.ForCatalog<T> getStatistic(T catalogType) {
        return null;
    }
}
