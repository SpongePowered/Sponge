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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterCatalog(EntityTypes.class)
public final class EntityTypeRegistryModule extends AbstractCatalogRegistryModule<EntityType>
    implements ExtraClassCatalogRegistryModule<EntityType, Entity>, SpongeAdditionalCatalogRegistryModule<EntityType> {


    public final Map<Class<? extends Entity>, EntityType> entityClassToTypeMappings = Maps.newHashMap();
    private final Set<FutureRegistration> customEntities = new HashSet<>();

    public static EntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public void registerEntityType(EntityType type) {
        this.map.put(type.getKey(), type);
        this.entityClassToTypeMappings.put(((SpongeEntityType) type).entityClass, type);
    }

    @Override
    public void registerDefaults() {
        register(CatalogKey.resolve("item"), newEntityTypeFromName("Item"));
        register(CatalogKey.resolve("experience_orb"), newEntityTypeFromName("xp_orb"));
        register(CatalogKey.resolve("area_effect_cloud"), newEntityTypeFromName("area_effect_cloud"));
        register(CatalogKey.resolve("dragon_fireball"), newEntityTypeFromName("dragon_fireball"));
        register(CatalogKey.resolve("leash_hitch"), newEntityTypeFromName("leash_knot"));
        register(CatalogKey.resolve("painting"), newEntityTypeFromName("painting"));
        register(CatalogKey.resolve("tipped_arrow"), newEntityTypeFromName("arrow"));
        register(CatalogKey.resolve("snowball"), newEntityTypeFromName("snowball"));
        register(CatalogKey.resolve("fireball"), newEntityTypeFromName("fireball"));
        register(CatalogKey.resolve("small_fireball"), newEntityTypeFromName("small_fireball"));
        register(CatalogKey.resolve("ender_pearl"), newEntityTypeFromName("ender_pearl"));
        register(CatalogKey.resolve("eye_of_ender"), newEntityTypeFromName("eye_of_ender_signal"));
        register(CatalogKey.resolve("splash_potion"), newEntityTypeFromName("potion"));
        register(CatalogKey.resolve("thrown_exp_bottle"), newEntityTypeFromName("xp_bottle"));
        register(CatalogKey.resolve("item_frame"), newEntityTypeFromName("item_frame"));
        register(CatalogKey.resolve("wither_skull"), newEntityTypeFromName("wither_skull"));
        register(CatalogKey.resolve("primed_tnt"), newEntityTypeFromName("tnt"));
        register(CatalogKey.resolve("falling_block"), newEntityTypeFromName("falling_block"));
        register(CatalogKey.resolve("firework"), newEntityTypeFromName("fireworks_rocket"));
        register(CatalogKey.resolve("armor_stand"), newEntityTypeFromName("armor_stand"));
        register(CatalogKey.resolve("boat"), newEntityTypeFromName("boat"));
        register(CatalogKey.resolve("rideable_minecart"), newEntityTypeFromName("minecart"));
        register(CatalogKey.resolve("chested_minecart"), newEntityTypeFromName("chest_minecart"));
        register(CatalogKey.resolve("furnace_minecart"), newEntityTypeFromName("furnace_minecart"));
        register(CatalogKey.resolve("tnt_minecart"), newEntityTypeFromName("tnt_minecart"));
        register(CatalogKey.resolve("hopper_minecart"), newEntityTypeFromName("hopper_minecart"));
        register(CatalogKey.resolve("mob_spawner_minecart"), newEntityTypeFromName("spawner_minecart"));
        register(CatalogKey.resolve("commandblock_minecart"), newEntityTypeFromName("commandblock_minecart"));
        register(CatalogKey.resolve("evocation_fangs"), newEntityTypeFromName("evocation_fangs"));
        register(CatalogKey.resolve("evocation_illager"), newEntityTypeFromName("evocation_illager"));
        register(CatalogKey.resolve("vex"), newEntityTypeFromName("vex"));
        register(CatalogKey.resolve("vindication_illager"), newEntityTypeFromName("vindication_illager"));
        register(CatalogKey.resolve("creeper"), newEntityTypeFromName("creeper"));
        register(CatalogKey.resolve("skeleton"), newEntityTypeFromName("skeleton"));
        register(CatalogKey.resolve("stray"), newEntityTypeFromName("stray"));
        register(CatalogKey.resolve("wither_skeleton"), newEntityTypeFromName("wither_skeleton"));
        register(CatalogKey.resolve("spider"), newEntityTypeFromName("spider"));
        register(CatalogKey.resolve("giant"), newEntityTypeFromName("giant"));
        register(CatalogKey.resolve("zombie"), newEntityTypeFromName("zombie"));
        register(CatalogKey.resolve("husk"), newEntityTypeFromName("husk"));
        register(CatalogKey.resolve("slime"), newEntityTypeFromName("slime"));
        register(CatalogKey.resolve("ghast"), newEntityTypeFromName("ghast"));
        register(CatalogKey.resolve("pig_zombie"), newEntityTypeFromName("zombie_pigman"));
        register(CatalogKey.resolve("enderman"), newEntityTypeFromName("enderman"));
        register(CatalogKey.resolve("cave_spider"), newEntityTypeFromName("cave_spider"));
        register(CatalogKey.resolve("silverfish"), newEntityTypeFromName("silverfish"));
        register(CatalogKey.resolve("blaze"), newEntityTypeFromName("blaze"));
        register(CatalogKey.resolve("magma_cube"), newEntityTypeFromName("magma_cube"));
        register(CatalogKey.resolve("ender_dragon"), newEntityTypeFromName("ender_dragon"));
        register(CatalogKey.resolve("wither"), newEntityTypeFromName("wither"));
        register(CatalogKey.resolve("bat"), newEntityTypeFromName("bat"));
        register(CatalogKey.resolve("witch"), newEntityTypeFromName("witch"));
        register(CatalogKey.resolve("endermite"), newEntityTypeFromName("endermite"));
        register(CatalogKey.resolve("guardian"), newEntityTypeFromName("guardian"));
        register(CatalogKey.resolve("elder_guardian"), newEntityTypeFromName("elder_guardian"));
        register(CatalogKey.resolve("pig"), newEntityTypeFromName("pig"));
        register(CatalogKey.resolve("sheep"), newEntityTypeFromName("sheep"));
        register(CatalogKey.resolve("cow"), newEntityTypeFromName("cow"));
        register(CatalogKey.resolve("chicken"), newEntityTypeFromName("chicken"));
        register(CatalogKey.resolve("squid"), newEntityTypeFromName("squid"));
        register(CatalogKey.resolve("wolf"), newEntityTypeFromName("wolf"));
        register(CatalogKey.resolve("mushroom_cow"), newEntityTypeFromName("mooshroom"));
        register(CatalogKey.resolve("snowman"), newEntityTypeFromName("snowman"));
        register(CatalogKey.resolve("ocelot"), newEntityTypeFromName("Ocelot"));
        register(CatalogKey.resolve("iron_golem"), newEntityTypeFromName("villager_golem"));

        register(CatalogKey.resolve("horse"), newEntityTypeFromName("horse"));
        register(CatalogKey.resolve("skeleton_horse"), newEntityTypeFromName("skeleton_horse"));
        register(CatalogKey.resolve("zombie_horse"), newEntityTypeFromName("zombie_horse"));
        register(CatalogKey.resolve("donkey"), newEntityTypeFromName("donkey"));
        register(CatalogKey.resolve("mule"), newEntityTypeFromName("mule"));
        register(CatalogKey.resolve("llama"), newEntityTypeFromName("llama"));

        register(CatalogKey.resolve("llama_spit"), newEntityTypeFromName("llama_spit"));
        register(CatalogKey.resolve("rabbit"), newEntityTypeFromName("rabbit"));
        register(CatalogKey.resolve("villager"), newEntityTypeFromName("villager"));
        register(CatalogKey.resolve("zombie_villager"), newEntityTypeFromName("zombie_villager"));
        register(CatalogKey.resolve("ender_crystal"), newEntityTypeFromName("ender_crystal"));
        register(CatalogKey.resolve("shulker"), newEntityTypeFromName("shulker"));
        register(CatalogKey.resolve("shulker_bullet"), newEntityTypeFromName("shulker_bullet"));
        register(CatalogKey.resolve("spectral_arrow"), newEntityTypeFromName("spectral_arrow"));
        register(CatalogKey.resolve("polar_bear"), newEntityTypeFromName("polar_bear"));
        register(CatalogKey.resolve("egg"), new SpongeEntityType(-1, new ResourceLocation("egg"), EntityEgg.class, new SpongeTranslation("item.egg.name")));
        register(CatalogKey.resolve("fishing_hook"), new SpongeEntityType(-2, new ResourceLocation("FishingHook"), EntityFishHook.class, new SpongeTranslation("item.fishingRod.name")));
        register(CatalogKey.resolve("lightning"), new SpongeEntityType(-3, new ResourceLocation("lightning"), EntityLightningBolt.class, null));
        register(CatalogKey.resolve("weather"), new SpongeEntityType(-4, new ResourceLocation("Weather"), EntityWeatherEffect.class, new SpongeTranslation("soundCategory.weather")));
        register(CatalogKey.resolve("player"), new SpongeEntityType(-5, new ResourceLocation("Player"), EntityPlayerMP.class, new SpongeTranslation("soundCategory.player")));
        register(CatalogKey.resolve("complex_part"), new SpongeEntityType(-6, new ResourceLocation("ComplexPart"), MultiPartEntityPart.class, null));
        register(CatalogKey.resolve("human"), registerCustomEntity(EntityHuman.class, "human", "Human", 300, null)); // TODO: Figure out what id to use, as negative ids no longer work
        //this.entityClassToTypeMappings.put("human", new SpongeEntityType(-6))

        register(CatalogKey.resolve("parrot"), newEntityTypeFromName("parrot"));
        register(CatalogKey.resolve("illusion_illager"), newEntityTypeFromName("illusion_illager"));
        register(CatalogKey.of("unknown", "unknown"), SpongeEntityType.UNKNOWN);
    }

    private SpongeEntityType newEntityTypeFromName(String mcName) {
        ResourceLocation resourceLoc = new ResourceLocation(mcName);
        Class<? extends Entity> cls = SpongeImplHooks.getEntityClass(resourceLoc);
        if (cls == null) {
            throw new IllegalArgumentException("No class mapping for entity name " + mcName);
        }
        final SpongeEntityType entityType = new SpongeEntityType(SpongeImplHooks.getEntityId(cls), resourceLoc, cls,
            new SpongeTranslation("entity." + SpongeImplHooks.getEntityTranslation(resourceLoc) + ".name"));
        KeyRegistryModule.getInstance().registerForEntityClass(cls);
        return entityType;
    }

    private SpongeEntityType registerCustomEntity(Class<? extends Entity> entityClass, String entityName, String oldName, int entityId, Translation translation) {
        this.customEntities.add(new FutureRegistration(entityId, new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, entityName), entityClass, oldName));
        return new SpongeEntityType(entityId, entityName, SpongeImpl.ECOSYSTEM_NAME, entityClass, translation);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();
        RegistryHelper.mapFields(EntityTypes.class, fieldName -> {
            if (fieldName.equals("UNKNOWN")) {
                return SpongeEntityType.UNKNOWN;
            }
            EntityType entityType = this.map.get(CatalogKey.resolve(fieldName));
            this.entityClassToTypeMappings.put(((SpongeEntityType) entityType).entityClass, entityType);
            return entityType;
        });
        this.map.put(CatalogKey.minecraft("ozelot"), this.map.get(CatalogKey.minecraft("ocelot")));

    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(EntityType extraCatalog) {
        this.map.put(extraCatalog.getKey(), extraCatalog);
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
