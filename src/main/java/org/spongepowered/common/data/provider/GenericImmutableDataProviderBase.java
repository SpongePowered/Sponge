package org.spongepowered.common.data.provider;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.lang.reflect.TypeVariable;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class GenericImmutableDataProviderBase<H, V extends Value<E>, E> extends ImmutableDataProvider<V, E> {

    private static final TypeVariable<?> holderTypeParameter = GenericImmutableDataProviderBase.class.getTypeParameters()[0];
    private final Class<H> holderType;

    GenericImmutableDataProviderBase(Key<V> key, Class<H> holderType) {
        super(key);
        this.holderType = holderType;
    }

    GenericImmutableDataProviderBase(Key<V> key) {
        super(key);
        this.holderType = (Class<H>) TypeToken.of(this.getClass()).resolveType(holderTypeParameter).getRawType();
    }

    private boolean isTypeAllowed(DataHolder dataHolder) {
        return this.holderType.isInstance(dataHolder);
    }

    /**
     * Gets whether the target data holder is supported.
     *
     * @param dataHolder The data holder
     * @return Whether supported
     */
    protected boolean supports(H dataHolder) {
        return true;
    }

    /**
     * Attempts to get data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return The element, if present
     */
    protected abstract Optional<E> getFrom(H dataHolder);

    /**
     * Attempts to set data for the target data holder.
     *
     * @param dataHolder The data holder
     * @param value The value
     * @return The new immutable object, if successful
     */
    protected abstract Optional<H> set(H dataHolder, E value);

    /**
     * Constructs a value for the given element and data holder.
     *
     * @param dataHolder The data holder
     * @param element The element
     * @return The value
     */
    protected V constructValue(H dataHolder, E element) {
        return Value.genericImmutableOf(this.getKey(), element);
    }

    /**
     * Attempts to remove the data from the target data holder.
     *
     * @param dataHolder The data holder
     * @return The new immutable object, if successful
     */
    protected Optional<H> removeFrom(H dataHolder) {
        return Optional.empty();
    }

    @Override
    public final Optional<E> get(DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getFrom((H) dataHolder);
    }

    @Override
    public Optional<V> getValue(DataHolder dataHolder) {
        return this.get(dataHolder).map(e -> this.constructValue((H) dataHolder, e));
    }

    @Override
    public boolean isSupported(DataHolder dataHolder) {
        return this.isTypeAllowed(dataHolder) && this.supports((H) dataHolder);
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> with(I immutable, E value) {
        if (!this.isSupported(immutable)) {
            return Optional.empty();
        }
        return (Optional<I>) this.set((H) immutable, value);
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> without(I immutable) {
        if (!this.isSupported(immutable)) {
            return Optional.empty();
        }
        return (Optional<I>) this.removeFrom((H) immutable);
    }
}
