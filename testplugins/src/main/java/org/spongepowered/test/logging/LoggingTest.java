package org.spongepowered.test.logging;

import com.google.inject.Inject;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("logging-test")
public class LoggingTest {

    private final System.Logger platformLogger;

    @Inject
    LoggingTest(final System.Logger platformLogger) {
        this.platformLogger = platformLogger;
    }


    @Listener
    private void onConstructed(final ConstructPluginEvent event) {
        this.platformLogger.log(
            System.Logger.Level.INFO,
            "Hello from {} on a platform logger",
            event.plugin().metadata().id()
        );
    }

}
