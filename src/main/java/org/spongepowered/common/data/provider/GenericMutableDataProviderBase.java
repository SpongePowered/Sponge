package org.spongepowered.common.data.provider;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.TypeVariable;
import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class GenericMutableDataProviderBase<H, V extends Value<E>, E> extends MutableDataProvider<V, E> {

    private static final TypeVariable<?> holderTypeParameter = GenericMutableDataProviderBase.class.getTypeParameters()[0];
    private final Class<H> holderType;

    GenericMutableDataProviderBase(Key<V> key, Class<H> holderType) {
        super(key);
        this.holderType = holderType;
    }

    GenericMutableDataProviderBase(Key<V> key) {
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
     * @return Whether applying was successful
     */
    protected abstract boolean set(H dataHolder, E value);

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
     * @return Whether the removal was successful
     */
    protected boolean removeFrom(H dataHolder) {
        return false;
    }

    @Override
    public final boolean isSupported(DataHolder dataHolder) {
        return this.isTypeAllowed(dataHolder) && this.supports((H) dataHolder);
    }

    @Override
    public Optional<V> getValue(DataHolder dataHolder) {
        return this.get(dataHolder).map(e -> this.constructValue((H) dataHolder, e));
    }

    @Override
    public final Optional<E> get(DataHolder dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return Optional.empty();
        }
        return this.getFrom((H) dataHolder);
    }

    @Override
    public DataTransactionResult offerValue(DataHolder.Mutable dataHolder, V value) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        final Value.Immutable<E> replacementValue = value.asImmutable();
        try {
            if (this.set((H) dataHolder, value.get())) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                originalValue.ifPresent(builder::replace);
                return builder.result(DataTransactionResult.Type.SUCCESS).success(replacementValue).build();
            }
            return DataTransactionResult.failResult(replacementValue);
        } catch (Exception e) {
            SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    @Override
    public final DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        final Value.Immutable<E> replacementValue = Value.genericImmutableOf(this.getKey(), element).asImmutable();
        try {
            if (this.set((H) dataHolder, element)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                originalValue.ifPresent(builder::replace);
                return builder.result(DataTransactionResult.Type.SUCCESS).success(replacementValue).build();
            }
            return DataTransactionResult.failResult(replacementValue);
        } catch (Exception e) {
            SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
            return DataTransactionResult.errorResult(replacementValue);
        }
    }

    @Override
    public final DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        if (!this.isSupported(dataHolder)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Value.Immutable<E>> originalValue = this.getFrom((H) dataHolder)
                .map(e -> this.constructValue((H) dataHolder, e).asImmutable());
        if (!originalValue.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        if (this.removeFrom((H) dataHolder)) {
            return DataTransactionResult.successRemove(originalValue.get());
        }
        return DataTransactionResult.failNoData();
    }
}
