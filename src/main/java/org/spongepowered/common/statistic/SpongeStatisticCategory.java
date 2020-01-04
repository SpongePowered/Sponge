package org.spongepowered.common.statistic;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticCategory;

import java.util.Collection;

public class SpongeStatisticCategory<S extends Statistic> implements StatisticCategory {

    @Override
    public Collection<S> getStatistics() {
        return null;
    }

    @Override
    public CatalogKey getKey() {
        return null;
    }
}
