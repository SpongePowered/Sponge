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
package org.spongepowered.common.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ArtTypes;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.type.CatTypes;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.FoxTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.LlamaTypes;
import org.spongepowered.api.data.type.MooshroomTypes;
import org.spongepowered.api.data.type.PandaGenes;
import org.spongepowered.api.data.type.ParrotTypes;
import org.spongepowered.api.data.type.PhantomPhases;
import org.spongepowered.api.data.type.ProfessionTypes;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.SpellTypes;
import org.spongepowered.api.data.type.TropicalFishShapes;
import org.spongepowered.api.data.type.VillagerTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.monster.raider.illager.Pillager;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.orientation.Orientations;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;

public final class EntityDataTest {
    private ServerLocation location;

    @BeforeEach
    public void prepare() {
        this.location = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get().location(Vector3d.ZERO);
    }

    @Disabled
    @Test
    public void testSheep() {
        final Entity sheep = this.location.createEntity(EntityTypes.SHEEP.get());
        final UUID playerId = UUID.randomUUID();

        DataTest.checkOfferData(sheep, Keys.ABSORPTION, 0.0);
        DataTest.checkOfferData(sheep, Keys.ABSORPTION, 10.0);
        DataTest.checkOfferData(sheep, Keys.ABSORPTION, 20.0);

        DataTest.checkGetData(sheep, Keys.AGE, 0);
        DataTest.checkOfferData(sheep, Keys.AGE, 10);

        DataTest.checkOfferData(sheep, Keys.BABY_TICKS, Ticks.ofWallClockSeconds(Sponge.server(), 1));
        DataTest.checkOfferData(sheep, Keys.BABY_TICKS, Ticks.zero());

        DataTest.checkGetData(sheep, Keys.BASE_SIZE, (double) 0.9f);

        DataTest.checkGetData(sheep, Keys.BREEDER, null);
        DataTest.checkOfferData(sheep, Keys.BREEDER, playerId);

        DataTest.checkGetData(sheep, Keys.BREEDING_COOLDOWN, Ticks.zero());
        DataTest.checkOfferData(sheep, Keys.BREEDING_COOLDOWN, Ticks.of(100));

        DataTest.checkGetData(sheep, Keys.CAN_BREED, false);
        DataTest.checkOfferData(sheep, Keys.CAN_BREED, true);

        DataTest.checkGetData(sheep, Keys.CUSTOM_NAME, null);
        DataTest.checkOfferData(sheep, Keys.CUSTOM_NAME, Component.text("A sheep"));

        DataTest.checkGetData(sheep, Keys.DISPLAY_NAME, Component.text("A sheep")
            .hoverEvent(HoverEvent.showEntity(ResourceKey.minecraft("sheep"), sheep.uniqueId(), Component.text("A sheep")))
            .insertion(sheep.uniqueId().toString()));

        DataTest.checkGetData(sheep, Keys.EYE_HEIGHT,  (double) (1.3f * 0.95f));
        DataTest.checkGetData(sheep, Keys.EYE_POSITION, this.location.position().add(0, (double) (1.3f * 0.95f), 0));

        DataTest.checkOfferData(sheep, Keys.FIRE_DAMAGE_DELAY, Ticks.of(20000));
        DataTest.checkOfferData(sheep, Keys.FIRE_TICKS, Ticks.of(10));

        DataTest.checkOfferData(sheep, Keys.HEAD_ROTATION, Vector3d.from(0, 90, 0));

        DataTest.checkOfferData(sheep, Keys.INVULNERABILITY_TICKS, Ticks.of(20));

        DataTest.checkOfferData(sheep, Keys.INVULNERABLE, true);
        DataTest.checkOfferData(sheep, Keys.INVULNERABLE, false);

        DataTest.checkOfferData(sheep, Keys.IS_ADULT, false);
        DataTest.checkOfferData(sheep, Keys.IS_ADULT, true);

        DataTest.checkOfferData(sheep, Keys.IS_CUSTOM_NAME_VISIBLE, false);

        DataTest.checkOfferData(sheep, Keys.IS_FLYING, true);
        DataTest.checkOfferData(sheep, Keys.IS_GLOWING, true);
        DataTest.checkOfferData(sheep, Keys.IS_GRAVITY_AFFECTED, false);
        DataTest.checkOfferData(sheep, Keys.IS_PERSISTENT, true);
        DataTest.checkOfferData(sheep, Keys.IS_SHEARED, true);
        DataTest.checkOfferData(sheep, Keys.IS_SILENT, true);
        DataTest.checkGetData(sheep, Keys.IS_WET, false);

        DataTest.checkOfferData(sheep, Keys.MAX_HEALTH, 100.0);
        DataTest.checkOfferData(sheep, Keys.NOTIFIER, playerId);
        DataTest.checkGetData(sheep, Keys.ON_GROUND, false);
        DataTest.checkOfferData(sheep, Keys.REMAINING_AIR, 1);
        DataTest.checkGetData(sheep, Keys.SCALE, 1.0);
        DataTest.checkOfferData(sheep, Keys.STUCK_ARROWS, 10);
        DataTest.checkOfferData(sheep, Keys.VELOCITY, Vector3d.UP.mul(0.1));

        DataTest.checkOfferData(sheep, Keys.WALKING_SPEED, 0.2);

        final EntitySnapshot snapshot = sheep.createSnapshot();
        DataTest.checkWithData(snapshot, Keys.CUSTOM_NAME, Component.text("Snapshot"));

        final EntityArchetype archetype = sheep.createArchetype();
        DataTest.checkOfferData(archetype, Keys.CUSTOM_NAME, Component.text("Archetype"));
    }

    @Test
    public void testMinecart() {
        final Entity minecart = this.location.createEntity(EntityTypes.MINECART.get());
        DataTest.checkOfferData(minecart, Keys.AIRBORNE_VELOCITY_MODIFIER, new Vector3d(2, 0.5, 2)); // falls at ~50% flies at -200%
        DataTest.checkOfferData(minecart, Keys.BLOCK_STATE, BlockTypes.COBBLESTONE.get().defaultState());
        DataTest.checkOfferData(minecart, Keys.DERAILED_VELOCITY_MODIFIER, Vector3d.RIGHT);
        DataTest.checkGetData(minecart, Keys.IS_ON_RAIL, false);
        DataTest.checkOfferData(minecart, Keys.MINECART_BLOCK_OFFSET, 1);
        DataTest.checkOfferData(minecart, Keys.POTENTIAL_MAX_SPEED, 20.0);
        DataTest.checkOfferData(minecart, Keys.SLOWS_UNOCCUPIED, false);
        DataTest.checkOfferData(minecart, Keys.VELOCITY, Vector3d.FORWARD);
        DataTest.checkOfferData(minecart, Keys.SWIFTNESS, 2.0);
    }

    @Test
    public void testZombifiedPiglin() {
        final Entity zombifiedPiglin = this.location.createEntity(EntityTypes.ZOMBIFIED_PIGLIN.get());
        final Entity pig = this.location.createEntity(EntityTypes.PIG.get());

        DataTest.checkGetData(zombifiedPiglin, Keys.ANGER_LEVEL, 0);
        DataTest.checkOfferData(zombifiedPiglin, Keys.ANGER_LEVEL, 10);
        DataTest.checkOfferData(zombifiedPiglin, Keys.TARGET_ENTITY, pig);
    }

    @Disabled
    @Test
    public void testPainting() {
        final Entity painting = this.location.createEntity(EntityTypes.PAINTING.get());
        DataTest.checkGetData(painting, Keys.ART_TYPE, ArtTypes.KEBAB.get());

        DataTest.checkGetData(painting, Keys.DIRECTION, Direction.SOUTH);
        DataTest.checkOfferData(painting, Keys.DIRECTION, Direction.NORTH);
    }

    @Test
    public void testRavager() {
        final Entity ravager = this.location.createEntity(EntityTypes.RAVAGER.get());

        DataTest.checkGetData(ravager, Keys.ATTACK_TIME, Ticks.zero());
        DataTest.checkOfferData(ravager, Keys.ATTACK_TIME, Ticks.of(200));

        DataTest.checkOfferData(ravager, Keys.CAN_JOIN_RAID, true);
        DataTest.checkOfferData(ravager, Keys.CAN_JOIN_RAID, false);

        DataTest.checkOfferData(ravager, Keys.IS_CELEBRATING, true);
        DataTest.checkGetData(ravager, Keys.IS_IMMOBILIZED, true);
        DataTest.checkGetData(ravager, Keys.IS_ROARING, false);
        DataTest.checkGetData(ravager, Keys.IS_STUNNED, false);

        DataTest.checkOfferData(ravager, Keys.ROARING_TIME, Ticks.of(20));

        DataTest.checkOfferData(ravager, Keys.STUNNED_TIME, Ticks.of(20));
        DataTest.checkOfferData(ravager, Keys.TARGET_POSITION, Vector3i.ONE);
    }

    @Disabled
    @Test
    public void testHuman() {
        final Entity human = this.location.createEntity(EntityTypes.HUMAN.get());

        DataTest.checkGetData(human, Keys.BASE_SIZE, (double) 0.6f);
        DataTest.checkGetData(human, Keys.EYE_HEIGHT, (double) 1.62f);

        DataTest.checkOfferData(human, Keys.CAN_FLY, true);
        DataTest.checkOfferData(human, Keys.FIRE_DAMAGE_DELAY, Ticks.of(20000));

        DataTest.checkOfferData(human, Keys.FOOD_LEVEL, 0);
        DataTest.checkOfferData(human, Keys.FOOD_LEVEL, 20);

        DataTest.checkOfferData(human, Keys.GAME_MODE, GameModes.SURVIVAL.get());
        DataTest.checkOfferData(human, Keys.GAME_MODE, GameModes.ADVENTURE.get());
        DataTest.checkOfferData(human, Keys.GAME_MODE, GameModes.CREATIVE.get());

        DataTest.checkGetData(human, Keys.HEIGHT, (double) 1.8f);

        DataTest.checkGetData(human, Keys.TRANSIENT, true);
    }

    @Test
    public void testVehicule() {
        final Entity donkey = this.location.createEntity(EntityTypes.DONKEY.get());
        final Entity wolf = this.location.createEntity(EntityTypes.WOLF.get());
        final Entity cat = this.location.createEntity(EntityTypes.CAT.get());
        final Entity chicken = this.location.createEntity(EntityTypes.CHICKEN.get());

        wolf.offer(Keys.VEHICLE, donkey);
        cat.offer(Keys.VEHICLE, wolf);
        chicken.offer(Keys.VEHICLE, cat);
        DataTest.checkGetData(chicken, Keys.BASE_VEHICLE, donkey);

        DataTest.checkGetData(donkey, Keys.PASSENGERS, List.of(wolf));
    }

    @Disabled
    @Test
    public void testGuardian() {
        final Entity guardian = this.location.createEntity(EntityTypes.GUARDIAN.get());
        final Living fish = this.location.createEntity(EntityTypes.TROPICAL_FISH.get());
        DataTest.checkOfferData(guardian, Keys.BEAM_TARGET_ENTITY, fish);
    }

    @Disabled
    @Test
    public void testFallingBlock() {
        final Entity fallingBlock = this.location.createEntity(EntityTypes.FALLING_BLOCK.get());

        DataTest.checkOfferData(fallingBlock, Keys.BLOCK_STATE, BlockTypes.SAND.get().defaultState());

        DataTest.checkGetData(fallingBlock, Keys.CAN_DROP_AS_ITEM, true);
        DataTest.checkOfferData(fallingBlock, Keys.CAN_DROP_AS_ITEM, false);

        DataTest.checkGetData(fallingBlock, Keys.CAN_HURT_ENTITIES, false);
        DataTest.checkOfferData(fallingBlock, Keys.CAN_HURT_ENTITIES, true);

        DataTest.checkGetData(fallingBlock, Keys.CAN_PLACE_AS_BLOCK, true);
        DataTest.checkOfferData(fallingBlock, Keys.CAN_PLACE_AS_BLOCK, false);

        DataTest.checkGetData(fallingBlock, Keys.DAMAGE_PER_BLOCK, 2.0);
        DataTest.checkOfferData(fallingBlock, Keys.DAMAGE_PER_BLOCK, 5.0);

        DataTest.checkGetData(fallingBlock, Keys.FALL_DISTANCE, 0.0);
        DataTest.checkOfferData(fallingBlock, Keys.FALL_DISTANCE, 20.0);

        DataTest.checkGetData(fallingBlock, Keys.FALL_TIME, Ticks.of(0));
        DataTest.checkOfferData(fallingBlock, Keys.FALL_TIME, Ticks.of(20));

        DataTest.checkOfferData(fallingBlock, Keys.MAX_FALL_DAMAGE, 50.0);
    }

    @Disabled
    @Test
    public void testArmorStand() {
        final ArmorStand armorStand = this.location.createEntity(EntityTypes.ARMOR_STAND.get());

        armorStand.equip(EquipmentTypes.CHEST.get(), ItemStack.of(ItemTypes.LEATHER_CHESTPLATE));
        armorStand.equip(EquipmentTypes.FEET.get(), ItemStack.of(ItemTypes.CHAINMAIL_BOOTS));
        armorStand.equip(EquipmentTypes.HEAD.get(), ItemStack.of(ItemTypes.GOLDEN_HELMET));
        armorStand.equip(EquipmentTypes.LEGS.get(), ItemStack.of(ItemTypes.DIAMOND_LEGGINGS));
        armorStand.equip(EquipmentTypes.MAINHAND.get(), ItemStack.of(ItemTypes.DIAMOND));
        armorStand.equip(EquipmentTypes.OFFHAND.get(), ItemStack.of(ItemTypes.DIAMOND));

        DataTest.checkOfferData(armorStand, Keys.BODY_ROTATIONS, Map.of(BodyParts.CHEST.get(), Vector3d.RIGHT));
        DataTest.checkOfferData(armorStand, Keys.CHEST_ROTATION, Vector3d.from(0, 90, 0));

        DataTest.checkOfferData(armorStand, Keys.HAS_ARMS, false);
        DataTest.checkOfferData(armorStand, Keys.HAS_ARMS, true);

        DataTest.checkOfferData(armorStand, Keys.HAS_BASE_PLATE, false);
        DataTest.checkOfferData(armorStand, Keys.HAS_BASE_PLATE, true);

        DataTest.checkOfferData(armorStand, Keys.HAS_MARKER, true);
        DataTest.checkOfferData(armorStand, Keys.HAS_MARKER, false);

        DataTest.checkOfferData(armorStand, Keys.HEAD_ROTATION, Vector3d.from(0, 90, 0));
        DataTest.checkOfferData(armorStand, Keys.LEFT_ARM_ROTATION, Vector3d.from(0, -90, -90));
        DataTest.checkOfferData(armorStand, Keys.LEFT_LEG_ROTATION, Vector3d.from(0, -90, -45));
        DataTest.checkOfferData(armorStand, Keys.RIGHT_ARM_ROTATION, Vector3d.from(0, 90, 90));
        DataTest.checkOfferData(armorStand, Keys.RIGHT_LEG_ROTATION, Vector3d.from(0, 90, 45));

        DataTest.checkOfferData(armorStand, Keys.IS_SMALL, true);
        DataTest.checkOfferData(armorStand, Keys.IS_SMALL, false);
    }

    @Test
    public void testCreeper() {
        final Entity creeper = this.location.createEntity(EntityTypes.CREEPER.get());

        DataTest.checkGetData(creeper, Keys.CAN_GRIEF, true);
        DataTest.checkOfferData(creeper, Keys.CAN_GRIEF, false);

        DataTest.checkOfferData(creeper, Keys.IS_CHARGED, false);
        DataTest.checkOfferData(creeper, Keys.IS_CHARGED, true);
    }

    @Test
    public void testBoat() {
        final Entity boat = this.location.createEntity(EntityTypes.OAK_BOAT.get());

        DataTest.checkOfferData(boat, Keys.CAN_MOVE_ON_LAND, true);
        DataTest.checkOfferData(boat, Keys.CAN_MOVE_ON_LAND, false);

        DataTest.checkGetData(boat, Keys.IS_IN_WATER, false);
        DataTest.checkOfferData(boat, Keys.MAX_SPEED, 1.0);

        DataTest.checkOfferData(boat, Keys.OCCUPIED_DECELERATION, 2.0);
        DataTest.checkOfferData(boat, Keys.UNOCCUPIED_DECELERATION, 2.0);
    }

    @Test
    public void testIllusioner() {
        final Entity illusioner = this.location.createEntity(EntityTypes.ILLUSIONER.get());

        DataTest.checkGetData(illusioner, Keys.CASTING_TIME, 0);

        DataTest.checkGetData(illusioner, Keys.CURRENT_SPELL, SpellTypes.NONE.get());
        DataTest.checkOfferData(illusioner, Keys.CURRENT_SPELL, SpellTypes.BLINDNESS.get());
    }

    @Disabled
    @Test
    public void testCat() {
        final Entity cat = this.location.createEntity(EntityTypes.CAT.get());

        DataTest.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.ALL_BLACK.get());
        DataTest.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.JELLIE.get());
        DataTest.checkOfferData(cat, Keys.CAT_TYPE, CatTypes.WHITE.get());

        DataTest.checkOfferData(cat, Keys.DYE_COLOR, DyeColors.LIME.get());

        DataTest.checkOfferData(cat, Keys.IS_BEGGING_FOR_FOOD, true);
        DataTest.checkOfferData(cat, Keys.IS_HISSING, true);
        DataTest.checkOfferData(cat, Keys.IS_LYING_DOWN, true);
        DataTest.checkOfferData(cat, Keys.IS_PURRING, true);
        DataTest.checkOfferData(cat, Keys.IS_RELAXED, true);
        DataTest.checkOfferData(cat, Keys.IS_SITTING, true);

        DataTest.checkOfferData(cat, Keys.IS_TAMED, true);
    }

    @Test
    public void testShulkerBullet() {
        final Entity shulkerBullet = this.location.createEntity(EntityTypes.SHULKER_BULLET.get());
        final Entity pig = this.location.createEntity(EntityTypes.PIG.get());

        DataTest.checkOfferData(shulkerBullet, Keys.CUSTOM_NAME, Component.text("Angry Shulker Bullet"));

        DataTest.checkGetData(shulkerBullet, Keys.DIRECTION, Direction.NONE);
        DataTest.checkOfferData(shulkerBullet, Keys.DIRECTION, Direction.NORTH);
        DataTest.checkOfferData(shulkerBullet, Keys.DIRECTION, Direction.UP);

        DataTest.checkGetData(shulkerBullet, Keys.DISPLAY_NAME, Component.text("Angry Shulker Bullet")
            .hoverEvent(HoverEvent.showEntity(ResourceKey.minecraft("shulker_bullet"), shulkerBullet.uniqueId(), Component.text("Angry Shulker Bullet")))
            .insertion(shulkerBullet.uniqueId().toString()));

        DataTest.checkOfferData(shulkerBullet, Keys.TARGET_ENTITY, pig);
    }

    @Test
    public void testDespawnDelay() {
        DataTest.checkGetData(this.location.createEntity(EntityTypes.ITEM.get()), Keys.DESPAWN_DELAY, Ticks.of(6000));
        DataTest.checkOfferData(this.location.createEntity(EntityTypes.EYE_OF_ENDER.get()), Keys.DESPAWN_DELAY, Ticks.of(500));
    }

    @Test
    public void testTNT() {
        final Entity tnt = this.location.createEntity(EntityTypes.TNT.get());
        final Living evilChicken = this.location.createEntity(EntityTypes.CHICKEN.get());

        DataTest.checkGetData(tnt, Keys.DETONATOR, null);
        DataTest.checkOfferData(tnt, Keys.DETONATOR, evilChicken);
        DataTest.checkOfferData(tnt, Keys.EXPLOSION_RADIUS, 1f);
        DataTest.checkOfferData(tnt, Keys.FUSE_DURATION, Ticks.of(10));
        DataTest.checkGetData(tnt, Keys.IS_PRIMED, true);
    }

    @Test
    public void testShulker() {
        final Entity shulker = this.location.createEntity(EntityTypes.SHULKER.get());
        DataTest.checkGetData(shulker, Keys.DIRECTION, Direction.DOWN);
        DataTest.checkOfferData(shulker, Keys.DIRECTION, Direction.NORTH);
        DataTest.checkOfferData(shulker, Keys.DIRECTION, Direction.UP);
    }

    @Disabled
    @Test
    public void testAreaEffectCloud() {
        final Entity areaEffectCloud = this.location.createEntity(EntityTypes.AREA_EFFECT_CLOUD.get());

        DataTest.checkOfferData(areaEffectCloud, Keys.DURATION, Ticks.of(50));
        DataTest.checkOfferData(areaEffectCloud, Keys.DURATION_ON_USE, Ticks.zero());

        DataTest.checkOfferData(areaEffectCloud, Keys.PARTICLE_EFFECT, ParticleEffect.builder().type(ParticleTypes.BUBBLE.get()).build());

        DataTest.checkOfferData(areaEffectCloud, Keys.RADIUS, 20.0);
        DataTest.checkOfferData(areaEffectCloud, Keys.RADIUS_ON_USE, -1.0);
        DataTest.checkOfferData(areaEffectCloud, Keys.RADIUS_PER_TICK, 0.0);
        DataTest.checkOfferData(areaEffectCloud, Keys.RADIUS_PER_TICK, 0.0);

        DataTest.checkOfferData(areaEffectCloud, Keys.REAPPLICATION_DELAY, Ticks.single());
        DataTest.checkOfferData(areaEffectCloud, Keys.WAIT_TIME, Ticks.single());
    }

    @Test
    public void testTropicalFish() {
        final Entity tropicalFish = this.location.createEntity(EntityTypes.TROPICAL_FISH.get());
        DataTest.checkOfferData(tropicalFish, Keys.DYE_COLOR, DyeColors.CYAN.get());
        DataTest.checkOfferData(tropicalFish, Keys.PATTERN_COLOR, DyeColors.CYAN.get());
        DataTest.checkOfferData(tropicalFish, Keys.TROPICAL_FISH_SHAPE, TropicalFishShapes.BETTY.get());
    }

    @Disabled
    @Test
    public void testPanda() {
        final Entity panda = this.location.createEntity(EntityTypes.PANDA.get());

        DataTest.checkOfferData(panda, Keys.EATING_TIME, Ticks.of(10));
        DataTest.checkOfferData(panda, Keys.HIDDEN_GENE, PandaGenes.WEAK.get());
        DataTest.checkOfferData(panda, Keys.KNOWN_GENE, PandaGenes.AGGRESSIVE.get());

        DataTest.checkOfferData(panda, Keys.IS_EATING, true);
        DataTest.checkGetData(panda, Keys.IS_FRIGHTENED, false);
        DataTest.checkOfferData(panda, Keys.IS_ROLLING_AROUND, true);
        DataTest.checkOfferData(panda, Keys.IS_SNEEZING, true);
        DataTest.checkOfferData(panda, Keys.IS_UNHAPPY, true);

        DataTest.checkOfferData(panda, Keys.SNEEZING_TIME, Ticks.of(2));
        DataTest.checkOfferData(panda, Keys.UNHAPPY_TIME, Ticks.of(20));
    }

    @Test
    public void testChicken() {
        final Entity chicken = this.location.createEntity(EntityTypes.CHICKEN.get());
        DataTest.checkOfferData(chicken, Keys.EGG_TIME, Ticks.of(0));
        DataTest.checkOfferData(chicken, Keys.EGG_TIME, Ticks.of(5000));
    }

    @Disabled
    @Test
    public void testFirework() {
        final ItemStack fireworkStar = ItemStack.of(ItemTypes.FIREWORK_STAR);
        final ItemStack fireworkRocket = ItemStack.of(ItemTypes.FIREWORK_ROCKET);
        final Entity rocket = this.location.createEntity(EntityTypes.FIREWORK_ROCKET.get());
        final List<FireworkEffect> fireworkEffects = Collections.singletonList(FireworkEffect.builder().shape(FireworkShapes.CREEPER).color(Color.RED).build());
        DataTest.checkOfferData(fireworkStar, Keys.FIREWORK_EFFECTS, fireworkEffects);
        DataTest.checkOfferData(fireworkRocket, Keys.FIREWORK_EFFECTS, fireworkEffects);
        DataTest.checkOfferData(rocket, Keys.FIREWORK_EFFECTS, fireworkEffects);
        DataTest.checkOfferData(rocket, Keys.FIREWORK_FLIGHT_MODIFIER, Ticks.of(5));
    }

    @Test
    public void testFox() {
        final Entity fox = this.location.createEntity(EntityTypes.FOX.get());
        final UUID playerId = UUID.randomUUID();

        DataTest.checkOfferData(fox, Keys.FIRST_TRUSTED, playerId);
        DataTest.checkOfferData(fox, Keys.SECOND_TRUSTED, playerId);

        DataTest.checkOfferData(fox, Keys.FOX_TYPE, FoxTypes.RED.get());
        DataTest.checkOfferData(fox, Keys.FOX_TYPE, FoxTypes.SNOW.get());

        DataTest.checkOfferData(fox, Keys.IS_CROUCHING, true);
        DataTest.checkOfferData(fox, Keys.IS_DEFENDING, true);

        DataTest.checkOfferData(fox, Keys.IS_FACEPLANTED, false);
        DataTest.checkOfferData(fox, Keys.IS_FACEPLANTED, true);

        DataTest.checkOfferData(fox, Keys.IS_INTERESTED, true);
        DataTest.checkOfferData(fox, Keys.IS_POUNCING, true);
        DataTest.checkOfferData(fox, Keys.IS_SLEEPING, true);
    }

    @Test
    public void testFurnaceMinecart() {
        final Entity furnaceMinecart = this.location.createEntity(EntityTypes.FURNACE_MINECART.get());
        DataTest.checkOfferData(furnaceMinecart, Keys.FUEL, 10);
        DataTest.checkGetData(furnaceMinecart, Keys.IS_ON_RAIL, false);
    }

    @Test
    public void testDonkey() {
        final Entity donkey = this.location.createEntity(EntityTypes.DONKEY.get());
        DataTest.checkGetData(donkey, Keys.HAS_CHEST, false);
        DataTest.checkOfferData(donkey, Keys.HAS_CHEST, true);
    }

    @Test
    public void testTurtle() {
        final Entity turtle = this.location.createEntity(EntityTypes.TURTLE.get());
        DataTest.checkOfferData(turtle, Keys.HAS_EGG, true);
        DataTest.checkOfferData(turtle, Keys.HOME_POSITION, new Vector3i(0, 0, 10));
        DataTest.checkOfferData(turtle, Keys.IS_GOING_HOME, true);
        DataTest.checkOfferData(turtle, Keys.IS_LAYING_EGG, true);
        DataTest.checkOfferData(turtle, Keys.TARGET_POSITION, Vector3i.ONE);
    }

    @Test
    public void testDolphin() {
        final Entity dolphin = this.location.createEntity(EntityTypes.DOLPHIN.get());
        DataTest.checkOfferData(dolphin, Keys.HAS_FISH, true);
        DataTest.checkOfferData(dolphin, Keys.SKIN_MOISTURE, 1);
    }

    @Disabled
    @Test
    public void testHorse() {
        final Entity horse = this.location.createEntity(EntityTypes.HORSE.get());
        final UUID playerId = UUID.randomUUID();

        DataTest.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.BLACK.get());
        DataTest.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.WHITE.get());

        DataTest.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.DARK_BROWN.get());
        DataTest.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.NONE.get());

        DataTest.checkOfferData(horse, Keys.HORSE_COLOR, HorseColors.WHITE.get());
        DataTest.checkOfferData(horse, Keys.HORSE_STYLE, HorseStyles.BLACK_DOTS.get());

        DataTest.checkOfferData(horse, Keys.IS_SADDLED, true);
        DataTest.checkOfferData(horse, Keys.OWNER, playerId);
    }

    @Test
    public void testSnowball() {
        final ItemStack snowball = ItemStack.of(ItemTypes.SNOWBALL, 16);
        DataTest.checkGetData(snowball, Keys.INACCURACY, 1.0);
        DataTest.checkOfferData(snowball, Keys.INACCURACY, 10.0);
    }

    @Test
    public void testBlaze() {
        final Entity blaze = this.location.createEntity(EntityTypes.BLAZE.get());
        DataTest.checkOfferData(blaze, Keys.IS_AFLAME, false);
        DataTest.checkOfferData(blaze, Keys.IS_AFLAME, true);

        DataTest.checkOfferData(blaze, Keys.IS_AI_ENABLED, true);
        DataTest.checkOfferData(blaze, Keys.IS_AI_ENABLED, false);
    }

    @Disabled
    @Test
    public void testWolf() {
        final Entity wolf = this.location.createEntity(EntityTypes.WOLF.get());
        final UUID playerId = UUID.randomUUID();

        DataTest.checkOfferData(wolf, Keys.IS_ANGRY, true);
        DataTest.checkOfferData(wolf, Keys.IS_ANGRY, false);
        DataTest.checkOfferData(wolf, Keys.IS_BEGGING_FOR_FOOD, true);
        DataTest.checkOfferData(wolf, Keys.IS_SITTING, true);
        DataTest.checkOfferData(wolf, Keys.IS_TAMED, true);
        DataTest.checkOfferData(wolf, Keys.IS_WET, true);

        DataTest.checkOfferData(wolf, Keys.OWNER, playerId);
    }

    @Test
    public void testPillager() {
        final Pillager pillager = this.location.createEntity(EntityTypes.PILLAGER.get());
        pillager.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.CROSSBOW));
        DataTest.checkOfferData(pillager, Keys.IS_CHARGING_CROSSBOW, false);
        DataTest.checkOfferData(pillager, Keys.IS_CHARGING_CROSSBOW, true);
    }

    @Test
    public void testSpider() {
        final Entity spider = this.location.createEntity(EntityTypes.SPIDER.get());
        DataTest.checkOfferData(spider, Keys.IS_CLIMBING, true);
    }

    @Test
    public void testVindicator() {
        final Entity vindicator = this.location.createEntity(EntityTypes.VINDICATOR.get());
        DataTest.checkOfferData(vindicator, Keys.IS_JOHNNY, true);
        DataTest.checkOfferData(vindicator, Keys.IS_LEADER, true);
        DataTest.checkOfferData(vindicator, Keys.IS_PATROLLING, true);
        DataTest.checkOfferData(vindicator, Keys.IS_PERSISTENT, true);
    }

    @Test
    public void testIronGolem() {
        final Entity ironGolem = this.location.createEntity(EntityTypes.IRON_GOLEM.get());
        DataTest.checkOfferData(ironGolem, Keys.IS_PLAYER_CREATED, true);
    }

    @Test
    public void testEnderman() {
        final Entity enderman = this.location.createEntity(EntityTypes.ENDERMAN.get());
        DataTest.checkOfferData(enderman, Keys.IS_SCREAMING, true);
    }

    @Test
    public void testBat() {
        final Entity bat = this.location.createEntity(EntityTypes.BAT.get());
        DataTest.checkOfferData(bat, Keys.IS_SLEEPING, true);
    }

    @Test
    public void testPolarBear() {
        final Entity polarBear = this.location.createEntity(EntityTypes.POLAR_BEAR.get());
        DataTest.checkOfferData(polarBear, Keys.IS_STANDING, true);
    }

    @Test
    public void testVillager() {
        final Entity villager = this.location.createEntity(EntityTypes.VILLAGER.get());
        final Entity zombieVillager = this.location.createEntity(EntityTypes.ZOMBIE_VILLAGER.get());

        DataTest.checkGetData(villager, Keys.CUSTOMER, null);
        DataTest.checkOfferData(villager, Keys.PROFESSION_TYPE, ProfessionTypes.ARMORER.get());
        DataTest.checkOfferData(villager, Keys.PROFESSION_LEVEL, 4);

        DataTest.checkOfferData(zombieVillager, Keys.PROFESSION_TYPE, ProfessionTypes.CLERIC.get());
        DataTest.checkOfferData(zombieVillager, Keys.PROFESSION_LEVEL, 1);

        final TradeOffer tradeOffer = TradeOffer.builder()
            .firstBuyingItem(ItemStack.of(ItemTypes.EMERALD))
            .sellingItem(ItemStack.of(ItemTypes.DIAMOND))
            .build();
        DataTest.checkOfferData(villager, Keys.TRADE_OFFERS, List.of(tradeOffer));

        DataTest.checkOfferData(villager, Keys.TRANSIENT, true);

        DataTest.checkOfferData(villager, Keys.VILLAGER_TYPE, VillagerTypes.SWAMP.get());
        DataTest.checkOfferData(zombieVillager, Keys.VILLAGER_TYPE, VillagerTypes.SWAMP.get());
    }

    @Test
    public void testOcelot() {
        final Entity ocelot = this.location.createEntity(EntityTypes.OCELOT.get());
        DataTest.checkOfferData(ocelot, Keys.IS_TRUSTING, true);
    }

    @Disabled
    @Test
    public void testItemStackSnapshot() {
        final ItemStackSnapshot itemStack = ItemStack.of(ItemTypes.GOLDEN_APPLE).asImmutable();
        final Entity itemFrame = this.location.createEntity(EntityTypes.ITEM_FRAME.get());
        final Entity item = this.location.createEntity(EntityTypes.ITEM.get());
        final Entity potion = this.location.createEntity(EntityTypes.POTION.get());

        DataTest.checkOfferData(item, Keys.ITEM_STACK_SNAPSHOT, itemStack);
        DataTest.checkOfferData(itemFrame, Keys.ITEM_STACK_SNAPSHOT, itemStack);
        DataTest.checkOfferData(potion, Keys.ITEM_STACK_SNAPSHOT, itemStack);

        DataTest.checkOfferData(item, Keys.PICKUP_DELAY, Ticks.of(5));
    }

    @Disabled
    @Test
    public void testItemFrame() {
        final Entity itemFrame = this.location.createEntity(EntityTypes.ITEM_FRAME.get());
        DataTest.checkOfferData(itemFrame, Keys.ORIENTATION, Orientations.LEFT.get());
    }

    @Test
    public void testVex() {
        final Entity vex = this.location.createEntity(EntityTypes.VEX.get());
        DataTest.checkOfferData(vex, Keys.LIFE_TICKS, Ticks.of(10));
    }

    @Disabled
    @Test
    public void testLlama() {
        final Entity llama = this.location.createEntity(EntityTypes.LLAMA.get());
        DataTest.checkOfferData(llama, Keys.LLAMA_TYPE, LlamaTypes.BROWN.get());
        DataTest.checkOfferData(llama, Keys.STRENGTH, 10);
    }

    @Test
    public void testMooshroom() {
        final Entity mooshroom = this.location.createEntity(EntityTypes.MOOSHROOM.get());
        DataTest.checkOfferData(mooshroom, Keys.MOOSHROOM_TYPE, MooshroomTypes.BROWN.get());
        DataTest.checkOfferData(mooshroom, Keys.MOOSHROOM_TYPE, MooshroomTypes.RED.get());
    }

    @Test
    public void testParrot() {
        final Entity parrot = this.location.createEntity(EntityTypes.PARROT.get());
        final UUID playerId = UUID.randomUUID();

        DataTest.checkOfferData(parrot, Keys.PARROT_TYPE, ParrotTypes.RED_BLUE.get());

        DataTest.checkOfferData(parrot, Keys.OWNER, playerId);
        DataTest.checkOfferData(parrot, Keys.OWNER, null);
    }

    @Test
    public void testPhantom() {
        final Entity phantom = this.location.createEntity(EntityTypes.PHANTOM.get());
        DataTest.checkOfferData(phantom, Keys.PHANTOM_PHASE, PhantomPhases.CIRCLE.get());
        DataTest.checkOfferData(phantom, Keys.PHANTOM_PHASE, PhantomPhases.SWOOP.get());
    }

    @Test
    public void testRabbit() {
        final Entity rabbit = this.location.createEntity(EntityTypes.RABBIT.get());
        DataTest.checkOfferData(rabbit, Keys.RABBIT_TYPE, RabbitTypes.GOLD.get());
    }

    @Test
    public void testEndCrystal() {
        final Entity endCrystal = this.location.createEntity(EntityTypes.END_CRYSTAL.get());
        DataTest.checkOfferData(endCrystal, Keys.SHOW_BOTTOM, true);
        DataTest.checkOfferData(endCrystal, Keys.TARGET_POSITION, Vector3i.ONE);
    }

    @Test
    public void testSlime() {
        final Entity slime = this.location.createEntity(EntityTypes.SLIME.get());
        DataTest.checkOfferData(slime, Keys.SIZE, 10);
    }

    @Test
    public void testWillShatter() {
        DataTest.checkOfferData(this.location.createEntity(EntityTypes.EYE_OF_ENDER.get()), Keys.WILL_SHATTER, true);
    }

    @Disabled
    @Test
    public void testWither() {
        final Entity wither = this.location.createEntity(EntityTypes.WITHER.get());
        final Entity pig = this.location.createEntity(EntityTypes.PIG.get());
        DataTest.checkOfferData(wither, Keys.WITHER_TARGETS, List.of(pig));
    }

    @Test
    public void testEvoker() {
        final Entity evoker = this.location.createEntity(EntityTypes.EVOKER.get());
        final Sheep sheep = this.location.createEntity(EntityTypes.SHEEP.get());
        DataTest.checkOfferData(evoker, Keys.WOLOLO_TARGET, sheep);
    }
}
