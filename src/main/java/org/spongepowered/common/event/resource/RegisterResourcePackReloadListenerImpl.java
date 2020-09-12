package org.spongepowered.common.event.resource;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.resource.RegisterResourceReloadListenerEvent;
import org.spongepowered.api.resource.ResourceReloadListener;

public class RegisterResourcePackReloadListenerImpl<E extends Engine> extends AbstractRegisterEvent<E, ResourceReloadListener> implements RegisterResourceReloadListenerEvent<E> {
    public RegisterResourcePackReloadListenerImpl(Cause cause, TypeToken<E> type) {
        super(cause, type);
    }
}
