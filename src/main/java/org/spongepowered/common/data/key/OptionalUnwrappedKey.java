package org.spongepowered.common.data.key;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.value.OptionalValue;
import org.spongepowered.api.data.value.Value;

import java.util.Comparator;
import java.util.function.BiPredicate;

public class OptionalUnwrappedKey<V extends Value<E>, E> extends SpongeKey<V, E> {

    private final OptionalKey<? extends OptionalValue<E>, E> wrappedKey;

    OptionalUnwrappedKey(CatalogKey key, TypeToken<V> valueToken,
            TypeToken<E> elementToken, Comparator<E> elementComparator,
            BiPredicate<E, E> elementIncludesTester,
            OptionalKey<? extends OptionalValue<E>, E> wrappedKey) {
        super(key, valueToken, elementToken, elementComparator, elementIncludesTester);
        this.wrappedKey = wrappedKey;
    }

    public OptionalKey<? extends OptionalValue<E>, E> getWrappedKey() {
        return this.wrappedKey;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("wrapped", this.wrappedKey);
    }
}
