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
import org.slf4j.Logger;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
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
import org.spongepowered.api.text.Text;

@Plugin(id = "advancement_test", name = "Advancement Test")
public class AdvancementTest {

    @Inject private Logger logger;

    private Advancement rootAdvancement;

    private ScoreAdvancementCriterion breakDirtCriterion;
    private Advancement breakDirtAdvancement;

    private Advancement cookDirtAdvancement;

    @Listener
    public void onRegisterAdvancementTrees(GameRegistryEvent.Register<AdvancementTree> event) {
        this.logger.info("Loading advancement trees...");
        // Create the advancement tree
        event.register(AdvancementTree.builder()
                .rootAdvancement(this.rootAdvancement)
                .background("minecraft:textures/blocks/dirt.png")
                .build("advancement_test", "dirt"));
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
                .build("advancement_test", "dirt");
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
                .build("advancement_test", "dirt_digger");
        event.register(this.breakDirtAdvancement);

        // Create the cook dirt advancement
        this.cookDirtAdvancement = Advancement.builder()
                .parent(this.rootAdvancement)
                .criterion(AdvancementCriterion.DUMMY)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.FURNACE)
                        .title(Text.of("Dirty cook"))
                        .description(Text.of("Cooking dirt? What could possibly go wrong?"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .build("advancement_test", "dirt_cooker");
        event.register(this.cookDirtAdvancement);
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.Break event, @First Player player) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.getFinal().getState().getType() == BlockTypes.AIR &&
                    (transaction.getOriginal().getState().getType() == BlockTypes.DIRT ||
                            transaction.getOriginal().getState().getType() == BlockTypes.GRASS)) {
                System.out.println("DEBUG");
                player.getProgress(this.breakDirtAdvancement).get(this.breakDirtCriterion).get().add(1);
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
            if (transaction.getSlot().getInventoryProperty(SlotIndex.class).get().getValue() == 0 &&
                    transaction.getFinal().getType() == ItemTypes.DIRT) {
                player.getProgress(this.cookDirtAdvancement).grant();
                break;
            }
        }
    }
}
