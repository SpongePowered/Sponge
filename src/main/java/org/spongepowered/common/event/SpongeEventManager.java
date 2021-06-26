package org.spongepowered.common.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.plugin.PluginContainer;

public interface SpongeEventManager extends EventManager {

    boolean post(final Event event, final PluginContainer plugin);
}
