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
package org.spongepowered.customdatatest;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCatalogEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Plugin("customdatatest")
public class CustomDataTest {

    @Inject
    private PluginContainer plugin;
    private Key<Value<Integer>> myKey;

    private enum Type {
        ITEMSTACK,
        ENTITY,
        BLOCKENTITY,
        PLAYER,
        USER
    }
    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Integer> numberKey = Parameter.integerNumber().orDefault(1).setKey("number").build();
        final Parameter.Value<Type> type = Parameter.enumValue(Type.class).orDefault(Type.ITEMSTACK).setKey("type").build();
        final Command.Parameterized myCommand = Command.builder()
                .parameter(type)
                .parameter(numberKey)
                .setExecutor(context -> {
                    final Integer number = context.requireOne(numberKey);
                    final ServerPlayer player = context.getCause().first(ServerPlayer.class).get();
                    switch (context.requireOne(type)) {
                        case ITEMSTACK:
                            final ItemStack stack = ItemStack.of(ItemTypes.PAPER);
                            stack.offer(this.myKey, number);
                            player.getInventory().offer(stack);
                            final List<Slot> slots = player.getInventory().query(QueryTypes.ITEM_STACK_CUSTOM.get().of(s -> s.get(this.myKey).isPresent())).slots();
                            final int itemSum = slots.stream().map(Slot::peek).mapToInt(item -> item.get(this.myKey).get()).sum();
                            player.sendActionBar(TextComponent.of(itemSum));
                            break;
                        case ENTITY:
                            final Entity entity = player.getWorld().createEntity(EntityTypes.MINECART.get(), player.getPosition().add(0, 3, 0));
                            entity.offer(this.myKey, number);
                            player.getWorld().spawnEntity(entity);
                            final int entitySum = player.getNearbyEntities(5).stream().filter(e -> e.get(this.myKey).isPresent()).mapToInt(e -> e.get(this.myKey).get()).sum();
                            player.sendActionBar(TextComponent.of(entitySum));
                            break;
                        case BLOCKENTITY:
                            player.getWorld().setBlock(player.getBlockPosition(), BlockTypes.DISPENSER.get().getDefaultState());
                            final BlockEntity blockEntity = player.getWorld().getBlockEntity(player.getBlockPosition()).get();
                            blockEntity.offer(this.myKey, number);
                            final int blockEntitySum = player.getWorld().getBlockEntities().stream().filter(e -> e.get(this.myKey).isPresent())
                                    .mapToInt(e -> e.get(this.myKey).get()).sum();
                            player.sendActionBar(TextComponent.of(blockEntitySum));
                            break;
                        case PLAYER:
                            final Integer integer = player.get(this.myKey).orElse(0);
                            player.sendActionBar(TextComponent.of(integer));
                            player.offer(this.myKey, number);
                            break;
                        case USER:
                            // delegate to player
                            this.customUserData(player.getUniqueId(), number);
                            player.kick(TextComponent.of("Setting User data..."));
                            final Scheduler scheduler = Sponge.getServer().getScheduler();
                            scheduler.submit(Task.builder().delayTicks(1).execute(() -> this.customUserData(player.getUniqueId(), number)).plugin(this.plugin).build());
                            scheduler.submit(Task.builder().delayTicks(2).execute(() -> this.customUserData(player.getUniqueId(), number)).plugin(this.plugin).build());
                            break;
                    }
                    return CommandResult.success();
                })
                .build();
        event.register(this.plugin, myCommand, "customdata");
    }

    @Listener
    public void onRegisterData(RegisterCatalogEvent<DataRegistration> event) {
        this.myKey = Key.builder().key(ResourceKey.of(this.plugin, "mydata")).type(TypeTokens.INTEGER_VALUE_TOKEN).build();
        final DataRegistration myRegistration = DataRegistration.builder()
                .key(this.myKey)
                .store(DataStore.builder().key(this.myKey, "mykey")
                        .holder(TypeTokens.ITEM_TYPE_TOKEN, TypeTokens.USER_TOKEN, TypeTokens.PLAYER_TOKEN)
                        .build())
                .key(ResourceKey.of(this.plugin, "mydataregistration"))
                .build();
        event.register(myRegistration);
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        final Optional<Integer> myValue = event.getPlayer().get(this.myKey);
        if (myValue.isPresent()) {
            System.out.println("CustomData: " + myValue.get());
        }
    }

    private void customUserData(UUID playerUUID, int number) {
        final Optional<User> user = Sponge.getServer().getUserManager().get(playerUUID);
        if (user.isPresent()) {
            final Integer integer = user.get().get(this.myKey).orElse(0);
            System.out.println("Custom data on user " + user.get().getName() + ":" + integer);
            user.get().offer(this.myKey, number);
        }
    }

}
