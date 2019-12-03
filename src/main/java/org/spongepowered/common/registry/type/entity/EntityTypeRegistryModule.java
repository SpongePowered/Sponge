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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.ResourceLocation;
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
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EntityTypeRegistryModule implements ExtraClassCatalogRegistryModule<EntityType, Entity>, SpongeAdditionalCatalogRegistryModule<EntityType> {

    @RegisterCatalog(EntityTypes.class)
    protected final Map<String, SpongeEntityType> entityTypeMappings = Maps.newHashMap();

    public final Map<Class<? extends Entity>, SpongeEntityType> entityClassToTypeMappings = Maps.newHashMap();
    private final Set<FutureRegistration> customEntities = new HashSet<>();

    public static EntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public void registerEntityType(SpongeEntityType type) {
        this.entityTypeMappings.put(type.getId(), type);
        this.entityClassToTypeMappings.put((type).entityClass, type);
    }

    @Override
    public Optional<EntityType> getById(String id) {
        if (!checkNotNull(id).contains(":")) {
            id = "minecraft:" + id;
        }
        if ("unknown:unknown".equalsIgnoreCase(id)) {
            return Optional.of(SpongeEntityType.UNKNOWN);
        }
        return Optional.ofNullable(this.entityTypeMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<EntityType> getAll() {
        return ImmutableList.copyOf(this.entityTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.entityTypeMappings.put("item", this.newEntityTypeFromName("Item"));
        this.entityTypeMappings.put("experience_orb", this.newEntityTypeFromName("xp_orb"));
        this.entityTypeMappings.put("area_effect_cloud", this.newEntityTypeFromName("area_effect_cloud"));
        this.entityTypeMappings.put("dragon_fireball", this.newEntityTypeFromName("dragon_fireball"));
        this.entityTypeMappings.put("leash_hitch", this.newEntityTypeFromName("leash_knot"));
        this.entityTypeMappings.put("painting", this.newEntityTypeFromName("painting"));
        this.entityTypeMappings.put("tipped_arrow", this.newEntityTypeFromName("arrow"));
        this.entityTypeMappings.put("snowball", this.newEntityTypeFromName("snowball"));
        this.entityTypeMappings.put("fireball", this.newEntityTypeFromName("LargeFireball", "fireball"));
        this.entityTypeMappings.put("small_fireball", this.newEntityTypeFromName("small_fireball"));
        this.entityTypeMappings.put("ender_pearl", this.newEntityTypeFromName("ender_pearl"));
        this.entityTypeMappings.put("eye_of_ender", this.newEntityTypeFromName("eye_of_ender_signal"));
        this.entityTypeMappings.put("splash_potion", this.newEntityTypeFromName("potion"));
        this.entityTypeMappings.put("thrown_exp_bottle", this.newEntityTypeFromName("xp_bottle"));
        this.entityTypeMappings.put("item_frame", this.newEntityTypeFromName("item_frame"));
        this.entityTypeMappings.put("wither_skull", this.newEntityTypeFromName("wither_skull"));
        this.entityTypeMappings.put("primed_tnt", this.newEntityTypeFromName("tnt"));
        this.entityTypeMappings.put("falling_block", this.newEntityTypeFromName("falling_block"));
        this.entityTypeMappings.put("firework", this.newEntityTypeFromName("fireworks_rocket"));
        this.entityTypeMappings.put("armor_stand", this.newEntityTypeFromName("armor_stand"));
        this.entityTypeMappings.put("boat", this.newEntityTypeFromName("boat"));
        this.entityTypeMappings.put("rideable_minecart", this.newEntityTypeFromName("minecart"));
        this.entityTypeMappings.put("chested_minecart", this.newEntityTypeFromName("chest_minecart"));
        this.entityTypeMappings.put("furnace_minecart", this.newEntityTypeFromName("furnace_minecart"));
        this.entityTypeMappings.put("tnt_minecart", this.newEntityTypeFromName("tnt_minecart"));
        this.entityTypeMappings.put("hopper_minecart", this.newEntityTypeFromName("hopper_minecart"));
        this.entityTypeMappings.put("mob_spawner_minecart", this.newEntityTypeFromName("spawner_minecart"));
        this.entityTypeMappings.put("commandblock_minecart", this.newEntityTypeFromName("commandblock_minecart"));
        this.entityTypeMappings.put("evocation_fangs", this.newEntityTypeFromName("evocation_fangs"));
        this.entityTypeMappings.put("evocation_illager", this.newEntityTypeFromName("evocation_illager"));
        this.entityTypeMappings.put("vex", this.newEntityTypeFromName("vex"));
        this.entityTypeMappings.put("vindication_illager", this.newEntityTypeFromName("vindication_illager"));
        this.entityTypeMappings.put("creeper", this.newEntityTypeFromName("creeper"));
        this.entityTypeMappings.put("skeleton", this.newEntityTypeFromName("skeleton"));
        this.entityTypeMappings.put("stray", this.newEntityTypeFromName("stray"));
        this.entityTypeMappings.put("wither_skeleton", this.newEntityTypeFromName("wither_skeleton"));
        this.entityTypeMappings.put("spider", this.newEntityTypeFromName("spider"));
        this.entityTypeMappings.put("giant", this.newEntityTypeFromName("giant"));
        this.entityTypeMappings.put("zombie", this.newEntityTypeFromName("zombie"));
        this.entityTypeMappings.put("husk", this.newEntityTypeFromName("husk"));
        this.entityTypeMappings.put("slime", this.newEntityTypeFromName("slime"));
        this.entityTypeMappings.put("ghast", this.newEntityTypeFromName("ghast"));
        this.entityTypeMappings.put("pig_zombie", this.newEntityTypeFromName("zombie_pigman"));
        this.entityTypeMappings.put("enderman", this.newEntityTypeFromName("enderman"));
        this.entityTypeMappings.put("cave_spider", this.newEntityTypeFromName("cave_spider"));
        this.entityTypeMappings.put("silverfish", this.newEntityTypeFromName("silverfish"));
        this.entityTypeMappings.put("blaze", this.newEntityTypeFromName("blaze"));
        this.entityTypeMappings.put("magma_cube", this.newEntityTypeFromName("magma_cube"));
        this.entityTypeMappings.put("ender_dragon", this.newEntityTypeFromName("ender_dragon"));
        this.entityTypeMappings.put("wither", this.newEntityTypeFromName("wither"));
        this.entityTypeMappings.put("bat", this.newEntityTypeFromName("bat"));
        this.entityTypeMappings.put("witch", this.newEntityTypeFromName("witch"));
        this.entityTypeMappings.put("endermite", this.newEntityTypeFromName("endermite"));
        this.entityTypeMappings.put("guardian", this.newEntityTypeFromName("guardian"));
        this.entityTypeMappings.put("elder_guardian", this.newEntityTypeFromName("elder_guardian"));
        this.entityTypeMappings.put("pig", this.newEntityTypeFromName("pig"));
        this.entityTypeMappings.put("sheep", this.newEntityTypeFromName("sheep"));
        this.entityTypeMappings.put("cow", this.newEntityTypeFromName("cow"));
        this.entityTypeMappings.put("chicken", this.newEntityTypeFromName("chicken"));
        this.entityTypeMappings.put("squid", this.newEntityTypeFromName("squid"));
        this.entityTypeMappings.put("wolf", this.newEntityTypeFromName("wolf"));
        this.entityTypeMappings.put("mushroom_cow", this.newEntityTypeFromName("mooshroom"));
        this.entityTypeMappings.put("snowman", this.newEntityTypeFromName("snowman"));
        this.entityTypeMappings.put("ocelot", this.newEntityTypeFromName("Ocelot"));
        this.entityTypeMappings.put("iron_golem", this.newEntityTypeFromName("villager_golem"));

        this.entityTypeMappings.put("horse", this.newEntityTypeFromName("horse"));
        this.entityTypeMappings.put("skeleton_horse", this.newEntityTypeFromName("skeleton_horse"));
        this.entityTypeMappings.put("zombie_horse", this.newEntityTypeFromName("zombie_horse"));
        this.entityTypeMappings.put("donkey", this.newEntityTypeFromName("donkey"));
        this.entityTypeMappings.put("mule", this.newEntityTypeFromName("mule"));
        this.entityTypeMappings.put("llama", this.newEntityTypeFromName("llama"));

        this.entityTypeMappings.put("llama_spit", this.newEntityTypeFromName("llama_spit"));
        this.entityTypeMappings.put("rabbit", this.newEntityTypeFromName("rabbit"));
        this.entityTypeMappings.put("villager", this.newEntityTypeFromName("villager"));
        this.entityTypeMappings.put("zombie_villager", this.newEntityTypeFromName("zombie_villager"));
        this.entityTypeMappings.put("ender_crystal", this.newEntityTypeFromName("ender_crystal"));
        this.entityTypeMappings.put("shulker", this.newEntityTypeFromName("shulker"));
        this.entityTypeMappings.put("shulker_bullet", this.newEntityTypeFromName("shulker_bullet"));
        this.entityTypeMappings.put("spectral_arrow", this.newEntityTypeFromName("spectral_arrow"));
        this.entityTypeMappings.put("polar_bear", this.newEntityTypeFromName("polar_bear"));
        this.entityTypeMappings.put("egg", new SpongeEntityType(-1, "egg", EggEntity.class, new SpongeTranslation("item.egg.name")));
        this.entityTypeMappings.put("fishing_hook", new SpongeEntityType(-2, "FishingHook", FishingBobberEntity.class, new SpongeTranslation("item.fishingRod.name")));
        this.entityTypeMappings.put("lightning", new SpongeEntityType(-3, "lightning", LightningBoltEntity.class, null));
        this.entityTypeMappings.put("weather", new SpongeEntityType(-4, "Weather", EntityWeatherEffect.class, new SpongeTranslation("soundCategory.weather")));
        this.entityTypeMappings.put("player", new SpongeEntityType(-5, "Player", ServerPlayerEntity.class, new SpongeTranslation("soundCategory.player")));
        this.entityTypeMappings.put("complex_part", new SpongeEntityType(-6, "ComplexPart", MultiPartEntityPart.class, null));
        this.entityTypeMappings.put("human", this.registerCustomEntity(EntityHuman.class, "human", "Human", 300, null)); // TODO: Figure out what id to use, as negative ids no longer work
        //this.entityClassToTypeMappings.put("human", new SpongeEntityType(-6))

        this.entityTypeMappings.put("parrot", this.newEntityTypeFromName("parrot"));
        this.entityTypeMappings.put("illusion_illager", this.newEntityTypeFromName("illusion_illager"));
    }

    private SpongeEntityType newEntityTypeFromName(String spongeName, String mcName) {
        ResourceLocation resourceLoc = new ResourceLocation(mcName);
        Class<? extends Entity> cls = SpongeImplHooks.getEntityClass(resourceLoc);
        if (cls == null) {
            throw new IllegalArgumentException("No class mapping for entity name " + mcName);
        }
        final SpongeEntityType entityType = new SpongeEntityType(SpongeImplHooks.getEntityId(cls), spongeName, cls,
            new SpongeTranslation("entity." + SpongeImplHooks.getEntityTranslation(resourceLoc) + ".name"));
        KeyRegistryModule.getInstance().registerForEntityClass(cls);
        return entityType;
    }

    private SpongeEntityType newEntityTypeFromName(String name) {
        return this.newEntityTypeFromName(name, name);
    }

    private SpongeEntityType registerCustomEntity(Class<? extends Entity> entityClass, String entityName, String oldName, int entityId, Translation translation) {
        this.customEntities.add(new FutureRegistration(entityId, new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, entityName), entityClass, oldName));
        return new SpongeEntityType(entityId, entityName, SpongeImpl.ECOSYSTEM_NAME, entityClass, translation);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        this.registerDefaults();
        RegistryHelper.mapFields(EntityTypes.class, fieldName -> {
            if (fieldName.equals("UNKNOWN")) {
                return SpongeEntityType.UNKNOWN;
            }
            SpongeEntityType entityType = this.entityTypeMappings.get(fieldName.toLowerCase(Locale.ENGLISH));
            this.entityClassToTypeMappings.put(entityType.entityClass, entityType);
            // remove old mapping
            this.entityTypeMappings.remove(fieldName.toLowerCase(Locale.ENGLISH));
            // add new mapping with minecraft id
            this.entityTypeMappings.put(entityType.getId(), entityType);
            return entityType;
        });
        // Won't be needed in the future
        this.entityTypeMappings.put("minecraft:ozelot", this.entityTypeMappings.get("minecraft:ocelot"));
        this.entityTypeMappings.put("minecraft:primed_tnt", this.entityTypeMappings.get("minecraft:tnt"));
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(EntityType extraCatalog) {
        final SpongeEntityType spongeEntityType = (SpongeEntityType) extraCatalog;
        this.entityTypeMappings.put(extraCatalog.getId(), spongeEntityType);
        this.entityClassToTypeMappings.put(spongeEntityType.entityClass, spongeEntityType);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends Entity> mappedClass) {
        return false;
    }

    @Override
    public SpongeEntityType getForClass(Class<? extends Entity> clazz) {
        SpongeEntityType type = this.entityClassToTypeMappings.get(clazz);
        if (type == null) {
            SpongeImpl.getLogger().warn(String.format("No entity type is registered for class %s", clazz.getName()));

            type = (SpongeEntityType) EntityTypes.UNKNOWN;
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
        for (EntityType type : this.entityTypeMappings.values()) {
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
