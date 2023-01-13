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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.filter.FilterGenerator;
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
import org.spongepowered.common.event.listener.InvalidIncludeExcludeListener;
import org.spongepowered.common.event.listener.RootListener;
import org.spongepowered.common.event.listener.SimpleListener;
import org.spongepowered.common.event.manager.AnnotatedEventListener;
import org.spongepowered.common.event.manager.ClassEventListenerFactory;
import org.spongepowered.common.event.manager.ListenerClassVisitorHelper;
import org.spongepowered.common.launch.SpongeExtension;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@ExtendWith(SpongeExtension.class)
public class EventFilterTest {

    public static final Cause TEST_CAUSE = Cause.of(EventContext.empty(), EventFilterTest.class);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final AnnotatedEventListener.Factory handlerFactory = new ClassEventListenerFactory(FilterGenerator::create, LOOKUP);

    @Test
    public void testSimpleEvent() throws Throwable {
        SimpleListener listener = new SimpleListener();
        AnnotatedEventListener annotatedEventListener = this.getListener(listener, "onEvent");
        annotatedEventListener.handle(new SubEvent(TEST_CAUSE));

        assertTrue(listener.called, "Simple listener was not called!");
    }

    @Test
    public void testDoubleListener() throws Throwable {
        DoubleListener listener = new DoubleListener();
        this.getListener(listener, "onEvent", InteractBlockEvent.Primary.class, Player.class, ItemStack.class);
    }

    @Test
    public void testCancelledEvent() throws Throwable {
        CancelledListener listener = new CancelledListener();
        SubEvent event = new SubEvent(TEST_CAUSE);

        AnnotatedEventListener normalListener = this.getListener(listener, "normalListener");
        AnnotatedEventListener uncalledListener = this.getListener(listener, "uncalledListener");
        AnnotatedEventListener afterCancelledListener = this.getListener(listener, "afterCancelledListener");
        AnnotatedEventListener alwaysCalledListener = this.getListener(listener, "alwaysCalledListener");

        normalListener.handle(event);
        assertTrue(listener.normalListenerWasCalled, "Un-annotated listener was not called when event was not cancelled!");

        alwaysCalledListener.handle(event);
        assertTrue(listener.alwaysCalledWasCalled, "Listener annotated with @IsCancelled(Tristate.UNDEFINED) was not called when event was not cancelled!");

        event.setCancelled(true);
        listener.normalListenerWasCalled = false;

        normalListener.handle(event);
        assertFalse(listener.normalListenerWasCalled, "Un-annotated listener was called when event was cancelled!");

        uncalledListener.handle(event);
        assertFalse(listener.uncalledWasCalled, "Listener annotated with @IsCancelled(Tristate.FALSE) was called!");

        afterCancelledListener.handle(event);
        assertTrue(listener.afterCancelledWasCalled, "Listener annotated with @IsCancelled was not called!");

        alwaysCalledListener.handle(event);
        assertTrue(listener.alwaysCalledWasCalled, "Listener annotated with @IsCancelled(Tristate.UNDEFINED) was not called!");
    }

    @Test
    public void testIncludeExcludeListener() throws Throwable {
        IncludeExcludeListener listener = new IncludeExcludeListener();

        AnnotatedEventListener includeListener       = this.getListener(listener, "includeListener", TestEvent.class);
        AnnotatedEventListener multiIncludeListener  = this.getListener(listener, "multiIncludeListener", TestEvent.class);

        AnnotatedEventListener excludeListener       = this.getListener(listener, "excludeListener", TestEvent.class);
        AnnotatedEventListener multiExcludeListener  = this.getListener(listener, "multiExcludeListener", TestEvent.class);

        TestEvent testEvent = new TestEvent(TEST_CAUSE);
        SubEvent subEvent = new SubEvent(TEST_CAUSE);

        includeListener.handle(testEvent);
        assertFalse(listener.includeListenerCalled, "Listener annotated with @Include was called!");

        includeListener.handle(subEvent);
        assertTrue(listener.includeListenerCalled, "Listener annotated with @Include was not called!");

        excludeListener.handle(subEvent);
        assertFalse(listener.exlcudeListenerCalled, "Listener annotated with @Exclude was called!");

        excludeListener.handle(testEvent);
        assertTrue(listener.exlcudeListenerCalled, "Listener annotated with @Exclude was not called!");

        multiIncludeListener.handle(testEvent);
        assertFalse(listener.multiIncludeListenerCalled, "Listener annotated with multi-target @Include was called!");

        multiIncludeListener.handle(subEvent);
        assertTrue(listener.multiIncludeListenerCalled, "Listener annotated with multi-target @Include was not called!");

        multiExcludeListener.handle(subEvent);
        assertFalse(listener.multiExcludeListenerCalled, "Listener annotated with multi-target @Exclude was called!");

        multiExcludeListener.handle(testEvent);
        assertTrue(listener.multiExcludeListenerCalled, "Listener annotated with multi-target @Exclude was not called!");
    }

    @Test
    public void testIncludeExcludeListener_InvalidListener() {
        assertThrows(RuntimeException.class, () -> this.getListener(new InvalidIncludeExcludeListener(), "invalidListener", TestEvent.class));
    }

    @Disabled
    @Test
    public void testFirstLastCauseListener() throws Throwable {
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
        assertFalse(listener.firstCauseCalled, "Listener was called with improper @First parameter!");

        firstCauseListener.handle(event2);
        assertTrue(listener.firstCauseCalled, "Listener with @First parameter was not called when proper Cause was provided!");

        lastCauseListener.handle(event1);
        assertFalse(listener.lastCauseCalled, "Listener was called with improper @Last parameter!");

        lastCauseListener.handle(event2);
        assertTrue(listener.lastCauseCalled, "Listener with @Last parameter was not called when proper Cause was provided!");

        firstCauseListenerInc.handle(event2);
        assertTrue(listener.firstCauseCalledInc, "Listener with @First with inclusions was not called when proper Cause was provided!");

        firstCauseListenerEx.handle(event2);
        assertFalse(listener.firstCauseCalledEx, "Listener with @First with exclusions was called when an improper Cause was provided!");

        lastCauseListenerInc.handle(event2);
        assertTrue(listener.lastCauseCalledInc, "Listener with @Last with inclusions was not called when proper Cause was provided!");

        lastCauseListenerEx.handle(event2);
        assertFalse(listener.lastCauseCalledEx, "Listener with @Last with exclusions was called when an improper Cause was provided!");
    }

    @Test
    public void testAllCauseListener() throws Throwable {
        AllCauseListener listener = new AllCauseListener();
        AnnotatedEventListener emptyListener = this.getListener(listener, "emptyListener", SubEvent.class, Component[].class);
        AnnotatedEventListener allCauseListener = this.getListener(listener, "allCauseListener", SubEvent.class, Player[].class);

        Cause cause1 = TEST_CAUSE;
        Cause cause2 = Cause.of(EventContext.empty(), mock(Player.class), "Hi", mock(Player.class));

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        emptyListener.handle(event1);
        assertTrue(listener.emptyListenerCalled, "Listener with @All(ignoreEmpty = false) was not called!");

        listener.emptyListenerCalled = false;

        emptyListener.handle(event2);
        assertTrue(listener.emptyListenerCalled, "Listener with @All(ignoreEmpty = false) was not called!");

        allCauseListener.handle(event1);
        assertFalse(listener.allCauseListenerCalled, "Listener with @All was called with improper array parameter!");

        allCauseListener.handle(event2);
        assertTrue(listener.allCauseListenerCalled, "Listener with @All was not called when proper Cause was provided!");
    }

    @Test
    public void testDataSupportsListener() throws Throwable {
        DataSupportsListener listener = new DataSupportsListener();
        AnnotatedEventListener supportsDisplayNameListener = this.getListener(listener, "supportsDisplayNameListener", SubEvent.class, Player.class);
        AnnotatedEventListener inverseSupportsDisplayNameListener = this.getListener(listener, "inverseSupportsDisplayNameListener", SubEvent.class, Player.class);

        Player player1 = mock(Player.class);
        when(player1.supports(Keys.DISPLAY_NAME)).thenReturn(false);

        Player player2 = mock(Player.class);
        when(player2.supports(Keys.DISPLAY_NAME)).thenReturn(true);

        Cause cause1 = Cause.of(EventContext.empty(), player1);
        Cause cause2 = Cause.of(EventContext.empty(), player2);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        supportsDisplayNameListener.handle(event1);
        assertFalse(listener.supportsDisplayNameListenerCalled, "Listener with @Supports was called!");

        supportsDisplayNameListener.handle(event2);
        assertTrue(listener.supportsDisplayNameListenerCalled, "Listener with @Supports was not called!");

        inverseSupportsDisplayNameListener.handle(event2);
        assertFalse(listener.inverseSupportsDisplayNameListenerCalled, "Inverse listener with @Supports was called!");

        inverseSupportsDisplayNameListener.handle(event1);
        assertTrue(listener.inverseSupportsDisplayNameListenerCalled, "Inverse listener with @Supports was not called!");
    }

    @Test
    public void testDataHasListener() throws Throwable {
        DataHasListener listener = new DataHasListener();
        AnnotatedEventListener hasDisplayNameListener = this.getListener(listener, "hasDisplayNameListener", SubEvent.class, Player.class);
        AnnotatedEventListener inverseHasDisplayNameListener = this.getListener(listener, "inverseHasDisplayNameListener", SubEvent.class, Player.class);

        Player player1 = mock(Player.class);
        when(player1.get(Keys.DISPLAY_NAME)).thenReturn(Optional.empty());

        Player player2 = mock(Player.class);
        when(player2.get(Keys.DISPLAY_NAME)).thenReturn(Optional.of(Component.text("name")));

        Cause cause1 = Cause.of(EventContext.empty(), player1);
        Cause cause2 = Cause.of(EventContext.empty(), player2);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        hasDisplayNameListener.handle(event1);
        assertFalse(listener.hasDisplayNameListenerCalled, "Listener with @Has was called!");

        hasDisplayNameListener.handle(event2);
        assertTrue(listener.hasDisplayNameListenerCalled, "Listener with @Has was not called!");

        inverseHasDisplayNameListener.handle(event2);
        assertFalse(listener.inverseDisplayNameListenerCalled, "Inverse listener with @Has was called!");

        inverseHasDisplayNameListener.handle(event1);
        assertTrue(listener.inverseDisplayNameListenerCalled, "Inverse listener with @Has was not called!");
    }

    @Disabled
    @Test
    public void testRootListener() throws Throwable {
        RootListener listener = new RootListener();
        AnnotatedEventListener rootListener = this.getListener(listener, "rootListener", SubEvent.class, Player.class);
        AnnotatedEventListener rootListenerInc = this.getListener(listener, "rootListenerInclude", SubEvent.class, Object.class);
        AnnotatedEventListener rootListenerEx = this.getListener(listener, "rootListenerExclude", SubEvent.class, Object.class);

        Cause cause1 = Cause.of(EventContext.empty(), "Hi", mock(Player.class));
        Cause cause2 = Cause.of(EventContext.empty(), mock(Player.class), 5);

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        rootListener.handle(event1);
        assertFalse(listener.rootListenerCalled, "Listener with @Root was called with improper parameter!");

        rootListener.handle(event2);
        assertTrue(listener.rootListenerCalled, "Listener with @Root was not called when proper Cause was provided!");

        rootListenerInc.handle(event1);
        assertFalse(listener.rootListenerCalledInc, "Listener with @Root with include was called with improper parameter!");

        rootListenerInc.handle(event2);
        assertTrue(listener.rootListenerCalledInc, "Listener with @Root with include was not called when proper Cause was provided!");

        rootListenerEx.handle(event2);
        assertFalse(listener.rootListenerCalledEx, "Listener with @Root with exclude was called with improper parameter!");

        rootListenerEx.handle(event1);
        assertTrue(listener.rootListenerCalledEx, "Listener with @Root with exclude was not called when proper Cause was provided!");
    }

    @Disabled
    @Test
    public void testBeforeCauseListener() throws Throwable {
        BeforeAfterCauseListener listener = new BeforeAfterCauseListener();
        AnnotatedEventListener beforeCauseListener = this.getListener(listener, "beforeCauseListener", SubEvent.class, Player.class);
        AnnotatedEventListener afterCauseListener = this.getListener(listener, "afterCauseListener", SubEvent.class, Entity.class);
        AnnotatedEventListener beforeCauseListenerInc = this.getListener(listener, "beforeCauseListenerInclude", SubEvent.class, Player.class);
        AnnotatedEventListener beforeCauseListenerEx = this.getListener(listener, "beforeCauseListenerExclude", SubEvent.class, Player.class);
        AnnotatedEventListener afterCauseListenerInc = this.getListener(listener, "afterCauseListenerInclude", SubEvent.class, Entity.class);
        AnnotatedEventListener afterCauseListenerEx = this.getListener(listener, "afterCauseListenerExclude", SubEvent.class, Entity.class);

        Cause cause1 = Cause.of(EventContext.empty(), "Foo", mock(Player.class), mock(World.class));
        Cause cause2 = Cause.of(EventContext.empty(), "Me", mock(BlockState.class), mock(Entity.class));

        SubEvent event1 = new SubEvent(cause1);
        SubEvent event2 = new SubEvent(cause2);

        beforeCauseListener.handle(event2);
        assertFalse(listener.beforeCauseCalled, "Listener with @Before was called with improper parameter!");

        beforeCauseListener.handle(event1);
        assertTrue(listener.beforeCauseCalled, "Listener with @Before was not called when proper Cause was provided!");

        afterCauseListener.handle(event1);
        assertFalse(listener.afterCauseCalled, "Listener with @After was called with improper parameter!");

        afterCauseListener.handle(event2);
        assertTrue(listener.afterCauseCalled, "Listener with @After was not called when proper Cause was provided!");

        beforeCauseListenerInc.handle(event1);
        assertTrue(listener.beforeCauseCalledInc, "Listener with @Before with inclusions was not called when proper Cause was provided!");

        beforeCauseListenerEx.handle(event1);
        assertFalse(listener.beforeCauseCalledEx, "Listener with @Before with exclusions was called when an improper Cause was provided!");

        afterCauseListenerInc.handle(event2);
        assertFalse(listener.afterCauseCalledInc, "Listener with @After with inclusions was called when an improper Cause was provided!");

        afterCauseListenerEx.handle(event2);
        assertTrue(listener.afterCauseCalledEx, "Listener with @After with exclusions was not called when proper Cause was provided!");
    }

    @Test
    public void testGetter() throws Throwable {
        GetterListener listener = new GetterListener();
        AnnotatedEventListener normalListener = this.getListener(listener, "normalListener", GetterEvent.class, TestObject.class);
        AnnotatedEventListener subClassListener = this.getListener(listener, "subClassListener", GetterEvent.class, SubObject.class);

        Cause cause = Cause.of(EventContext.empty(), Component.text());

        GetterEvent normalEvent = new GetterEvent(cause, new TestObject());
        GetterEvent subClassEvent = new GetterEvent(cause, new SubObject());

        normalListener.handle(normalEvent);
        assertTrue(listener.normalCalled, "Listener with @Getter was not called when the targeted getter returned the same type!");

        listener.normalCalled = false;

        normalListener.handle(subClassEvent);
        assertTrue(listener.normalCalled, "Listener with @Getter was not called when the targeted getter returned a subtype of the expected type!");

        subClassListener.handle(normalEvent);
        assertFalse(listener.subClassCalled,
                "Listener with @Getter was called when the targeted getter returned a supertype! How is this even possible???");

        listener.subClassCalled = false;

        subClassListener.handle(subClassEvent);
        assertTrue(listener.normalCalled,
                "Listener with @Getter was not called when the targeted getter returned a subtype of the expected type, which matched the parameter type!");

    }

    @Test
    public void testCovariantGetter() throws Throwable {
        CovariantGetterListener listener = new CovariantGetterListener();
        AnnotatedEventListener covariantListener = this.getListener(listener, "covariantListener", CovariantEvent.OverloadedEvent.class, SubObject.class);

        covariantListener.handle(new CovariantEvent.OverloadedEvent(Cause.of(EventContext.empty(), Component.text()), new SubObject()));
        assertTrue(listener.covariantListenerCalled, "Listener with @Getter targeting a method with a covariant return type was not called!");

    }

    public static class TestEvent implements Event, Cancellable {

        private final Cause cause;
        private boolean cancelled = false;

        public TestEvent(Cause cause) {
            this.cause = cause;
        }

        @Override
        public Cause cause() {
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
        public Cause cause() {
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

    private AnnotatedEventListener getListener(Object listener, String method) throws Throwable {
        return this.getListener(listener, method, SubEvent.class);
    }

    private AnnotatedEventListener getListener(Object listener, String methodName, Class<?>... parameterTypes) throws Throwable {
        return this.handlerFactory.create(listener, ListenerClassVisitorHelper.getMethod(listener.getClass(), methodName, parameterTypes), null);
    }
}
