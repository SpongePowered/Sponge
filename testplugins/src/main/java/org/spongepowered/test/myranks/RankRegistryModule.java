package org.spongepowered.test.myranks;

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.test.myranks.api.Rank;
import org.spongepowered.test.myranks.api.Ranks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RankRegistryModule implements AdditionalCatalogRegistryModule<Rank> {

    @RegisterCatalog(Ranks.class)
    private final Map<String, Rank> rankMap = new HashMap<>();

    @Override
    public void registerDefaults() {
        register(new RankImpl("user", "User"));
        register(new RankImpl("staff", "Staff"));
    }

    private void register(Rank rank) {
        this.rankMap.put(rank.getId(), rank);
    }

    @Override
    public void registerAdditionalCatalog(Rank extraCatalog) {
        if (!this.rankMap.containsKey(extraCatalog.getId())) {
            register(extraCatalog);
        }
    }

    @Override
    public Optional<Rank> getById(String id) {
        return Optional.ofNullable(this.rankMap.get(id));
    }

    @Override
    public Collection<Rank> getAll() {
        return Collections.unmodifiableCollection(this.rankMap.values());
    }
}
