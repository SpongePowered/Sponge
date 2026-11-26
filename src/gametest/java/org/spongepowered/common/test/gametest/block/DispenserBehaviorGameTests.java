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
package org.spongepowered.common.test.gametest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.test.gametest.EventRecorder;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.function.Consumer;

/**
 * Each dispense behavior, through the events the dispenser causes and in the order it causes them, and what is left
 * in the world and the dispenser's slot when one of those events is cancelled.
 *
 * <p>A cancelled event rolls back what it covers and what followed from it, never what it followed from. A spawn or
 * block change cancelled as the phase unwinds leaves the dispenser's item used up, while cancelling the dispenser's
 * slot change rolls the whole dispense back. The tests that do not get there yet are optional, each with the known
 * issue it shows: changes to an entity's data, such as a saddle or equipment, are not transactions, so cancelling
 * the slot change cannot undo them, and no {@link ChangeEntityEquipmentEvent} is caused by a dispenser.</p>
 *
 * <p>The {@code sponge:dispenser/behavior/*} structures are a bedrock floor with a south-facing dispenser at (1,1,1),
 * so its front is (1,1,2) and the test powers it with a redstone block behind it at (1,1,0):</p>
 * <ul>
 *     <li>{@code front_air}: nothing else. Entities a behavior acts on are spawned at the front by the test.</li>
 *     <li>{@code front_walled}: glass at (0,1,2), (2,1,2) and (1,1,3), so placed water stays one source.</li>
 *     <li>{@code front_water}: as {@code front_walled}, with a water source at the front.</li>
 *     <li>{@code front_wheat}: age 0 wheat at the front, on moist farmland at (1,0,2).</li>
 *     <li>{@code front_respawn_anchor}, {@code front_copper}, {@code front_dirt}, {@code front_rail}: that block at the
 *     front, the rail running north to south.</li>
 *     <li>{@code golem} and {@code wither_base}: the dispenser at (1,3,1) instead, facing (1,3,2). {@code golem} has
 *     snow blocks at (1,1,2) and (1,2,2); {@code wither_base} has a wither's soul sand T with no skulls, its stem at
 *     (1,1,2) and its arms from (0,2,2) to (2,2,2).</li>
 * </ul>
 */
@GameTestGroup("dispenser_behavior")
public final class DispenserBehaviorGameTests {

    private static final String AIR = "sponge:dispenser/behavior/front_air";
    private static final String WALLED = "sponge:dispenser/behavior/front_walled";
    private static final String WATER = "sponge:dispenser/behavior/front_water";
    private static final String WHEAT = "sponge:dispenser/behavior/front_wheat";
    private static final String ANCHOR = "sponge:dispenser/behavior/front_respawn_anchor";
    private static final String COPPER = "sponge:dispenser/behavior/front_copper";
    private static final String DIRT = "sponge:dispenser/behavior/front_dirt";
    private static final String RAIL = "sponge:dispenser/behavior/front_rail";
    private static final String GOLEM = "sponge:dispenser/behavior/golem";
    private static final String WITHER = "sponge:dispenser/behavior/wither_base";
    private static final BlockPos DISPENSER = new BlockPos(1, 1, 1);
    private static final BlockPos FRONT = new BlockPos(1, 1, 2);
    private static final BlockPos RAISED = new BlockPos(1, 3, 1);
    private static final BlockPos RAISED_FRONT = new BlockPos(1, 3, 2);

    private static final String INVENTORY = "ChangeInventoryEvent";
    private static final String CANCELLED = " [cancelled]";
    private static final List<String> SPAWN = List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "SpawnEntityEvent");
    private static final List<String> SPAWN_CANCELLED =
        List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "SpawnEntityEvent" + DispenserBehaviorGameTests.CANCELLED);
    private static final List<String> SPAWN_SLOT_CANCELLED =
        List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED);
    private static final List<String> BLOCK = List.of(DispenserBehaviorGameTests.INVENTORY, "All", "NotifyNeighbor", "Post");
    private static final List<String> BLOCK_CANCELLED = List.of(DispenserBehaviorGameTests.INVENTORY, "All" + DispenserBehaviorGameTests.CANCELLED);
    private static final List<String> SLOT_ONLY = List.of(DispenserBehaviorGameTests.INVENTORY);
    private static final List<String> SLOT_ONLY_CANCELLED = List.of(DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED);
    // An entity dropping an item: its own Pre events, then the slot change, then the drop
    private static final List<String> ENTITY_DROP = List.of("DropItemEvent.Pre", "ConstructEntityEvent.Pre", "SpawnEntityEvent.Pre",
        DispenserBehaviorGameTests.INVENTORY, "DropItemEvent.Dispense");

    // DefaultDispenseItemBehavior: the Pre comes before the item leaves the slot, so cancelling it means nothing happens

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void drop(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.DIAMOND), null,
            List.of("DropItemEvent.Pre", "SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "DropItemEvent.Dispense"), slot -> {
                GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The diamond is still in the dispenser: " + slot);
                helper.assertEntityPresent(EntityTypes.ITEM);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void dropCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.DIAMOND), DropItemEvent.Pre.class,
            List.of("DropItemEvent.Pre" + DispenserBehaviorGameTests.CANCELLED), slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.DIAMOND), "The dispenser holds " + slot + ", not the diamond");
                helper.assertEntityNotPresent(EntityTypes.ITEM);
            });
    }

    // ProjectileDispenseBehavior

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void projectile(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARROW), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The arrow is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.ARROW);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void projectileCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARROW), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED, slot -> {
            helper.assertEntityNotPresent(EntityTypes.ARROW);
            DispenserBehaviorGameTests.assertUsedUp(helper, slot);
        });
    }

    // Spawn eggs

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void spawnEgg(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.PIG_SPAWN_EGG), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The egg is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.PIG);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void spawnEggCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.PIG_SPAWN_EGG), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.PIG);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // Armor stand

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void armorStand(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARMOR_STAND), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The armor stand is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.ARMOR_STAND);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void armorStandCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARMOR_STAND), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.ARMOR_STAND);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // Saddle, horse armor and chest, on a tamed animal in front

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void saddle(final GameTestHelper helper) {
        final AbstractHorse horse = DispenserBehaviorGameTests.tamed(helper, EntityTypes.HORSE);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SADDLE), null, DispenserBehaviorGameTests.SLOT_ONLY, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The saddle is still in the dispenser: " + slot);
            GameTestAssertions.assertTrue(helper, horse.isSaddled(), "The horse was not saddled");
        });
    }

    // Known issue: the saddle is back in the slot and on the horse
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void saddleCancelled(final GameTestHelper helper) {
        final AbstractHorse horse = DispenserBehaviorGameTests.tamed(helper, EntityTypes.HORSE);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SADDLE), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.SADDLE), "The dispenser holds " + slot + ", not the saddle");
                GameTestAssertions.assertFalse(helper, horse.isSaddled(), "The horse was saddled");
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void horseArmor(final GameTestHelper helper) {
        final AbstractHorse horse = DispenserBehaviorGameTests.tamed(helper, EntityTypes.HORSE);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.IRON_HORSE_ARMOR), null, DispenserBehaviorGameTests.SLOT_ONLY, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The horse armor is still in the dispenser: " + slot);
            GameTestAssertions.assertTrue(helper, horse.isWearingBodyArmor(), "The horse is not wearing the armor");
        });
    }

    // Known issue: the armor is back in the slot and on the horse
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void horseArmorCancelled(final GameTestHelper helper) {
        final AbstractHorse horse = DispenserBehaviorGameTests.tamed(helper, EntityTypes.HORSE);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.IRON_HORSE_ARMOR), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.IRON_HORSE_ARMOR), "The dispenser holds " + slot + ", not the horse armor");
                GameTestAssertions.assertFalse(helper, horse.isWearingBodyArmor(), "The horse is wearing the armor");
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void chest(final GameTestHelper helper) {
        final Donkey donkey = DispenserBehaviorGameTests.tamed(helper, EntityTypes.DONKEY);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.CHEST), null, DispenserBehaviorGameTests.SLOT_ONLY, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The chest is still in the dispenser: " + slot);
            GameTestAssertions.assertTrue(helper, donkey.hasChest(), "The donkey has no chest");
        });
    }

    // Known issue: the chest is back in the slot and on the donkey
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void chestCancelled(final GameTestHelper helper) {
        final Donkey donkey = DispenserBehaviorGameTests.tamed(helper, EntityTypes.DONKEY);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.CHEST), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.CHEST), "The dispenser holds " + slot + ", not the chest");
                GameTestAssertions.assertFalse(helper, donkey.hasChest(), "The donkey has a chest");
            });
    }

    // BoatDispenseItemBehavior

    // Known issue: NeoForge's patched BoatDispenseItemBehavior#execute spawns an extra boat before its fluid check
    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40, required = false)
    void boat(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.OAK_BOAT), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The boat is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.OAK_BOAT);
        });
    }

    // Known issue: NeoForge's patched BoatDispenseItemBehavior#execute spawns an extra boat before its fluid check
    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40, required = false)
    void boatCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.OAK_BOAT), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED, slot -> {
            helper.assertEntityNotPresent(EntityTypes.OAK_BOAT);
            DispenserBehaviorGameTests.assertUsedUp(helper, slot);
        });
    }

    // Filled buckets

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void waterBucket(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.WATER_BUCKET), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.is(Items.BUCKET), "The dispenser holds " + slot + ", not the emptied bucket");
            helper.assertBlockPresent(Blocks.WATER, DispenserBehaviorGameTests.FRONT);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void waterBucketCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.WATER_BUCKET), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                GameTestAssertions.assertTrue(helper, slot.is(Items.BUCKET), "The dispenser holds " + slot + ", not the emptied bucket");
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void fishBucket(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.COD_BUCKET), null,
            List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "All", "NotifyNeighbor", "SpawnEntityEvent", "Post"), slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.BUCKET), "The dispenser holds " + slot + ", not the emptied bucket");
                helper.assertBlockPresent(Blocks.WATER, DispenserBehaviorGameTests.FRONT);
                helper.assertEntityPresent(EntityTypes.COD);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void fishBucketCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.COD_BUCKET), SpawnEntityEvent.class,
            List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "All", "NotifyNeighbor", "SpawnEntityEvent" + DispenserBehaviorGameTests.CANCELLED,
                "Post"), slot -> {
                helper.assertEntityNotPresent(EntityTypes.COD);
                helper.assertBlockPresent(Blocks.WATER, DispenserBehaviorGameTests.FRONT);
                GameTestAssertions.assertTrue(helper, slot.is(Items.BUCKET), "The dispenser holds " + slot + ", not the emptied bucket");
            });
    }

    // Empty bucket

    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40)
    void bucket(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BUCKET), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.is(Items.WATER_BUCKET), "The dispenser holds " + slot + ", not the filled bucket");
            helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
        });
    }

    // The cancelled pickup does not reach back to the slot change it followed from, so the bucket is filled all the same
    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40)
    void bucketCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BUCKET), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED, slot -> {
            helper.assertBlockPresent(Blocks.WATER, DispenserBehaviorGameTests.FRONT);
            GameTestAssertions.assertTrue(helper, slot.is(Items.WATER_BUCKET), "The dispenser holds " + slot + ", not the filled bucket");
        });
    }

    // Flint and steel

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void flintAndSteel(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.FLINT_AND_STEEL), null,
            List.of(DispenserBehaviorGameTests.INVENTORY, "All", "ScheduleBlockUpdate", "NotifyNeighbor", "Post"), slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.FLINT_AND_STEEL) && slot.getDamageValue() == 1, "The dispenser holds " + slot + ", not the used flint and steel");
                helper.assertBlockPresent(Blocks.FIRE, DispenserBehaviorGameTests.FRONT);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void flintAndSteelCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.FLINT_AND_STEEL), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                GameTestAssertions.assertTrue(helper, slot.is(Items.FLINT_AND_STEEL) && slot.getDamageValue() == 1, "The dispenser holds " + slot + ", not the used flint and steel");
            });
    }

    // Bone meal: the crop grows in a phase of its own, so its events come before the slot change

    @GameTest(structure = DispenserBehaviorGameTests.WHEAT, maxTicks = 40)
    void boneMeal(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BONE_MEAL), null, List.of("All", "Post", DispenserBehaviorGameTests.INVENTORY), slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The bone meal is still in the dispenser: " + slot);
            helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, CropBlock.AGE, age -> age > 0, Component.literal("grown"));
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WHEAT, maxTicks = 40)
    void boneMealCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BONE_MEAL), ChangeBlockEvent.All.class,
            List.of("All" + DispenserBehaviorGameTests.CANCELLED, DispenserBehaviorGameTests.INVENTORY), slot -> {
                helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, CropBlock.AGE, 0);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // TNT

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void tnt(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.TNT), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The TNT is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.TNT);
            // Before it goes off
            helper.getEntities(EntityTypes.TNT).forEach(PrimedTnt::discard);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void tntCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.TNT), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED, slot -> {
            helper.assertEntityNotPresent(EntityTypes.TNT);
            DispenserBehaviorGameTests.assertUsedUp(helper, slot);
        });
    }

    // Heads and armor, onto an armor stand in front

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void head(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityTypes.ARMOR_STAND, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SKELETON_SKULL), null, DispenserBehaviorGameTests.SLOT_ONLY, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The skull is still in the dispenser: " + slot);
            GameTestAssertions.assertTrue(helper, stand.getItemBySlot(EquipmentSlot.HEAD).is(Items.SKELETON_SKULL), "The armor stand is not wearing the skull");
        });
    }

    // Known issue: the skull is back in the slot and on the armor stand
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void headCancelled(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityTypes.ARMOR_STAND, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SKELETON_SKULL), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.SKELETON_SKULL), "The dispenser holds " + slot + ", not the skull");
                GameTestAssertions.assertTrue(helper, stand.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "The armor stand is wearing the skull");
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void armor(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityTypes.ARMOR_STAND, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.IRON_CHESTPLATE), null, DispenserBehaviorGameTests.SLOT_ONLY, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The chestplate is still in the dispenser: " + slot);
            GameTestAssertions.assertTrue(helper, stand.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE), "The armor stand is not wearing the chestplate");
        });
    }

    // Known issue: the chestplate is back in the slot and on the armor stand
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void armorCancelled(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityTypes.ARMOR_STAND, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.IRON_CHESTPLATE), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.IRON_CHESTPLATE), "The dispenser holds " + slot + ", not the chestplate");
                GameTestAssertions.assertTrue(helper, stand.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), "The armor stand is wearing the chestplate");
            });
    }

    // Wither skeleton skull, placed on a wither's base; with no other skulls on it, no wither spawns

    @GameTest(structure = DispenserBehaviorGameTests.WITHER, maxTicks = 40)
    void witherSkull(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.WITHER_SKELETON_SKULL), null,
            DispenserBehaviorGameTests.BLOCK, slot -> {
                GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The skull is still in the dispenser: " + slot);
                helper.assertBlockPresent(Blocks.WITHER_SKELETON_SKULL, DispenserBehaviorGameTests.RAISED_FRONT);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WITHER, maxTicks = 40)
    void witherSkullCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.WITHER_SKELETON_SKULL), ChangeBlockEvent.All.class,
            DispenserBehaviorGameTests.BLOCK_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // Carved pumpkin, completing a snow golem

    @GameTest(structure = DispenserBehaviorGameTests.GOLEM, maxTicks = 40)
    void carvedPumpkin(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.CARVED_PUMPKIN), null,
            List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "All", "All", "SpawnEntityEvent", "NotifyNeighbor", "Post"), slot -> {
                GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The pumpkin is still in the dispenser: " + slot);
                helper.assertEntityPresent(EntityTypes.SNOW_GOLEM);
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT.below());
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.GOLEM, maxTicks = 40)
    void carvedPumpkinCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.CARVED_PUMPKIN), SpawnEntityEvent.class,
            List.of("SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY, "All", "All", "SpawnEntityEvent" + DispenserBehaviorGameTests.CANCELLED,
                "NotifyNeighbor", "Post"), slot -> {
                helper.assertEntityNotPresent(EntityTypes.SNOW_GOLEM);
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT.below());
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // ShulkerBoxDispenseBehavior

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void shulkerBox(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SHULKER_BOX), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The shulker box is still in the dispenser: " + slot);
            helper.assertBlockPresent(Blocks.SHULKER_BOX, DispenserBehaviorGameTests.FRONT);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void shulkerBoxCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SHULKER_BOX), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // Glass bottle, filled from the water in front, which it leaves in place

    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40)
    void glassBottle(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.GLASS_BOTTLE), null, DispenserBehaviorGameTests.SLOT_ONLY, slot ->
            GameTestAssertions.assertTrue(helper, slot.is(Items.POTION), "The dispenser holds " + slot + ", not the water bottle"));
    }

    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40)
    void glassBottleCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.GLASS_BOTTLE), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> GameTestAssertions.assertTrue(helper, slot.is(Items.GLASS_BOTTLE), "The dispenser holds " + slot + ", not the glass bottle"));
    }

    // Glowstone, charging a respawn anchor

    @GameTest(structure = DispenserBehaviorGameTests.ANCHOR, maxTicks = 40)
    void glowstone(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.GLOWSTONE), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The glowstone is still in the dispenser: " + slot);
            helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, RespawnAnchorBlock.CHARGE, 1);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.ANCHOR, maxTicks = 40)
    void glowstoneCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.GLOWSTONE), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED,
            slot -> {
                helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, RespawnAnchorBlock.CHARGE, 0);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // ShearsDispenseItemBehavior, on a snow golem in front, which always drops exactly one pumpkin

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void shears(final GameTestHelper helper) {
        final SnowGolem golem = helper.spawnWithNoFreeWill(EntityTypes.SNOW_GOLEM, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SHEARS), null, DispenserBehaviorGameTests.ENTITY_DROP, slot -> {
            GameTestAssertions.assertTrue(helper, slot.is(Items.SHEARS) && slot.getDamageValue() == 1, "The dispenser holds " + slot + ", not the used shears");
            GameTestAssertions.assertFalse(helper, golem.hasPumpkin(), "The snow golem still has its pumpkin");
            helper.assertEntityPresent(EntityTypes.ITEM);
        });
    }

    // Known issue: the snow golem stays sheared
    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40, required = false)
    void shearsCancelled(final GameTestHelper helper) {
        final SnowGolem golem = helper.spawnWithNoFreeWill(EntityTypes.SNOW_GOLEM, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SHEARS), ChangeInventoryEvent.class,
            List.of("DropItemEvent.Pre", "ConstructEntityEvent.Pre", "SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED),
            slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.SHEARS) && slot.getDamageValue() == 0, "The dispenser holds " + slot + ", not the unused shears");
                helper.assertEntityNotPresent(EntityTypes.ITEM);
                GameTestAssertions.assertTrue(helper, golem.hasPumpkin(), "The snow golem was sheared");
            });
    }

    // Brush, on an armadillo in front

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void brush(final GameTestHelper helper) {
        helper.spawnWithNoFreeWill(EntityTypes.ARMADILLO, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BRUSH), null, DispenserBehaviorGameTests.ENTITY_DROP, slot -> {
            GameTestAssertions.assertTrue(helper, slot.is(Items.BRUSH) && slot.getDamageValue() > 0, "The dispenser holds " + slot + ", not the used brush");
            helper.assertEntityPresent(EntityTypes.ITEM);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void brushCancelled(final GameTestHelper helper) {
        helper.spawnWithNoFreeWill(EntityTypes.ARMADILLO, DispenserBehaviorGameTests.FRONT);
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BRUSH), ChangeInventoryEvent.class,
            List.of("DropItemEvent.Pre", "ConstructEntityEvent.Pre", "SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED),
            slot -> {
                GameTestAssertions.assertTrue(helper, slot.is(Items.BRUSH) && slot.getDamageValue() == 0, "The dispenser holds " + slot + ", not the unused brush");
                helper.assertEntityNotPresent(EntityTypes.ITEM);
            });
    }

    // Honeycomb, waxing copper in front

    @GameTest(structure = DispenserBehaviorGameTests.COPPER, maxTicks = 40)
    void honeycomb(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.HONEYCOMB), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The honeycomb is still in the dispenser: " + slot);
            helper.assertBlockPresent(Blocks.COPPER_BLOCK.waxed().unaffected(), DispenserBehaviorGameTests.FRONT);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.COPPER, maxTicks = 40)
    void honeycombCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.HONEYCOMB), ChangeBlockEvent.All.class, DispenserBehaviorGameTests.BLOCK_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.COPPER_BLOCK.weathering().unaffected(), DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertUsedUp(helper, slot);
            });
    }

    // Water bottle, turning dirt in front to mud

    @GameTest(structure = DispenserBehaviorGameTests.DIRT, maxTicks = 40)
    void waterBottle(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, PotionContents.createItemStack(Items.POTION, Potions.WATER), null, DispenserBehaviorGameTests.BLOCK, slot -> {
            GameTestAssertions.assertTrue(helper, slot.is(Items.GLASS_BOTTLE), "The dispenser holds " + slot + ", not the emptied bottle");
            helper.assertBlockPresent(Blocks.MUD, DispenserBehaviorGameTests.FRONT);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.DIRT, maxTicks = 40)
    void waterBottleCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, PotionContents.createItemStack(Items.POTION, Potions.WATER), ChangeBlockEvent.All.class,
            DispenserBehaviorGameTests.BLOCK_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.DIRT, DispenserBehaviorGameTests.FRONT);
                GameTestAssertions.assertTrue(helper, slot.is(Items.GLASS_BOTTLE), "The dispenser holds " + slot + ", not the emptied bottle");
            });
    }

    // Minecart, onto the rail in front

    @GameTest(structure = DispenserBehaviorGameTests.RAIL, maxTicks = 40)
    void minecart(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.MINECART), null, DispenserBehaviorGameTests.SPAWN, slot -> {
            GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The minecart is still in the dispenser: " + slot);
            helper.assertEntityPresent(EntityTypes.MINECART);
        });
    }

    @GameTest(structure = DispenserBehaviorGameTests.RAIL, maxTicks = 40)
    void minecartCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.MINECART), SpawnEntityEvent.class, DispenserBehaviorGameTests.SPAWN_CANCELLED, slot -> {
            helper.assertEntityNotPresent(EntityTypes.MINECART);
            DispenserBehaviorGameTests.assertUsedUp(helper, slot);
        });
    }

    // Cancelling the slot change: the parent of everything the behavior did, so all of it is rolled back with it

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void dropSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.DIAMOND), ChangeInventoryEvent.class,
            List.of("DropItemEvent.Pre", "SpawnEntityEvent.Pre", DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED), slot -> {
                helper.assertEntityNotPresent(EntityTypes.ITEM);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.DIAMOND);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void projectileSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARROW), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.ARROW);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.ARROW);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void spawnEggSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.PIG_SPAWN_EGG), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED, slot -> {
                helper.assertEntityNotPresent(EntityTypes.PIG);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.PIG_SPAWN_EGG);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void armorStandSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.ARMOR_STAND), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED, slot -> {
                helper.assertEntityNotPresent(EntityTypes.ARMOR_STAND);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.ARMOR_STAND);
            });
    }

    // Known issue: NeoForge's patched BoatDispenseItemBehavior#execute spawns an extra boat before its fluid check
    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40, required = false)
    void boatSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.OAK_BOAT), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.OAK_BOAT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.OAK_BOAT);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void waterBucketSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.WATER_BUCKET), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.WATER_BUCKET);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WALLED, maxTicks = 40)
    void fishBucketSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.COD_BUCKET), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED, slot -> {
                helper.assertEntityNotPresent(EntityTypes.COD);
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.COD_BUCKET);
            });
    }

    // What a protection plugin cancels to stop a bucket being filled from water it may not take
    @GameTest(structure = DispenserBehaviorGameTests.WATER, maxTicks = 40)
    void bucketSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BUCKET), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.WATER, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.BUCKET);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void flintAndSteelSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.FLINT_AND_STEEL), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                GameTestAssertions.assertTrue(helper, slot.is(Items.FLINT_AND_STEEL) && slot.getDamageValue() == 0, "The dispenser holds " + slot + ", not the unused flint and steel");
            });
    }

    // Known issue: the crop grows in its own phase, already unwound by the slot change, so it stays grown
    @GameTest(structure = DispenserBehaviorGameTests.WHEAT, maxTicks = 40, required = false)
    void boneMealSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.BONE_MEAL), ChangeInventoryEvent.class,
            List.of("All", "Post", DispenserBehaviorGameTests.INVENTORY + DispenserBehaviorGameTests.CANCELLED), slot -> {
                helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, CropBlock.AGE, 0);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.BONE_MEAL);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void tntSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.TNT), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.TNT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.TNT);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.WITHER, maxTicks = 40)
    void witherSkullSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.WITHER_SKELETON_SKULL), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.WITHER_SKELETON_SKULL);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.GOLEM, maxTicks = 40)
    void carvedPumpkinSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.RAISED, new ItemStack(Items.CARVED_PUMPKIN), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED, slot -> {
                helper.assertEntityNotPresent(EntityTypes.SNOW_GOLEM);
                helper.assertBlockPresent(Blocks.SNOW_BLOCK, DispenserBehaviorGameTests.RAISED_FRONT.below());
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.RAISED_FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.CARVED_PUMPKIN);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.AIR, maxTicks = 40)
    void shulkerBoxSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.SHULKER_BOX), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.AIR, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.SHULKER_BOX);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.ANCHOR, maxTicks = 40)
    void glowstoneSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.GLOWSTONE), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> {
                helper.assertBlockProperty(DispenserBehaviorGameTests.FRONT, RespawnAnchorBlock.CHARGE, 0);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.GLOWSTONE);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.COPPER, maxTicks = 40)
    void honeycombSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.HONEYCOMB), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED,
            slot -> {
                helper.assertBlockPresent(Blocks.COPPER_BLOCK.weathering().unaffected(), DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.HONEYCOMB);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.DIRT, maxTicks = 40)
    void waterBottleSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, PotionContents.createItemStack(Items.POTION, Potions.WATER), ChangeInventoryEvent.class,
            DispenserBehaviorGameTests.SLOT_ONLY_CANCELLED, slot -> {
                helper.assertBlockPresent(Blocks.DIRT, DispenserBehaviorGameTests.FRONT);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.POTION);
            });
    }

    @GameTest(structure = DispenserBehaviorGameTests.RAIL, maxTicks = 40)
    void minecartSlotChangeCancelled(final GameTestHelper helper) {
        DispenserBehaviorGameTests.dispense(helper, new ItemStack(Items.MINECART), ChangeInventoryEvent.class, DispenserBehaviorGameTests.SPAWN_SLOT_CANCELLED,
            slot -> {
                helper.assertEntityNotPresent(EntityTypes.MINECART);
                DispenserBehaviorGameTests.assertKept(helper, slot, Items.MINECART);
            });
    }

    private static void assertKept(final GameTestHelper helper, final ItemStack slot, final Item item) {
        GameTestAssertions.assertTrue(helper, slot.is(item) && slot.getCount() == 1, "The dispenser holds " + slot + ", not its " + item);
    }

    // A cancelled spawn or block change does not reach back to the slot change it followed from
    private static void assertUsedUp(final GameTestHelper helper, final ItemStack slot) {
        GameTestAssertions.assertTrue(helper, slot.isEmpty(), "The cancel gave the dispenser back " + slot);
    }

    private static <E extends AbstractHorse> E tamed(final GameTestHelper helper, final EntityType<E> type) {
        final E animal = helper.spawnWithNoFreeWill(type, DispenserBehaviorGameTests.FRONT);
        animal.setTamed(true);
        return animal;
    }

    private static void dispense(final GameTestHelper helper, final ItemStack stack, final @Nullable Class<? extends Cancellable> cancel,
            final List<String> expected, final Consumer<ItemStack> outcome) {
        DispenserBehaviorGameTests.dispense(helper, DispenserBehaviorGameTests.DISPENSER, stack, cancel, expected, outcome);
    }

    /**
     * Loads {@code stack} into the dispenser at {@code at}, powers it from behind and, once it has fired, asserts the
     * events it caused, in order and with {@code " [cancelled]"} on each cancelled one, then {@code outcome} against
     * what is left in its slot. With {@code cancel}, every event of exactly that API type the dispenser causes is
     * cancelled.
     */
    private static void dispense(final GameTestHelper helper, final BlockPos at, final ItemStack stack,
            final @Nullable Class<? extends Cancellable> cancel, final List<String> expected, final Consumer<ItemStack> outcome) {
        final DispenserBlockEntity dispenser = helper.getBlockEntity(at, DispenserBlockEntity.class);
        dispenser.setItem(0, stack);
        final EventRecorder recorder = EventRecorder.start(helper)
            // No longer a SpawnEntityEvent, so the recorder leaves it out unless asked
            .alsoRecord(SpawnEntityEvent.Pre.class)
            .alsoRecord(DropItemEvent.Pre.class)
            .alsoRecord(ChangeInventoryEvent.class)
            .alsoRecord(ChangeEntityEquipmentEvent.class)
            .alsoRecord(ConstructEntityEvent.Pre.class);
        final Vector3i absolute = VecHelper.toVector3i(helper.absolutePos(at));
        final EventListener<Event> canceller = event -> {
            if (event.getClass().getInterfaces()[0] == cancel
                && event.cause().allOf(LocatableBlock.class).stream().anyMatch(block -> block.blockPosition().equals(absolute))) {
                ((Cancellable) event).setCancelled(true);
            }
        };
        if (cancel != null) {
            SpongeGameTests.listen(Event.class, canceller);
        }
        final String causedBy = "(" + at.getX() + "," + at.getY() + "," + at.getZ() + ")";

        helper.setBlock(at.north(), Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenIdle(8)
            .thenExecute(() -> {
                recorder.stop();
                Sponge.eventManager().unregisterListeners(canceller);
                final List<EventRecorder.Entry> caused = recorder.entries().stream().filter(entry -> entry.cause().endsWith(causedBy)).toList();
                final List<String> events = caused.stream()
                    .map(entry -> entry.event() + (entry.cancelled() ? DispenserBehaviorGameTests.CANCELLED : ""))
                    .toList();
                GameTestAssertions.assertTrue(helper, events.equals(expected), "Expected " + expected + " but got " + caused);
                outcome.accept(dispenser.getItem(0));
            })
            .thenSucceed();
    }
}
