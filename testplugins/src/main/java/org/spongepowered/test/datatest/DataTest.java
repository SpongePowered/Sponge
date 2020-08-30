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
package org.spongepowered.test.datatest;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.EndGateway;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.ArmorMaterials;
import org.spongepowered.api.data.type.ArtTypes;
import org.spongepowered.api.data.type.AttachmentSurfaces;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.FoxTypes;
import org.spongepowered.api.data.type.MooshroomTypes;
import org.spongepowered.api.data.type.PandaGenes;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.ProfessionTypes;
import org.spongepowered.api.data.type.RailDirections;
import org.spongepowered.api.data.type.SlabPortions;
import org.spongepowered.api.data.type.SpellTypes;
import org.spongepowered.api.data.type.StairShapes;
import org.spongepowered.api.data.type.ToolTypes;
import org.spongepowered.api.data.type.TropicalFishShapes;
import org.spongepowered.api.data.type.VillagerTypes;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.data.type.WoodTypes;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.EnderCrystal;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.animal.Turtle;
import org.spongepowered.api.entity.living.monster.Patroller;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

@Plugin("datatest")
public final class DataTest  {

    private final PluginContainer plugin;

    @Inject
    public DataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                .setExecutionRequirements(cc -> cc.first(ServerPlayer.class).isPresent())
                .setExecutor(context -> {
                    this.testData(context.getCause().first(ServerPlayer.class).get());
                    return CommandResult.success();
                })
                .build()
        , "datatest");
    }


    public void testData(final ServerPlayer player) {
        final ServerWorld world = player.getWorld();
        final Vector3d position = player.getPosition();
        final Vector3i blockPos = position.toInt();

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

        // TODO missing impl
//        final Entity minecart = world.createEntity(EntityTypes.MINECART.get(), position);
//        this.checkGetData(minecart, Keys.AIRBORNE_VELOCITY_MODIFIER, Vector3d.ZERO);
//        this.checkOfferData(minecart, Keys.AIRBORNE_VELOCITY_MODIFIER, Vector3d.UP);

        final Entity zombiePigman = world.createEntity(EntityTypes.ZOMBIE_PIGMAN.get(), position);
        this.checkGetData(zombiePigman, Keys.ANGER_LEVEL, 0);
        this.checkOfferData(zombiePigman, Keys.ANGER_LEVEL, 10);

        final ItemStack goldenApple = ItemStack.of(ItemTypes.ENCHANTED_GOLDEN_APPLE);
        final List<PotionEffect> notchAppleEffects = Arrays.asList(
                PotionEffect.builder().potionType(PotionEffectTypes.REGENERATION).amplifier(1).ambient(false).duration(400).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.RESISTANCE).amplifier(0).ambient(false).duration(6000).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.FIRE_RESISTANCE).amplifier(0).ambient(false).duration(6000).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION).amplifier(3).ambient(false).duration(2400).build());
        this.checkGetWeightedData(goldenApple, Keys.APPLICABLE_POTION_EFFECTS, notchAppleEffects);

        this.checkOfferListData(goldenApple, Keys.APPLIED_ENCHANTMENTS, Arrays.asList(Enchantment.of(EnchantmentTypes.SHARPNESS, 5)));
        this.checkOfferListData(goldenApple, Keys.APPLIED_ENCHANTMENTS, Arrays.asList(Enchantment.of(EnchantmentTypes.PROTECTION, 4)));

        this.checkGetData(ItemStack.of(ItemTypes.DIAMOND_LEGGINGS), Keys.ARMOR_MATERIAL, ArmorMaterials.DIAMOND.get());
        this.checkGetData(ItemStack.of(ItemTypes.LEATHER_BOOTS), Keys.ARMOR_MATERIAL, ArmorMaterials.LEATHER.get());
        this.checkGetData(ItemStack.of(ItemTypes.TURTLE_HELMET), Keys.ARMOR_MATERIAL, ArmorMaterials.TURTLE.get());

        final Entity painting = world.createEntity(EntityTypes.PAINTING.get(), position);
        this.checkGetData(painting, Keys.ART_TYPE, ArtTypes.KEBAB.get()); // TODO test offer (only works on valid painting)

        world.setBlock(blockPos, BlockTypes.LAPIS_BLOCK.get().getDefaultState());
        world.setBlock(blockPos.add(0, 0, -1), BlockTypes.LEVER.get().getDefaultState());
        this.checkGetData(world.getBlock(blockPos), Keys.ATTACHMENT_SURFACE, null);
        BlockState leverState = world.getBlock(blockPos.add(0, 0, -1));
        this.checkWithData(leverState, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.WALL.get());
        this.checkWithData(leverState, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.FLOOR.get());
        world.setBlock(blockPos, BlockTypes.AIR.get().getDefaultState());
        world.setBlock(blockPos.add(0, 0, -1), BlockTypes.AIR.get().getDefaultState());

        // TODO         Keys.ATTACK_DAMAGE

        final Entity ravager = world.createEntity(EntityTypes.RAVAGER.get(), position);
        this.checkGetData(ravager, Keys.ATTACK_TIME, 0);
        this.checkOfferData(ravager, Keys.ATTACK_TIME, 5);

        final ItemStack writtenBookStack = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        this.checkOfferData(writtenBookStack, Keys.AUTHOR, TextComponent.of("You"));

        BlockState logState = BlockTypes.OAK_LOG.get().getDefaultState();
        this.checkWithData(logState, Keys.AXIS, Axis.Y);
        this.checkWithData(logState, Keys.AXIS, Axis.X);

        this.checkOfferData(sheep, Keys.BABY_TICKS, 50);
        this.checkOfferData(sheep, Keys.BABY_TICKS, 0);

        final List<BannerPatternLayer> pattern = Arrays.asList(BannerPatternLayer.of(BannerPatternShapes.BASE, DyeColors.BLACK), BannerPatternLayer.of(BannerPatternShapes.RHOMBUS, DyeColors.ORANGE));

        final ItemStack shieldStack = ItemStack.of(ItemTypes.SHIELD);
        this.checkGetListData(shieldStack, Keys.BANNER_PATTERN_LAYERS, Collections.emptyList());
        this.checkOfferListData(shieldStack, Keys.BANNER_PATTERN_LAYERS,pattern);

        final ItemStack bannerStack = ItemStack.of(ItemTypes.RED_BANNER);
        this.checkGetListData(bannerStack, Keys.BANNER_PATTERN_LAYERS,  Collections.emptyList());
        this.checkOfferListData(bannerStack, Keys.BANNER_PATTERN_LAYERS, pattern);

// TODO NPE when generating event
//        world.setBlock(blockPos, BlockTypes.RED_BANNER.get().getDefaultState());
//        final BlockEntity blockEntity = world.getBlockEntity(blockPos).get();
//        this.checkOfferListData(blockEntity, Keys.BANNER_PATTERN_LAYERS, pattern);

        this.checkGetData(sheep, Keys.BASE_SIZE, (double)0.9f);
        this.checkGetData(player, Keys.BASE_SIZE, (double)0.6f);

        final Entity donkey = world.createEntity(EntityTypes.DONKEY.get(), position);
        final Entity wolf = world.createEntity(EntityTypes.WOLF.get(), position);
        final Entity cat = world.createEntity(EntityTypes.CAT.get(), position);
        final Entity chicken = world.createEntity(EntityTypes.CHICKEN.get(), position);
        wolf.offer(Keys.VEHICLE, donkey);
        cat.offer(Keys.VEHICLE, wolf);
        chicken.offer(Keys.VEHICLE, cat);
        this.checkGetData(chicken, Keys.BASE_VEHICLE, donkey);

        final Entity guardian = world.createEntity(EntityTypes.GUARDIAN.get(), position);
        this.checkOfferData(guardian, Keys.BEAM_TARGET_ENTITY, player);

// TODO World.get(IIIKey) is abstract
//        final ServerLocation location = world.getLocation(blockPos);
//        this.checkGetData(location, Keys.BIOME_TEMPERATURE, world.getBiome(blockPos).getTemperature());

        final BlockState obisidanState = BlockTypes.OBSIDIAN.get().getDefaultState();
        this.checkGetData(obisidanState, Keys.BLAST_RESISTANCE, 1200.0);
        final BlockState dirtState = BlockTypes.DIRT.get().getDefaultState();
        this.checkGetData(dirtState, Keys.BLAST_RESISTANCE, 0.5);
        final BlockState bricksState = BlockTypes.BRICKS.get().getDefaultState();
        this.checkGetData(bricksState, Keys.BLAST_RESISTANCE, 6.0);

        // TODO Keys.BLOCK_LIGHT

        final Entity fallingBlock = world.createEntity(EntityTypes.FALLING_BLOCK.get(), position);
        final BlockState sandState = BlockTypes.SAND.get().getDefaultState();
        this.checkOfferData(fallingBlock, Keys.BLOCK_STATE, sandState);

        // TODO Keys.BLOCK_TEMPERATURE

// TODO missing BodyPart registration
        final Entity armorStand = world.createEntity(EntityTypes.ARMOR_STAND.get(), position);
//        armorStand.offer(Keys.BODY_ROTATIONS, ImmutableMap.of(BodyParts.CHEST.get(), Vector3d.RIGHT));
//        world.spawnEntity(armorStand);

        // TODO wither.get(Keys.BOSS_BAR)

        final ItemStack jungleAxe = ItemStack.of(ItemTypes.WOODEN_AXE);
        this.checkGetSetData(jungleAxe, Keys.BREAKABLE_BLOCK_TYPES, null);
        this.checkOfferSetData(jungleAxe, Keys.BREAKABLE_BLOCK_TYPES, new HashSet<>(Arrays.asList(BlockTypes.COCOA.get(), BlockTypes.JUNGLE_LEAVES.get())));

        this.checkGetData(sheep, Keys.BREEDER, null);
        this.checkOfferData(sheep, Keys.BREEDER, player.getUniqueId());

        this.checkGetData(sheep, Keys.BREEDING_COOLDOWN, 0);
        this.checkOfferData(sheep, Keys.BREEDING_COOLDOWN, 100);

        this.checkGetData(jungleAxe, Keys.BURN_TIME, 200);
        this.checkGetData(ItemStack.of(ItemTypes.COAL), Keys.BURN_TIME, 1600);

        this.checkGetData(sheep, Keys.CAN_BREED, false); // Breeding CD = 100 from above
        this.checkOfferData(sheep, Keys.CAN_BREED, true);

        this.checkGetData(fallingBlock, Keys.CAN_DROP_AS_ITEM, true);
        this.checkOfferData(fallingBlock, Keys.CAN_DROP_AS_ITEM, false);

        this.checkOfferData(player, Keys.CAN_FLY, true);
        this.checkOfferData(player, Keys.CAN_FLY, true);

        // TODO missing GrieferBridge?
//        final Entity creeper = world.createEntity(EntityTypes.CREEPER.get(), position);
//        this.checkGetData(creeper, Keys.CAN_GRIEF, true);
//        this.checkOfferData(creeper, Keys.CAN_GRIEF, false);

        // TODO thats a long list of blocktypes
        final Optional<Set<BlockType>> blockTypes = jungleAxe.get(Keys.CAN_HARVEST);

        this.checkGetData(fallingBlock, Keys.CAN_HURT_ENTITIES, false);
        this.checkOfferData(fallingBlock, Keys.CAN_HURT_ENTITIES, true);

        // TODO maybe only when actually falling
//        final Entity fallingAnvil = world.createEntity(EntityTypes.FALLING_BLOCK.get(), position);
//        fallingAnvil.offer(Keys.BLOCK_STATE, BlockTypes.ANVIL.get().getDefaultState());
//        this.checkGetData(fallingAnvil, Keys.CAN_HURT_ENTITIES, true);


        this.checkOfferData(ravager, Keys.CAN_JOIN_RAID, true);
        this.checkOfferData(ravager, Keys.CAN_JOIN_RAID, false);

        // TODO missing data provider
        final Entity boat = world.createEntity(EntityTypes.BOAT.get(), position);
        this.checkOfferData(boat, Keys.CAN_MOVE_ON_LAND, true);
        this.checkOfferData(boat, Keys.CAN_MOVE_ON_LAND, false);

        this.checkGetData(fallingBlock, Keys.CAN_PLACE_AS_BLOCK, true);
        this.checkOfferData(fallingBlock, Keys.CAN_PLACE_AS_BLOCK, false);

        final Entity illusioner = world.createEntity(EntityTypes.ILLUSIONER.get(), position);
        this.checkGetData(illusioner, Keys.CASTING_TIME, 0);

// TODO missing provider?
//        this.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.ALL_BLACK.get());
//        this.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.JELLIE.get());
//        this.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.WHITE.get());

        // TODO
//        world.setBlock(blockPos, BlockTypes.CHEST.get().getDefaultState());
//        final Optional<ChestAttachmentType> chestAttachmentType = world.get(blockPos, Keys.CHEST_ATTACHMENT_TYPE);
//        world.setBlock(blockPos.add(0, 0, 1), BlockTypes.CHEST.get().getDefaultState());

        // TODO Keys.CHEST_ROTATION

        final ItemStack leatherBoots = ItemStack.of(ItemTypes.LEATHER_BOOTS);
        final ItemStack potion = ItemStack.of(ItemTypes.POTION);
        final ItemStack splashPotion = ItemStack.of(ItemTypes.SPLASH_POTION);
        this.checkOfferData(leatherBoots, Keys.COLOR, Color.BLACK);
        this.checkOfferData(potion, Keys.COLOR, Color.WHITE);
        this.checkOfferData(splashPotion, Keys.COLOR, Color.RED);

        // TODO COMMAND

        // TODO NPE in event?
//        world.setBlock(blockPos, BlockTypes.COMPARATOR.get().getDefaultState());
//        BlockState comparator = world.getBlock(blockPos);
//        this.checkGetData(comparator, Keys.COMPARATOR_MODE, ComparatorModes.COMPARE.get());
//        comparator = comparator.with(Keys.COMPARATOR_MODE, ComparatorModes.SUBTRACT.get()).get();
//        this.checkGetData(comparator, Keys.COMPARATOR_MODE, ComparatorModes.SUBTRACT.get());

        // TODO Keys.CONNECTED_DIRECTIONS

        final ItemStack waterBucket = ItemStack.of(ItemTypes.WATER_BUCKET);
        this.checkGetData(waterBucket, Keys.CONTAINER_ITEM, ItemTypes.BUCKET.get());

        // TODO Keys.COOLDOWN missing dataprovider?
//        world.setBlock(blockPos, BlockTypes.HOPPER.get().getDefaultState());
//        this.checkGetData(world.getBlockEntity(blockPos).get(), Keys.COOLDOWN, 0);

        // TODO Keys.CREATOR

// TODO missing spelltype NONE or return null
//        this.checkGetData(illusioner, Keys.CURRENT_SPELL, SpellTypes.NONE.get());
        this.checkOfferData(illusioner, Keys.CURRENT_SPELL, SpellTypes.BLINDNESS.get());

        // TODO Keys.CUSTOM_ATTACK_DAMAGE

        this.checkGetData(leatherBoots, Keys.DAMAGE_ABSORPTION, 1.0);
        this.checkGetData(ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE), Keys.DAMAGE_ABSORPTION, 8.0);

        this.checkGetData(fallingBlock, Keys.DAMAGE_PER_BLOCK, 2.0);
        this.checkOfferData(fallingBlock, Keys.DAMAGE_PER_BLOCK, 5.0);

        BlockState leavesState = BlockTypes.ACACIA_LEAVES.get().getDefaultState();
        this.checkGetData(leavesState, Keys.DECAY_DISTANCE, 7);
        this.checkWithData(leavesState, Keys.DECAY_DISTANCE, 2);

        // TODO Keys.DERAILED_VELOCITY_MODIFIER

        // TODO missing ItemEntityBridge
        // TODO also other dataholders
//        final Entity itemEntity = world.createEntity(EntityTypes.ITEM.get(), position);
//        this.checkGetData(itemEntity, Keys.DESPAWN_DELAY, 6000);

        final Entity tntEntity = world.createEntity(EntityTypes.TNT.get(), position);
        this.checkGetData(tntEntity, Keys.DETONATOR, null);
        this.checkOfferData(tntEntity, Keys.DETONATOR, player);

        // TODO Keys.DIRECTION for other dataholders
        this.checkGetData(painting, Keys.DIRECTION, Direction.SOUTH);
        // TODO offer fails
//        this.checkOfferData(painting, Keys.DIRECTION, Direction.NORTH);
//        this.checkOfferData(painting, Keys.DIRECTION, Direction.UP);
// TODO missing provider?
//        final Entity shulkerEntity = world.createEntity(EntityTypes.SHULKER.get(), position);
//        this.checkGetData(shulkerEntity, Keys.DIRECTION, Direction.SOUTH);
//        this.checkOfferData(shulkerEntity, Keys.DIRECTION, Direction.NORTH);
//        this.checkOfferData(shulkerEntity, Keys.DIRECTION, Direction.UP);
        final Entity shulkerBullet = world.createEntity(EntityTypes.SHULKER_BULLET.get(), position);
        this.checkGetData(shulkerBullet, Keys.DIRECTION, Direction.NONE);
        this.checkOfferData(shulkerBullet, Keys.DIRECTION, Direction.NORTH);
        this.checkOfferData(shulkerBullet, Keys.DIRECTION, Direction.UP);

        final BlockState acaciaStairs = BlockTypes.ACACIA_STAIRS.get().getDefaultState();
        this.checkGetData(acaciaStairs, Keys.DIRECTION, Direction.NORTH);
        this.checkWithData(acaciaStairs, Keys.DIRECTION, Direction.WEST);

        this.checkOfferData(jungleAxe, Keys.DISPLAY_NAME, TextComponent.of("Jungle Axe"));
        // TODO bridge$getDisplayNameTex is abstract
        //        this.checkOfferData(shulkerBullet, Keys.DISPLAY_NAME, TextComponent.of("Angry Shulker Bullet"));
        // TODO BlockEntity DisplayName

        player.get(Keys.DOMINANT_HAND).get();

        // TODO Keys.DOOR_HINGE

        // TODO Keys.DO_EXACT_TELEPORT

        final Entity areaEffectCloud = world.createEntity(EntityTypes.AREA_EFFECT_CLOUD.get(), position);
        this.checkOfferData(areaEffectCloud, Keys.DURATION, 50);
        this.checkOfferData(areaEffectCloud, Keys.DURATION_ON_USE, -1); // TODO does it work?
        world.spawnEntity(areaEffectCloud);

// TODO this is broken
//        this.checkOfferData(cat, Keys.DYE_COLOR, DyeColors.LIME.get());
//        this.checkGetData(ItemStack.of(ItemTypes.RED_WOOL), Keys.DYE_COLOR, DyeColors.RED.get());
//        this.checkGetData(bannerStack, Keys.DYE_COLOR, DyeColors.RED.get());
//        this.checkGetData(BlockTypes.BLUE_CONCRETE.get().getDefaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
//        // TODO banner blockentity
        final Entity tropicalFish = world.createEntity(EntityTypes.TROPICAL_FISH.get(), position);
//        this.checkOfferData(tropicalFish, Keys.DYE_COLOR, DyeColors.CYAN.get());

        final Entity panda = world.createEntity(EntityTypes.PANDA.get(), position);
        this.checkOfferData(panda, Keys.EATING_TIME, 10);

        this.checkGetData(jungleAxe, Keys.EFFICIENCY, 2.0);
        this.checkGetData(ItemStack.of(ItemTypes.DIAMOND_SHOVEL), Keys.EFFICIENCY, 8.0);

        this.checkOfferData(chicken, Keys.EGG_TIME, 0);
        this.checkOfferData(chicken, Keys.EGG_TIME, 5000);

        // TODO Keys.END_GATEWAY_AGE

        // Keys.EQUIPMENT_TYPE is for inventories
        this.checkOfferData(player, Keys.EXHAUSTION, 1.0);

// TODO bridge$refreshExp is Abstract
//        this.checkOfferData(player, Keys.EXPERIENCE, 0);
//        this.checkGetData(player, Keys.EXPERIENCE_FROM_START_OF_LEVEL, 0);
//        this.checkOfferData(player, Keys.EXPERIENCE_LEVEL, 1);
//        this.checkOfferData(player, Keys.EXPERIENCE_SINCE_LEVEL, 1);

//        this.checkOfferData(tntEntity, Keys.EXPLOSION_RADIUS, 1);

        this.checkGetData(player, Keys.EYE_HEIGHT, (double)1.62f);
        this.checkGetData(sheep, Keys.EYE_HEIGHT,  (double)(1.3f * 0.95f));

        this.checkGetData(sheep, Keys.EYE_POSITION, position.add(0, (double) (1.3f * 0.95f), 0));

//        this.checkGetData(fallingBlock, Keys.FALL_DISTANCE, 0.0);
//        this.checkOfferData(fallingBlock, Keys.FALL_DISTANCE, 20.0);

        this.checkGetData(fallingBlock, Keys.FALL_TIME, 0);
        this.checkOfferData(fallingBlock, Keys.FALL_TIME, 20);

// TODO missing FireworkShapes supplier
        final ItemStack fireworkStar = ItemStack.of(ItemTypes.FIREWORK_STAR);
        final ItemStack fireworkRocket = ItemStack.of(ItemTypes.FIREWORK_ROCKET);
        final Entity rocket = world.createEntity(EntityTypes.FIREWORK_ROCKET.get(), position);
//        final List<FireworkEffect> fireworkEffects = Arrays.asList(FireworkEffect.builder().color(Color.RED).build());
//        this.checkOfferListData(fireworkStar, Keys.FIREWORK_EFFECTS, fireworkEffects);
//        this.checkOfferListData(fireworkRocket, Keys.FIREWORK_EFFECTS, fireworkEffects);
//        this.checkOfferListData(rocket, Keys.FIREWORK_EFFECTS, fireworkEffects);
        world.spawnEntity(rocket);

        this.checkOfferData(rocket, Keys.FIREWORK_FLIGHT_MODIFIER, 5);

        // TODO bridge$setFireImmunityTicks is abstract
//        this.checkOfferData(sheep, Keys.FIRE_DAMAGE_DELAY, 20000);

        this.checkOfferData(sheep, Keys.FIRE_TICKS, 10);

        this.checkOfferData(player, Keys.FIRST_DATE_JOINED, Instant.now().minus(1, TemporalUnits.DAYS));

        final Entity fox = world.createEntity(EntityTypes.FOX.get(), position);
        this.checkOfferData(fox, Keys.FIRST_TRUSTED, player.getUniqueId());

// TODO missing dataprovider
//        this.checkGetData(waterBucket, Keys.FLUID_ITEM_STACK, FluidStackSnapshot.builder().fluid(FluidTypes.WATER).build());

        final BlockState waterBlockState = BlockTypes.WATER.get().getDefaultState();
        this.checkGetData(waterBlockState, Keys.FLUID_LEVEL, 8);

        // TODO Keys.FLUID_TANK_CONTENTS

        this.checkGetData(player, Keys.FLYING_SPEED, (double)0.05f);

        this.checkOfferData(player, Keys.FOOD_LEVEL, 0);
        this.checkOfferData(player, Keys.FOOD_LEVEL, 20);

        this.checkOfferData(fox, Keys.FOX_TYPE, FoxTypes.RED.get());
        this.checkOfferData(fox, Keys.FOX_TYPE, FoxTypes.SNOW.get());

        final Entity furnaceMinecart = world.createEntity(EntityTypes.FURNACE_MINECART.get(), position);
        this.checkOfferData(furnaceMinecart, Keys.FUEL, 10);
        // TODO BrewingStand/FurnaceBlockEntity Keys.FUEL

// TODO missing FusedExplosiveBridge
//        this.checkOfferData(tntEntity, Keys.FUSE_DURATION, 0);

        final GameMode gameMode = player.get(Keys.GAME_MODE).orElse(GameModes.CREATIVE.get());
        this.checkOfferData(player, Keys.GAME_MODE, GameModes.SURVIVAL.get());
        this.checkOfferData(player, Keys.GAME_MODE, GameModes.ADVENTURE.get());
        this.checkOfferData(player, Keys.GAME_MODE, GameModes.CREATIVE.get());
        player.offer(Keys.GAME_MODE, gameMode);

        final ItemStack playerHeadStack = ItemStack.of(ItemTypes.PLAYER_HEAD);
        this.checkOfferData(playerHeadStack, Keys.GAME_PROFILE, player.getProfile());
        // TODO Block Keys.GAME_PROFILE

        this.checkGetData(writtenBookStack, Keys.GENERATION, 0);
        this.checkOfferData(writtenBookStack, Keys.GENERATION, 2);

        final BlockState melonStemState = BlockTypes.MELON_STEM.get().getDefaultState();
        final BlockState cactusState = BlockTypes.CACTUS.get().getDefaultState();
        this.checkGetData(melonStemState, Keys.GROWTH_STAGE, 0);
        this.checkWithData(melonStemState, Keys.GROWTH_STAGE, 4);
        this.checkGetData(cactusState, Keys.GROWTH_STAGE, 0);
        this.checkWithData(cactusState, Keys.GROWTH_STAGE, 4);


        this.checkGetData(obisidanState, Keys.HARDNESS, 50.0);
        this.checkGetData(dirtState, Keys.HARDNESS, 0.5);
        this.checkGetData(bricksState, Keys.HARDNESS, 2.0);

        this.checkOfferData(armorStand, Keys.HAS_ARMS, true);
        this.checkOfferData(armorStand, Keys.HAS_ARMS, false);

        this.checkGetData(armorStand, Keys.HAS_BASE_PLATE, true);
        this.checkOfferData(armorStand, Keys.HAS_BASE_PLATE, false);

        this.checkGetData(donkey, Keys.HAS_CHEST, false);
        this.checkOfferData(donkey, Keys.HAS_CHEST, true);

        final Entity turtle = world.createEntity(EntityTypes.TURTLE.get(), position);
        this.checkOfferData(turtle, Keys.HAS_EGG, true);

        final Entity dolphin = world.createEntity(EntityTypes.DOLPHIN.get(), position);
        this.checkOfferData(dolphin, Keys.HAS_FISH, true);

        this.checkOfferData(armorStand, Keys.HAS_MARKER, true);

        final BlockState mushroomBlockState = BlockTypes.BROWN_MUSHROOM_BLOCK.get().getDefaultState();
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_DOWN, true);
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_EAST, true);
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_NORTH, true);
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_SOUTH, true);
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_UP, true);
        this.checkGetData(mushroomBlockState, Keys.HAS_PORES_WEST, true);

        this.checkOfferData(player, Keys.HAS_VIEWED_CREDITS, true);

        // TODO armorStand Keys.HEAD_ROTATION
        this.checkOfferData(sheep, Keys.HEAD_ROTATION, position);

        // TODO Keys.HEALING_CRYSTAL

// TODO bridge$resetDeathEventsPosted?
//        this.checkOfferData(player, Keys.HEALTH, 1.0);
//        this.checkOfferData(player, Keys.HEALTH, 20.0);

// TODO bridge$isHealthScaled
//        this.checkOfferData(player, Keys.HEALTH_SCALE, 1.0);

        this.checkGetData(player, Keys.HEIGHT, (double) 1.8f);

        this.checkGetData(obisidanState, Keys.HELD_ITEM, ItemTypes.OBSIDIAN.get());
        this.checkGetData(waterBlockState, Keys.HELD_ITEM, null);

        this.checkOfferData(panda, Keys.HIDDEN_GENE, PandaGenes.WEAK.get());

        this.checkOfferData(jungleAxe, Keys.HIDE_ATTRIBUTES, true);
        this.checkOfferData(jungleAxe, Keys.HIDE_CAN_DESTROY, true);

        final ItemStack stoneStack = ItemStack.of(ItemTypes.STONE);
        this.checkOfferData(stoneStack, Keys.HIDE_CAN_PLACE, true);

        this.checkOfferData(jungleAxe, Keys.HIDE_ENCHANTMENTS, true);

        this.checkOfferData(shieldStack, Keys.HIDE_MISCELLANEOUS, true);

        this.checkOfferData(jungleAxe, Keys.HIDE_UNBREAKABLE, true);

        this.checkOfferData(turtle, Keys.HOME_POSITION, blockPos);

        final Entity horse = world.createEntity(EntityTypes.HORSE.get(), position);
// TODO HorseColor unregistered
//        this.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.BLACK.get());
//        this.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.WHITE.get());
//        this.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.DARK_BROWN.get());
//        this.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.NONE.get());
//        this.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.WHITE.get());
//        this.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.BLACK_DOTS.get());

        final Entity itemEntity = world.createEntity(EntityTypes.ITEM.get(), position);
// TODO missing ItemEntityBridge?
//        this.checkOfferData(itemEntity, Keys.INFINITE_DESPAWN_DELAY, true);
//        this.checkOfferData(itemEntity, Keys.INFINITE_DESPAWN_DELAY, false);
//        this.checkOfferData(itemEntity, Keys.INFINITE_PICKUP_DELAY, true);
//        world.spawnEntity(itemEntity);

// TODO InstrumentTypes unregistered
        //        this.checkGetData(dirtState, Keys.INSTRUMENT_TYPE, InstrumentTypes.XYLOPHONE.get());

        final BlockState daylightDetectorState = BlockTypes.DAYLIGHT_DETECTOR.get().getDefaultState();
        this.checkGetData(daylightDetectorState, Keys.INVERTED, false);
        this.checkWithData(daylightDetectorState, Keys.INVERTED, true);

        this.checkOfferData(sheep, Keys.INVULNERABILITY_TICKS, 20);

// TODO bridge$getIsInvulnerable is abstract
//        this.checkOfferData(sheep, Keys.INVULNERABLE, true);
//        this.checkOfferData(sheep, Keys.INVULNERABLE, false);

        final BlockState fenceGateState = BlockTypes.ACACIA_FENCE_GATE.get().getDefaultState();
        this.checkGetData(fenceGateState, Keys.IN_WALL, false);
        this.checkWithData(fenceGateState, Keys.IN_WALL, true);

        this.checkOfferData(sheep, Keys.IS_ADULT, false);
        this.checkOfferData(sheep, Keys.IS_ADULT, true);

        final Entity blaze = world.createEntity(EntityTypes.BLAZE.get(), position);
        this.checkOfferData(blaze, Keys.IS_AFLAME, true);

        this.checkOfferData(blaze, Keys.IS_AI_ENABLED, true);
        this.checkOfferData(blaze, Keys.IS_AI_ENABLED, false);

// TODO AggressiveEntityBridge
//        this.checkOfferData(wolf, Keys.IS_ANGRY, true);
//        this.checkOfferData(wolf, Keys.IS_ANGRY, false);
//        this.checkOfferData(zombiePigman, Keys.IS_ANGRY, true);
//        this.checkOfferData(zombiePigman, Keys.IS_ANGRY, false);

        final BlockState torchState = BlockTypes.TORCH.get().getDefaultState();
        this.checkGetData(torchState, Keys.IS_ATTACHED, false);
        this.checkGetData(dirtState, Keys.IS_ATTACHED, null);

// TODO provider
//        this.checkOfferData(cat, Keys.IS_BEGGING_FOR_FOOD, true);
//        this.checkOfferData(wolf, Keys.IS_BEGGING_FOR_FOOD, true);

        this.checkOfferData(ravager, Keys.IS_CELEBRATING, true);

        final Entity creeper = world.createEntity(EntityTypes.CREEPER.get(), position);
        this.checkOfferData(creeper, Keys.IS_CHARGED, true);
        this.checkOfferData(creeper, Keys.IS_CHARGED, false);

        final Entity pillager = world.createEntity(EntityTypes.PILLAGER.get(), position);
        this.checkOfferData(pillager, Keys.IS_CHARGING_CROSSBOW, true);

        final Entity spider = world.createEntity(EntityTypes.SPIDER.get(), position);
        this.checkOfferData(spider, Keys.IS_CLIMBING, true);

        final BlockState fenceState = BlockTypes.ACACIA_FENCE.get().getDefaultState();

        this.checkWithData(fenceState, Keys.IS_CONNECTED_EAST, true);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_NORTH, false);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_SOUTH, true);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_WEST, false);

        final BlockState wallState = BlockTypes.ANDESITE_WALL.get().getDefaultState();
        this.checkWithData(fenceState, Keys.IS_CONNECTED_EAST, true);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_NORTH, false);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_SOUTH, true);
        this.checkWithData(fenceState, Keys.IS_CONNECTED_WEST, false);
        this.checkWithData(wallState, Keys.IS_CONNECTED_UP, true);

        final BlockState vineState = BlockTypes.VINE.get().getDefaultState();
        this.checkWithData(vineState, Keys.IS_CONNECTED_UP, false);
        this.checkWithData(vineState, Keys.IS_CONNECTED_UP, true);

        // TODO Keys.IS_CRITICAL_HIT

        this.checkOfferData(fox, Keys.IS_CROUCHING, true);

        this.checkOfferData(sheep, Keys.IS_CUSTOM_NAME_VISIBLE, false);

// TODO provider
//        this.checkOfferData(fox, Keys.IS_DEFENDING, true);

        final BlockState tripWireState = BlockTypes.TRIPWIRE.get().getDefaultState();
        this.checkWithData(tripWireState, Keys.IS_DISARMED, true);

        this.checkOfferData(panda, Keys.IS_EATING, true);

        // TODO Keys.IS_EFFECT_ONLY

        this.checkOfferData(player, Keys.IS_ELYTRA_FLYING, true);
        this.checkOfferData(player, Keys.IS_ELYTRA_FLYING, false);

        final BlockState pistonState = BlockTypes.PISTON.get().getDefaultState();
        this.checkWithData(pistonState, Keys.IS_EXTENDED, true);

        this.checkOfferData(fox, Keys.IS_FACEPLANTED, true);

        this.checkGetData(dirtState, Keys.IS_FLAMMABLE, false);
        this.checkGetData(bricksState, Keys.IS_FLAMMABLE, false);
        this.checkGetData(leavesState, Keys.IS_FLAMMABLE, true);
        this.checkGetData(fenceState, Keys.IS_FLAMMABLE, true);

        this.checkOfferData(player, Keys.IS_FLYING, true);
        this.checkOfferData(sheep, Keys.IS_FLYING, true);

        this.checkGetData(panda, Keys.IS_FRIGHTENED, false);

        // TODO Keys.IS_FULL_BLOCK

        this.checkOfferData(sheep, Keys.IS_GLOWING, true);

        this.checkOfferData(turtle, Keys.IS_GOING_HOME, true);

        this.checkGetData(dirtState, Keys.IS_GRAVITY_AFFECTED, false);
        this.checkGetData(sandState, Keys.IS_GRAVITY_AFFECTED, true);

// TODO provider
//        this.checkOfferData(cat, Keys.IS_HISSING, true);

        this.checkGetData(ravager, Keys.IS_IMMOBILIZED, true);

        // TODO Keys.IS_INDIRECTLY_POWERED

        this.checkOfferData(fox, Keys.IS_INTERESTED, true);

        this.checkGetData(boat, Keys.IS_IN_WATER, false);

        final Entity vindicator = world.createEntity(EntityTypes.VINDICATOR.get(), position);
        this.checkOfferData(vindicator, Keys.IS_JOHNNY, true);

        this.checkOfferData(turtle, Keys.IS_LAYING_EGG, true);

        this.checkOfferData(vindicator, Keys.IS_LEADER, true);

        final BlockState furnaceState = BlockTypes.FURNACE.get().getDefaultState();
        final BlockState campfireState = BlockTypes.CAMPFIRE.get().getDefaultState();
        final BlockState redstoneTorchState = BlockTypes.REDSTONE_TORCH.get().getDefaultState();
        this.checkWithData(furnaceState, Keys.IS_LIT, false);
        this.checkWithData(furnaceState, Keys.IS_LIT, true);
        this.checkWithData(campfireState, Keys.IS_LIT, false);
        this.checkWithData(campfireState, Keys.IS_LIT, true);
        this.checkWithData(redstoneTorchState, Keys.IS_LIT, false);
        this.checkWithData(redstoneTorchState, Keys.IS_LIT, true);

//        this.checkOfferData(cat, Keys.IS_LYING_DOWN, true);

        final BlockState bedState = BlockTypes.BLACK_BED.get().getDefaultState();
        this.checkWithData(bedState, Keys.IS_OCCUPIED, true);
        this.checkWithData(bedState, Keys.IS_OCCUPIED, false);

        this.checkGetData(furnaceMinecart, Keys.IS_ON_RAIL, false);

        this.checkWithData(fenceGateState, Keys.IS_OPEN, true);
        this.checkWithData(fenceGateState, Keys.IS_OPEN, false);

        this.checkGetData(waterBlockState, Keys.IS_PASSABLE, true);
        this.checkGetData(dirtState, Keys.IS_PASSABLE, false);

        this.checkOfferData(vindicator, Keys.IS_PATROLLING, true);

        this.checkOfferData(sheep, Keys.IS_PERSISTENT, true);
        this.checkOfferData(vindicator, Keys.IS_PERSISTENT, false);

//        this.check...(armorStand, Keys.IS_PLACING_DISABLED, ...);

        final Entity ironGolem = world.createEntity(EntityTypes.IRON_GOLEM.get(), position);
        this.checkOfferData(ironGolem, Keys.IS_PLAYER_CREATED, true);

        this.checkOfferData(fox, Keys.IS_POUNCING, true);

//        this.checkWithData(leverState, Keys.IS_POWERED, true);
//        this.checkWithData(leverState, Keys.IS_POWERED, false);

//        this.checkOfferData(tntEntity, Keys.IS_PRIMED, true);
//        this.checkOfferData(tntEntity, Keys.IS_PRIMED, false);

//        this.checkOfferData(cat, Keys.IS_PURRING, true);
//        this.checkOfferData(cat, Keys.IS_RELAXED, true);

        this.checkGetData(waterBlockState, Keys.IS_REPLACEABLE, true);
        this.checkGetData(dirtState, Keys.IS_REPLACEABLE, false);

//        this.checkGetData(ravager, Keys.IS_ROARING, false);

        this.checkOfferData(panda, Keys.IS_ROLLING_AROUND, true);
// TODO AbstractHorseEntityBridge
//        this.checkOfferData(horse, Keys.IS_SADDLED, true);

        final Entity enderman = world.createEntity(EntityTypes.ENDERMAN.get(), position);
        this.checkOfferData(enderman, Keys.IS_SCREAMING, true);

        this.checkOfferData(sheep, Keys.IS_SHEARED, true);

        this.checkOfferData(sheep, Keys.IS_SILENT, true);

        this.checkOfferData(wolf, Keys.IS_SITTING, true);
        this.checkOfferData(cat, Keys.IS_SITTING, true);
//        this.checkOfferData(panda, Keys.IS_SITTING, true);
//        this.checkOfferData(fox, Keys.IS_SITTING, true);

        final Entity bat = world.createEntity(EntityTypes.BAT.get(), position);
        this.checkOfferData(bat, Keys.IS_SLEEPING, true);
        this.checkOfferData(fox, Keys.IS_SLEEPING, true);
//        this.checkGetData(player, Keys.IS_SLEEPING, true);

//        this.checkOfferData(player, Keys.IS_SLEEPING_IGNORED, true);

        this.checkOfferData(armorStand, Keys.IS_SMALL, true);

        this.checkGetData(player, Keys.IS_SNEAKING, false);

        this.checkOfferData(panda, Keys.IS_SNEEZING, true);

//        this.checkWithData(dirtState, Keys.IS_SNOWY, true);

        this.checkGetData(dirtState, Keys.IS_SOLID, true);
        this.checkGetData(obisidanState, Keys.IS_SOLID, true);
        this.checkGetData(waterBlockState, Keys.IS_SOLID, false);

        this.checkGetData(player, Keys.IS_SPRINTING, false);

        final Entity polarBear = world.createEntity(EntityTypes.POLAR_BEAR.get(), position);
        this.checkOfferData(polarBear, Keys.IS_STANDING, true);

        this.checkGetData(ravager, Keys.IS_STUNNED, false);

        this.checkGetData(dirtState, Keys.IS_SURROGATE_BLOCK, false);

        // TODO Keys.IS_TAKING_DISABLED

        this.checkOfferData(cat, Keys.IS_TAMED, true);
        this.checkOfferData(wolf, Keys.IS_TAMED, true);

        final Entity villager = world.createEntity(EntityTypes.VILLAGER.get(), position);
        this.checkGetData(villager, Keys.IS_TRADING, false);

        final Entity ocelot = world.createEntity(EntityTypes.OCELOT.get(), position);
        this.checkOfferData(ocelot, Keys.IS_TRUSTING, true);

        this.checkOfferData(jungleAxe, Keys.IS_UNBREAKABLE, true);
        this.checkGetData(obisidanState, Keys.IS_UNBREAKABLE, false);
        final BlockState bedrockState = BlockTypes.BEDROCK.get().getDefaultState();
        this.checkGetData(bedrockState, Keys.IS_UNBREAKABLE, true);

//        this.checkOfferData(panda, Keys.IS_UNHAPPY, true);

        this.checkWithData(acaciaStairs, Keys.IS_WATERLOGGED, true);

        this.checkOfferData(wolf, Keys.IS_WET, true);
        final BlockState spongeState = BlockTypes.SPONGE.get().getDefaultState();
        final BlockState wetSpongeState = BlockTypes.WET_SPONGE.get().getDefaultState();
//        this.checkGetData(spongeState, Keys.IS_WET, false);
//        this.checkGetData(wetSpongeState, Keys.IS_WET, true);
        this.checkGetData(sheep, Keys.IS_WET, false);

//        this.checkGetData(jungleAxe, Keys.ITEM_DURABILITY, ..);

        this.checkOfferData(itemEntity, Keys.ITEM_STACK_SNAPSHOT, jungleAxe.createSnapshot());

        final Entity itemFrame = world.createEntity(EntityTypes.ITEM_FRAME.get(), position);
        this.checkOfferData(itemFrame, Keys.ITEM_STACK_SNAPSHOT, stoneStack.createSnapshot());
        // TODO JukeBox
        // TODO Lectern
        final Entity potionEntity = world.createEntity(EntityTypes.POTION.get(), position);
        this.checkOfferData(potionEntity, Keys.ITEM_STACK_SNAPSHOT, splashPotion.createSnapshot()); // TODO unset original value causes logging error

        // TODO Keys.KNOCKBACK_STRENGTH

        this.checkOfferData(panda, Keys.KNOWN_GENE, PandaGenes.AGGRESSIVE.get());

        this.checkOfferData(sheep, Keys.LAST_ATTACKER, player);

        // TODO Keys.LAST_COMMAND_OUTPUT

//        this.checkOfferData(player, Keys.LAST_DATE_JOINED, Instant.now().minus(1, TemporalUnits.DAYS));
//        this.checkOfferData(player, Keys.LAST_DATE_PLAYED, Instant.now().minus(1, TemporalUnits.DAYS));
        final User user = Sponge.getServer().getUserManager().get(player.getUniqueId()).get();
//        this.checkOfferData(user, Keys.LAST_DATE_JOINED, Instant.now().minus(1, TemporalUnits.DAYS));
//        this.checkOfferData(user, Keys.LAST_DATE_PLAYED, Instant.now().minus(1, TemporalUnits.DAYS));

        final BlockState snowState = BlockTypes.SNOW.get().getDefaultState();
        final BlockState cakeState = BlockTypes.CAKE.get().getDefaultState();
        this.checkWithData(snowState, Keys.LAYER, 4);
        this.checkWithData(cakeState, Keys.LAYER, 4);

        // TODO Keys.LEASH_HOLDER

        // TODO Keys.LEFT_ARM_ROTATION
        // TODO Keys.LEFT_LEG_ROTATION

        final Entity vex = world.createEntity(EntityTypes.VEX.get(), position);
        this.checkOfferData(vex, Keys.LIFE_TICKS, 10);

        this.checkGetData(dirtState, Keys.LIGHT_EMISSION, 0);
        final BlockState glowstoneState = BlockTypes.GLOWSTONE.get().getDefaultState();
        this.checkGetData(glowstoneState, Keys.LIGHT_EMISSION, 15);

        final Entity llama = world.createEntity(EntityTypes.LLAMA.get(), position);
//        this.checkOfferData(llama, Keys.LLAMA_TYPE, LlamaTypes.BROWN.get());

        // TODO Keys.LOCK_TOKEN on BlockEntity
//        this.checkOfferData(jungleAxe, Keys.LOCK_TOKEN, "Key");
        this.checkOfferListData(jungleAxe, Keys.LORE, Arrays.asList(TextComponent.of("Loreline1"), TextComponent.of("Loreline2")));

// TODO NPE?
//        this.checkGetData(dirtState, Keys.MATTER_STATE, MatterStates.SOLID.get());
//        this.checkGetData(waterBlockState, Keys.MATTER_STATE, MatterStates.LIQUID.get());
//        this.checkGetData(BlockTypes.AIR.get().getDefaultState(), Keys.MATTER_STATE, MatterStates.GAS.get());

//        this.checkOfferData(player, Keys.MAX_AIR, 20);

        // TODO Keys.MAX_BURN_TIME
        // TODO Keys.MAX_COOK_TIME

        this.checkGetData(jungleAxe, Keys.MAX_DURABILITY, 59);

        this.checkOfferData(fallingBlock, Keys.MAX_FALL_DAMAGE, 50.0);

        this.checkOfferData(sheep, Keys.MAX_HEALTH, 100.0);

        // TODO Keys.MAX_NEARBY_ENTITIES
        // TODO Keys.MAX_SPAWN_DELAY

        this.checkOfferData(boat, Keys.MAX_SPEED, 1.0);

        // MAX_STACK_SIZE

        final Entity minecartEntity = world.createEntity(EntityTypes.MINECART.get(), position);
//        this.checkOfferData(minecartEntity, Keys.MINECART_BLOCK_OFFSET, 1);

        // TODO Keys.MIN_SPAWN_DELAY

        final BlockState farmlandState = BlockTypes.FARMLAND.get().getDefaultState();
        this.checkWithData(farmlandState, Keys.MOISTURE, 1);

        final Entity mooshroom = world.createEntity(EntityTypes.MOOSHROOM.get(), position);
        this.checkOfferData(mooshroom, Keys.MOOSHROOM_TYPE, MooshroomTypes.BROWN.get());
        this.checkOfferData(mooshroom, Keys.MOOSHROOM_TYPE, MooshroomTypes.RED.get());

        final ItemStack musicDiscStack = ItemStack.of(ItemTypes.MUSIC_DISC_11);
//        this.checkGetData(musicDiscStack, Keys.MUSIC_DISC, MusicDiscs.ELEVEN.get());

        // TODO Keys.NEXT_ENTITY_TO_SPAWN

        final BlockState noteBlockState = BlockTypes.NOTE_BLOCK.get().getDefaultState();
// TODO missing supplier
//        this.checkWithData(noteBlockState, Keys.NOTE_PITCH, NotePitches.E1.get());

//        this.checkOfferData(sheep, Keys.NOTIFIER, player.getUniqueId());

        this.checkOfferData(boat, Keys.OCCUPIED_DECELERATION, 2.0);

        this.checkGetData(sheep, Keys.ON_GROUND, false);

// TODO failed offer?
//        this.checkOfferListData(writtenBookStack, Keys.PAGES, Arrays.asList(TextComponent.of("Page 1"), TextComponent.of("Page 2")));

        final Entity parrot = world.createEntity(EntityTypes.PARROT.get(), position);
// TODO missing supplier
//        this.checkOfferData(parrot, Keys.PARROT_TYPE, ParrotTypes.RED_AND_BLUE.get());

// TODO missing ParticleEffect.Builder registration
//        this.checkOfferData(areaEffectCloud, Keys.PARTICLE_EFFECT, ParticleEffect.builder().type(ParticleTypes.BUBBLE.get()).build());

        // TODO Keys.PASSED_COOK_TIME

        this.checkGetListData(donkey, Keys.PASSENGERS, Arrays.asList(wolf));

//        this.checkOfferData(tropicalFish, Keys.PATTERN_COLOR, DyeColors.CYAN.get());

        final Entity phantom = world.createEntity(EntityTypes.PHANTOM.get(), position);
//        this.checkOfferData(phantom, Keys.PHANTOM_PHASE, PhantomPhases.CIRCLING.get());
//        this.checkOfferData(phantom, Keys.PHANTOM_PHASE, PhantomPhases.SWOOPING.get());

//        this.checkOfferData(itemEntity, Keys.PICKUP_DELAY, 5);

        // TODO Keys.PICKUP_RULE

        this.checkWithData(pistonState, Keys.PISTON_TYPE, PistonTypes.NORMAL.get());
        this.checkWithData(pistonState, Keys.PISTON_TYPE, PistonTypes.STICKY.get());

        this.checkOfferSetData(stoneStack, Keys.PLACEABLE_BLOCK_TYPES, new HashSet<>(Arrays.asList(BlockTypes.OBSIDIAN.get())));

        final ItemStack writableBookStack = ItemStack.of(ItemTypes.WRITABLE_BOOK);
//        this.checkOfferListData(writableBookStack, Keys.PLAIN_PAGES, Arrays.asList("Page 1", "Page 2"));

        // Keys.PLUGIN_CONTAINER

        final Set<Direction> directionSet = new HashSet<>(Arrays.asList(Direction.DOWN, Direction.NORTH));
        this.checkGetSetData(mushroomBlockState.with(Keys.PORES, directionSet).get(), Keys.PORES, directionSet);

        this.checkWithData(bedState, Keys.PORTION_TYPE, PortionTypes.BOTTOM.get());
        this.checkWithData(bedState, Keys.PORTION_TYPE, PortionTypes.TOP.get());
        final BlockState doorState = BlockTypes.ACACIA_DOOR.get().getDefaultState();
        this.checkWithData(doorState, Keys.PORTION_TYPE, PortionTypes.TOP.get());

//        this.checkOfferData(minecartEntity, Keys.POTENTIAL_MAX_SPEED, 20.0);

        this.checkOfferListData(sheep, Keys.POTION_EFFECTS, notchAppleEffects);
        this.checkOfferListData(potion, Keys.POTION_EFFECTS, notchAppleEffects);
        this.checkOfferListData(splashPotion, Keys.POTION_EFFECTS, notchAppleEffects);
        this.checkOfferListData(areaEffectCloud, Keys.POTION_EFFECTS, notchAppleEffects);

// TODO missing PotionTypes providers
//        this.checkOfferData(potion, Keys.POTION_TYPE, PotionTypes.AWKWARD.get());
//        this.checkOfferData(splashPotion, Keys.POTION_TYPE, PotionTypes.MUNDANE.get());

        final BlockState redstoneWireState = BlockTypes.REDSTONE_WIRE.get().getDefaultState();
        this.checkWithData(redstoneWireState, Keys.POWER, 10);

        // TODO Keys.PRIMARY_POTION_EFFECT_TYPE

        this.checkOfferData(villager, Keys.PROFESSION_TYPE, ProfessionTypes.ARMORER.get());
        this.checkOfferData(villager, Keys.PROFESSION_LEVEL, 4);

        final Entity rabbit = world.createEntity(EntityTypes.RABBIT.get(), position);
// TODO missing RabbitTypes providers
//        this.checkOfferData(rabbit, Keys.RABBIT_TYPE, RabbitTypes.GOLD.get());

        this.checkOfferData(areaEffectCloud, Keys.RADIUS, 20.0);
        this.checkOfferData(areaEffectCloud, Keys.RADIUS_ON_USE, -1.0);
        this.checkOfferData(areaEffectCloud, Keys.RADIUS_PER_TICK, 0.0);
        this.checkOfferData(areaEffectCloud, Keys.RADIUS_PER_TICK, 0.0);

        // TODO this.checkOfferData(ravager, Keys.RAID_WAVE, );

        final BlockState railState = BlockTypes.RAIL.get().getDefaultState();
        this.checkWithData(railState, Keys.RAIL_DIRECTION, RailDirections.ASCENDING_EAST.get());

        this.checkOfferData(areaEffectCloud, Keys.REAPPLICATION_DELAY, 1);

        final BlockState repeaterState = BlockTypes.REPEATER.get().getDefaultState();
        this.checkWithData(repeaterState, Keys.REDSTONE_DELAY, 2);

        this.checkOfferData(sheep, Keys.REMAINING_AIR, 1);

        // TODO Keys.REMAINING_BREW_TIME

        // TODO Keys.REMAINING_SPAWN_DELAY

        this.checkGetData(goldenApple, Keys.REPLENISHED_FOOD, 4);
        this.checkGetData(goldenApple, Keys.REPLENISHED_SATURATION, (double) 9.6f);

//        this.checkGetData(dirtState, Keys.REPRESENTED_INSTRUMENT, InstrumentTypes.XYLOPHONE.get());

        // TODO Keys.REQUIRED_PLAYER_RANGE

        // Keys.RESPAWN_LOCATIONS

        this.checkOfferData(armorStand, Keys.RIGHT_ARM_ROTATION, Vector3d.UP);
        this.checkOfferData(armorStand, Keys.RIGHT_LEG_ROTATION, Vector3d.UP);

        this.checkOfferData(ravager, Keys.ROARING_TIME, 20);

//        this.checkOfferData(itemFrame, Keys.ROTATION, Rotations.LEFT.get());

        this.checkOfferData(player, Keys.SATURATION, 20.0);

        this.checkGetData(sheep, Keys.SCALE, 1.0);

        // Keys.SCOREBOARD_TAGS

        // Keys.SECONDARY_POTION_EFFECT_TYPE

        this.checkOfferData(fox, Keys.SECOND_TRUSTED, player.getUniqueId());

        // Keys.SHOOTER

        final Entity endCrystal = world.createEntity(EntityTypes.END_CRYSTAL.get(), position);
        this.checkOfferData(endCrystal, Keys.SHOW_BOTTOM, true);

        // Keys.SIGN_LINES

        final Entity slime = world.createEntity(EntityTypes.SLIME.get(), position);
        this.checkOfferData(slime, Keys.SIZE, 10);

        final Entity human = world.createEntity(EntityTypes.HUMAN.get(), position);
        // TODO HumanEntity#getSkinProperty NPE / offer not working?
        //        this.checkOfferData(human, Keys.SKIN_PROFILE_PROPERTY, player.get(Keys.SKIN_PROFILE_PROPERTY).get());

        this.checkOfferData(dolphin, Keys.SKIN_MOISTURE, 1);

        // Keys.SKY_LIGHT

        final BlockState slabState = BlockTypes.BIRCH_SLAB.get().getDefaultState();
//        this.checkWithData(slabState, Keys.SLAB_PORTION, SlabPortions.BOTTOM.get());
//        this.checkWithData(slabState, Keys.SLAB_PORTION, SlabPortions.FULL.get());

        this.checkOfferData(player, Keys.SLEEP_TIMER, 20);

        // Keys.SLOT_INDEX
        // Keys.SLOT_POSITION
        // Keys.SLOT_SIDE

//        this.checkOfferData(minecartEntity, Keys.SLOWS_UNOCCUPIED, false);

        this.checkOfferData(panda, Keys.SNEEZING_TIME, 2);

        // Keys.SPAWNABLE_ENTITIES
        // Keys.SPAWN_COUNT
        // Keys.SPAWN_RANGE

        this.checkOfferData(player, Keys.SPECTATOR_TARGET, sheep);
        this.checkOfferData(player, Keys.SPECTATOR_TARGET, player);

        this.checkWithData(acaciaStairs, Keys.STAIR_SHAPE, StairShapes.INNER_LEFT.get());
        this.checkWithData(acaciaStairs, Keys.STAIR_SHAPE, StairShapes.OUTER_LEFT.get());
        this.checkWithData(acaciaStairs, Keys.STAIR_SHAPE, StairShapes.STRAIGHT.get());

        // Keys.STATISTICS

        final ItemStack enchantedBook = ItemStack.of(ItemTypes.ENCHANTED_BOOK);
        this.checkOfferListData(enchantedBook, Keys.STORED_ENCHANTMENTS, Arrays.asList(Enchantment.of(EnchantmentTypes.SHARPNESS, 5), Enchantment.of(EnchantmentTypes.PROTECTION, 4)));

        this.checkOfferData(llama, Keys.STRENGTH, 10);

        // Keys.STRUCTURE_AUTHOR
        // Keys.STRUCTURE_IGNORE_ENTITIES
        // Keys.STRUCTURE_INTEGRITY
        // Keys.STRUCTURE_MODE
        // Keys.STRUCTURE_POSITION
        // Keys.STRUCTURE_POWERED
        // Keys.STRUCTURE_SEED
        // Keys.STRUCTURE_SHOW_AIR
        // Keys.STRUCTURE_SHOW_BOUNDING_BOX
        // Keys.STRUCTURE_SIZE

        this.checkOfferData(sheep, Keys.STUCK_ARROWS, 10);

        this.checkOfferData(ravager, Keys.STUNNED_TIME, 20);

        // Keys.SUCCESS_COUNT

        // Keys.SUSPENDED

//        this.checkOfferData(minecartEntity, Keys.SWIFTNESS, 2.0);

        this.checkOfferData(horse, Keys.TAMER, player.getUniqueId());
        this.checkOfferData(wolf, Keys.TAMER, player.getUniqueId());
        this.checkOfferData(parrot, Keys.TAMER, player.getUniqueId());

// TODO missing dataprovider       this.checkOfferData(zombiePigman, Keys.TARGET_ENTITY, player);
        this.checkOfferData(shulkerBullet, Keys.TARGET_ENTITY, sheep);
        // FishingBobber

        final Entity eyeOfEnder = world.createEntity(EntityTypes.EYE_OF_ENDER.get(), position);
//        this.checkOfferData(eyeOfEnder, Keys.TARGET_LOCATION, position);

        this.checkOfferData(ravager, Keys.TARGET_POSITION, blockPos);
        this.checkOfferData(turtle, Keys.TARGET_POSITION, blockPos);
        // EndGateway
        this.checkOfferData(endCrystal, Keys.TARGET_POSITION, blockPos);

        // Keys.TICKS_REMAINING
        this.checkGetData(jungleAxe, Keys.TOOL_TYPE, ToolTypes.WOOD.get());

        // Keys.TRACKS_OUTPUT

        final TradeOffer tradeOffer = TradeOffer.builder()
                .firstBuyingItem(ItemStack.of(ItemTypes.EMERALD))
                .sellingItem(jungleAxe)
                .build();
        this.checkOfferListData(villager, Keys.TRADE_OFFERS, Arrays.asList(tradeOffer));
//        world.spawnEntity(villager);

//        this.checkOfferData(villager, Keys.TRANSIENT, true);

//        this.checkOfferData(tropicalFish, Keys.TROPICAL_FISH_SHAPE, TropicalFishShapes.BETTY.get());

        this.checkOfferData(panda, Keys.UNHAPPY_TIME, 20);

        // Keys.UNIQUE_ID

        this.checkOfferData(boat, Keys.UNOCCUPIED_DECELERATION, 2.0);

        final BlockState tntState = BlockTypes.TNT.get().getDefaultState();
        this.checkWithData(tntState, Keys.UNSTABLE, true);

        // Keys.UPDATE_GAME_PROFILE

        // Keys.VANISH
        // Keys.VANISH_IGNORES_COLLISION
        // Keys.VANISH_PREVENTS_TARGETING

        this.checkOfferData(sheep, Keys.VELOCITY, Vector3d.UP);

        this.checkOfferData(villager, Keys.VILLAGER_TYPE, VillagerTypes.SWAMP.get());
        final Entity zombieVillager = world.createEntity(EntityTypes.ZOMBIE_VILLAGER.get(), position);
        this.checkOfferData(zombieVillager, Keys.VILLAGER_TYPE, VillagerTypes.SWAMP.get());

        this.checkOfferData(areaEffectCloud, Keys.WAIT_TIME, 1);

        this.checkOfferData(player, Keys.WALKING_SPEED, 0.1);
        this.checkOfferData(sheep, Keys.WALKING_SPEED, 0.2);

        this.checkOfferData(eyeOfEnder, Keys.WILL_SHATTER, true);

        this.checkWithData(redstoneWireState, Keys.WIRE_ATTACHMENT_EAST, WireAttachmentTypes.NONE.get());
        this.checkWithData(redstoneWireState, Keys.WIRE_ATTACHMENT_NORTH, WireAttachmentTypes.UP.get());
        this.checkWithData(redstoneWireState, Keys.WIRE_ATTACHMENT_SOUTH, WireAttachmentTypes.SIDE.get());
        this.checkWithData(redstoneWireState, Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.UP.get());
        final Map<Direction, WireAttachmentType> map = new HashMap<>();
        map.put(Direction.NORTH, WireAttachmentTypes.NONE.get());
        map.put(Direction.EAST, WireAttachmentTypes.NONE.get());
        map.put(Direction.SOUTH, WireAttachmentTypes.NONE.get());
        map.put(Direction.WEST, WireAttachmentTypes.UP.get());
        this.checkGetMapData(redstoneWireState.with(Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.UP.get()).get(), Keys.WIRE_ATTACHMENTS, map);

        final Entity wither = world.createEntity(EntityTypes.WITHER.get(), position);
        this.checkOfferListData(wither, Keys.WITHER_TARGETS, Arrays.asList(player));

        final Entity evoker = world.createEntity(EntityTypes.EVOKER.get(), position);
        this.checkOfferData(evoker, Keys.WOLOLO_TARGET, (Sheep) sheep);

        this.checkOfferData(boat, Keys.WOOD_TYPE, WoodTypes.ACACIA.get());
    }

    private <T> void checkOfferSetData(final DataHolder.Mutable holder, final Supplier<Key<SetValue<T>>> key, final Set<T> value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (this.checkResult(holder, key, value, result)) {
            this.checkGetSetData(holder, key, value);
        }
    }

    private <T> void checkOfferListData(final DataHolder.Mutable holder, final Supplier<Key<ListValue<T>>> key, final List<T> value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (this.checkResult(holder, key, value, result)) {
            this.checkGetListData(holder, key, value);
        }
    }

    private <T> void checkWithData(final DataHolder.Immutable<?> holder, final Supplier<Key<Value<T>>> key, final T value) {
        DataHolder.Immutable<?> newHolder = holder.with(key, value).get();
        this.checkGetData(newHolder, key, value);
    }


    private <T> void checkOfferData(final DataHolder.Mutable holder, final Supplier<Key<Value<T>>> key, final T value) {
        final DataTransactionResult result = holder.offer(key, value);
        if (this.checkResult(holder, key, value, result)) {
            this.checkGetData(holder, key, value);
        }
    }

    private <V extends Value<?>> boolean checkResult(final DataHolder.Mutable holder, final Supplier<Key<V>> key, final Object value, final DataTransactionResult result) {
        if (!result.isSuccessful()) {
            this.plugin.getLogger().error("Failed offer on {} for {} with {}.", holder.getClass().getSimpleName(), key.get().getKey()
                    .asString(), value);
            return true;
        }
        return false;
    }

    private <T> void checkGetWeightedData(final DataHolder holder, final Supplier<Key<WeightedCollectionValue<T>>> key, final List<T> expected) {
        final Optional<WeightedTable<T>> gotValue = holder.get(key);
        if (gotValue.isPresent()) {
            final List<T> actual = gotValue.get().get(new Random());
            if (!Objects.deepEquals(actual.toArray(), expected.toArray())) {
                this.plugin.getLogger().error("Value differs om {} for {}.\nExpected: {}\nActual:   {}", holder.getClass().getSimpleName(),
                        key.get().getKey().asString(), expected, actual);
            }
        } else {
            this.plugin.getLogger().error("Value is missing on {} for {}.\nExpected: {}", holder.getClass().getSimpleName(),
                    key.get().getKey().asString(), expected);
        }
    }

    private <T> void checkGetListData(final DataHolder holder, final Supplier<Key<ListValue<T>>> key, final List<T> expected) {
        this.checkData(holder, key.get().getKey().asString(), expected, holder.get(key).orElse(null));
    }

    private <T> void checkGetSetData(final DataHolder holder, final Supplier<Key<SetValue<T>>> key, final Set<T> expected) {
        this.checkData(holder, key.get().getKey().asString(), expected, holder.get(key).orElse(null));
    }

    private <K, V> void checkGetMapData(final DataHolder holder, final Supplier<Key<MapValue<K, V>>> key, final Map<K, V> expected) {
        this.checkData(holder, key.get().getKey().asString(), expected, holder.get(key).orElse(null));
    }

    private <T> void checkGetData(final DataHolder holder, final Supplier<Key<Value<T>>> key, final T expected) {
        this.checkData(holder, key.get().getKey().asString(), expected, holder.get(key).orElse(null));
    }

    private <T> void checkData(final DataHolder holder, final String key, final T expectedValue, @Nullable final T actualValue) {
        if (actualValue != null) {
            if (!Objects.equals(actualValue, expectedValue)) {
                this.plugin.getLogger().error("Value differs on {} for {}.\nExpected: {}\nActual:   {}", holder.getClass().getSimpleName(), key,
                        expectedValue, actualValue);
            }
        } else if (expectedValue != null) {
            this.plugin.getLogger().error("Value is missing on {} for {}.\nExpected: {}", holder.getClass().getSimpleName(), key, expectedValue);
        }
    }
}
