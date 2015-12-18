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
package org.spongepowered.common.registry.type.effect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.common.effect.sound.SpongeSound;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class SoundRegistryModule implements CatalogRegistryModule<SoundType> {

    @RegisterCatalog(SoundTypes.class)
    private final Map<String, SoundType> soundNames = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        final Map<String, String> soundMappings = Maps.newHashMap();
        soundMappings.put("ambience_cave", "ambient.cave.cave");
        soundMappings.put("ambience_rain", "ambient.weather.rain");
        soundMappings.put("ambience_thunder", "ambient.weather.thunder");
        soundMappings.put("anvil_break", "random.anvil_break");
        soundMappings.put("anvil_land", "random.anvil_land");
        soundMappings.put("anvil_use", "random.anvil_use");
        soundMappings.put("arrow_hit", "random.bowhit");
        soundMappings.put("burp", "random.burp");
        soundMappings.put("chest_close", "random.chestclosed");
        soundMappings.put("chest_open", "random.chestopen");
        soundMappings.put("click", "random.click");
        soundMappings.put("door_close", "random.door_close");
        soundMappings.put("door_open", "random.door_open");
        soundMappings.put("drink", "random.drink");
        soundMappings.put("eat", "random.eat");
        soundMappings.put("explode", "random.explode");
        soundMappings.put("fall_big", "game.player.hurt.fall.big");
        soundMappings.put("fall_small", "game.player.hurt.fall.small");
        soundMappings.put("fire", "fire.fire");
        soundMappings.put("fire_ignite", "fire.ignite");
        soundMappings.put("firecharge_use", "item.fireCharge.use");
        soundMappings.put("fizz", "random.fizz");
        soundMappings.put("fuse", "game.tnt.primed");
        soundMappings.put("glass", "dig.glass");
        soundMappings.put("gui_button", "gui.button.press");
        soundMappings.put("hurt_flesh", "game.player.hurt");
        soundMappings.put("item_break", "random.break");
        soundMappings.put("item_pickup", "random.pop");
        soundMappings.put("lava", "liquid.lava");
        soundMappings.put("lava_pop", "liquid.lavapop");
        soundMappings.put("level_up", "random.levelup");
        soundMappings.put("minecart_base", "minecart.base");
        soundMappings.put("minecart_inside", "minecart.inside");
        soundMappings.put("music_game", "music.game");
        soundMappings.put("music_creative", "music.game.creative");
        soundMappings.put("music_end", "music.game.end");
        soundMappings.put("music_credits", "music.game.end.credits");
        soundMappings.put("music_dragon", "music.game.end.dragon");
        soundMappings.put("music_nether", "music.game.nether");
        soundMappings.put("music_menu", "music.menu");
        soundMappings.put("note_bass", "note.bass");
        soundMappings.put("note_piano", "note.harp");
        soundMappings.put("note_bass_drum", "note.bd");
        soundMappings.put("note_sticks", "note.hat");
        soundMappings.put("note_bass_guitar", "note.bassattack");
        soundMappings.put("note_snare_drum", "note.snare");
        soundMappings.put("note_pling", "note.pling");
        soundMappings.put("orb_pickup", "random.orb");
        soundMappings.put("piston_extend", "tile.piston.out");
        soundMappings.put("piston_retract", "tile.piston.in");
        soundMappings.put("portal", "portal.portal");
        soundMappings.put("portal_travel", "portal.travel");
        soundMappings.put("portal_trigger", "portal.trigger");
        soundMappings.put("potion_smash", "game.potion.smash");
        soundMappings.put("records_11", "records.11");
        soundMappings.put("records_13", "records.13");
        soundMappings.put("records_blocks", "records.blocks");
        soundMappings.put("records_cat", "records.cat");
        soundMappings.put("records_chirp", "records.chirp");
        soundMappings.put("records_far", "records.far");
        soundMappings.put("records_mall", "records.mall");
        soundMappings.put("records_mellohi", "records.mellohi");
        soundMappings.put("records_stal", "records.stal");
        soundMappings.put("records_strad", "records.strad");
        soundMappings.put("records_wait", "records.wait");
        soundMappings.put("records_ward", "records.ward");
        soundMappings.put("shoot_arrow", "random.bow");
        soundMappings.put("splash", "random.splash");
        soundMappings.put("splash2", "game.player.swim.splash");
        soundMappings.put("step_grass", "step.grass");
        soundMappings.put("step_gravel", "step.gravel");
        soundMappings.put("step_ladder", "step.ladder");
        soundMappings.put("step_sand", "step.sand");
        soundMappings.put("step_snow", "step.snow");
        soundMappings.put("step_stone", "step.stone");
        soundMappings.put("step_wood", "step.wood");
        soundMappings.put("step_wool", "step.cloth");
        soundMappings.put("swim", "game.player.swim");
        soundMappings.put("water", "liquid.water");
        soundMappings.put("wood_click", "random.wood_click");
        soundMappings.put("bat_death", "mob.bat.death");
        soundMappings.put("bat_hurt", "mob.bat.hurt");
        soundMappings.put("bat_idle", "mob.bat.idle");
        soundMappings.put("bat_loop", "mob.bat.loop");
        soundMappings.put("bat_takeoff", "mob.bat.takeoff");
        soundMappings.put("blaze_breath", "mob.blaze.breathe");
        soundMappings.put("blaze_death", "mob.blaze.death");
        soundMappings.put("blaze_hit", "mob.blaze.hit");
        soundMappings.put("cat_hiss", "mob.cat.hiss");
        soundMappings.put("cat_hit", "mob.cat.hitt");
        soundMappings.put("cat_meow", "mob.cat.meow");
        soundMappings.put("cat_purr", "mob.cat.purr");
        soundMappings.put("cat_purreow", "mob.cat.purreow");
        soundMappings.put("chicken_idle", "mob.chicken.say");
        soundMappings.put("chicken_hurt", "mob.chicken.hurt");
        soundMappings.put("chicken_egg_pop", "mob.chicken.plop");
        soundMappings.put("chicken_walk", "mob.chicken.step");
        soundMappings.put("cow_idle", "mob.cow.say");
        soundMappings.put("cow_hurt", "mob.cow.hurt");
        soundMappings.put("cow_walk", "mob.cow.step");
        soundMappings.put("creeper_hiss", "creeper.primed");
        soundMappings.put("creeper_hit", "mob.creeper.say");
        soundMappings.put("creeper_death", "mob.creeper.death");
        soundMappings.put("enderdragon_death", "mob.enderdragon.end");
        soundMappings.put("enderdragon_growl", "mob.enderdragon.growl");
        soundMappings.put("enderdragon_hit", "mob.enderdragon.hit");
        soundMappings.put("enderdragon_wings", "mob.enderdragon.wings");
        soundMappings.put("enderman_death", "mob.endermen.death");
        soundMappings.put("enderman_hit", "mob.endermen.hit");
        soundMappings.put("enderman_idle", "mob.endermen.idle");
        soundMappings.put("enderman_teleport", "mob.endermen.portal");
        soundMappings.put("enderman_scream", "mob.endermen.scream");
        soundMappings.put("enderman_stare", "mob.endermen.stare");
        soundMappings.put("ghast_scream", "mob.ghast.scream");
        soundMappings.put("ghast_scream2", "mob.ghast.affectionate_scream");
        soundMappings.put("ghast_charge", "mob.ghast.charge");
        soundMappings.put("ghast_death", "mob.ghast.death");
        soundMappings.put("ghast_fireball", "mob.ghast.fireball");
        soundMappings.put("ghast_moan", "mob.ghast.moan");
        soundMappings.put("guardian_idle", "mob.guardian.idle");
        soundMappings.put("guardian_attack", "mob.guardian.attack");
        soundMappings.put("guardian_curse", "mob.guardian.curse");
        soundMappings.put("guardian_flop", "mob.guardian.flop");
        soundMappings.put("guardian_elder_idle", "mob.guardian.elder.idle");
        soundMappings.put("guardian_land_idle", "mob.guardian.land.idle");
        soundMappings.put("guardian_hit", "mob.guardian.hit");
        soundMappings.put("guardian_elder_hit", "mob.guardian.elder.hit");
        soundMappings.put("guardian_land_hit", "mob.guardian.land.hit");
        soundMappings.put("guardian_death", "mob.guardian.death");
        soundMappings.put("guardian_elder_death", "mob.guardian.elder.death");
        soundMappings.put("guardian_land_death", "mob.guardian.land.death");
        soundMappings.put("hostile_death", "game.hostile.die");
        soundMappings.put("hostile_hurt", "game.hostile.hurt");
        soundMappings.put("hostile_fall_big", "game.hostile.hurt.fall.big");
        soundMappings.put("hostile_fall_small", "game.hostile.hurt.fall.small");
        soundMappings.put("hostile_swim", "game.hostile.swim");
        soundMappings.put("hostile_splash", "game.hostile.swim.splash");
        soundMappings.put("irongolem_death", "mob.irongolem.death");
        soundMappings.put("irongolem_hit", "mob.irongolem.hit");
        soundMappings.put("irongolem_throw", "mob.irongolem.throw");
        soundMappings.put("irongolem_walk", "mob.irongolem.walk");
        soundMappings.put("magmacube_walk", "mob.magmacube.big");
        soundMappings.put("magmacube_walk2", "mob.magmacube.small");
        soundMappings.put("magmacube_jump", "mob.magmacube.jump");
        soundMappings.put("neutral_death", "game.neutral.die");
        soundMappings.put("neutral_hurt", "game.neutral.hurt");
        soundMappings.put("neutral_fall_big", "game.neutral.hurt.fall.big");
        soundMappings.put("neutral_fall_small", "game.neutral.hurt.fall.small");
        soundMappings.put("neutral_swim", "game.neutral.swim");
        soundMappings.put("neutral_splash", "game.neutral.swim.splash");
        soundMappings.put("pig_idle", "mob.pig.say");
        soundMappings.put("pig_death", "mob.pig.death");
        soundMappings.put("pig_walk", "mob.pig.step");
        soundMappings.put("player_death", "game.player.die");
        soundMappings.put("rabbit_idle", "mob.rabbit.idle");
        soundMappings.put("rabbit_hurt", "mob.rabbit.hurt");
        soundMappings.put("rabbit_hop", "mob.rabbit.hop");
        soundMappings.put("rabbit_death", "mob.rabbit.death");
        soundMappings.put("sheep_idle", "mob.sheep.say");
        soundMappings.put("sheep_shear", "mob.sheep.shear");
        soundMappings.put("sheep_walk", "mob.sheep.step");
        soundMappings.put("silverfish_hit", "mob.silverfish.hit");
        soundMappings.put("silverfish_death", "mob.silverfish.kill");
        soundMappings.put("silverfish_idle", "mob.silverfish.say");
        soundMappings.put("silverfish_walk", "mob.silverfish.step");
        soundMappings.put("skeleton_idle", "mob.skeleton.say");
        soundMappings.put("skeleton_death", "mob.skeleton.death");
        soundMappings.put("skeleton_hurt", "mob.skeleton.hurt");
        soundMappings.put("skeleton_walk", "mob.skeleton.step");
        soundMappings.put("slime_attack", "mob.slime.attack");
        soundMappings.put("slime_walk", "mob.slime.big");
        soundMappings.put("slime_walk2", "mob.slime.small");
        soundMappings.put("spider_idle", "mob.spider.say");
        soundMappings.put("spider_death", "mob.spider.death");
        soundMappings.put("spider_walk", "mob.spider.step");
        soundMappings.put("wither_death", "mob.wither.death");
        soundMappings.put("wither_hurt", "mob.wither.hurt");
        soundMappings.put("wither_idle", "mob.wither.idle");
        soundMappings.put("wither_shoot", "mob.wither.shoot");
        soundMappings.put("wither_spawn", "mob.wither.spawn");
        soundMappings.put("wolf_bark", "mob.wolf.bark");
        soundMappings.put("wolf_death", "mob.wolf.death");
        soundMappings.put("wolf_growl", "mob.wolf.growl");
        soundMappings.put("wolf_howl", "mob.wolf.howl");
        soundMappings.put("wolf_hurt", "mob.wolf.hurt");
        soundMappings.put("wolf_pant", "mob.wolf.panting");
        soundMappings.put("wolf_shake", "mob.wolf.shake");
        soundMappings.put("wolf_walk", "mob.wolf.step");
        soundMappings.put("wolf_whine", "mob.wolf.whine");
        soundMappings.put("zombie_metal", "mob.zombie.metal");
        soundMappings.put("zombie_wood", "mob.zombie.wood");
        soundMappings.put("zombie_woodbreak", "mob.zombie.woodbreak");
        soundMappings.put("zombie_idle", "mob.zombie.say");
        soundMappings.put("zombie_death", "mob.zombie.death");
        soundMappings.put("zombie_hurt", "mob.zombie.hurt");
        soundMappings.put("zombie_infect", "mob.zombie.infect");
        soundMappings.put("zombie_unfect", "mob.zombie.unfect");
        soundMappings.put("zombie_remedy", "mob.zombie.remedy");
        soundMappings.put("zombie_walk", "mob.zombie.step");
        soundMappings.put("zombie_pig_idle", "mob.zombiepig.zpig");
        soundMappings.put("zombie_pig_angry", "mob.zombiepig.zpigangry");
        soundMappings.put("zombie_pig_death", "mob.zombiepig.zpigdeath");
        soundMappings.put("zombie_pig_hurt", "mob.zombiepig.zpighurt");
        soundMappings.put("dig_wool", "dig.cloth");
        soundMappings.put("dig_grass", "dig.grass");
        soundMappings.put("dig_gravel", "dig.gravel");
        soundMappings.put("dig_sand", "dig.sand");
        soundMappings.put("dig_snow", "dig.snow");
        soundMappings.put("dig_stone", "dig.stone");
        soundMappings.put("dig_wood", "dig.wood");
        soundMappings.put("firework_blast", "fireworks.blast");
        soundMappings.put("firework_blast2", "fireworks.blast_far");
        soundMappings.put("firework_large_blast", "fireworks.largeblast");
        soundMappings.put("firework_large_blast2", "fireworks.largeblast_far");
        soundMappings.put("firework_twinkle", "fireworks.twinkle");
        soundMappings.put("firework_twinkle2", "fireworks.twinkle_far");
        soundMappings.put("firework_launch", "fireworks.launch");
        soundMappings.put("successful_hit", "random.successful_hit");
        soundMappings.put("horse_angry", "mob.horse.angry");
        soundMappings.put("horse_armor", "mob.horse.armor");
        soundMappings.put("horse_breathe", "mob.horse.breathe");
        soundMappings.put("horse_death", "mob.horse.death");
        soundMappings.put("horse_gallop", "mob.horse.gallop");
        soundMappings.put("horse_hit", "mob.horse.hit");
        soundMappings.put("horse_idle", "mob.horse.idle");
        soundMappings.put("horse_jump", "mob.horse.jump");
        soundMappings.put("horse_land", "mob.horse.land");
        soundMappings.put("horse_saddle", "mob.horse.leather");
        soundMappings.put("horse_soft", "mob.horse.soft");
        soundMappings.put("horse_wood", "mob.horse.wood");
        soundMappings.put("donkey_angry", "mob.horse.donkey.angry");
        soundMappings.put("donkey_death", "mob.horse.donkey.death");
        soundMappings.put("donkey_hit", "mob.horse.donkey.hit");
        soundMappings.put("donkey_idle", "mob.horse.donkey.idle");
        soundMappings.put("horse_skeleton_death", "mob.horse.skeleton.death");
        soundMappings.put("horse_skeleton_hit", "mob.horse.skeleton.hit");
        soundMappings.put("horse_skeleton_idle", "mob.horse.skeleton.idle");
        soundMappings.put("horse_zombie_death", "mob.horse.zombie.death");
        soundMappings.put("horse_zombie_hit", "mob.horse.zombie.hit");
        soundMappings.put("horse_zombie_idle", "mob.horse.zombie.idle");
        soundMappings.put("villager_death", "mob.villager.death");
        soundMappings.put("villager_haggle", "mob.villager.haggle");
        soundMappings.put("villager_hit", "mob.villager.hit");
        soundMappings.put("villager_idle", "mob.villager.idle");
        soundMappings.put("villager_no", "mob.villager.no");
        soundMappings.put("villager_yes", "mob.villager.yes");

        soundMappings.forEach((soundName, soundId) -> {
            final SoundType soundType = new SpongeSound(soundName, soundId);
            this.soundNames.put(soundName, soundType);
            this.soundNames.put(soundId, soundType);
        });
    }

    @Override
    public Optional<SoundType> getById(String id) {
        return Optional.ofNullable(this.soundNames.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<SoundType> getAll() {
        return ImmutableList.copyOf(this.soundNames.values());
    }
}
