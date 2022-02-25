package org.spongepowered.collections.test.multimap;

public class Parent implements DemoInterface {

    public static final class SingleChild extends Parent {

    }

    public static class SecondChild extends Parent {

    }

    public static class ThirdChild extends Parent {

        public static class FourthChild extends ThirdChild {

        }
    }
}
