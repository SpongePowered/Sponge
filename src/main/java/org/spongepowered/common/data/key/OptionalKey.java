package org.spongepowered.common.data.key;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.value.OptionalValue;
import org.spongepowered.api.data.value.Value;

import java.lang.reflect.TypeVariable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiPredicate;

public class OptionalKey<V extends OptionalValue<E>, E> extends SpongeKey<V, Optional<E>> {

    private static final TypeVariable<?> optionalElementParameter = Optional.class.getTypeParameters()[0];

    private static <E> TypeToken<Value<E>> createValueToken(TypeToken<E> elementToken) {
        return new TypeToken<Value<E>>() {}.where(new TypeParameter<E>() {}, elementToken);
    }

    private final OptionalUnwrappedKey<Value<E>, E> unwrappedKey;

    OptionalKey(CatalogKey key, TypeToken<V> valueToken,
            TypeToken<Optional<E>> elementToken, Comparator<Optional<E>> elementComparator,
            BiPredicate<Optional<E>, Optional<E>> elementIncludesTester) {
        super(key, valueToken, elementToken, elementComparator, elementIncludesTester);

        final TypeToken<E> unwrappedElementToken = (TypeToken<E>) elementToken.resolveType(optionalElementParameter);
        final TypeToken<Value<E>> unwrappedValueToken = createValueToken(unwrappedElementToken);
        final CatalogKey unwrappedKey = CatalogKey.of(key.getNamespace(), key.getValue() + "_non_optional");
        final Comparator<E> unwrappedComparator = (o1, o2) -> elementComparator.compare(Optional.ofNullable(o1), Optional.ofNullable(o2));
        final BiPredicate<E, E> unwrappedIncludesTester = (o1, o2) -> elementIncludesTester.test(Optional.ofNullable(o1), Optional.ofNullable(o2));

        this.unwrappedKey = new OptionalUnwrappedKey<>(unwrappedKey, unwrappedValueToken, unwrappedElementToken,
                unwrappedComparator, unwrappedIncludesTester, this);
    }

    public OptionalUnwrappedKey<Value<E>, E> getUnwrappedKey() {
        return this.unwrappedKey;
    }
}
