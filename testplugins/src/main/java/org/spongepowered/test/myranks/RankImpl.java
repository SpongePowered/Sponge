package org.spongepowered.test.myranks;

import org.spongepowered.test.myranks.api.Rank;

public class RankImpl implements Rank {

    private final String id;
    private final String name;

    public RankImpl(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Rank && this.id.equals(((Rank) obj).getId()));
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
