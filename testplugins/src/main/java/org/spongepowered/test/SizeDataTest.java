/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.test;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

@Plugin(id = "sizedatatest", name = "Size Data Test", description = "A plugin to test the size data implementation.")
public class SizeDataTest {

    @Listener
    public void onGameInit(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .description(Text.of("Size Command"))
                        .arguments(
                                GenericArguments.onlyOne(GenericArguments.catalogedElement(Text.of("entity"), EntityType.class)),
                                GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("base"))),
                                GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("height"))))
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be an in-game player to use this command!"));
                            }
                            final EntityType entityType = args.<EntityType>getOne("entity").orElse(EntityTypes.ZOMBIE);

                            final Double base = args.<Double>getOne("base").get();
                            final Double height = args.<Double>getOne("height").get();

                            final Player player = (Player) src;

                            final Entity entity = player.getWorld().createEntity(entityType, getSpawnPosition(player));
                            entity.offer(Keys.BASE_SIZE, base.floatValue());
                            entity.offer(Keys.HEIGHT, height.floatValue());

                            player.getWorld().spawnEntity(entity, Cause.of(NamedCause.source(player)));

                            player.sendMessage(Text.of(TextColors.DARK_GREEN, "You have successfully spawned a " + entityType.getName() + " with the following stats:"));
                            player.sendMessage(Text.of(TextColors.GOLD, "Base size(width): ", TextColors.GRAY, entity.get(Keys.BASE_SIZE).orElse(1f)));
                            player.sendMessage(Text.of(TextColors.GOLD, "Height: ", TextColors.GRAY, entity.get(Keys.HEIGHT).orElse(1f)));
                            player.sendMessage(Text.of(TextColors.GOLD, "Scale: ", TextColors.GRAY, entity.get(Keys.SCALE).orElse(1f)));

                            return CommandResult.success();
                        })
                        .build(),
                "sizetest");
    }

    private Vector3i getSpawnPosition(Player player) {
        final BlockRay<World> playerBlockRay = BlockRay.from(player).distanceLimit(350).build();

        BlockRayHit<World> finalHitRay = null;
        Vector3i previousPosition = Vector3i.ONE;

        while (playerBlockRay.hasNext()) {
            final BlockRayHit<World> currentHitRay = playerBlockRay.next();

            if (!player.getWorld().getBlockType(currentHitRay.getBlockPosition()).equals(BlockTypes.AIR)) {
                finalHitRay = currentHitRay;
                break;
            }
            previousPosition = currentHitRay.getBlockPosition();
        }

        if (finalHitRay == null) {
            return player.getLocation().getBlockPosition();
        } else {
            return previousPosition;
        }
    }

}
