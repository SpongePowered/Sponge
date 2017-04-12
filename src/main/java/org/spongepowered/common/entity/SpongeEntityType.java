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

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Locale;

import javax.annotation.Nullable;

public class SpongeEntityType extends SpongeCatalogType.Translatable implements EntityType {

    public static final EntityType UNKNOWN = new EntityType() {

        private final Translation translation = new SpongeTranslation("entity.generic.name");

        @Override
        public Translation getTranslation() {
            return this.translation;
        }

        @Override
        public String getName() {
            return "Unknown";
        }

        @Override
        public String getId() {
            return "unknown:unknown";
        }

        @Override
        public Class<? extends org.spongepowered.api.entity.Entity> getEntityClass() {
            throw new UnsupportedOperationException("Unknown entity type has no entity class");
        }

    };

    public final int entityTypeId;
    public final String entityName;
    public final String modId;
    public final Class<? extends Entity> entityClass;
    private EnumCreatureType creatureType;
    private final Translation translation;
    // currently not used
    public int trackingRange;
    public int updateFrequency;
    public boolean sendsVelocityUpdates;

    public SpongeEntityType(int id, String name, Class<? extends Entity> clazz, Translation translation) {
        this(id, name.toLowerCase(Locale.ENGLISH), "minecraft", clazz, translation);
    }

    public SpongeEntityType(int id, String name, String modId, Class<? extends Entity> clazz, Translation translation) {
        super(modId.toLowerCase(Locale.ENGLISH) + ":" + name.toLowerCase(Locale.ENGLISH), check(translation));
        this.entityTypeId = id;
        this.entityName = name.toLowerCase(Locale.ENGLISH);
        this.entityClass = clazz;
        this.modId = modId.toLowerCase(Locale.ENGLISH);
        String translationName;
        ResourceLocation loc = EntityList.getKey(clazz);
        if (loc == null) {
            translationName = "generic";
        } else {
            translationName = loc.getResourcePath();
        }

        this.translation = new SpongeTranslation("entity." + translationName + ".name");
    }

    private static Translation check(@Nullable Translation translation) {
        if (translation == null) {
            return UNKNOWN.getTranslation();
        } else {
            return translation;
        }
    }

    @Override
    public String getName() {
        return this.entityName;
    }

    public String getModId() {
        return this.modId;
    }

    public EnumCreatureType getEnumCreatureType() {
        return this.creatureType;
    }

    public void setEnumCreatureType(EnumCreatureType type) {
        this.creatureType = type;
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

}
