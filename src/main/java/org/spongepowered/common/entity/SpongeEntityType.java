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
package org.spongepowered.common.entity;

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.EntityTrackerCategory;
import org.spongepowered.common.config.category.EntityTrackerModCategory;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Locale;

import javax.annotation.Nullable;

public class SpongeEntityType extends SpongeCatalogType.Translatable implements EntityType {

    public static final EntityType UNKNOWN = new SpongeEntityType(-999999, "Unknown", "unknown", Entity.class, new SpongeTranslation("entity.generic.name")) {

        @Override
        public String getName() {
            return "Unknown";
        }

        @Override
        public Class<? extends org.spongepowered.api.entity.Entity> getEntityClass() {
            throw new UnsupportedOperationException("Unknown entity type has no entity class");
        }

        @Override
        public void initializeTrackerState() {
            // no need to initialize
        }

        @Override
        public boolean isKnown() {
            return false;
        }
    };

    public final int entityTypeId;
    public final String entityName;
    public final String modId;
    public final Class<? extends Entity> entityClass;
    private final boolean isVanilla;
    private EntityClassification creatureType;
    private boolean activationRangeInitialized = false;
    @Nullable private Timing timing = null;
    // Used by tracker config
    public boolean allowsBlockBulkCapture = true;
    public boolean allowsEntityBulkCapture = true;
    public boolean allowsBlockEventCreation = true;
    public boolean allowsEntityEventCreation = true;
    public boolean isModdedDamageEntityMethod = false;

    public SpongeEntityType(int id, String name, Class<? extends Entity> clazz, Translation translation) {
        this(id, name.toLowerCase(Locale.ENGLISH), "minecraft", clazz, translation);
    }

    public SpongeEntityType(int id, String name, String modId, Class<? extends Entity> clazz, Translation translation) {
        super(modId.toLowerCase(Locale.ENGLISH) + ":" + name.toLowerCase(Locale.ENGLISH), check(translation));
        this.entityTypeId = id;
        this.entityName = name.toLowerCase(Locale.ENGLISH);
        this.entityClass = clazz;
        this.modId = modId.toLowerCase(Locale.ENGLISH);
        this.isVanilla = this.entityClass.getName().startsWith("net.minecraft.");
        this.initializeTrackerState();
    }

    private static Translation check(@Nullable Translation translation) {
        if (translation == null) {
            return UNKNOWN.getTranslation();
        }
        return translation;
    }

    @Override
    public String getName() {
        return this.entityName;
    }

    public String getModId() {
        return this.modId;
    }

    @Nullable
    public EntityClassification getEnumCreatureType() {
        return this.creatureType;
    }

    public void setEnumCreatureType(EntityClassification type) {
        this.creatureType = type;
    }

    public boolean isActivationRangeInitialized() {
        return this.activationRangeInitialized;
    }

    public void setActivationRangeInitialized(boolean flag) {
        this.activationRangeInitialized = flag;
    }

    public void initializeTrackerState() {
        final SpongeConfig<TrackerConfig> trackerConfigAdapter = SpongeImpl.getTrackerConfigAdapter();
        final EntityTrackerCategory entityTrackerCat = trackerConfigAdapter.getConfig().getEntityTracker();
        EntityTrackerModCategory entityTrackerModCat = entityTrackerCat.getModMappings().get(this.modId);

        if (entityTrackerModCat == null) {
            entityTrackerModCat = new EntityTrackerModCategory();
            entityTrackerCat.getModMappings().put(this.modId, entityTrackerModCat);
        }
        if (!entityTrackerModCat.isEnabled()) {
            this.allowsBlockBulkCapture = false;
            this.allowsEntityBulkCapture = false;
            this.allowsBlockEventCreation = false;
            this.allowsEntityEventCreation = false;
            entityTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> this.allowsBlockBulkCapture);
            entityTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> this.allowsEntityBulkCapture);
            entityTrackerModCat.getBlockEventCreationMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> this.allowsBlockEventCreation);
            entityTrackerModCat.getEntityEventCreationMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> this.allowsEntityEventCreation);
        } else {
            this.allowsBlockBulkCapture = entityTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsEntityBulkCapture = entityTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsBlockEventCreation = entityTrackerModCat.getBlockEventCreationMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> true);
            this.allowsEntityEventCreation = entityTrackerModCat.getEntityEventCreationMap().computeIfAbsent(this.entityName.toLowerCase(Locale.ENGLISH), k -> true);
        }

        if (entityTrackerCat.autoPopulateData()) {
            trackerConfigAdapter.save();
        }

        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "damageEntity" : "func_70665_d";
            Class<?>[] argTypes = {DamageSource.class, float.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (!(clazz.equals(LivingEntity.class) || clazz.equals(PlayerEntity.class) || clazz.equals(ServerPlayerEntity.class))) {
                this.isModdedDamageEntityMethod = true;
            }
        } catch (Throwable ex) {
            // ignore
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends org.spongepowered.api.entity.Entity> getEntityClass() {
        return (Class<? extends org.spongepowered.api.entity.Entity>) this.entityClass;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("modid", this.modId)
                .add("class", this.entityClass.getName());
    }

    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getEntityTiming(this);
        }
        return this.timing;
    }

    public boolean isKnown() {
        return true;
    }

    public boolean isVanilla() {
        return this.isVanilla;
    }
}
