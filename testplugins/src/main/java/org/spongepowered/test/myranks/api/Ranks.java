package org.spongepowered.test.myranks.api;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public class Ranks {

    public static final Rank USER = DummyObjectProvider.createFor(Rank.class, "USER");
    public static final Rank STAFF = DummyObjectProvider.createFor(Rank.class, "STAFF");


    private Ranks() {
    }
}
