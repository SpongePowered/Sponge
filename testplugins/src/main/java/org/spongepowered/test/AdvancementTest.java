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
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.advancement.AdvancementTreeEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

@Plugin(id = "advancement_test", name = "Advancement Test")
public class AdvancementTest {

    @Inject private Logger logger;

    private AdvancementTree advancementTree;
    private Advancement rootAdvancement;

    private ScoreAdvancementCriterion breakDirtCriterion;
    private Advancement breakDirtAdvancement;

    private Advancement cookDirtAdvancement;
    @Nullable private Advancement suicidalAdvancement;

    @Inject private PluginContainer pluginContainer;

    private Trigger<MyTriggerConfig> trigger;

    @ConfigSerializable
    public static class MyTriggerConfig implements FilteredTriggerConfiguration {

        @Setting("chance")
        private float chance;
    }

    @Listener
    public void onRegister(GameRegistryEvent.Register event) {
        this.logger.info("onRegister: " + event.getCatalogType().getName());
    }

    @Listener
    public void onRegister2(GameRegistryEvent.Register<?> event) {
        this.logger.info("onRegister<?>: " + event.getCatalogType().getName());
    }

    @Listener
    public void onRegister3(GameRegistryEvent.Register<? extends Trigger> event) {
        this.logger.info("onRegister<? extends Trigger>: " + event.getCatalogType().getName());
    }

    @Listener
    public void onRegisterTriggers(GameRegistryEvent.Register<Trigger> event) {
        this.logger.info("Advancements test source: " + this.pluginContainer.getSource().orElse(null));
        this.trigger = Trigger.builder()
                .typeSerializableConfig(MyTriggerConfig.class)
                .listener(triggerEvent -> {
                    final Random random = new Random();
                    final float value = random.nextFloat();
                    final float chance = triggerEvent.getTrigger().getConfiguration().chance;
                    triggerEvent.setResult(value < chance);
                    triggerEvent.getTargetEntity().sendMessage(Text.of(value + " < " + chance + " -> " + triggerEvent.getResult()));
                })
                .build("my_trigger");
        event.register(this.trigger);
    }

    @Listener
    public void onRegisterAdvancementTrees(GameRegistryEvent.Register<AdvancementTree> event) {
        this.logger.info("Loading advancement trees...");
        // Create the advancement tree
        this.advancementTree = AdvancementTree.builder()
                .rootAdvancement(this.rootAdvancement)
                .background("minecraft:textures/blocks/dirt.png")
                .build("dirt");
        event.register(this.advancementTree);
    }

    @Listener
    public void onRegisterAdvancements(GameRegistryEvent.Register<Advancement> event) {
        this.logger.info("Loading advancements...");
        // Create the root advancement
        this.rootAdvancement = Advancement.builder()
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Text.of("Dirt? Dirt!"))
                        .build())
                .criterion(AdvancementCriterion.DUMMY)
                .build("dirt");
        event.register(this.rootAdvancement);

        // Create the break dirt advancement and criterion
        this.breakDirtCriterion = ScoreAdvancementCriterion.builder()
                .goal(10)
                .build("broken_dirt");
        this.breakDirtAdvancement = Advancement.builder()
                .parent(this.rootAdvancement)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.STONE_SHOVEL)
                        .title(Text.of("Digger"))
                        .description(Text.of("Start digging."))
                        .build())
                .criterion(this.breakDirtCriterion)
                .build("dirt_digger");
        event.register(this.breakDirtAdvancement);

        // Create the cook dirt advancement
        this.cookDirtAdvancement = Advancement.builder()
                .parent(this.rootAdvancement)
                .criterion(AdvancementCriterion.DUMMY)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.FURNACE)
                        .title(Text.of("Dirty cook"))
                        .description(Text.of("Try to cook dirt"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .build("dirt_cooker");
        event.register(this.cookDirtAdvancement);

        this.suicidalAdvancement = null;
        event.getRegistryModule().getById("minecraft:adventure_root").ifPresent(parent -> {
            // Create the suicidal advancement
            this.suicidalAdvancement = Advancement.builder()
                    .parent(parent)
                    .criterion(AdvancementCriterion.DUMMY)
                    .displayInfo(DisplayInfo.builder()
                            .icon(ItemTypes.TNT)
                            .title(Text.of("Suicidal?"))
                            .description(Text.of("Put TNT in a burning furnace"))
                            .type(AdvancementTypes.CHALLENGE)
                            .hidden(true)
                            .build())
                    .build("suicidal");
            event.register(this.suicidalAdvancement);
        });
    }

    @Listener
    public void onGenerateTreeLayout(AdvancementTreeEvent.GenerateLayout event) {
        if (event.getTree() != this.advancementTree) {
            return;
        }
        this.logger.info("Updating advancement tree layout...");
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
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.getFinal().getState().getType() == BlockTypes.AIR &&
                    (transaction.getOriginal().getState().getType() == BlockTypes.DIRT ||
                            transaction.getOriginal().getState().getType() == BlockTypes.GRASS)) {
                player.getProgress(this.breakDirtAdvancement).get(this.breakDirtCriterion).get().add(1);
            } else if (transaction.getFinal().getState().getType() == BlockTypes.AIR &&
                    (transaction.getOriginal().getState().getType() == BlockTypes.LEAVES ||
                            transaction.getOriginal().getState().getType() == BlockTypes.LEAVES2)) {
                this.trigger.trigger(player);
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
        for (SlotTransaction transaction : event.getTransactions()) {
            if (transaction.getSlot().getInventoryProperty(SlotIndex.class).get().getValue() == 0) {
                if (transaction.getFinal().getType() == ItemTypes.DIRT) {
                    player.getProgress(this.cookDirtAdvancement).grant();
                } else if (this.suicidalAdvancement != null && (transaction.getFinal().getType() == ItemTypes.TNT ||
                        transaction.getFinal().getType() == ItemTypes.TNT_MINECART)) {
                    player.getProgress(this.suicidalAdvancement).grant();
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
}
