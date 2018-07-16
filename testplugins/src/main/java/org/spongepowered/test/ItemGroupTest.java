package org.spongepowered.test;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemGroup;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Plugin(id = "item_group_test", name = "Item Group Test", version = "0.0.0", description = ItemGroupTest.DESCRIPTION)
public class ItemGroupTest {

    public static final String DESCRIPTION = "Right click an item to get the ItemGroup";

    @Listener
    public void onUseItem(InteractItemEvent.Secondary event, @First Player player) {
        Optional<ItemGroup> ig = event.getItemStack().getType().getItemGroup();
        player.sendMessage(Text.of("This item is in the item group: " + ig.map(ItemGroup::getName).orElse("none")));
    }
}
