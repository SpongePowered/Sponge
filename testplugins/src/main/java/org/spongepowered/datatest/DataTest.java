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
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
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
import org.spongepowered.api.data.type.SpellTypes;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
        final List<PotionEffect> expectedEffects = Arrays.asList(
                PotionEffect.builder().potionType(PotionEffectTypes.REGENERATION).amplifier(1).ambient(false).duration(400).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.RESISTANCE).amplifier(0).ambient(false).duration(6000).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.FIRE_RESISTANCE).amplifier(0).ambient(false).duration(6000).build(),
                PotionEffect.builder().potionType(PotionEffectTypes.ABSORPTION).amplifier(3).ambient(false).duration(2400).build());
        this.checkGetWeightedData(goldenApple, Keys.APPLICABLE_POTION_EFFECTS, expectedEffects);

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
        this.checkGetData(leverState, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.WALL.get());
        leverState = leverState.with(Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.FLOOR.get()).get();
        this.checkGetData(leverState, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.FLOOR.get());
        world.setBlock(blockPos, BlockTypes.AIR.get().getDefaultState());
        world.setBlock(blockPos.add(0, 0, -1), BlockTypes.AIR.get().getDefaultState());

        // TODO         Keys.ATTACK_DAMAGE

        final Entity ravager = world.createEntity(EntityTypes.RAVAGER.get(), position);
        this.checkGetData(ravager, Keys.ATTACK_TIME, 0);
        this.checkOfferData(ravager, Keys.ATTACK_TIME, 5);
        ravager.remove();

        final ItemStack book = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        this.checkOfferData(book, Keys.AUTHOR, TextComponent.of("You"));

        world.setBlock(blockPos, BlockTypes.OAK_LOG.get().getDefaultState());
        BlockState logState = world.getBlock(blockPos);
        this.checkGetData(logState, Keys.AXIS, Axis.Y);
        logState = logState.with(Keys.AXIS, Axis.X).get();
        this.checkGetData(logState, Keys.AXIS, Axis.X);
        world.setBlock(blockPos, BlockTypes.AIR.get().getDefaultState());

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
        donkey.remove();
        wolf.remove();
        cat.remove();
        chicken.remove();

        final Entity guardian = world.createEntity(EntityTypes.GUARDIAN.get(), position);
        this.checkOfferData(guardian, Keys.BEAM_TARGET_ENTITY, player);
        guardian.remove();

// TODO World.get(IIIKey) is abstract
//        final ServerLocation location = world.getLocation(blockPos);
//        this.checkGetData(location, Keys.BIOME_TEMPERATURE, world.getBiome(blockPos).getTemperature());

        this.checkGetData(BlockTypes.OBSIDIAN.get().getDefaultState(), Keys.BLAST_RESISTANCE, 1200.0);
        this.checkGetData(BlockTypes.DIRT.get().getDefaultState(), Keys.BLAST_RESISTANCE, 0.5);
        this.checkGetData(BlockTypes.BRICKS.get().getDefaultState(), Keys.BLAST_RESISTANCE, 6.0);

        // TODO Keys.BLOCK_LIGHT

        final Entity fallingBlock = world.createEntity(EntityTypes.FALLING_BLOCK.get(), position);
        this.checkOfferData(fallingBlock, Keys.BLOCK_STATE, BlockTypes.SAND.get().getDefaultState());

        // TODO Keys.BLOCK_TEMPERATURE

// TODO missing BodyPart registration
//        final Entity armorStand = world.createEntity(EntityTypes.ARMOR_STAND.get(), position);
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
//        fallingAnvil.remove();


        this.checkOfferData(ravager, Keys.CAN_JOIN_RAID, true);
        this.checkOfferData(ravager, Keys.CAN_JOIN_RAID, false);

        // TODO missing data provider
//        final Entity boat = world.createEntity(EntityTypes.BOAT.get(), position);
//        this.checkGetData(boat, Keys.CAN_MOVE_ON_LAND, true);
//        this.checkOfferData(boat, Keys.CAN_MOVE_ON_LAND, false);

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
        this.checkOfferData(leatherBoots, Keys.COLOR, Color.BLACK);
        this.checkOfferData(potion, Keys.COLOR, Color.WHITE);

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
        leavesState = leavesState.with(Keys.DECAY_DISTANCE, 2).get();
        this.checkGetData(leavesState, Keys.DECAY_DISTANCE, 2);

        // TODO Keys.DERAILED_VELOCITY_MODIFIER

        // TODO missing ItemEntityBridge
        // TODO also other dataholders
//        final Entity itemEntity = world.createEntity(EntityTypes.ITEM.get(), position);
//        this.checkGetData(itemEntity, Keys.DESPAWN_DELAY, 6000);

        final Entity tntEntity = world.createEntity(EntityTypes.TNT.get(), position);
        this.checkGetData(tntEntity, Keys.DETONATOR, null);
        this.checkOfferData(tntEntity, Keys.DETONATOR, player);
        tntEntity.remove();

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
        this.checkGetData(acaciaStairs.with(Keys.DIRECTION, Direction.WEST).get(), Keys.DIRECTION, Direction.WEST);

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
//        final Entity tropicalFish = world.createEntity(EntityTypes.TROPICAL_FISH.get(), position);
//        this.checkOfferData(tropicalFish, Keys.DYE_COLOR, DyeColors.CYAN.get());
//        tropicalFish.remove();
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
