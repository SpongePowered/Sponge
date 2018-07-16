package org.spongepowered.test;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id="individual-plugin-reload-test",
        name="Individual Plugin Reload Test",
        description = "Tests sending GameReloadEvent to specific plugins.",
        version = "1.0.0")
public class IndividualPluginReloadTest {

    @Inject private Logger logger;

    @Listener
    public void onReload(GameReloadEvent event) {
        this.logger.info("GameReloadEvent: " + event);
    }

}