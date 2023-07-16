package org.spongepowered.test.interact;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent.Primary;
import org.spongepowered.test.LoadableModule;

/**
 *   @author CanardNocturne
 */
@Plugin("clickcontainereventcancelledtest")
public class ClickContainerEventCancelledTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public ClickContainerEventCancelledTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new ClickContainerEventCancelledTest.ClickContainerListener());
    }

    public static class ClickContainerListener {
        @Listener
        private void onInteractItem(final ClickContainerEvent.Primary event) {
            event.setCancelled(true);
            Sponge.server().broadcastAudience().sendMessage(Identity.nil(), Component.text("/*************"));
            Sponge.server().broadcastAudience().sendMessage(Identity.nil(), Component.text().append(Component.text("/* Event: ")).append(Component.text(event.getClass().getSimpleName())).build());
            Sponge.server().broadcastAudience().sendMessage(Identity.nil(),
                    Component.text().append(Component.text("/* Hand: "))
                            .append(Component.text(event.context().get(EventContextKeys.USED_HAND).map(h -> RegistryTypes.HAND_TYPE.keyFor(Sponge.game(), h).formatted()).orElse("UNKNOWN")))
                            .build()
            );
            Sponge.game().systemSubject().sendMessage(Identity.nil(), Component.text().append(Component.text("/ Cause: ")).append(Component.text(event.cause().all().toString())).build());
            Sponge.game().systemSubject().sendMessage(Identity.nil(), Component.text().append(Component.text("/ Context: ")).append(Component.text(event.context().toString())).build());
            Sponge.game().systemSubject().sendMessage(Identity.nil(),
                    Component.text().append(Component.text("/ Cancelled: ")).append(Component.text(event.isCancelled())).build());
        }
    }



}
