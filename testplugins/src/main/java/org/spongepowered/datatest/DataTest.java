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
package org.spongepowered.datatest;

import com.google.inject.Inject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Plugin("datatest")
public class DataTest  {

    @Inject
    private PluginContainer plugin;

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Parameterized myCommand = Command.builder()
                .setExecutionRequirements(cc -> cc.first(ServerPlayer.class).isPresent())
                .setExecutor(context -> {
                    this.testData(context.getCause().first(ServerPlayer.class).get());
                    return CommandResult.success();
                })
                .build();
        event.register(this.plugin, myCommand, "datatest");
    }


    public void testData(ServerPlayer player) {
        final ServerWorld world = player.getWorld();
        final Vector3d position = player.getPosition();

        this.checkOfferData(player, Keys.ABSORPTION, 0.0);
        this.checkOfferData(player, Keys.ABSORPTION, 10.0);
        this.checkOfferData(player, Keys.ABSORPTION, 20.0);
        // TODO launchProjectile is abstract
//        final Optional<Arrow> arrow = player.launchProjectile(Arrow.class, player.getHeadDirection());
//        this.checkOfferData(arrow.get(), Keys.ACCELERATION, Vector3d.UP);
        // TODO Keys.ACTIVE_ITEM is only when actually using items
        // Test: get during event + setting empty & remove

        // TODO check serialize
        this.checkOfferData(player, Keys.AFFECTS_SPAWNING, false);
        this.checkOfferData(player, Keys.AFFECTS_SPAWNING, true);

        final Entity sheep = world.createEntity(EntityTypes.SHEEP.get(), position);
        this.checkGetData(sheep, Keys.AGE, 0);
        this.checkOfferData(player, Keys.AGE, 10);
        sheep.remove();

        // TODO missing impl
//        final Entity minecart = world.createEntity(EntityTypes.MINECART.get(), position);
//        this.checkGetData(minecart, Keys.AIRBORNE_VELOCITY_MODIFIER, Vector3d.ZERO);
//        this.checkOfferData(minecart, Keys.AIRBORNE_VELOCITY_MODIFIER, Vector3d.UP);

        final Entity zombiePigman = world.createEntity(EntityTypes.ZOMBIE_PIGMAN.get(), position);
        this.checkGetData(zombiePigman, Keys.ANGER_LEVEL, 0);
        this.checkOfferData(zombiePigman, Keys.ANGER_LEVEL, 10);
        zombiePigman.remove();

        final ItemStack goldenApple = ItemStack.of(ItemTypes.ENCHANTED_GOLDEN_APPLE);
        final WeightedTable<PotionEffect> appleEffects = goldenApple.get(Keys.APPLICABLE_POTION_EFFECTS).get();
        this.checkGetData(goldenApple, Keys.APPLICABLE_POTION_EFFECTS, appleEffects); // TODO this does not check anything
    }

    private <T> void checkOfferData(DataHolder.Mutable holder, Supplier<Key<SetValue<T>>> key, Set<T> value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (!result.isSuccessful()) {
            System.err.println("Failed offer on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString() + " with " + value);
            return;
        }
        this.checkGetData(holder, key, value);
    }

    private <T> void checkOfferData(DataHolder.Mutable holder, Supplier<Key<WeightedCollectionValue<T>>> key, WeightedTable<T> value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (!result.isSuccessful()) {
            System.err.println("Failed offer on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString() + " with " + value);
            return;
        }
        this.checkGetData(holder, key, value);
    }

    private <T> void checkOfferData(DataHolder.Mutable holder, Supplier<Key<Value<T>>> key, T value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (!result.isSuccessful()) {
            System.err.println("Failed offer on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString() + " with " + value);
            return;
        }
        this.checkGetData(holder, key, value);
    }

    private <T> void checkGetData(DataHolder holder, Supplier<Key<WeightedCollectionValue<T>>> key, WeightedTable<T> expected) {
        final Optional<WeightedTable<T>> gotValue = holder.get(key);
        if (gotValue.isPresent()) {
            if (!Objects.equals(gotValue.get(), expected)) {
                System.err.println("Value differs on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString()
                        + " expected: " + expected + " actual: " + gotValue.get());
            }
        } else {
            System.err.println("Value is missing on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString());
        }
    }

    private <T> void checkGetData(DataHolder holder, Supplier<Key<SetValue<T>>> key, Set<T> expected) {
        final Optional<Set<T>> gotValue = holder.get(key);
        if (gotValue.isPresent()) {
            if (!Objects.equals(gotValue.get(), expected)) {
                System.err.println("Value differs on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString()
                        + " expected: " + expected + " actual: " + gotValue.get());
            }
        } else {
            System.err.println("Value is missing on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString());
        }
    }

    private <T> void checkGetData(DataHolder holder, Supplier<Key<Value<T>>> key, T expected) {
        final Optional<T> gotValue = holder.get(key);
        if (gotValue.isPresent()) {
            if (!Objects.equals(gotValue.get(), expected)) {
                System.err.println("Value differs on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString()
                        + " expected: " + expected + " actual: " + gotValue.get());
            }
        } else {
            System.err.println("Value is missing on " + holder.getClass().getSimpleName() + " for " + key.get().getKey().asString());
        }
    }

}
