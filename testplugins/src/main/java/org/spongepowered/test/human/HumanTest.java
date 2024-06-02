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
package org.spongepowered.test.human;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.ai.goal.builtin.LookRandomlyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.SwimGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AttackLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RandomWalkingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Optional;

@Plugin("humantest")
public final class HumanTest {

    private final PluginContainer plugin;

    @Inject
    public HumanTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<String> nameParameter = Parameter.string().key("name").optional().build();
        final Parameter.Value<String> mimicParameter = Parameter.string().key("mimic_username").optional().build();

        event.register(this.plugin, Command
                    .builder()
                    .addParameter(nameParameter)
                    .addParameter(mimicParameter)
                    .permission(this.plugin.metadata().id() + ".command.human.create")
                    .executor(context -> {
                        final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                        final Optional<String> optName = context.one(nameParameter);
                        final String mimicUsername = context.one(mimicParameter).orElse(optName.orElse(player.name()));
                        final boolean result = this.spawnHuman(player.serverLocation(), optName.orElse(player.name()), mimicUsername);
                        return result ? CommandResult.success() : CommandResult.error(Component.text("Failed to spawn the human!"));
                    })
                    .build()
            , "ch", "createhuman"
        );
    }

    public boolean spawnHuman(ServerLocation at, String name, String mimicUsername) {
        final Human human = at.world().createEntity(EntityTypes.HUMAN.get(), at.position());
        human.offer(Keys.CUSTOM_NAME, Component.text(name));
        human.useSkinFor(mimicUsername);
        final boolean result = at.world().spawnEntity(human);

        this.initHumanEquipment(human);
        this.initHumanGoals(human);

        return result;
    }

    public void initHumanGoals(Human human) {
        final GoalExecutor<Agent> targetGoal = human.goal(GoalExecutorTypes.TARGET.get()).orElse(null);
//        targetGoal.addGoal(0, FindNearestAttackableTargetGoal.builder().chance(1).target(ServerPlayer.class).build(human));
//        targetGoal.addGoal(0, FindNearestAttackableTargetGoal.builder().chance(1).target(Monster.class).build(human));
        targetGoal.addGoal(1, FindNearestAttackableTargetGoal.builder().chance(1).target(Human.class).build(human));

        final GoalExecutor<Agent> normalGoal = human.goal(GoalExecutorTypes.NORMAL.get()).orElse(null);
        normalGoal.addGoal(0, SwimGoal.builder().swimChance(0.8f).build(human));
//        normalGoal.addGoal(0, AvoidLivingGoal.builder().targetSelector(l -> l instanceof Creeper).searchDistance(5).closeRangeSpeed(7).farRangeSpeed(2).build(human));
        normalGoal.addGoal(1, AttackLivingGoal.builder().longMemory().speed(4).build(human));
        normalGoal.addGoal(2, RandomWalkingGoal.builder().speed(3).build(human));
        normalGoal.addGoal(3, LookAtGoal.builder().maxDistance(8f).watch(Human.class).build(human));
        normalGoal.addGoal(4, LookRandomlyGoal.builder().build(human));
    }

    public void initHumanEquipment(Human human) {
        human.setLegs(ItemStack.of(ItemTypes.LEATHER_LEGGINGS));
        human.setChest(ItemStack.of(ItemTypes.IRON_CHESTPLATE));
        human.setFeet(ItemStack.of(ItemTypes.GOLDEN_BOOTS));
        human.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.STONE_AXE));
        human.setItemInHand(HandTypes.OFF_HAND, ItemStack.of(ItemTypes.GOLDEN_APPLE));

        human.offer(Keys.MAX_HEALTH, 500d);
        human.offer(Keys.HEALTH, 500d);
    }
}
