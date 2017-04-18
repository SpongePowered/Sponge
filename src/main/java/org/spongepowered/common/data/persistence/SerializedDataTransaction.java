package org.spongepowered.common.data.persistence;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;

public class SerializedDataTransaction {

    public static Builder builder() {
        return new Builder();
    }

    public final ImmutableList<DataView> failedData;
    public final ImmutableList<DataManipulator<?, ?>> deserializedManipulators;

    private SerializedDataTransaction(Builder builder) {
        this.failedData = builder.serializedData.build();
        this.deserializedManipulators = builder.deserializedManipulators.build();
    }


    public static final class Builder {

        private final ImmutableList.Builder<DataView> serializedData = ImmutableList.builder();
        private final ImmutableList.Builder<DataManipulator<?, ?>> deserializedManipulators = ImmutableList.builder();

        Builder() {
        }

        public void failedData(DataView serializedData) {
            this.serializedData.add(serializedData);
        }

        public void successfulData(DataManipulator<?, ?> success) {
            this.deserializedManipulators.add(success);
        }

        public SerializedDataTransaction build() {
            return new SerializedDataTransaction(this);
        }
    }
}
