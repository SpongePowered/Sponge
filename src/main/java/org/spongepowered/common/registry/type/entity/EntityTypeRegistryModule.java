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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.type.OcelotTypes;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.SkeletonTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class EntityTypeRegistryModule implements ExtraClassCatalogRegistryModule<EntityType, Entity>, SpongeAdditionalCatalogRegistryModule<EntityType> {

    @RegisterCatalog(EntityTypes.class)
    protected final Map<String, EntityType> entityTypeMappings = Maps.newHashMap();

    public final Map<Class<? extends Entity>, EntityType> entityClassToTypeMappings = Maps.newHashMap();

    public static EntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public void registerEntityType(EntityType type) {
        this.entityTypeMappings.put(type.getId(), type);
        this.entityClassToTypeMappings.put(((SpongeEntityType) type).entityClass, type);
    }

    @Override
    public Optional<EntityType> getById(String id) {
        if (!checkNotNull(id).contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.entityTypeMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<EntityType> getAll() {
        return ImmutableList.copyOf(this.entityTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.entityTypeMappings.put("item", newEntityTypeFromName("Item"));
        this.entityTypeMappings.put("experience_orb", newEntityTypeFromName("XPOrb"));
        this.entityTypeMappings.put("leash_hitch", newEntityTypeFromName("LeashKnot"));
        this.entityTypeMappings.put("painting", newEntityTypeFromName("Painting"));
        this.entityTypeMappings.put("arrow", newEntityTypeFromName("Arrow"));
        this.entityTypeMappings.put("snowball", newEntityTypeFromName("Snowball"));
        this.entityTypeMappings.put("fireball", newEntityTypeFromName("LargeFireball", "Fireball"));
        this.entityTypeMappings.put("small_fireball", newEntityTypeFromName("SmallFireball"));
        this.entityTypeMappings.put("ender_pearl", newEntityTypeFromName("ThrownEnderpearl"));
        this.entityTypeMappings.put("eye_of_ender", newEntityTypeFromName("EyeOfEnderSignal"));
        this.entityTypeMappings.put("splash_potion", newEntityTypeFromName("ThrownPotion"));
        this.entityTypeMappings.put("thrown_exp_bottle", newEntityTypeFromName("ThrownExpBottle"));
        this.entityTypeMappings.put("item_frame", newEntityTypeFromName("ItemFrame"));
        this.entityTypeMappings.put("wither_skull", newEntityTypeFromName("WitherSkull"));
        this.entityTypeMappings.put("primed_tnt", newEntityTypeFromName("PrimedTnt"));
        this.entityTypeMappings.put("falling_block", newEntityTypeFromName("FallingSand"));
        this.entityTypeMappings.put("firework", newEntityTypeFromName("FireworksRocketEntity"));
        this.entityTypeMappings.put("armor_stand", newEntityTypeFromName("ArmorStand"));
        this.entityTypeMappings.put("boat", newEntityTypeFromName("Boat"));
        this.entityTypeMappings.put("rideable_minecart", newEntityTypeFromName("MinecartRideable"));
        this.entityTypeMappings.put("chested_minecart", newEntityTypeFromName("MinecartChest"));
        this.entityTypeMappings.put("furnace_minecart", newEntityTypeFromName("MinecartFurnace"));
        this.entityTypeMappings.put("tnt_minecart", newEntityTypeFromName("MinecartTnt", "MinecartTNT"));
        this.entityTypeMappings.put("hopper_minecart", newEntityTypeFromName("MinecartHopper"));
        this.entityTypeMappings.put("mob_spawner_minecart", newEntityTypeFromName("MinecartSpawner"));
        this.entityTypeMappings.put("commandblock_minecart", newEntityTypeFromName("MinecartCommandBlock"));
        this.entityTypeMappings.put("creeper", newEntityTypeFromName("Creeper"));
        this.entityTypeMappings.put("skeleton", newEntityTypeFromName("Skeleton"));
        this.entityTypeMappings.put("spider", newEntityTypeFromName("Spider"));
        this.entityTypeMappings.put("giant", newEntityTypeFromName("Giant"));
        this.entityTypeMappings.put("zombie", newEntityTypeFromName("Zombie"));
        this.entityTypeMappings.put("slime", newEntityTypeFromName("Slime"));
        this.entityTypeMappings.put("ghast", newEntityTypeFromName("Ghast"));
        this.entityTypeMappings.put("pig_zombie", newEntityTypeFromName("PigZombie"));
        this.entityTypeMappings.put("enderman", newEntityTypeFromName("Enderman"));
        this.entityTypeMappings.put("cave_spider", newEntityTypeFromName("CaveSpider"));
        this.entityTypeMappings.put("silverfish", newEntityTypeFromName("Silverfish"));
        this.entityTypeMappings.put("blaze", newEntityTypeFromName("Blaze"));
        this.entityTypeMappings.put("magma_cube", newEntityTypeFromName("LavaSlime"));
        this.entityTypeMappings.put("ender_dragon", newEntityTypeFromName("EnderDragon"));
        this.entityTypeMappings.put("wither", newEntityTypeFromName("WitherBoss"));
        this.entityTypeMappings.put("bat", newEntityTypeFromName("Bat"));
        this.entityTypeMappings.put("witch", newEntityTypeFromName("Witch"));
        this.entityTypeMappings.put("endermite", newEntityTypeFromName("Endermite"));
        this.entityTypeMappings.put("guardian", newEntityTypeFromName("Guardian"));
        this.entityTypeMappings.put("pig", newEntityTypeFromName("Pig"));
        this.entityTypeMappings.put("sheep", newEntityTypeFromName("Sheep"));
        this.entityTypeMappings.put("cow", newEntityTypeFromName("Cow"));
        this.entityTypeMappings.put("chicken", newEntityTypeFromName("Chicken"));
        this.entityTypeMappings.put("squid", newEntityTypeFromName("Squid"));
        this.entityTypeMappings.put("wolf", newEntityTypeFromName("Wolf"));
        this.entityTypeMappings.put("mushroom_cow", newEntityTypeFromName("MushroomCow"));
        this.entityTypeMappings.put("snowman", newEntityTypeFromName("SnowMan"));
        this.entityTypeMappings.put("ocelot", newEntityTypeFromName("Ocelot", "Ozelot"));
        this.entityTypeMappings.put("iron_golem", newEntityTypeFromName("VillagerGolem"));
        this.entityTypeMappings.put("horse", newEntityTypeFromName("EntityHorse"));
        this.entityTypeMappings.put("rabbit", newEntityTypeFromName("Rabbit"));
        this.entityTypeMappings.put("villager", newEntityTypeFromName("Villager"));
        this.entityTypeMappings.put("ender_crystal", newEntityTypeFromName("EnderCrystal"));
        this.entityTypeMappings.put("egg", new SpongeEntityType(-1, "Egg", EntityEgg.class, new SpongeTranslation("item.egg.name")));
        this.entityTypeMappings.put("fishing_hook", new SpongeEntityType(-2, "FishingHook", EntityFishHook.class, new SpongeTranslation("item.fishingRod.name")));
        this.entityTypeMappings.put("lightning", new SpongeEntityType(-3, "Lightning", EntityLightningBolt.class, null));
        this.entityTypeMappings.put("weather", new SpongeEntityType(-4, "Weather", EntityWeatherEffect.class, new SpongeTranslation("soundCategory.weather")));
        this.entityTypeMappings.put("player", new SpongeEntityType(-5, "Player", EntityPlayerMP.class, new SpongeTranslation("soundCategory.player")));
        this.entityTypeMappings.put("complex_part", new SpongeEntityType(-6, "ComplexPart", EntityDragonPart.class, null));
        this.entityTypeMappings.put("human", registerCustomEntity(EntityHuman.class, "Human", -7, null));
    }

    @SuppressWarnings("unchecked")
    private SpongeEntityType newEntityTypeFromName(String spongeName, String mcName) {
        return new SpongeEntityType(EntityList.stringToIDMapping.get(mcName), spongeName,
                EntityList.stringToClassMapping.get(mcName),
                new SpongeTranslation("entity." + mcName + ".name"));
    }

    private SpongeEntityType newEntityTypeFromName(String name) {
        return newEntityTypeFromName(name, name);
    }

    @SuppressWarnings("unchecked")
    private SpongeEntityType registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int entityId, Translation translation) {
        String entityFullName = String.format("%s.%s", SpongeImpl.ECOSYSTEM_NAME, entityName);
        EntityList.classToStringMapping.put(entityClass, entityFullName);
        EntityList.stringToClassMapping.put(entityFullName, entityClass);
        return new SpongeEntityType(entityId, entityName, SpongeImpl.ECOSYSTEM_NAME, entityClass, translation);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();
        RegistryHelper.mapFields(EntityTypes.class, fieldName -> {
            if (fieldName.equals("UNKNOWN")) {
                return SpongeEntityType.UNKNOWN;
            }
            EntityType entityType = this.entityTypeMappings.get(fieldName.toLowerCase(Locale.ENGLISH));
            this.entityClassToTypeMappings.put(((SpongeEntityType) entityType).entityClass, entityType);
            // remove old mapping
            this.entityTypeMappings.remove(fieldName.toLowerCase(Locale.ENGLISH));
            // add new mapping with minecraft id
            this.entityTypeMappings.put(entityType.getId(), entityType);
            return entityType;
        });
        this.entityTypeMappings.put("minecraft:ozelot", this.entityTypeMappings.get("minecraft:ocelot"));

        RegistryHelper.mapFields(SkeletonTypes.class, SpongeEntityConstants.SKELETON_TYPES);
        RegistryHelper.mapFields(HorseColors.class, SpongeEntityConstants.HORSE_COLORS);
        RegistryHelper.mapFields(HorseVariants.class, SpongeEntityConstants.HORSE_VARIANTS);
        RegistryHelper.mapFields(HorseStyles.class, SpongeEntityConstants.HORSE_STYLES);
        RegistryHelper.mapFields(OcelotTypes.class, SpongeEntityConstants.OCELOT_TYPES);
        RegistryHelper.mapFields(RabbitTypes.class, SpongeEntityConstants.RABBIT_TYPES);
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(EntityType extraCatalog) {
        this.entityTypeMappings.put(extraCatalog.getId(), extraCatalog);
        this.entityClassToTypeMappings.put(((SpongeEntityType) extraCatalog).entityClass, extraCatalog);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends Entity> mappedClass) {
        return false;
    }

    @Override
    public EntityType getForClass(Class<? extends Entity> clazz) {
        EntityType type = this.entityClassToTypeMappings.get(clazz);
        if (type == null) {
            type = EntityTypes.UNKNOWN;
        }
        return type;
    }

    EntityTypeRegistryModule() {
    }

    private static final class Holder {

        static final EntityTypeRegistryModule INSTANCE = new EntityTypeRegistryModule();
    }

    public Optional<EntityType> getEntity(Class<? extends org.spongepowered.api.entity.Entity> entityClass) {
        for (EntityType type : this.entityTypeMappings.values()) {
            if (entityClass.isAssignableFrom(type.getEntityClass())) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
