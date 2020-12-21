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
package org.spongepowered.test.advancement;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.advancement.AdvancementTreeEvent;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("advancementtest")
public final class AdvancementTest implements LoadableModule {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    private boolean enabled = false;
    private Advancement rootAdvancement;
    private Trigger<InventoryChangeTriggerConfig> inventoryChangeTrigger;
    private TriggerListeners listeners = new TriggerListeners();
    private ScoreAdvancementCriterion counter1;
    private AdvancementCriterion counter1Bypass;
    private ScoreAdvancementCriterion counter2;
    private Advancement counterAdvancement1;
    private Advancement counterAdvancement2;

    @Override
    public void enable(final CommandContext ctx) {
        this.enabled = true;
        Sponge.getEventManager().registerListeners(this.plugin, this.listeners);
        try {
            Sponge.getCommandManager().process("reload");
        } catch (final CommandException e) {
            e.printStackTrace();
        }
        ctx.getCause().first(ServerPlayer.class).map(player -> player.getProgress(this.rootAdvancement).grant());
    }

    @Override
    public void disable(final CommandContext ctx) {
        this.enabled = false;
        Sponge.getEventManager().unregisterListeners(this.listeners);
        try {
            Sponge.getCommandManager().process("reload");
        } catch (final CommandException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onTreeAdjust(final AdvancementTreeEvent.GenerateLayout event) {
        final AdvancementTree tree = event.getTree();
        if (tree.equals(this.rootAdvancement)) {
            final TreeLayoutElement layoutElement1 = event.getLayout().getElement(this.counterAdvancement1).get();
            final TreeLayoutElement layoutElement2 = event.getLayout().getElement(this.counterAdvancement2).get();
            layoutElement1.setPosition(layoutElement2.getPosition());
            layoutElement2.setPosition(layoutElement2.getPosition().add(-1,2));
        }
    }

    @Listener
    public void onGranted(final AdvancementEvent.Grant event) {
        this.logger.info("{} was granted", event.getAdvancement().getKey());
    }

    @Listener
    public void onGranted(final AdvancementEvent.Revoke event) {
        this.logger.info("{} was revoked", event.getAdvancement().getKey());
    }

    @Listener
    public void onTrigger(final CriterionEvent.Trigger<?> event) {
        this.logger.info("{} for {} was triggered", event.getTrigger().getType().getKey(), event.getAdvancement().getKey());
    }

    @Listener
    public void onTriggerRegistry(final RegisterRegistryValueEvent.GameScoped event) {
        Sponge.getDataManager().registerBuilder(InventoryChangeTriggerConfig.class, new InventoryChangeTriggerConfig.Builder());
        this.inventoryChangeTrigger = Trigger.builder()
                .dataSerializableConfig(InventoryChangeTriggerConfig.class)
                .listener(triggerEvent -> {
                    final ItemStack stack = triggerEvent.getTrigger().getConfiguration().stack;
                    final int found = triggerEvent.getPlayer().getInventory().query(QueryTypes.ITEM_STACK_IGNORE_QUANTITY, stack).totalQuantity();
                    triggerEvent.setResult(stack.getQuantity() <= found);
                })
                .key(ResourceKey.of(this.plugin, "my_inventory_trigger"))
                .name("my_inventory_trigger")
                .build();
        event.registry(RegistryTypes.TRIGGER).register(ResourceKey.of(this.plugin, "my_inventory_trigger"), this.inventoryChangeTrigger);
    }


    @Listener
    @SuppressWarnings("unchecked")
    public void onAdvancementRegistry(final RegisterDataPackValueEvent event) {

        if (!this.enabled) {
            return;
        }

        this.rootAdvancement = Advancement.builder()
                .criterion(AdvancementCriterion.dummy())
                .displayInfo(DisplayInfo.builder()
                                .icon(ItemTypes.COMMAND_BLOCK)
                                .title(Component.text("Advancement Tests"))
                                .description(Component.text("Dummy trigger. Granted manually after testplugin is enabled"))
                                .build())
                .root().background("textures/gui/advancements/backgrounds/stone.png")
                .key(ResourceKey.of(this.plugin, "root"))
                .build();
        event.register(this.rootAdvancement);

        final AdvancementCriterion someDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(this.inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT)))
                        .build()
        ).name("some_dirt").build();

        final Advancement someDirt = Advancement.builder()
                .criterion(someDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got dirt!"))
                        .type(AdvancementTypes.TASK)
                        .build())
                .parent(this.rootAdvancement)
                .key(ResourceKey.of(this.plugin, "some_dirt"))
                .build();
        event.register(someDirt);

        final AdvancementCriterion lotsOfDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(this.inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64)))
                        .build()
        ).name("lots_of_dirt").build();

        final Advancement lotsOfDirt = Advancement.builder()
                .criterion(lotsOfDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got more dirt!"))
                        .type(AdvancementTypes.GOAL)
                        .build())
                .parent(someDirt)
                .key(ResourceKey.of(this.plugin, "lots_of_dirt"))
                .build();
        event.register(lotsOfDirt);

        final AdvancementCriterion tonsOfDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(this.inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64*9)))
                        .build()
        ).name("tons_of_dirt").build();

        final Advancement tonsOfDirt = Advancement.builder()
                .criterion(tonsOfDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got tons of dirt!"))
                        .type(AdvancementTypes.CHALLENGE)
                        .hidden(true)
                        .build())
                .parent(lotsOfDirt)
                .key(ResourceKey.of(this.plugin, "tons_of_dirt"))
                .build();
        event.register(tonsOfDirt);

        this.counter1 = ScoreAdvancementCriterion.builder().goal(10).name("counter").build();
        this.counter1Bypass = AdvancementCriterion.dummy();
        this.counterAdvancement1 = Advancement.builder()
                .criterion(OrCriterion.of(this.counter1, this.counter1Bypass))
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.CHEST)
                        .title(Component.text("Open some chests."))
                        .type(AdvancementTypes.GOAL)
                        .build())
                .parent(this.rootAdvancement)
                .key(ResourceKey.of(this.plugin, "counting"))
                .build();
        event.register(this.counterAdvancement1);

        this.counter2 = ScoreAdvancementCriterion.builder().goal(20).name("counter").build();
        this.counterAdvancement2 = Advancement.builder()
                .criterion(this.counter2)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.CHEST)
                        .title(Component.text("Open more chests"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .parent(this.counterAdvancement1)
                .key(ResourceKey.of(this.plugin, "counting_more"))
                .build();
        event.register(this.counterAdvancement2);

        final AdvancementCriterion a = AdvancementCriterion.builder().name("A").build();
        final AdvancementCriterion b = AdvancementCriterion.builder().name("B").build();
        final AdvancementCriterion c = AdvancementCriterion.builder().name("C").build();
        final AdvancementCriterion d = AdvancementCriterion.builder().name("D").build();
        final AdvancementCriterion e = AdvancementCriterion.builder().name("E").build();
        final AdvancementCriterion f = AdvancementCriterion.builder().name("F").build();
        final AdvancementCriterion combinationCriterion = OrCriterion.of(a, AndCriterion.of(b, OrCriterion.of(c, d)), AndCriterion.of(e, f));
        final Advancement combinationAdvancement = Advancement.builder()
                .criterion(combinationCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.CHEST)
                        .title(Component.text("A || (B & (C || D)) || (E & F)"))
                        .description(Component.text("ABE ABF ACDE ACDF"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .parent(this.counterAdvancement1)
                .key(ResourceKey.of(this.plugin, "combination"))
                .build();
        event.register(combinationAdvancement);
    }

    public class TriggerListeners {

        @Listener
        public void onContainerEvent(final ChangeInventoryEvent event, @First final ServerPlayer player) {
            AdvancementTest.this.inventoryChangeTrigger.trigger(player);
        }

        @Listener
        public void onConainterEvent(final InteractContainerEvent.Open event, @First final ServerPlayer player) {

            final AdvancementProgress progress1 = player.getProgress(AdvancementTest.this.counterAdvancement1);
            if (progress1.achieved()) {
                final AdvancementProgress progress2 = player.getProgress(AdvancementTest.this.counterAdvancement2);
                progress2.require(AdvancementTest.this.counter2).add(1);

            } else {
                progress1.require(AdvancementTest.this.counter1).add(1);
                final Object carrier = ((CarriedInventory) event.getContainer()).getCarrier().orElse(null);
                if (carrier instanceof BlockCarrier) {
                    if (((BlockCarrier) carrier).getLocation().getBlockType().isAnyOf(BlockTypes.TRAPPED_CHEST)) {
                        progress1.require(AdvancementTest.this.counter1Bypass).grant();
                    }
                }
            }
        }

    }

    public static class InventoryChangeTriggerConfig implements FilteredTriggerConfiguration, DataSerializable {
        private ItemStack stack;

        public InventoryChangeTriggerConfig(final ItemStack stack) {
            this.stack = stack;
        }

        public InventoryChangeTriggerConfig(final DataView stack) {
            this.stack = ItemStack.builder().fromContainer(stack).build();
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return this.stack.toContainer();
        }

        private static class Builder extends AbstractDataBuilder<InventoryChangeTriggerConfig> {

            public Builder() {
                super(InventoryChangeTriggerConfig.class, 1);
            }

            @Override
            protected Optional<InventoryChangeTriggerConfig> buildContent(final DataView container) throws InvalidDataException {
                return Optional.of(new InventoryChangeTriggerConfig(container));
            }
        }
    }

}
