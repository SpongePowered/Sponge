package org.spongepowered.common.world.border;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;

public class WorldBorderTest {

    @Test
    public void testBorderIsApplied() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();

        final WorldBorder border = WorldBorder.builder().center(1, 1).initialDiameter(1).build();

        world.setBorder(border);

        Assertions.assertEquals(border, world.border());
    }

    @Test
    public void testBorderIsWorldSpecific() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
        final ServerWorld nether = Sponge.server().worldManager().world(DefaultWorldKeys.THE_NETHER).get();

        final WorldBorder border = WorldBorder.builder().center(1, 1).initialDiameter(1).build();
        final WorldBorder netherBorder = WorldBorder.builder().center(2, 2).initialDiameter(2).build();

        world.setBorder(border);
        nether.setBorder(netherBorder);

        Assertions.assertEquals(border, world.border());
        Assertions.assertEquals(netherBorder, nether.border());
    }

}
