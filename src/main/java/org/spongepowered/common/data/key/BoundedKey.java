package org.spongepowered.common.data.key;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.value.BoundedValue;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class BoundedKey<V extends BoundedValue<E>, E> extends SpongeKey<V, E> {

    private final Supplier<E> minimum;
    private final Supplier<E> maximum;

    BoundedKey(CatalogKey key, TypeToken<V> valueToken,
            TypeToken<E> elementToken, Comparator<E> elementComparator,
            BiPredicate<E, E> elementIncludesTester, Supplier<E> minimum, Supplier<E> maximum) {
        super(key, valueToken, elementToken, elementComparator, elementIncludesTester);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Supplier<E> getMinimum() {
        return this.minimum;
    }

    public Supplier<E> getMaximum() {
        return this.maximum;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("minimum", this.maximum.get())
                .add("maximum", this.maximum.get());
    }
}
