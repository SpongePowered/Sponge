package org.spongepowered.common.event.resource;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.resource.RegisterPackDiscovererEvent;
import org.spongepowered.api.resource.pack.PackDiscoverer;

public class RegisterPackFinderEventImpl<E extends Engine> extends AbstractRegisterEvent<E, PackDiscoverer> implements RegisterPackDiscovererEvent<E> {
    public RegisterPackFinderEventImpl(Cause cause, TypeToken<E> type) {
        super(cause, type);
    }
}
