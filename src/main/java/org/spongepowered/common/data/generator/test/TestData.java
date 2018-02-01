package org.spongepowered.common.data.generator.test;

import org.spongepowered.api.data.generator.KeyValue;
import org.spongepowered.api.data.manipulator.DataManipulator;

public interface TestData extends DataManipulator<TestData, ImmutableTestData> {

    @KeyValue("test_string")
    String getString();

    @KeyValue("test_int")
    int getInt();
}
