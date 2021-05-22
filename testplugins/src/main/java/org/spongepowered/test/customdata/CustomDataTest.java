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
package org.spongepowered.test.customdata;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Plugin("customdatatest")
public final class CustomDataTest {

    private final PluginContainer plugin;
    private Key<Value<Integer>> myDataKey;
    private Key<Value<String>> mySimpleDataKey;
    private Key<Value<ItemType>> myItemTypeKey;

    @Inject
    public CustomDataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    private enum Type {
        ITEMSTACK,
        ENTITY,
        BLOCKENTITY,
        PLAYER,
        USER,
        BLOCK
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Integer> numberKey = Parameter.integerNumber().key("number").optional().build();
        final Parameter.Value<Type> type = Parameter.enumValue(Type.class).key("type").optional().build();
        final Command.Parameterized myCommand = Command.builder()
                .addParameter(type)
                .addParameter(numberKey)
                .executor(context -> {
                    final Integer number = context.one(numberKey).orElse(1);
                    final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                    switch (context.one(type).orElse(Type.ITEMSTACK)) {
                        case ITEMSTACK:
                            final ItemStack stack = ItemStack.of(ItemTypes.PAPER);
                            stack.offer(this.myDataKey, number);
                            stack.offer(this.mySimpleDataKey, "It works! " + number);
                            stack.offer(this.myItemTypeKey, ItemTypes.PAPER.get());
                            player.inventory().offer(stack);
                            final List<Slot> slots = player.inventory().query(QueryTypes.ITEM_STACK_CUSTOM.get().of(s -> s.get(this.myDataKey).isPresent())).slots();
                            final int itemSum = slots.stream().map(Slot::peek).mapToInt(item -> item.get(this.myDataKey).get()).sum();
                            player.sendActionBar(Component.text(itemSum));
                            slots.stream().map(Slot::peek).map(s -> s.get(this.mySimpleDataKey)).forEach(data -> data.ifPresent(value -> player.sendMessage(Identity.nil(), Component.text(value))));
                            break;
                        case ENTITY:
                            final Entity entity = player.world().createEntity(EntityTypes.MINECART.get(), player.position().add(0, 3, 0));
                            entity.offer(this.myDataKey, number);
                            player.world().spawnEntity(entity);
                            final int entitySum = player.nearbyEntities(5).stream().filter(e -> e.get(this.myDataKey).isPresent()).mapToInt(e -> e.get(this.myDataKey).get()).sum();
                            player.sendActionBar(Component.text(entitySum));
                            break;
                        case BLOCKENTITY:
                            player.world().setBlock(player.blockPosition(), BlockTypes.DISPENSER.get().defaultState());
                            final BlockEntity blockEntity = player.world().blockEntity(player.blockPosition()).get();
                            blockEntity.offer(this.myDataKey, number);
                            final int blockEntitySum = player.world().blockEntities().stream().filter(e -> e.get(this.myDataKey).isPresent())
                                    .mapToInt(e -> e.get(this.myDataKey).get()).sum();
                            player.sendActionBar(Component.text(blockEntitySum));
                            break;
                        case PLAYER:
                            final Integer integer = player.get(this.myDataKey).orElse(0);
                            player.sendActionBar(Component.text(integer));
                            player.offer(this.myDataKey, number);
                            break;
                        case USER:
                            // delegate to player
                            this.customUserData(player.uniqueId(), number);
                            player.kick(Component.text("Setting User data..."));
                            final Scheduler scheduler = Sponge.server().scheduler();
                            scheduler.submit(Task.builder().delay(Ticks.single()).execute(() -> this.customUserData(player.uniqueId(), number)).plugin(this.plugin).build());
                            scheduler.submit(Task.builder().delay(Ticks.of(2)).execute(() -> this.customUserData(player.uniqueId(), number)).plugin(this.plugin).build());
                            break;
                        case BLOCK:
                            // try out custom data-stores
                            final Integer oldNumber = player.world().get(player.blockPosition(), this.myDataKey).orElse(0);
                            player.sendActionBar(Component.text(oldNumber));
                            player.world().offer(player.blockPosition(), this.myDataKey, oldNumber + number);
                    }
                    return CommandResult.success();
                })
                .build();
        event.register(this.plugin, myCommand, "customdata");
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event) {
        final ResourceKey key = ResourceKey.of(this.plugin, "mydata");
        this.myDataKey = Key.builder().key(key).elementType(Integer.class).build();

        final DataProvider<Value<Integer>, Integer> blockDataProvider = DataProvider.mutableBuilder()
                .key(this.myDataKey).dataHolder(ServerLocation.class)
                .get(this::getData).set(this::setData).delete(this::removeData)
                .build();


        final DataStore dataStore = DataStore.of(this.myDataKey, DataQuery.of("mykey"), ItemStack.class, User.class, ServerPlayer.class, BlockEntity.class, Entity.class);
        final DataRegistration myRegistration = DataRegistration.builder()
                .dataKey(this.myDataKey)
                .store(dataStore)
                .provider(blockDataProvider)
                .build();

        event.register(myRegistration);

        // Or if it is super simple data

        this.mySimpleDataKey = Key.from(this.plugin, "mysimpledata", String.class);
        event.register(DataRegistration.of(this.mySimpleDataKey, ItemStack.class));

        this.myItemTypeKey = Key.from(this.plugin, "myitemtypedata", ItemType.class);
        event.register(DataRegistration.of(this.myItemTypeKey, ItemStack.class));
    }

    // replace with mongoDB - for web-scale
    private Map<ResourceKey, Map<Vector3i, Integer>> myCustomData = new HashMap<>();

    private DataTransactionResult removeData(final ServerLocation serverLocation) {
        final Integer removed = this.myCustomData.getOrDefault(serverLocation.worldKey(), Collections.emptyMap()).remove(serverLocation.blockPosition());
        if (removed == null) {
            return DataTransactionResult.failNoData();
        }
        return DataTransactionResult.successRemove(Value.immutableOf(this.myDataKey, removed));
    }

    private DataTransactionResult setData(final ServerLocation serverLocation, final Integer value) {
        final Map<Vector3i, Integer> worldData = this.myCustomData.computeIfAbsent(serverLocation.worldKey(), k -> new HashMap<>());
        worldData.put(serverLocation.blockPosition(), value);
        return DataTransactionResult.successResult(Value.immutableOf(this.myDataKey, value));
    }

    private Integer getData(final ServerLocation serverLocation) {
        return this.myCustomData.getOrDefault(serverLocation.worldKey(), Collections.emptyMap()).get(serverLocation.blockPosition());
    }

    @Listener
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        final Optional<Integer> myValue = event.player().get(this.myDataKey);
        myValue.ifPresent(integer -> this.plugin.logger().info("CustomData: {}", integer));
    }

    private void customUserData(final UUID playerUUID, final int number) {
        final Optional<User> user = Sponge.server().userManager().find(playerUUID);
        if (user.isPresent()) {
            final Integer integer = user.get().get(this.myDataKey).orElse(0);
            this.plugin.logger().info("Custom data on user {}: {}", user.get().name(), integer);
            user.get().offer(this.myDataKey, number);
        }
    }
}
