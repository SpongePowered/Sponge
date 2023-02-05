package org.spongepowered.common.world.server;

import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.TicketType;
import org.spongepowered.common.accessor.server.level.TicketTypeAccessor;

import java.util.Comparator;
import java.util.Objects;

public final class SpongeTicketTypeBuilder<T> implements TicketType.Builder<T> {

    private String name;
    private Comparator<T> comparator;
    private long lifetime = -1;

    @Override
    public TicketType.Builder<T> reset() {
        this.name = null;
        this.comparator = null;
        this.lifetime = -1;
        return this;
    }

    @Override
    public TicketType.Builder<T> name(final String name) {
        this.name = Objects.requireNonNull(name, "Name cannot null");
        return this;
    }

    @Override
    public TicketType.Builder<T> comparator(final Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public TicketType.Builder<T> lifetime(final Ticks lifetime) {
        this.lifetime = Objects.requireNonNull(lifetime, "Lifetime cannot be null").ticks();
        return this;
    }

    @Override
    public TicketType.Builder<T> neverExpires() {
        this.lifetime = 0;
        return this;
    }

    @Override
    public TicketType<T> build() {
        Objects.requireNonNull(this.name, "Name cannot nulll");
        if (this.lifetime < 0) {
            throw new IllegalStateException("The lifetime is required to be a positive integer");
        }
        if (this.comparator == null) {
            this.comparator = (v1, v2) -> 0;
        }

        return (TicketType<T>) TicketTypeAccessor.accessor$createInstance(this.name, this.comparator, this.lifetime);
    }
}
