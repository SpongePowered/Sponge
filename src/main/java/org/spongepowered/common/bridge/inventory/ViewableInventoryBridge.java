package org.spongepowered.common.bridge.inventory;

import net.minecraft.inventory.container.Container;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ContainerType;

import java.util.Set;

public interface ViewableInventoryBridge {

    void viewableBridge$addContainer(Container container);

    void viewableBridge$removeContainer(Container container);

    Set<Player> viewableBridge$getViewers();

    ContainerType viewableBridge$getType();

    boolean viewableBridge$hasViewers();
}
