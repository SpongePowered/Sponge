package org.spongepowered.common.world.volume.stream;

import org.spongepowered.api.world.volume.stream.StreamOptions;

import java.util.Objects;

public class SpongeStreamOptionsBuilder implements StreamOptions.Builder {

    boolean copies = false;
    StreamOptions.LoadingStyle loadingStyle = StreamOptions.LoadingStyle.LAZILY_UNGENERATED;

    @Override
    public StreamOptions.Builder setCarbonCopy(final boolean copies) {
        this.copies = copies;
        return this;
    }

    @Override
    public StreamOptions.Builder setLoadingStyle(final StreamOptions.LoadingStyle style) {
        this.loadingStyle = Objects.requireNonNull(style, "LoadingStyle cannot be null!");
        return this;
    }

    @Override
    public StreamOptions.Builder reset() {
        this.copies = false;
        this.loadingStyle = StreamOptions.LoadingStyle.LAZILY_UNGENERATED;
        return this;
    }

    @Override
    public StreamOptions build() {
        return new SpongeStreamOptions(this);
    }
}
