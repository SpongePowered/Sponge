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
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryRegistrationSet;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("advancementtest")
public final class AdvancementTest implements LoadableModule {

    public static final String NAMESPACE = "advancementtest";

    public static final class Triggers {

        public static final class Holder {

            private static final RegistryRegistrationSet<Trigger<?>> TRIGGERS = Triggers.TRIGGER_BUILDER.build();
        }

        private static final RegistryRegistrationSet.Builder<Trigger<?>> TRIGGER_BUILDER = RegistryRegistrationSet.builder(RegistryTypes.TRIGGER, Sponge::game);

        public static final DefaultedRegistryReference<Trigger<InventoryChangeTriggerConfig>> INVENTORY_CHANGE_TRIGGER = Triggers.TRIGGER_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "my_inventory_trigger"),
            () -> Trigger.builder()
                .dataSerializableConfig(InventoryChangeTriggerConfig.class)
                .listener(triggerEvent -> {
                    final ItemStack stack = triggerEvent.trigger().configuration().stack;
                    final int found = triggerEvent.player().inventory().query(QueryTypes.ITEM_STACK_IGNORE_QUANTITY, stack).totalQuantity();
                    triggerEvent.setResult(stack.quantity() <= found);
                })
                .name("my_inventory_trigger")
                .build()
        );
    }

    public static final class AdvancementCriteria {

        public static final AdvancementCriterion SOME_DIRT = AdvancementCriterion.builder().trigger(
            Triggers.INVENTORY_CHANGE_TRIGGER.get(),
            FilteredTrigger.builder()
                .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT)))
                .build()).name("some_dirt").build();

        public static final AdvancementCriterion LOTS_OF_DIRT = AdvancementCriterion.builder().trigger(
            Triggers.INVENTORY_CHANGE_TRIGGER.get(),
            FilteredTrigger.builder()
                .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64)))
                .build()).name("lots_of_dirt").build();

        public static final AdvancementCriterion TONS_OF_DIRT = AdvancementCriterion.builder().trigger(
            Triggers.INVENTORY_CHANGE_TRIGGER.get(),
            FilteredTrigger.builder()
                .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64*9)))
                .build()).name("tons_of_dirt").build();

        public static final ScoreAdvancementCriterion COUNTER_1 = ScoreAdvancementCriterion.builder().goal(10).name("counter").build();
        public static final AdvancementCriterion COUNTER_1_BYPASS = AdvancementCriterion.dummy();

        public static final ScoreAdvancementCriterion COUNTER_2 = ScoreAdvancementCriterion.builder().goal(20).name("counter").build();

        public static final AdvancementCriterion A = AdvancementCriterion.builder().name("A").build();
        public static final AdvancementCriterion B = AdvancementCriterion.builder().name("B").build();
        public static final AdvancementCriterion C = AdvancementCriterion.builder().name("C").build();
        public static final AdvancementCriterion D = AdvancementCriterion.builder().name("D").build();
        public static final AdvancementCriterion E = AdvancementCriterion.builder().name("E").build();
        public static final AdvancementCriterion F = AdvancementCriterion.builder().name("F").build();

        public static final AdvancementCriterion COMBINATION = OrCriterion.of(
            AdvancementCriteria.A,
            AndCriterion.of(AdvancementCriteria.B, OrCriterion.of(AdvancementCriteria.C, AdvancementCriteria.D)),
            AndCriterion.of(AdvancementCriteria.E, AdvancementCriteria.F));
    }

    public static final class Advancements {

        public static final class Holder {

            private static final RegistryRegistrationSet<Advancement> ADVANCEMENTS = Advancements.ADVANCEMENT_BUILDER.build();
        }

        private static final RegistryRegistrationSet.Builder<Advancement> ADVANCEMENT_BUILDER = RegistryRegistrationSet.builder(RegistryTypes.ADVANCEMENT, Sponge::server);

        public static final DefaultedRegistryReference<Advancement> ROOT_ADVANCEMENT = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "root"),
            () -> Advancement.builder()
                .criterion(AdvancementCriterion.dummy())
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.COMMAND_BLOCK)
                    .title(Component.text("Advancement Tests"))
                    .description(Component.text("Dummy trigger. Granted manually after testplugin is enabled"))
                    .build())
                .root().background(ResourceKey.minecraft("textures/gui/advancements/backgrounds/stone.png"))
                .build());

        public static final DefaultedRegistryReference<Advancement> SOME_DIRT = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "some_dirt"),
            () -> Advancement.builder()
                .criterion(AdvancementCriteria.SOME_DIRT)
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.DIRT)
                    .title(Component.text("Got dirt!"))
                    .type(AdvancementTypes.TASK)
                    .build())
                .parent(Advancements.ROOT_ADVANCEMENT.location())
                .build());

        public static final DefaultedRegistryReference<Advancement> LOTS_OF_DIRT = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "lots_of_dirt"),
            () -> Advancement.builder()
                .criterion(AdvancementCriteria.LOTS_OF_DIRT)
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.DIRT)
                    .title(Component.text("Got more dirt!"))
                    .type(AdvancementTypes.GOAL)
                    .build())
                .parent(Advancements.SOME_DIRT.location())
                .build());

        public static final DefaultedRegistryReference<Advancement> TONS_OF_DIRT = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "tons_of_dirt"),
            () -> Advancement.builder()
                .criterion(AdvancementCriteria.TONS_OF_DIRT)
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.DIRT)
                    .title(Component.text("Got tons of dirt!"))
                    .type(AdvancementTypes.CHALLENGE)
                    .hidden(true)
                    .build())
                .parent(Advancements.LOTS_OF_DIRT.location())
                .build());

        public static final DefaultedRegistryReference<Advancement> COUNTER_1 = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "counting"),
            () -> Advancement.builder()
                .criterion(OrCriterion.of(AdvancementCriteria.COUNTER_1, AdvancementCriteria.COUNTER_1_BYPASS))
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.CHEST)
                    .title(Component.text("Open some chests."))
                    .type(AdvancementTypes.GOAL)
                    .build())
                .parent(Advancements.ROOT_ADVANCEMENT.location())
                .build());

        public static final DefaultedRegistryReference<Advancement> COUNTER_2 = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "counting_more"),
            () -> Advancement.builder()
                .criterion(AdvancementCriteria.COUNTER_2)
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.CHEST)
                    .title(Component.text("Open more chests"))
                    .type(AdvancementTypes.CHALLENGE)
                    .build())
                .parent(Advancements.COUNTER_1.location())
                .build());

        public static final DefaultedRegistryReference<Advancement> COMBINATION = Advancements.ADVANCEMENT_BUILDER.register(
            ResourceKey.of(AdvancementTest.NAMESPACE, "combination"),
            () -> Advancement.builder()
                .criterion(AdvancementCriteria.COMBINATION)
                .displayInfo(DisplayInfo.builder()
                    .icon(ItemTypes.CHEST)
                    .title(Component.text("A || (B & (C || D)) || (E & F)"))
                    .description(Component.text("ABE ABF ACDE ACDF"))
                    .type(AdvancementTypes.CHALLENGE)
                    .build())
                .parent(Advancements.COUNTER_1.location())
                .build());
    }

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    private boolean enabled = false;
    private TriggerListeners listeners = new TriggerListeners();

    @Override
    public void enable(final CommandContext ctx) {
        this.enabled = true;
        Sponge.eventManager().registerListeners(this.plugin, this.listeners);
        Sponge.server().dataPackManager().reload().join();
        ctx.cause().first(ServerPlayer.class).map(player -> player.progress(Advancements.ROOT_ADVANCEMENT.get()).grant());
    }

    @Override
    public void disable(final CommandContext ctx) {
        this.enabled = false;
        Sponge.eventManager().unregisterListeners(this.listeners);
        Sponge.server().dataPackManager().reload();
    }

    @Listener
    private void onTreeAdjust(final AdvancementTreeEvent.GenerateLayout event) {
        final AdvancementTree tree = event.tree();
        Advancements.ROOT_ADVANCEMENT.find(event.registryHolder()).filter(tree.rootAdvancement()::equals).ifPresent(advancement -> {
            final TreeLayoutElement layoutElement1 = tree.layoutElement(Advancements.COUNTER_1.get(event.registryHolder())).get();
            final TreeLayoutElement layoutElement2 = tree.layoutElement(Advancements.COUNTER_2.get(event.registryHolder())).get();
            layoutElement1.setPosition(layoutElement2.position());
            layoutElement2.setPosition(layoutElement2.position().add(-1,2));
        });
    }

    @Listener
    private void onGranted(final AdvancementEvent.Grant event) {
        this.logger.info("{} was granted", event.advancementKey());
    }

    @Listener
    private void onGranted(final AdvancementEvent.Revoke event) {
        this.logger.info("{} was revoked", event.advancementKey());
    }

    @Listener
    private void onTrigger(final CriterionEvent.Trigger<?> event) {
        this.logger.info("{} for {} was triggered", event.type().key(RegistryTypes.TRIGGER), event.advancementKey());
    }

    @Listener
    private void onTriggerRegistry(final RegisterDataEvent event) {
        Sponge.dataManager().registerBuilder(InventoryChangeTriggerConfig.class, new InventoryChangeTriggerConfig.Builder());
    }


    @Listener
    private void onRegisterRegistryValueEvent(final RegisterRegistryValueEvent event) {

        if (!this.enabled) {
            return;
        }

        event.register(Triggers.Holder.TRIGGERS);
        event.register(Advancements.Holder.ADVANCEMENTS);
    }

    class TriggerListeners {

        @Listener
        private void onContainerEvent(final ChangeInventoryEvent event, @First final ServerPlayer player) {
            Triggers.INVENTORY_CHANGE_TRIGGER.get().trigger(player);
        }

        @Listener
        private void onConainterEvent(final InteractContainerEvent.Open event, @First final ServerPlayer player) {

            final AdvancementProgress progress1 = player.progress(Advancements.COUNTER_1.get());
            if (progress1.achieved()) {
                final AdvancementProgress progress2 = player.progress(Advancements.COUNTER_2.get());
                progress2.require(AdvancementCriteria.COUNTER_2).add(1);

            } else {
                progress1.require(AdvancementCriteria.COUNTER_1).add(1);
                final Object carrier = ((CarriedInventory) event.container()).carrier().orElse(null);
                if (carrier instanceof BlockCarrier) {
                    if (((BlockCarrier) carrier).location().blockType().isAnyOf(BlockTypes.TRAPPED_CHEST)) {
                        progress1.require(AdvancementCriteria.COUNTER_1_BYPASS).grant();
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
        public int contentVersion() {
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
