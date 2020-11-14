package org.spongepowered.common.world.volume.stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.volume.stream.StreamOptions;

import java.util.Objects;
import java.util.StringJoiner;

public class SpongeStreamOptions implements StreamOptions {

    private final boolean copies;
    private final LoadingStyle loadingStyle;

    SpongeStreamOptions(final SpongeStreamOptionsBuilder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null!");
        this.loadingStyle = builder.loadingStyle;
        this.copies = builder.copies;
    }

    @Override
    public boolean carbonCopy() {
        return this.copies;
    }

    @Override
    public LoadingStyle loadingStyle() {
        return this.loadingStyle;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeStreamOptions that = (SpongeStreamOptions) o;
        return this.copies == that.copies && this.loadingStyle == that.loadingStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.copies, this.loadingStyle);
    }

    @Override
    public String toString() {
        return new StringJoiner(
            ", ",
            SpongeStreamOptions.class.getSimpleName() + "[",
            "]"
        )
            .add("copies=" + this.copies)
            .add("loadingStyle=" + this.loadingStyle)
            .toString();
    }
}
