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

import com.flowpowered.math.vector.Vector2d;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.advancement.AdvancementTreeEvent;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Plugin(id = AdvancementTest.ID, name = "Advancement Test", version = "0.0.0", description = "test custom advancement")
public class AdvancementTest implements LoadableModule {

    public static final String ID = "advancement_test";

    private static final String ADVANCEMENT_TREE = "dirt";
    private static final String ROOT_ADVANCEMENT = "dirt";
    private static final String BREAK_DIRT_CRITERION = "broken_dirt";
    private static final String BREAK_DIRT_ADVANCEMENT = "dirt_digger";
    private static final String COOK_DIRT_ADVANCEMENT = "dirt_cooker";
    private static final String SUICIDAL_ADVANCEMENT = "suicidal";
    private static final String TRIGGER = "my_trigger";

    @Inject private Logger logger;
    @Inject private PluginContainer pluginContainer;

    private final AdvancementListener listener = new AdvancementListener();

    @ConfigSerializable
    public static class MyTriggerConfig implements FilteredTriggerConfiguration {

        @Setting("chance")
        private float chance;
    }

    @SuppressWarnings("rawtypes")
    @Listener
    public void onRegisterTriggers(GameRegistryEvent.Register<Trigger> event) {
        this.logger.info("Advancements test source: " + this.pluginContainer.getSource().orElse(null));
        Trigger trigger = Trigger.builder()
                .typeSerializableConfig(MyTriggerConfig.class)
                .listener(triggerEvent -> {
                    final Random random = new Random();
                    final float value = random.nextFloat();
                    final float chance = triggerEvent.getTrigger().getConfiguration().chance;
                    triggerEvent.setResult(value < chance);
                    triggerEvent.getTargetEntity().sendMessage(Text.of(value + " < " + chance + " -> " + triggerEvent.getResult()));
                })
                .id(TRIGGER)
                .build();
        event.register(trigger);
    }

    @Listener
    public void onRegisterAdvancementTrees(GameRegistryEvent.Register<AdvancementTree> event) {
        this.logger.info("Loading advancement trees...");
        // Create the advancement tree
        Advancement rootAdvancement = Sponge.getRegistry().getType(Advancement.class, ID + ":" + ROOT_ADVANCEMENT).get();
        AdvancementTree advancementTree = AdvancementTree.builder()
                .rootAdvancement(rootAdvancement)
                .background("minecraft:textures/blocks/dirt.png")
                .id(ADVANCEMENT_TREE)
                .build();
        event.register(advancementTree);
    }

    @Listener
    public void onRegisterAdvancements(GameRegistryEvent.Register<Advancement> event) {
        this.logger.info("Loading advancements...");
        // Create the root advancement
        Advancement rootAdvancement = Advancement.builder()
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Text.of("Dirt? Dirt!"))
                        .build())
                .criterion(AdvancementCriterion.DUMMY)
                .id(ROOT_ADVANCEMENT)
                .build();
        event.register(rootAdvancement);

        // Create the break dirt advancement and criterion
        ScoreAdvancementCriterion breakDirtCriterion = ScoreAdvancementCriterion.builder()
                .goal(10)
                .name(BREAK_DIRT_CRITERION)
                .build();
        Advancement breakDirtAdvancement = Advancement.builder()
                .parent(rootAdvancement)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.STONE_SHOVEL)
                        .title(Text.of("Digger"))
                        .description(Text.of("Start digging."))
                        .build())
                .criterion(breakDirtCriterion)
                .id(BREAK_DIRT_ADVANCEMENT)
                .build();
        event.register(breakDirtAdvancement);

        // Create the cook dirt advancement
        Advancement cookDirtAdvancement = Advancement.builder()
                .parent(rootAdvancement)
                .criterion(AdvancementCriterion.DUMMY)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.FURNACE)
                        .title(Text.of("Dirty cook"))
                        .description(Text.of("Try to cook dirt"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .id(COOK_DIRT_ADVANCEMENT)
                .build();
        event.register(cookDirtAdvancement);

        event.getRegistryModule().getById("minecraft:adventure_root").ifPresent(parent -> {
            // Create the suicidal advancement
            Advancement suicidalAdvancement = Advancement.builder()
                    .parent(parent)
                    .criterion(AdvancementCriterion.DUMMY)
                    .displayInfo(DisplayInfo.builder()
                            .icon(ItemTypes.TNT)
                            .title(Text.of("Suicidal?"))
                            .description(Text.of("Put TNT in a burning furnace"))
                            .type(AdvancementTypes.CHALLENGE)
                            .hidden(true)
                            .build())
                    .id(SUICIDAL_ADVANCEMENT)
                    .build();
            event.register(suicidalAdvancement);
        });
    }

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.pluginContainer, this.listener);
    }

    public static class AdvancementListener {

        @Listener
        public void onPlayerJoin(ClientConnectionEvent.Join event) {
            Advancement rootAdvancement = Sponge.getRegistry().getType(Advancement.class, ID + ":" + ROOT_ADVANCEMENT).get();
            event.getTargetEntity().getProgress(rootAdvancement).grant();
        }

        @Listener
        public void onGenerateTreeLayout(AdvancementTreeEvent.GenerateLayout event) {
            AdvancementTree advancementTree = Sponge.getRegistry().getType(AdvancementTree.class, ID + ":" + ADVANCEMENT_TREE).get();
            if (event.getTree() != advancementTree) {
                return;
            }
            Sponge.getServer().getBroadcastChannel().send(Text.of("Updating advancement tree layout..."));
            // Make the tree start at y 0, for every level within the tree
            // The min y position mapped by the used x positions
            // For example:
            //    |- y
            // x -|- z
            //    |- w
            // to
            // x -|- y
            //    |- z
            //    |- w
            final Map<Double, Double> values = new HashMap<>();
            for (TreeLayoutElement element : event.getLayout().getElements()) {
                final Vector2d pos = element.getPosition();
                if (!values.containsKey(pos.getX()) || pos.getY() < values.get(pos.getX())) {
                    values.put(pos.getX(), pos.getY());
                }
            }
            for (TreeLayoutElement element : event.getLayout().getElements()) {
                final Vector2d pos = element.getPosition();
                element.setPosition(pos.getX(), pos.getY() - values.get(pos.getX()));
            }
        /*
        // Rotate the advancement tree
        // The lines are currently drawn wrongly, that might be something
        // for later as it involves "tricking" the client
        double maxY = 0;
        for (TreeLayoutElement element : event.getLayout().getElements()) {
            maxY = Math.max(maxY, element.getPosition().getY());
        }
        for (TreeLayoutElement element : event.getLayout().getElements()) {
            final Vector2d pos = element.getPosition();
            element.setPosition(maxY - pos.getY(), pos.getX());
        }
        */
        }

        @Listener
        public void onChangeBlock(ChangeBlockEvent.Break event, @First Player player) {
            Trigger<?> trigger = Sponge.getRegistry().getType(Trigger.class, ID + ":" + TRIGGER).get();
            Advancement breakDirtAdvancement = Sponge.getRegistry().getType(Advancement.class, ID + ":" + BREAK_DIRT_ADVANCEMENT).get();
            Criterion breakDirtCriterion = Sponge.getRegistry().getType(Criterion.class, ID + ":" + BREAK_DIRT_CRITERION).get();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                if (transaction.getFinal().getState().getType() == BlockTypes.AIR &&
                        (transaction.getOriginal().getState().getType() == BlockTypes.DIRT ||
                                transaction.getOriginal().getState().getType() == BlockTypes.GRASS)) {

                    player.getProgress(breakDirtAdvancement).get((ScoreAdvancementCriterion) breakDirtCriterion).get().add(1);
                } else if (transaction.getFinal().getState().getType() == BlockTypes.AIR &&
                        (transaction.getOriginal().getState().getType() == BlockTypes.LEAVES ||
                                transaction.getOriginal().getState().getType() == BlockTypes.LEAVES2)) {
                    trigger.trigger(player);
                }
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Listener
        public void onChangeInventory(ChangeInventoryEvent event, @First Player player,
                                      @Getter("getTargetInventory") CarriedInventory<?> container) {
            if (!container.getName().get().equals("Furnace")) {
                return;
            }
            final Carrier carrier = container.getCarrier().orElse(null);
            if (!(carrier instanceof Furnace)) {
                return;
            }
            final Furnace furnace = (Furnace) carrier;
            final int passed = furnace.passedBurnTime().get();
            final int max = furnace.maxBurnTime().get();
            if (max <= 0 || passed >= max) {
                return;
            }
            Advancement cookDirtAdvancement = Sponge.getRegistry().getType(Advancement.class, ID + ":" + COOK_DIRT_ADVANCEMENT).get();
            Advancement suicidalAdvancement = Sponge.getRegistry().getType(Advancement.class, ID + ":" + SUICIDAL_ADVANCEMENT).get();
            for (SlotTransaction transaction : event.getTransactions()) {
                if (transaction.getSlot().getInventoryProperty(SlotIndex.class).get().getValue() == 0) {
                    if (transaction.getFinal().getType() == ItemTypes.DIRT) {
                        player.getProgress(cookDirtAdvancement).grant();
                    } else if (suicidalAdvancement != null && (transaction.getFinal().getType() == ItemTypes.TNT ||
                            transaction.getFinal().getType() == ItemTypes.TNT_MINECART)) {
                        player.getProgress(suicidalAdvancement).grant();
                        final Explosion explosion = Explosion.builder()
                                .location(furnace.getLocation())
                                .shouldBreakBlocks(true)
                                .canCauseFire(true)
                                .shouldDamageEntities(true)
                                .radius(7)
                                .build();
                        explosion.getWorld().triggerExplosion(explosion);
                    }
                }
            }
        }

        @Listener
        public void onCriterionGrant(CriterionEvent.Grant event) {
            event.getTargetEntity().sendMessage(Text.of(TextColors.GREEN, "Congratulations on achieving criterion " + event.getCriterion().getName()));
        }

        @Listener
        public void onAdvancementGrant(AdvancementEvent.Grant event) {
            event.getTargetEntity().sendMessage(Text.of(TextColors.BLUE, "You achieved advancement " + event.getAdvancement().getName()));
        }
    }
}
