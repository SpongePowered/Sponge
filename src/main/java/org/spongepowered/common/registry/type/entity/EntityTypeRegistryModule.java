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
        this.register("area_effect_cloud");
        this.register("armor_stand");
        this.register("arrow", "tipped_arrow");
        this.register("bat");
        this.register("blaze");
        this.register("boat");
        this.register("cave_spider");
        this.register("chicken");
        this.register("cod");
        this.register("cow");
        this.register("creeper");
        this.register("donkey");
        this.register("dolphin");
        this.register("dragon_fireball");
        this.register("drowned");
        this.register("elder_guardian");
        this.register("end_crystal", "ender_crystal");
        this.register("ender_dragon");
        this.register("enderman");
        this.register("endermite");
        this.register("evoker_fangs", "evocation_fangs");
        this.register("evoker", "evocation_illager");
        this.register("experience_orb");
        this.register("eye_of_ender");
        this.register("falling_block");
        this.register("firework_rocket", "firework");
        this.register("ghast");
        this.register("giant");
        this.register("guardian");
        this.register("horse");
        this.register("husk");
        this.register("illusioner", "illusion_illager");
        this.register("item");
        this.register("item_frame");
        this.register("fireball");
        this.register("leash_knot", "leash_hitch");
        this.register("llama");
        this.register("llama_spit");
        this.register("magma_cube");
        this.register("minecart", "rideable_minecart");
        this.register("chest_minecart", "chested_minecart");
        this.register("command_block_minecart", "commandblock_minecart");
        this.register("furnace_minecart");
        this.register("hopper_minecart");
        this.register("spawner_minecart", "mob_spawner_minecart");
        this.register("tnt_minecart");
        this.register("mule");
        this.register("mooshroom", "mushroom_cow");
        this.register("ocelot");
        this.register("painting");
        this.register("parrot");
        this.register("pig");
        this.register("pufferfish");
        this.register("zombie_pigman", "pig_zombie");
        this.register("polar_bear");
        this.register("tnt", "primed_tnt");
        this.register("rabbit");
        this.register("salmon");
        this.register("sheep");
        this.register("shulker");
        this.register("shulker_bullet");
        this.register("silverfish");
        this.register("skeleton");
        this.register("skeleton_horse");
        this.register("slime");
        this.register("small_fireball");
        this.register("snow_golem", "snowman");
        this.register("snowball");
        this.register("spectral_arrow");
        this.register("spider");
        this.register("squid");
        this.register("stray");
        this.register("tropical_fish");
        this.register("turtle");
        this.register("egg");
        this.register("ender_pearl");
        this.register("experience_bottle", "thrown_exp_bottle");
        this.register("potion", "splash_potion");
        this.register("vex");
        this.register("villager");
        this.register("iron_golem");
        this.register("vindicator", "vindication_illager");
        this.register("witch");
        this.register("wither");
        this.register("wither_skeleton");
        this.register("wither_skull");
        this.register("wolf");
        this.register("zombie");
        this.register("zombie_horse");
        this.register("zombie_villager");
        this.register("phantom");
        this.register("lightning_bolt", "lightning");
        this.register("player");
        this.register("fishing_bobber", "fishing_hook");
        this.register("trident");

        // TODO(kashike): why the hell do we even have these as entity types in the API? They definitely should not be.
        register(CatalogKey.minecraft("weather"), new SpongeEntityType(-4, new ResourceLocation("Weather"), EntityWeatherEffect.class, new SpongeTranslation("soundCategory.weather")));
        register(CatalogKey.minecraft("complex_part"), new SpongeEntityType(-6, new ResourceLocation("complex_part"), new ResourceLocation("ComplexPart"), MultiPartEntityPart.class, null));

        register(CatalogKey.sponge("human"), createHumanEntityType()); // TODO: Figure out what id to use, as negative ids no longer work
        //this.entityClassToTypeMappings.put("human", new SpongeEntityType(-6))

        register(CatalogKey.of("unknown", "unknown"), SpongeEntityType.UNKNOWN);
    }

    private SpongeEntityType register(final String key) {
        return this.register(CatalogKey.minecraft(key));
    }

    private SpongeEntityType register(final CatalogKey key) {
        final SpongeEntityType type = this.newEntityTypeFromName(key);
        this.register(key, type);
        return type;
    }

    private void register(final String key, final String spongeKey) {
        final SpongeEntityType type = this.register(key);
        this.register(CatalogKey.minecraft(spongeKey), type);
    }

    @Override
    protected String marshalFieldKey(String key) {
        return key.replace("minecraft:", "").replace("sponge:", "");
    }

    @Override
    protected boolean filterAll(EntityType element) {
        return element != SpongeEntityType.UNKNOWN;
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
