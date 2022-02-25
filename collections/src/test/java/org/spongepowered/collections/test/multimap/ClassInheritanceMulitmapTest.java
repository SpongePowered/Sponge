package org.spongepowered.collections.test.multimap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.spongepowered.collections.multimap.ClassInheritanceOrderedMultimap;

import java.util.List;
import java.util.function.Function;

public class ClassInheritanceMulitmapTest {

    @Test
    public void testCreate() {
        final ClassInheritanceOrderedMultimap<DemoInterface, Function<DemoInterface, Integer>> testMap = ClassInheritanceOrderedMultimap.create(DemoInterface.class);
        testMap.put(DemoInterface.class, (d) -> 0);
    }
    @Test
    public void testGet() {
        final ClassInheritanceOrderedMultimap<DemoInterface, Function<DemoInterface, Integer>> testMap = ClassInheritanceOrderedMultimap.create(DemoInterface.class);
        testMap.put(DemoInterface.class, (d) -> 0);
        final List<Function<DemoInterface, Integer>> functions = testMap.get(DemoInterface.class);
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
        assertEquals(1, functions.size());
    }

    @Test
    public void testMulticreate() {
        final ClassInheritanceOrderedMultimap<DemoInterface, Function<DemoInterface, Integer>> testMap = ClassInheritanceOrderedMultimap.create(DemoInterface.class);
        testMap.put(DemoInterface.class, (d) -> 0);
        testMap.put(Single.class, (a) -> 1);
        final List<Function<DemoInterface, Integer>> functions = testMap.get(Single.class);
        assertNotNull(functions);
        assertEquals(2, functions.size());
    }
    @Test
    public void testHierarchyOrder() {
        final ClassInheritanceOrderedMultimap<DemoInterface, Function<DemoInterface, Integer>> testMap = ClassInheritanceOrderedMultimap.create(DemoInterface.class);
        testMap.put(DemoInterface.class, (d) -> 0);

        testMap.put(Parent.class, (a) -> 2);
        testMap.put(Parent.SingleChild.class, a -> 3);
        final List<Function<DemoInterface, Integer>> functions = testMap.get(Parent.SingleChild.class);
        assertNotNull(functions);
        assertEquals(3, functions.size());
    }
    @Test
    public void testMultiHierarchyOrder() {
        final ClassInheritanceOrderedMultimap<DemoInterface, Function<DemoInterface, Integer>> testMap = ClassInheritanceOrderedMultimap.create(DemoInterface.class);
        testMap.put(DemoInterface.class, (d) -> 0);

        testMap.put(Parent.SecondChild.class, a -> 4);
        testMap.put(Parent.ThirdChild.class, a -> 5);
        testMap.put(Parent.ThirdChild.FourthChild.class, a -> 6);
    }
}
