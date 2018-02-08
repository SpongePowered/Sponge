package org.spongepowered.test;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.plugin.Plugin;

@Plugin(authors = "gabizou", name = "DeathTest", id = "deathtest")
public class DeathTest {

    @Listener
    public void onDeath(DestructEntityEvent.Death entityEvent, @Getter("getTargetEntity") Player player) {
        entityEvent.setCancelled(true);
    }

}
