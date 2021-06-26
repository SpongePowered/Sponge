package org.spongepowered.vanilla.launch.inject;

import com.google.inject.AbstractModule;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.vanilla.launch.event.VanillaEventManager;

public final class SpongeVanillaModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(EventManager.class).to(VanillaEventManager.class);
    }
}
