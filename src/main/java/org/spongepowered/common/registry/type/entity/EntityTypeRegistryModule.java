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
        newEntityType(CatalogKey.minecraft("item"));
        newEntityType(CatalogKey.minecraft("experience_orb"), CatalogKey.minecraft("xp_orb"));
        newEntityType(CatalogKey.minecraft("area_effect_cloud"));
        newEntityType(CatalogKey.minecraft("dragon_fireball"));
        newEntityType(CatalogKey.minecraft("leash_hitch"), CatalogKey.minecraft("leash_knot"));
        newEntityType(CatalogKey.minecraft("painting"));
        newEntityType(CatalogKey.minecraft("tipped_arrow"), CatalogKey.minecraft("arrow"));
        newEntityType(CatalogKey.minecraft("snowball"));
        newEntityType(CatalogKey.minecraft("fireball"));
        newEntityType(CatalogKey.minecraft("small_fireball"));
        newEntityType(CatalogKey.minecraft("ender_pearl"));
        newEntityType(CatalogKey.minecraft("eye_of_ender"), CatalogKey.minecraft("eye_of_ender_signal"));
        newEntityType(CatalogKey.minecraft("splash_potion"), CatalogKey.minecraft("potion"));
        newEntityType(CatalogKey.minecraft("thrown_exp_bottle"), CatalogKey.minecraft("xp_bottle"));
        newEntityType(CatalogKey.minecraft("item_frame"));
        newEntityType(CatalogKey.minecraft("wither_skull"));
        newEntityType(CatalogKey.minecraft("primed_tnt"), CatalogKey.minecraft("tnt"));
        newEntityType(CatalogKey.minecraft("falling_block"));
        newEntityType(CatalogKey.minecraft("firework"), CatalogKey.minecraft("fireworks_rocket"));
        newEntityType(CatalogKey.minecraft("armor_stand"));
        newEntityType(CatalogKey.minecraft("boat"));
        newEntityType(CatalogKey.minecraft("rideable_minecart"), CatalogKey.minecraft("minecart"));
        newEntityType(CatalogKey.minecraft("chested_minecart"), CatalogKey.minecraft("chest_minecart"));
        newEntityType(CatalogKey.minecraft("furnace_minecart"));
        newEntityType(CatalogKey.minecraft("tnt_minecart"));
        newEntityType(CatalogKey.minecraft("hopper_minecart"));
        newEntityType(CatalogKey.minecraft("mob_spawner_minecart"), CatalogKey.minecraft("spawner_minecart"));
        newEntityType(CatalogKey.minecraft("commandblock_minecart"), CatalogKey.minecraft("commandblock_minecart"));
        newEntityType(CatalogKey.minecraft("evocation_fangs"));
        newEntityType(CatalogKey.minecraft("evocation_illager"));
        newEntityType(CatalogKey.minecraft("vex"));
        newEntityType(CatalogKey.minecraft("vindication_illager"));
        newEntityType(CatalogKey.minecraft("creeper"));
        newEntityType(CatalogKey.minecraft("skeleton"));
        newEntityType(CatalogKey.minecraft("stray"));
        newEntityType(CatalogKey.minecraft("wither_skeleton"));
        newEntityType(CatalogKey.minecraft("spider"));
        newEntityType(CatalogKey.minecraft("giant"));
        newEntityType(CatalogKey.minecraft("zombie"));
        newEntityType(CatalogKey.minecraft("husk"));
        newEntityType(CatalogKey.minecraft("slime"));
        newEntityType(CatalogKey.minecraft("ghast"));
        newEntityType(CatalogKey.minecraft("pig_zombie"), CatalogKey.minecraft("zombie_pigman"));
        newEntityType(CatalogKey.minecraft("enderman"));
        newEntityType(CatalogKey.minecraft("cave_spider"));
        newEntityType(CatalogKey.minecraft("silverfish"));
        newEntityType(CatalogKey.minecraft("blaze"));
        newEntityType(CatalogKey.minecraft("magma_cube"));
        newEntityType(CatalogKey.minecraft("ender_dragon"));
        newEntityType(CatalogKey.minecraft("wither"));
        newEntityType(CatalogKey.minecraft("bat"));
        newEntityType(CatalogKey.minecraft("witch"));
        newEntityType(CatalogKey.minecraft("endermite"));
        newEntityType(CatalogKey.minecraft("guardian"));
        newEntityType(CatalogKey.minecraft("elder_guardian"));
        newEntityType(CatalogKey.minecraft("pig"));
        newEntityType(CatalogKey.minecraft("sheep"));
        newEntityType(CatalogKey.minecraft("cow"));
        newEntityType(CatalogKey.minecraft("chicken"));
        newEntityType(CatalogKey.minecraft("squid"));
        newEntityType(CatalogKey.minecraft("wolf"));
        newEntityType(CatalogKey.minecraft("mushroom_cow"), CatalogKey.minecraft("mooshroom"));
        newEntityType(CatalogKey.minecraft("snowman"));
        newEntityType(CatalogKey.minecraft("ocelot"));
        newEntityType(CatalogKey.minecraft("iron_golem"), CatalogKey.minecraft("villager_golem"));

        newEntityType(CatalogKey.minecraft("horse"));
        newEntityType(CatalogKey.minecraft("skeleton_horse"));
        newEntityType(CatalogKey.minecraft("zombie_horse"));
        newEntityType(CatalogKey.minecraft("donkey"));
        newEntityType(CatalogKey.minecraft("mule"));
        newEntityType(CatalogKey.minecraft("llama"));

        newEntityType(CatalogKey.minecraft("llama_spit"));
        newEntityType(CatalogKey.minecraft("rabbit"));
        newEntityType(CatalogKey.minecraft("villager"));
        newEntityType(CatalogKey.minecraft("zombie_villager"));
        newEntityType(CatalogKey.minecraft("ender_crystal"));
        newEntityType(CatalogKey.minecraft("shulker"));
        newEntityType(CatalogKey.minecraft("shulker_bullet"));
        newEntityType(CatalogKey.minecraft("spectral_arrow"));
        newEntityType(CatalogKey.minecraft("polar_bear"));
        register(CatalogKey.minecraft("egg"), new SpongeEntityType(-1, new ResourceLocation("egg"), new ResourceLocation("egg"), EntityEgg.class, new SpongeTranslation("item.egg.name")));
        register(CatalogKey.minecraft("fishing_hook"), new SpongeEntityType(-2, new ResourceLocation("fishing_hook"), new ResourceLocation("FishingHook"), EntityFishHook.class, new SpongeTranslation("item.fishingRod.name")));
        register(CatalogKey.minecraft("lightning"), new SpongeEntityType(-3, new ResourceLocation("lightning"), EntityLightningBolt.class, null));
        register(CatalogKey.minecraft("weather"), new SpongeEntityType(-4, new ResourceLocation("Weather"), EntityWeatherEffect.class, new SpongeTranslation("soundCategory.weather")));
        register(CatalogKey.minecraft("player"), new SpongeEntityType(-5, new ResourceLocation("Player"), EntityPlayerMP.class, new SpongeTranslation("soundCategory.player")));
        register(CatalogKey.minecraft("complex_part"), new SpongeEntityType(-6, new ResourceLocation("complex_part"), new ResourceLocation("ComplexPart"), MultiPartEntityPart.class, null));
        register(CatalogKey.sponge("human"), createHumanEntityType()); // TODO: Figure out what id to use, as negative ids no longer work
        //this.entityClassToTypeMappings.put("human", new SpongeEntityType(-6))

        newEntityType(CatalogKey.minecraft("parrot"));
        newEntityType(CatalogKey.minecraft("illusion_illager"));
        register(CatalogKey.of("unknown", "unknown"), SpongeEntityType.UNKNOWN);
    }



    private void newEntityType(CatalogKey minecraftKey) {
        final SpongeEntityType spongeEntityType = newEntityTypeFromName(minecraftKey);
        register(minecraftKey, spongeEntityType);
    }

    @Override
    protected String marshalFieldKey(String key) {
        return key.replace("minecraft:", "").replace("sponge:", "");
    }

    @Override
    protected boolean filterAll(EntityType element) {
        return element != SpongeEntityType.UNKNOWN;
    }

    private void newEntityType(CatalogKey spongeKey, CatalogKey minecraftKey) {
        final SpongeEntityType spongeEntityType = newEntityTypeFromName(minecraftKey);
        register(spongeKey, spongeEntityType);
        if (!spongeKey.equals(minecraftKey)) { // Because we do this....
            register(minecraftKey, spongeEntityType);
        }
    }

    private SpongeEntityType newEntityTypeFromName(CatalogKey minecraftKey) {
        ResourceLocation minecraftLocation = (ResourceLocation) (Object) minecraftKey;
        Class<? extends Entity> cls = SpongeImplHooks.getEntityClass(minecraftLocation);
        if (cls == null) {
            throw new IllegalArgumentException("No class mapping for entity name " + minecraftLocation);
        }
        final SpongeEntityType entityType = new SpongeEntityType(SpongeImplHooks.getEntityId(cls), minecraftLocation, cls,
            new SpongeTranslation("entity." + SpongeImplHooks.getEntityTranslation(minecraftLocation) + ".name"));
        KeyRegistryModule.getInstance().registerForEntityClass(cls);
        return entityType;
    }

    private SpongeEntityType createHumanEntityType() {
        this.customEntities.add(new FutureRegistration(300, new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, "human"), EntityHuman.class, "Human"));
        return new SpongeEntityType(300, new ResourceLocation("sponge:human"), new ResourceLocation("sponge:human"), EntityHuman.class, null);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();
        RegistryHelper.mapFields(EntityTypes.class, fieldName -> {
            if (fieldName.equals("UNKNOWN")) {
                return SpongeEntityType.UNKNOWN;
            }
            final CatalogKey key = fieldName.equalsIgnoreCase("human") ? CatalogKey.sponge(fieldName) : CatalogKey.minecraft(fieldName);
            EntityType entityType = this.map.get(key);
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
