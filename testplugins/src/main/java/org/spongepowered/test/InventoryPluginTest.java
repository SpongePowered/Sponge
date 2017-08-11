package org.spongepowered.test;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "inventoryplugintest", description = "A plugin to test the owner of an inventory.")
public class InventoryPluginTest {

    @Listener
    public void onInventoryClose(InteractInventoryEvent.Close event, @First Player player) {
        player.sendMessage(Text.of("This inventory was brought to you by ", event.getTargetInventory().getPlugin().getName(), "."));
    }
}
