/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.gen.DefineableClassLoader;
import org.spongepowered.common.event.listener.AllCauseListener;
import org.spongepowered.common.event.listener.BeforeAfterCauseListener;
import org.spongepowered.common.event.listener.CancelledListener;
import org.spongepowered.common.event.listener.CovariantGetterListener;
import org.spongepowered.common.event.listener.DataHasListener;
import org.spongepowered.common.event.listener.DataSupportsListener;
import org.spongepowered.common.event.listener.DoubleListener;
import org.spongepowered.common.event.listener.FirstLastCauseListener;
import org.spongepowered.common.event.listener.GetterListener;
import org.spongepowered.common.event.listener.IncludeExcludeListener;
import org.spongepowered.common.event.listener.InvalidCancelledListener;
import org.spongepowered.common.event.listener.InvalidIncludeExcludeListener;
import org.spongepowered.common.event.listener.RootListener;
import org.spongepowered.common.event.listener.SimpleListener;

import java.util.Optional;

public class EventFilterTest {
    
    public static final Cause TEST_CAUSE = Cause.of(EventContext.empty(), EventFilterTest.class);
    
    private final DefineableClassLoader classLoader = new DefineableClassLoader(getClass().getClassLoader());
    private final AnnotatedEventListener.Factory handlerFactory = new ClassEventListenerFactory("org.spongepowered.common.event.listener",
            new FilterFactory("org.spongepowered.common.event.filters", this.classLoader), this.classLoader);

    @Test
    public void testSimpleEvent() throws Exception {
        SimpleListener listener = new SimpleListener();
        AnnotatedEventListener annotatedEventListener = this.getListener(listener, "onEvent");
        annotatedEventListener.handle(new SubEvent(TEST_CAUSE));

        Assert.assertTrue("Simple listener was not called!", listener.called);
    }

    @Test
    public void testDoubleListener() throws Exception {
        DoubleListener listener = new DoubleListener();
        this.getListener(listener, "onEvent", ChangeBlockEvent.Break.class, Player.class, ItemStack.class);
    }

    @Test
    public void testCancelledEvent() throws Exception {
        CancelledListener listener = new CancelledListener();
        SubEvent event = new SubEvent(TEST_CAUSE);

        AnnotatedEventListener normalListener = this.getListener(listener, "normalListener");
        AnnotatedEventListener uncalledListener = this.getListener(listener, "uncalledListener");
        AnnotatedEventListener afterCancelledListener = this.getListener(listener, "afterCancelledListener");
        AnnotatedEventListener alwaysCalledListener = this.getListener(listener, "alwaysCalledListener");

        normalListener.handle(event);
        Assert.assertTrue("Un-annotated listener was not called when event was not cancelled!", listener.normalListenerWasCalled);

        alwaysCalledListener.handle(event);
        Assert.assertTrue("Listener annotated with @IsCancelled(Tristate.UNDEFINED) was not called when event was not cancelled!", listener.alwaysCalledWasCalled);

        event.setCancelled(true);
        listener.normalListenerWasCalled = false;

        /*
         * normalListener.handle(event); Assert.assertFalse(
         * "Un-annotated listener was called when event was cancelled!", listener.normalListenerWasCalled);
         */

        uncalledListener.handle(event);
        Assert.assertFalse("Listener annotated with @IsCancelled(Tristate.FALSE) was called!", listener.uncalledWasCalled);

        afterCancelledListener.handle(event);
        Assert.assertTrue("Listener annotated with @IsCancelled was not called!", listener.afterCancelledWasCalled);

        alwaysCalledListener.handle(event);
        Assert.assertTrue("Listener annotated with @IsCancelled(Tristate.UNDEFINED) was not called!", listener.alwaysCalledWasCalled);
    }

    @Test(expected = RuntimeException.class)
    public void testCancelledEvent_InvalidListener() throws Exception {
        this.getListener(new InvalidCancelledListener(), "onEvent", UncancellableEvent.class);
    }

    @Test
    public void testIncludeExcludeListener() throws Exception {
        IncludeExcludeListener listener = new IncludeExcludeListener();

        AnnotatedEventListener includeListener       = this.getListener(listener, "includeListener", TestEvent.class);
        AnnotatedEventListener multiIncludeListener  = this.getListener(listener, "multiIncludeListener", TestEvent.class);

        AnnotatedEventListener excludeListener       = this.getListener(listener, "excludeListener", TestEvent.class);
        AnnotatedEventListener multiExcludeListener  = this.getListener(listener, "multiExcludeListener", TestEvent.class);

        TestEvent testEvent = new TestEvent(TEST_CAUSE);
        SubEvent subEvent = new SubEvent(TEST_CAUSE);

        includeListener.handle(testEvent);
        Assert.assertFalse("Listener annotated with @Include was called!", listener.includeListenerCalled);

        includeListener.handle(subEvent);
        Assert.assertTrue("Listener annotated with @Include was not called!", listener.includeListenerCalled);

        excludeListener.handle(subEvent);
        Assert.assertFalse("Listener annotated with @Exclude was called!", listener.exlcudeListenerCalled);

        excludeListener.handle(testEvent);
        Assert.assertTrue("Listener annotated with @Exclude was not called!", listener.exlcudeListenerCalled);

        multiIncludeListener.handle(testEvent);
        Assert.assertFalse("Listener annotated with multi-target @Include was called!", listener.multiIncludeListenerCalled);

        multiIncludeListener.handle(subEvent);
        Assert.assertTrue("Listener annotated with multi-target @Include was not called!", listener.multiIncludeListenerCalled);

        multiExcludeListener.handle(subEvent);
        Assert.assertFalse("Listener annotated with multi-target @Exclude was called!", listener.multiExcludeListenerCalled);

        multiExcludeListener.handle(testEvent);
        Assert.assertTrue("Listener annotated with multi-target @Exclude was not called!", listener.multiExcludeListenerCalled);
    }

    @Test(expected = RuntimeException.class)
    public void testIncludeExcludeListener_InvalidListener() throws Exception {
        this.getListener(new InvalidIncludeExcludeListener(), "invalidListener", TestEvent.class);
    }

    @Test
    public void testFirstLastCauseListener() throws Exception {
        FirstLastCauseListener listener = new FirstLastCauseListener();
        AnnotatedEventListener firstCauseListener = this.getListener(listener, "firstCauseListener", SubEvent.class, Player.class);
        AnnotatedEventListener lastCauseListener = this.getListener(listener, "lastCauseListener", SubEvent.class, Player.class);
        AnnotatedEventListener firstCauseListenerInc = this.getListener(listener, "firstCauseListenerInclude", SubEvent.class, Player.class);
        AnnotatedEventListener firstCauseListenerEx = this.getListener(listener, "firstCauseListenerExclude", SubEvent.class, Player.class);
        AnnotatedEventListener lastCauseListenerInc = this.getListener(listener, "lastCauseListenerInclude", SubEvent.class, Entity.class);
        AnnotatedEventListener lastCauseListenerEx = this.getListener(listener, "lastCauseListenerExclude", SubEvent.class, Entity.class);

        Cause cause1 = Cause.of(EventContext.empty(), "Foo", 'a');
        Cause cause2 = Cause.of(EventContext.empty(),  "Foo", mock(Player.class), 7);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        firstCauseListener.handle(event1);
        Assert.assertFalse("Listener was called with improper @First parameter!", listener.firstCauseCalled);

        firstCauseListener.handle(event2);
        Assert.assertTrue("Listener with @First parameter was not called when proper Cause was provided!", listener.firstCauseCalled);

        lastCauseListener.handle(event1);
        Assert.assertFalse("Listener was called with improper @Last parameter!", listener.lastCauseCalled);

        lastCauseListener.handle(event2);
        Assert.assertTrue("Listener with @Last parameter was not called when proper Cause was provided!", listener.lastCauseCalled);

        firstCauseListenerInc.handle(event2);
        Assert.assertTrue("Listener with @First with inclusions was not called when proper Cause was provided!", listener.firstCauseCalledInc);

        firstCauseListenerEx.handle(event2);
        Assert.assertFalse("Listener with @First with exclusions was called when an improper Cause was provided!", listener.firstCauseCalledEx);

        lastCauseListenerInc.handle(event2);
        Assert.assertTrue("Listener with @Last with inclusions was not called when proper Cause was provided!", listener.lastCauseCalledInc);

        lastCauseListenerEx.handle(event2);
        Assert.assertFalse("Listener with @Last with exclusions was called when an improper Cause was provided!", listener.lastCauseCalledEx);
    }

    @Test
    public void testAllCauseListener() throws Exception {
        AllCauseListener listener = new AllCauseListener();
        AnnotatedEventListener emptyListener = this.getListener(listener, "emptyListener", SubEvent.class, BiomeTypes[].class);
        AnnotatedEventListener allCauseListener = this.getListener(listener, "allCauseListener", SubEvent.class, Player[].class);

        Cause cause1 = TEST_CAUSE;
        Cause cause2 = Cause.of(EventContext.empty(), mock(Player.class), "Hi", mock(Player.class));

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        emptyListener.handle(event1);
        Assert.assertTrue("Listener with @All(ignoreEmpty = false) was not called!", listener.emptyListenerCalled);

        listener.emptyListenerCalled = false;

        emptyListener.handle(event2);
        Assert.assertTrue("Listener with @All(ignoreEmpty = false) was not called!", listener.emptyListenerCalled);

        allCauseListener.handle(event1);
        Assert.assertFalse("Listener with @All was called with improper array parameter!", listener.allCauseListenerCalled);

        allCauseListener.handle(event2);
        Assert.assertTrue("Listener with @All was not called when proper Cause was provided!", listener.allCauseListenerCalled);
    }

    @Test
    public void testDataSupportsListener() throws Exception {
        DataSupportsListener listener = new DataSupportsListener();
        AnnotatedEventListener supportsSkinListener = this.getListener(listener, "supportsSkinListener", SubEvent.class, Player.class);
        AnnotatedEventListener inverseSupportsSkinListener = this.getListener(listener, "inverseSupportsSkinListener", SubEvent.class, Player.class);

        Player player1 = mock(Player.class);
        when(player1.supports(SkinData.class)).thenReturn(false);

        Player player2 = mock(Player.class);
        when(player2.supports(SkinData.class)).thenReturn(true);

        Cause cause1 = Cause.of(EventContext.empty(), player1);
        Cause cause2 = Cause.of(EventContext.empty(), player2);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        supportsSkinListener.handle(event1);
        Assert.assertFalse("Listener with @Supports was called!", listener.supportsSkinListenerCalled);

        supportsSkinListener.handle(event2);
        Assert.assertTrue("Listener with @Supports was not called!", listener.supportsSkinListenerCalled);

        inverseSupportsSkinListener.handle(event2);
        Assert.assertFalse("Inverse listener with @Supports was called!", listener.inverseSupportsSkinListenerCalled);

        inverseSupportsSkinListener.handle(event1);
        Assert.assertTrue("Inverse listener with @Supports was not called!", listener.inverseSupportsSkinListenerCalled);
    }

    @Test
    public void testDataHasListener() throws Exception {
        DataHasListener listener = new DataHasListener();
        AnnotatedEventListener hasSkinListener = this.getListener(listener, "hasSkinListener", SubEvent.class, Player.class);
        AnnotatedEventListener inversehasSkinListener = this.getListener(listener, "inverseHasSkinListener", SubEvent.class, Player.class);

        Player player1 = mock(Player.class);
        when(player1.get(SkinData.class)).thenReturn(Optional.empty());

        Player player2 = mock(Player.class);
        when(player2.get(SkinData.class)).thenReturn(Optional.of(mock(SkinData.class)));

        Cause cause1 = Cause.of(EventContext.empty(), player1);
        Cause cause2 = Cause.of(EventContext.empty(), player2);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        hasSkinListener.handle(event1);
        Assert.assertFalse("Listener with @Has was called!", listener.hasSkinListenerCalled);

        hasSkinListener.handle(event2);
        Assert.assertTrue("Listener with @Has was not called!", listener.hasSkinListenerCalled);

        inversehasSkinListener.handle(event2);
        Assert.assertFalse("Inverse listener with @Has was called!", listener.inverseHasSkinListenerCalled);

        inversehasSkinListener.handle(event1);
        Assert.assertTrue("Inverse listener with @Has was not called!", listener.inverseHasSkinListenerCalled);
    }

    @Test
    public void testRootListener() throws Exception {
        RootListener listener = new RootListener();
        AnnotatedEventListener rootListener = this.getListener(listener, "rootListener", SubEvent.class, Player.class);
        AnnotatedEventListener rootListenerInc = this.getListener(listener, "rootListenerInclude", SubEvent.class, Object.class);
        AnnotatedEventListener rootListenerEx = this.getListener(listener, "rootListenerExclude", SubEvent.class, Object.class);

        Cause cause1 = Cause.of(EventContext.empty(), "Hi", mock(Player.class));
        Cause cause2 = Cause.of(EventContext.empty(), mock(Player.class), 5);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        rootListener.handle(event1);
        Assert.assertFalse("Listener with @Root was called with improper parameter!", listener.rootListenerCalled);

        rootListener.handle(event2);
        Assert.assertTrue("Listener with @Root was not called when proper Cause was provided!", listener.rootListenerCalled);

        rootListenerInc.handle(event1);
        Assert.assertFalse("Listener with @Root with include was called with improper parameter!", listener.rootListenerCalledInc);

        rootListenerInc.handle(event2);
        Assert.assertTrue("Listener with @Root with include was not called when proper Cause was provided!", listener.rootListenerCalledInc);

        rootListenerEx.handle(event2);
        Assert.assertFalse("Listener with @Root with exclude was called with improper parameter!", listener.rootListenerCalledEx);

        rootListenerEx.handle(event1);
        Assert.assertTrue("Listener with @Root with exclude was not called when proper Cause was provided!", listener.rootListenerCalledEx);
    }

    @Test
    public void testBeforeCauseListener() throws Exception {
        BeforeAfterCauseListener listener = new BeforeAfterCauseListener();
        AnnotatedEventListener beforeCauseListener = this.getListener(listener, "beforeCauseListener", SubEvent.class, Player.class);
        AnnotatedEventListener afterCauseListener = this.getListener(listener, "afterCauseListener", SubEvent.class, Entity.class);
        AnnotatedEventListener beforeCauseListenerInc = this.getListener(listener, "beforeCauseListenerInclude", SubEvent.class, Player.class);
        AnnotatedEventListener beforeCauseListenerEx = this.getListener(listener, "beforeCauseListenerExclude", SubEvent.class, Player.class);
        AnnotatedEventListener afterCauseListenerInc = this.getListener(listener, "afterCauseListenerInclude", SubEvent.class, Entity.class);
        AnnotatedEventListener afterCauseListenerEx = this.getListener(listener, "afterCauseListenerExclude", SubEvent.class, Entity.class);

        Cause cause1 = Cause.of(EventContext.empty(), "Foo", mock(Player.class), mock(Extent.class));
        Cause cause2 = Cause.of(EventContext.empty(), "Me", mock(BlockState.class), mock(Entity.class));

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        beforeCauseListener.handle(event2);
        Assert.assertFalse("Listener with @Before was called with improper parameter!", listener.beforeCauseCalled);

        beforeCauseListener.handle(event1);
        Assert.assertTrue("Listener with @Before was not called when proper Cause was provided!", listener.beforeCauseCalled);

        afterCauseListener.handle(event1);
        Assert.assertFalse("Listener with @After was called with improper parameter!", listener.afterCauseCalled);

        afterCauseListener.handle(event2);
        Assert.assertTrue("Listener with @After was not called when proper Cause was provided!", listener.afterCauseCalled);

        beforeCauseListenerInc.handle(event1);
        Assert.assertTrue("Listener with @Before with inclusions was not called when proper Cause was provided!", listener.beforeCauseCalledInc);

        beforeCauseListenerEx.handle(event1);
        Assert.assertFalse("Listener with @Before with exclusions was called when an improper Cause was provided!", listener.beforeCauseCalledEx);

        afterCauseListenerInc.handle(event2);
        Assert.assertFalse("Listener with @After with inclusions was called when an improper Cause was provided!", listener.afterCauseCalledInc);

        afterCauseListenerEx.handle(event2);
        Assert.assertTrue("Listener with @After with exclusions was not called when proper Cause was provided!", listener.afterCauseCalledEx);
    }

    @Test
    public void testGetter() throws Exception {
        GetterListener listener = new GetterListener();
        AnnotatedEventListener normalListener = this.getListener(listener, "normalListener", GetterEvent.class, TestObject.class);
        AnnotatedEventListener subClassListener = this.getListener(listener, "subClassListener", GetterEvent.class, SubObject.class);

        Cause cause = Cause.of(EventContext.empty(), Text.of());

        GetterEvent normalEvent = new GetterEvent(cause, new TestObject());
        GetterEvent subClassEvent = new GetterEvent(cause, new SubObject());

        normalListener.handle(normalEvent);
        Assert.assertTrue("Listener with @Getter was not called when the targeted getter returned the same type!", listener.normalCalled);

        listener.normalCalled = false;

        normalListener.handle(subClassEvent);
        Assert.assertTrue("Listener with @Getter was not called when the targeted getter returned a subtype of the expected type!", listener.normalCalled);

        subClassListener.handle(normalEvent);
        Assert.assertFalse("Listener with @Getter was called when the targeted getter returned a supertype! How is this even possible???", listener.subClassCalled);

        listener.subClassCalled = false;

        subClassListener.handle(subClassEvent);
        Assert.assertTrue("Listener with @Getter was not called when the targeted getter returned a subtype of the expected type, which matched the parameter type!", listener.normalCalled);

    }

    @Test
    public void testCovariantGetter() throws Exception {
        CovariantGetterListener listener = new CovariantGetterListener();
        AnnotatedEventListener covariantListener = this.getListener(listener, "covariantListener", CovariantEvent.OverloadedEvent.class, SubObject.class);

        covariantListener.handle(new CovariantEvent.OverloadedEvent(Cause.of(EventContext.empty(), Text.of()), new SubObject()));
        Assert.assertTrue("Listener with @Getter targeting a method with a covariant return type was not called!", listener.covariantListenerCalled);

    }

    public static class TestEvent implements Event, Cancellable {

        private final Cause cause;
        private boolean cancelled = false;

        public TestEvent(Cause cause) {
            this.cause = cause;
        }

        @Override
        public Cause getCause() {
            return this.cause;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

    }

    public static class GetterEvent extends TestEvent {

        private TestObject testObject;

        public GetterEvent(Cause cause, TestObject testObject) {
            super(cause);
            this.testObject = testObject;
        }

        public TestObject getTestObject() {
            return this.testObject;
        }
    }

    public static class SubEvent extends TestEvent {

        public SubEvent(Cause cause) {
            super(cause);
        }
    }

    public static class OtherSubEvent extends TestEvent {

        public OtherSubEvent(Cause cause) {
            super(cause);
        }
    }

    public static class UncancellableEvent implements Event {

        @Override
        public Cause getCause() {
            return TEST_CAUSE;
        }
    }

    public static class TestObject {

    }

    public static class SubObject extends TestObject {

    }

    public static class CovariantEvent implements Event {

        protected final Cause cause;
        protected final TestObject testObject;

        public CovariantEvent(Cause cause, TestObject testObject) {
            this.cause = cause;
            this.testObject = testObject;
        }

        @Override
        public Cause getCause() {
            return this.cause;
        }

        public TestObject getTestObject() {
            return this.testObject;
        }

        public static class OverloadedEvent extends CovariantEvent {

            public OverloadedEvent(Cause cause, SubObject subObject) {
                super(cause, subObject);
            }

            @Override
            public SubObject getTestObject() {
                return (SubObject) this.testObject;
            }
        }
    }

    private AnnotatedEventListener getListener(Object listener, String method) throws Exception {
        return this.getListener(listener, method, SubEvent.class);
    }

    private AnnotatedEventListener getListener(Object listener, String method, Class<?>... classes) throws Exception {
        return this.handlerFactory.create(listener, listener.getClass().getMethod(method, classes));
    }
}
