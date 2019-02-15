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
package org.spongepowered.common.registry.type.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityDrowned;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPhantom;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCod;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityDolphin;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityPufferFish;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySalmon;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTropicalFish;
import net.minecraft.entity.passive.EntityTurtle;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityEvokerFangs;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityTrident;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.entity.UnknownEntityType;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@RegisterCatalog(EntityTypes.class)
public final class EntityTypeRegistryModule extends AbstractCatalogRegistryModule<EntityType>
    implements ExtraClassCatalogRegistryModule<EntityType, Entity>, SpongeAdditionalCatalogRegistryModule<EntityType> {

    public final Map<Class<? extends Entity>, EntityType> entityClassToTypeMappings = Maps.newHashMap();
    private final Set<FutureRegistration> customEntities = new HashSet<>();

    public static EntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        this.register(net.minecraft.entity.EntityType.AREA_EFFECT_CLOUD, EntityAreaEffectCloud.class);
        this.register(net.minecraft.entity.EntityType.ARMOR_STAND, EntityArmorStand.class);
        this.register(net.minecraft.entity.EntityType.ARROW, EntityTippedArrow.class);
        this.register(net.minecraft.entity.EntityType.BAT, EntityBat.class);
        this.register(net.minecraft.entity.EntityType.BLAZE, EntityBlaze.class);
        this.register(net.minecraft.entity.EntityType.BOAT, EntityBoat.class);
        this.register(net.minecraft.entity.EntityType.CAVE_SPIDER, EntityCaveSpider.class);
        this.register(net.minecraft.entity.EntityType.CHICKEN, EntityChicken.class);
        this.register(net.minecraft.entity.EntityType.COD, EntityCod.class);
        this.register(net.minecraft.entity.EntityType.COW, EntityCow.class);
        this.register(net.minecraft.entity.EntityType.CREEPER, EntityCreeper.class);
        this.register(net.minecraft.entity.EntityType.DONKEY, EntityDonkey.class);
        this.register(net.minecraft.entity.EntityType.DOLPHIN, EntityDolphin.class);
        this.register(net.minecraft.entity.EntityType.DRAGON_FIREBALL, EntityDragonFireball.class);
        this.register(net.minecraft.entity.EntityType.DROWNED, EntityDrowned.class);
        this.register(net.minecraft.entity.EntityType.ELDER_GUARDIAN, EntityElderGuardian.class);
        this.register(net.minecraft.entity.EntityType.END_CRYSTAL, EntityEnderCrystal.class);
        this.register(net.minecraft.entity.EntityType.ENDER_DRAGON, EntityDragon.class);
        this.register(net.minecraft.entity.EntityType.ENDERMAN, EntityEnderman.class);
        this.register(net.minecraft.entity.EntityType.ENDERMITE, EntityEndermite.class);
        this.register(net.minecraft.entity.EntityType.EVOKER_FANGS, EntityEvokerFangs.class);
        this.register(net.minecraft.entity.EntityType.EVOKER, EntityEvoker.class);
        this.register(net.minecraft.entity.EntityType.EXPERIENCE_ORB, EntityXPOrb.class);
        this.register(net.minecraft.entity.EntityType.EYE_OF_ENDER, EntityEnderEye.class);
        this.register(net.minecraft.entity.EntityType.FALLING_BLOCK, EntityFallingBlock.class);
        this.register(net.minecraft.entity.EntityType.FIREWORK_ROCKET, EntityFireworkRocket.class);
        this.register(net.minecraft.entity.EntityType.GHAST, EntityGhast.class);
        this.register(net.minecraft.entity.EntityType.GIANT, EntityGiantZombie.class);
        this.register(net.minecraft.entity.EntityType.GUARDIAN, EntityGuardian.class);
        this.register(net.minecraft.entity.EntityType.HORSE, EntityHorse.class);
        this.register(net.minecraft.entity.EntityType.HUSK, EntityHusk.class);
        this.register(net.minecraft.entity.EntityType.ILLUSIONER, EntityIllusionIllager.class);
        this.register(net.minecraft.entity.EntityType.ITEM, EntityItem.class);
        this.register(net.minecraft.entity.EntityType.ITEM_FRAME, EntityItemFrame.class);
        this.register(net.minecraft.entity.EntityType.FIREBALL, EntityLargeFireball.class);
        this.register(net.minecraft.entity.EntityType.LEASH_KNOT, EntityLeashKnot.class);
        this.register(net.minecraft.entity.EntityType.LLAMA, EntityLlama.class);
        this.register(net.minecraft.entity.EntityType.LLAMA_SPIT, EntityLlamaSpit.class);
        this.register(net.minecraft.entity.EntityType.MAGMA_CUBE, EntityMagmaCube.class);
        this.register(net.minecraft.entity.EntityType.MINECART, EntityMinecartEmpty.class);
        this.register(net.minecraft.entity.EntityType.CHEST_MINECART, EntityMinecartChest.class);
        this.register(net.minecraft.entity.EntityType.COMMAND_BLOCK_MINECART, EntityMinecartCommandBlock.class);
        this.register(net.minecraft.entity.EntityType.FURNACE_MINECART, EntityMinecartFurnace.class);
        this.register(net.minecraft.entity.EntityType.HOPPER_MINECART, EntityMinecartHopper.class);
        this.register(net.minecraft.entity.EntityType.SPAWNER_MINECART, EntityMinecartMobSpawner.class);
        this.register(net.minecraft.entity.EntityType.TNT_MINECART, EntityMinecartTNT.class);
        this.register(net.minecraft.entity.EntityType.MULE, EntityMule.class);
        this.register(net.minecraft.entity.EntityType.MOOSHROOM, EntityMooshroom.class);
        this.register(net.minecraft.entity.EntityType.OCELOT, EntityOcelot.class);
        this.register(net.minecraft.entity.EntityType.PAINTING, EntityPainting.class);
        this.register(net.minecraft.entity.EntityType.PARROT, EntityParrot.class);
        this.register(net.minecraft.entity.EntityType.PIG, EntityPig.class);
        this.register(net.minecraft.entity.EntityType.PUFFERFISH, EntityPufferFish.class);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_PIGMAN, EntityPigZombie.class);
        this.register(net.minecraft.entity.EntityType.POLAR_BEAR, EntityPolarBear.class);
        this.register(net.minecraft.entity.EntityType.TNT, EntityTNTPrimed.class);
        this.register(net.minecraft.entity.EntityType.RABBIT, EntityRabbit.class);
        this.register(net.minecraft.entity.EntityType.SALMON, EntitySalmon.class);
        this.register(net.minecraft.entity.EntityType.SHEEP, EntitySheep.class);
        this.register(net.minecraft.entity.EntityType.SHULKER, EntityShulker.class);
        this.register(net.minecraft.entity.EntityType.SHULKER_BULLET, EntityShulkerBullet.class);
        this.register(net.minecraft.entity.EntityType.SILVERFISH, EntitySilverfish.class);
        this.register(net.minecraft.entity.EntityType.SKELETON, EntitySkeleton.class);
        this.register(net.minecraft.entity.EntityType.SKELETON_HORSE, EntitySkeletonHorse.class);
        this.register(net.minecraft.entity.EntityType.SLIME, EntitySlime.class);
        this.register(net.minecraft.entity.EntityType.SMALL_FIREBALL, EntitySmallFireball.class);
        this.register(net.minecraft.entity.EntityType.SNOW_GOLEM, EntitySnowman.class);
        this.register(net.minecraft.entity.EntityType.SNOWBALL, EntitySnowball.class);
        this.register(net.minecraft.entity.EntityType.SPECTRAL_ARROW, EntitySpectralArrow.class);
        this.register(net.minecraft.entity.EntityType.SPIDER, EntitySpider.class);
        this.register(net.minecraft.entity.EntityType.SQUID, EntitySquid.class);
        this.register(net.minecraft.entity.EntityType.STRAY, EntityStray.class);
        this.register(net.minecraft.entity.EntityType.TROPICAL_FISH, EntityTropicalFish.class);
        this.register(net.minecraft.entity.EntityType.TURTLE, EntityTurtle.class);
        this.register(net.minecraft.entity.EntityType.EGG, EntityEgg.class);
        this.register(net.minecraft.entity.EntityType.ENDER_PEARL, EntityEnderPearl.class);
        this.register(net.minecraft.entity.EntityType.EXPERIENCE_BOTTLE, EntityExpBottle.class);
        this.register(net.minecraft.entity.EntityType.POTION, EntityPotion.class);
        this.register(net.minecraft.entity.EntityType.VEX, EntityVex.class);
        this.register(net.minecraft.entity.EntityType.VILLAGER, EntityVillager.class);
        this.register(net.minecraft.entity.EntityType.IRON_GOLEM, EntityIronGolem.class);
        this.register(net.minecraft.entity.EntityType.VINDICATOR, EntityVindicator.class);
        this.register(net.minecraft.entity.EntityType.WITCH, EntityWitch.class);
        this.register(net.minecraft.entity.EntityType.WITHER, EntityWither.class);
        this.register(net.minecraft.entity.EntityType.WITHER_SKELETON, EntityWitherSkeleton.class);
        this.register(net.minecraft.entity.EntityType.WITHER_SKULL, EntityWitherSkull.class);
        this.register(net.minecraft.entity.EntityType.WOLF, EntityWolf.class);
        this.register(net.minecraft.entity.EntityType.ZOMBIE, EntityZombie.class);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_HORSE, EntityZombieHorse.class);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_VILLAGER, EntityZombieVillager.class);
        this.register(net.minecraft.entity.EntityType.PHANTOM, EntityPhantom.class);
        this.register(net.minecraft.entity.EntityType.LIGHTNING_BOLT, EntityLightningBolt.class);
        this.register(net.minecraft.entity.EntityType.PLAYER, EntityPlayer.class);
        this.register(net.minecraft.entity.EntityType.FISHING_BOBBER, EntityFishHook.class);
        this.register(net.minecraft.entity.EntityType.TRIDENT, EntityTrident.class);

        this.registerCustom(new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, "human"), EntityHuman::new, EntityHuman.class, 300, "Human"); // TODO: This needs work!

        this.register(CatalogKey.of("unknown", "unknown"), UnknownEntityType.INSTANCE);
    }

    private <T extends Entity> void register(final net.minecraft.entity.EntityType<T> type, final Class<T> klass) {
        final ResourceLocation key = net.minecraft.entity.EntityType.getId(type);
        final SpongeEntityType<T, ?> sponge = new SpongeEntityType<>(key, type, klass);
        this.register((CatalogKey) (Object) key, sponge);
        this.entityClassToTypeMappings.put(klass, sponge);
        KeyRegistryModule.getInstance().registerForEntityClass(klass);
    }

    private <T extends Entity> void registerCustom(final ResourceLocation key, final Function<? super World, ? extends T> factory, final Class<T> klass, final int networkId, final String oldName) {
        final net.minecraft.entity.EntityType<T> type = net.minecraft.entity.EntityType.Builder.create(klass, factory).build(key.toString());
        final SpongeEntityType<T, ?> sponge = new SpongeEntityType<>(key, type, klass, networkId);
        this.customEntities.add(new FutureRegistration(networkId, key, klass, oldName));
        this.register((CatalogKey) (Object) key, sponge);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();
        RegistryHelper.mapFields(EntityTypes.class, fieldName -> {
            if (fieldName.equals("UNKNOWN")) {
                return UnknownEntityType.INSTANCE;
            }
            final CatalogKey key = fieldName.equalsIgnoreCase("human") ? CatalogKey.sponge(fieldName) : CatalogKey.minecraft(fieldName);
            EntityType entityType = this.map.get(key);
            this.entityClassToTypeMappings.putIfAbsent(((SpongeEntityType<?, ?>) entityType).entityClass, entityType);
            return entityType;
        });
        this.map.put(CatalogKey.minecraft("ozelot"), this.map.get(CatalogKey.minecraft("ocelot")));

    }

    @Override
    protected boolean filterAll(final EntityType type) {
        return type != UnknownEntityType.INSTANCE;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(EntityType extraCatalog) {
        this.map.put(extraCatalog.getKey(), extraCatalog);
        this.entityClassToTypeMappings.put(((SpongeEntityType<?, ?>) extraCatalog).entityClass, extraCatalog);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends Entity> mappedClass) {
        return false;
    }

    @Override
    public EntityType getForClass(Class<? extends Entity> clazz) {
        EntityType type = this.entityClassToTypeMappings.get(clazz);
        if (type == null) {
            SpongeImpl.getLogger().warn(String.format("No entity type is registered for class %s", clazz.getName()));

            type = EntityTypes.UNKNOWN;
            this.entityClassToTypeMappings.put(clazz, type);
        }
        return type;
    }

    EntityTypeRegistryModule() {
    }

    private static final class Holder {

        static final EntityTypeRegistryModule INSTANCE = new EntityTypeRegistryModule();
    }

    public Optional<EntityType> getEntity(Class<? extends org.spongepowered.api.entity.Entity> entityClass) {
        for (EntityType type : this.map.values()) {
            if (entityClass.isAssignableFrom(type.getEntityClass())) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public Set<FutureRegistration> getCustomEntities() {
        return ImmutableSet.copyOf(this.customEntities);
    }

    public static final class FutureRegistration {

        public final int id;
        public final ResourceLocation name;
        public final Class<? extends Entity> type;
        public final String oldName;

        FutureRegistration(int id, ResourceLocation name, Class<? extends Entity> type, String oldName) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.oldName = oldName;
        }
    }

}
