package org.spongepowered.test;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.PardonIpEvent;
import org.spongepowered.api.event.user.PardonUserEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "pardoneventtest", name = "Pardon Event Test", description = "A plugin to test PardonUserEvent and PardonIpEvent")
public class PardonEventTest {

    @Listener
    public void onPardonIpEvent(PardonIpEvent event) {
        event.getCause().first(Player.class).ifPresent(player ->
                player.sendMessage(Text.of(player.getName() + " removed a " +
                        event.getBan().getType().getName() + " ban")));
    }

    @Listener
    public void onPardonUserEvent(PardonUserEvent event) {
        event.getCause().first(Player.class).ifPresent(player ->
                player.sendMessage(Text.of(player.getName() + " removed a " +
                        event.getBan().getType().getName() + " ban")));
    }
}
