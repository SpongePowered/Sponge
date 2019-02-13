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
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;

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
        this.register(net.minecraft.entity.EntityType.AREA_EFFECT_CLOUD);
        this.register(net.minecraft.entity.EntityType.ARMOR_STAND);
        this.register(net.minecraft.entity.EntityType.ARROW);
        this.register(net.minecraft.entity.EntityType.BAT);
        this.register(net.minecraft.entity.EntityType.BLAZE);
        this.register(net.minecraft.entity.EntityType.BOAT);
        this.register(net.minecraft.entity.EntityType.CAVE_SPIDER);
        this.register(net.minecraft.entity.EntityType.CHICKEN);
        this.register(net.minecraft.entity.EntityType.COD);
        this.register(net.minecraft.entity.EntityType.COW);
        this.register(net.minecraft.entity.EntityType.CREEPER);
        this.register(net.minecraft.entity.EntityType.DONKEY);
        this.register(net.minecraft.entity.EntityType.DOLPHIN);
        this.register(net.minecraft.entity.EntityType.DRAGON_FIREBALL);
        this.register(net.minecraft.entity.EntityType.DROWNED);
        this.register(net.minecraft.entity.EntityType.ELDER_GUARDIAN);
        this.register(net.minecraft.entity.EntityType.END_CRYSTAL);
        this.register(net.minecraft.entity.EntityType.ENDER_DRAGON);
        this.register(net.minecraft.entity.EntityType.ENDERMAN);
        this.register(net.minecraft.entity.EntityType.ENDERMITE);
        this.register(net.minecraft.entity.EntityType.EVOKER_FANGS);
        this.register(net.minecraft.entity.EntityType.EVOKER);
        this.register(net.minecraft.entity.EntityType.EXPERIENCE_ORB);
        this.register(net.minecraft.entity.EntityType.EYE_OF_ENDER);
        this.register(net.minecraft.entity.EntityType.FALLING_BLOCK);
        this.register(net.minecraft.entity.EntityType.FIREWORK_ROCKET);
        this.register(net.minecraft.entity.EntityType.GHAST);
        this.register(net.minecraft.entity.EntityType.GIANT);
        this.register(net.minecraft.entity.EntityType.GUARDIAN);
        this.register(net.minecraft.entity.EntityType.HORSE);
        this.register(net.minecraft.entity.EntityType.HUSK);
        this.register(net.minecraft.entity.EntityType.ILLUSIONER);
        this.register(net.minecraft.entity.EntityType.ITEM);
        this.register(net.minecraft.entity.EntityType.ITEM_FRAME);
        this.register(net.minecraft.entity.EntityType.FIREBALL);
        this.register(net.minecraft.entity.EntityType.LEASH_KNOT);
        this.register(net.minecraft.entity.EntityType.LLAMA);
        this.register(net.minecraft.entity.EntityType.LLAMA_SPIT);
        this.register(net.minecraft.entity.EntityType.MAGMA_CUBE);
        this.register(net.minecraft.entity.EntityType.MINECART);
        this.register(net.minecraft.entity.EntityType.CHEST_MINECART);
        this.register(net.minecraft.entity.EntityType.COMMAND_BLOCK_MINECART);
        this.register(net.minecraft.entity.EntityType.FURNACE_MINECART);
        this.register(net.minecraft.entity.EntityType.HOPPER_MINECART);
        this.register(net.minecraft.entity.EntityType.SPAWNER_MINECART);
        this.register(net.minecraft.entity.EntityType.TNT_MINECART);
        this.register(net.minecraft.entity.EntityType.MULE);
        this.register(net.minecraft.entity.EntityType.MOOSHROOM);
        this.register(net.minecraft.entity.EntityType.OCELOT);
        this.register(net.minecraft.entity.EntityType.PAINTING);
        this.register(net.minecraft.entity.EntityType.PARROT);
        this.register(net.minecraft.entity.EntityType.PIG);
        this.register(net.minecraft.entity.EntityType.PUFFERFISH);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_PIGMAN);
        this.register(net.minecraft.entity.EntityType.POLAR_BEAR);
        this.register(net.minecraft.entity.EntityType.TNT);
        this.register(net.minecraft.entity.EntityType.RABBIT);
        this.register(net.minecraft.entity.EntityType.SALMON);
        this.register(net.minecraft.entity.EntityType.SHEEP);
        this.register(net.minecraft.entity.EntityType.SHULKER);
        this.register(net.minecraft.entity.EntityType.SHULKER_BULLET);
        this.register(net.minecraft.entity.EntityType.SILVERFISH);
        this.register(net.minecraft.entity.EntityType.SKELETON);
        this.register(net.minecraft.entity.EntityType.SKELETON_HORSE);
        this.register(net.minecraft.entity.EntityType.SLIME);
        this.register(net.minecraft.entity.EntityType.SMALL_FIREBALL);
        this.register(net.minecraft.entity.EntityType.SNOW_GOLEM);
        this.register(net.minecraft.entity.EntityType.SNOWBALL);
        this.register(net.minecraft.entity.EntityType.SPECTRAL_ARROW);
        this.register(net.minecraft.entity.EntityType.SPIDER);
        this.register(net.minecraft.entity.EntityType.SQUID);
        this.register(net.minecraft.entity.EntityType.STRAY);
        this.register(net.minecraft.entity.EntityType.TROPICAL_FISH);
        this.register(net.minecraft.entity.EntityType.TURTLE);
        this.register(net.minecraft.entity.EntityType.EGG);
        this.register(net.minecraft.entity.EntityType.ENDER_PEARL);
        this.register(net.minecraft.entity.EntityType.EXPERIENCE_BOTTLE);
        this.register(net.minecraft.entity.EntityType.POTION);
        this.register(net.minecraft.entity.EntityType.VEX);
        this.register(net.minecraft.entity.EntityType.VILLAGER);
        this.register(net.minecraft.entity.EntityType.IRON_GOLEM);
        this.register(net.minecraft.entity.EntityType.VINDICATOR);
        this.register(net.minecraft.entity.EntityType.WITCH);
        this.register(net.minecraft.entity.EntityType.WITHER);
        this.register(net.minecraft.entity.EntityType.WITHER_SKELETON);
        this.register(net.minecraft.entity.EntityType.WITHER_SKULL);
        this.register(net.minecraft.entity.EntityType.WOLF);
        this.register(net.minecraft.entity.EntityType.ZOMBIE);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_HORSE);
        this.register(net.minecraft.entity.EntityType.ZOMBIE_VILLAGER);
        this.register(net.minecraft.entity.EntityType.PHANTOM);
        this.register(net.minecraft.entity.EntityType.LIGHTNING_BOLT);
        this.register(net.minecraft.entity.EntityType.PLAYER);
        this.register(net.minecraft.entity.EntityType.FISHING_BOBBER);
        this.register(net.minecraft.entity.EntityType.TRIDENT);

        register(CatalogKey.sponge("human"), createHumanEntityType()); // TODO: Figure out what id to use, as negative ids no longer work
        //this.entityClassToTypeMappings.put("human", new SpongeEntityType(-6))

        register(CatalogKey.of("unknown", "unknown"), SpongeEntityType.UNKNOWN);
    }

    private <T extends Entity> void register(final net.minecraft.entity.EntityType<T> type) {
        final SpongeEntityType sponge = this.createType(type);
        this.register((CatalogKey) (Object) net.minecraft.entity.EntityType.getId(type), sponge);
    }

    private <T extends Entity> SpongeEntityType createType(final net.minecraft.entity.EntityType<T> type) {
        final SpongeEntityType sponge = new SpongeEntityType(net.minecraft.entity.EntityType.getId(type), type);
        KeyRegistryModule.getInstance().registerForEntityClass(type.getEntityClass());
        return sponge;
    }

    @Override
    protected boolean filterAll(final EntityType type) {
        return type != SpongeEntityType.UNKNOWN;
    }

    private SpongeEntityType createHumanEntityType() {
        final ResourceLocation key = new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, "human");
        this.customEntities.add(new FutureRegistration(300, new ResourceLocation(SpongeImpl.ECOSYSTEM_ID, "human"), EntityHuman.class, "Human"));
        final net.minecraft.entity.EntityType<EntityHuman> type = net.minecraft.entity.EntityType.Builder.create(EntityHuman.class, EntityHuman::new).build("sponge:human");
        new SpongeEntityType(key, type);
        return new SpongeEntityType(new ResourceLocation("sponge:human"), 300, type);
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
