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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.ShulkerBullet;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

@Plugin(id = "shulkertest", version = "0.0.0", name = "ShulkerTest", description = "Play with Shulkers!")
public class ShulkerTest implements LoadableModule {

    @Inject private PluginContainer container;

    private final ShulkerListener listener = new ShulkerListener();

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Only players can execute this command"));
            }

            ((Player) src).getNearbyEntities(20).forEach(entity -> {
                if (entity instanceof ShulkerBullet) {
                    entity.remove(Keys.TARGETED_ENTITY);
                }
            });

            return CommandResult.success();
        }).build(), "untarget");

        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Only players can execute this command"));
            }

            Iterator<Entity> iterator = ((Player) src).getNearbyEntities(entity -> entity instanceof Shulker).iterator();
            if (iterator.hasNext()) {
                Shulker shulker = (Shulker) iterator.next();
                World world = shulker.getWorld();
                Random random = new Random();
                // Creates the spider at a random location within a 10x5x10 zone around the bullet
                Entity spider = world.createEntity(EntityTypes.SPIDER, shulker.getLocation().add(
                        random.nextInt(20) - 10,
                        random.nextInt(5),
                        random.nextInt(20) - 10).getPosition());
                world.spawnEntity(spider);

                shulker.launchWithTarget(ShulkerBullet.class, spider);
            }

            return CommandResult.success();
        }).build(), "launch");

        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Only players can execute this command"));
            }

            ((Player) src).getNearbyEntities(entity -> entity instanceof ShulkerBullet)
                    .forEach(bullet -> System.out.println(bullet.get(Keys.TARGETED_ENTITY).orElse(null)));
            return CommandResult.success();
        }).build(), "target");
    }



    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class ShulkerListener {

        private final Random random = new Random();

        @Listener
        public void onEntitySpawn(SpawnEntityEvent event) {

            event.getEntities().forEach(entity -> {
                if (entity instanceof Shulker) {
                    Collection<DyeColor> dyeColors = Sponge.getRegistry().getAllOf(DyeColor.class);
                    DyeColor dyeColor = dyeColors.toArray(new DyeColor[]{})[this.random.nextInt(dyeColors.size())];
                    entity.offer(Keys.DYE_COLOR, dyeColor);
                }
            });
        }

        @Listener
        public void onBlockImpact(CollideBlockEvent.Impact event) {
            event.setCancelled(true);
        }

        @Listener
        public void onEntityImpact(CollideEntityEvent.Impact event) {
            event.setCancelled(true);
        }
    }
}
