package org.spongepowered.test.myranks.api;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;

public class Keys {

    public static final Key<Value<Rank>> RANK =
            Key.builder().id("rank")
                    .name("Rank")
                    .query(DataQuery.of("Rank"))
                    .type(new TypeToken<Value<Rank>>() {})
                    .build();
}
