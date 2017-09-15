package org.spongepowered.common.event.listener;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.common.event.EventFilterTest;

public class ContextValueCauseListener {

    public boolean namedCauseListenerCalled;
    public boolean namedCauseListenerCalledInc;
    public boolean namedCauseListenerCalledEx;

    @Listener
    public void namedCauseListener(EventFilterTest.SubEvent event, @ContextValue(EventContextKeys.Named.OWNER_NAME) User state) {
        this.namedCauseListenerCalled = true;
    }

    @Listener
    public void namedCauseListenerInclude(EventFilterTest.SubEvent event, @ContextValue(value = EventContextKeys.Named.OWNER_NAME, typeFilter=User.class) Object state) {
        this.namedCauseListenerCalledInc = true;
    }

    @Listener
    public void namedCauseListenerExclude(EventFilterTest.SubEvent event, @ContextValue(value = EventContextKeys.Named.OWNER_NAME, typeFilter=User.class, inverse = true) Object state) {
        this.namedCauseListenerCalledEx = true;
    }


}
