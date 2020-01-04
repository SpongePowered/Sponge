package org.spongepowered.common.statistic;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.statistic.Statistic;

import java.text.NumberFormat;
import java.util.Optional;

public class SpongeStatistic implements Statistic {

    @Override
    public CatalogKey getKey() {
        return null;
    }

    @Override
    public Optional<Criterion> getCriterion() {
        return Optional.empty();
    }

    @Override
    public NumberFormat getFormat() {
        return null;
    }
}
