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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public final class TextureUtil {

    private TextureUtil() {
    }

    public static Set<SkinPart> fromFlags(int flags) {
        return Sponge.getRegistry().getAllOf(SkinPart.class).stream()
                .map(part -> (SpongeSkinPart) part)
                .filter(part -> part.test(flags))
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Nullable
    public static ProfileProperty fromProfile(GameProfile profile) {
        for (Property property : checkNotNull(profile, "profile").getProperties().get("textures")) {
            return (ProfileProperty) property;
        }

        return null;
    }

    public static void toPropertyMap(PropertyMap map, @Nullable ProfileProperty property) {
        checkNotNull(map, "property map");

        map.removeAll("textures");

        if (property != null) {
            map.put("textures", (Property) property);
        }
    }

    @Nullable
    public static ProfileProperty read(NBTTagCompound compound) {
        checkNotNull(compound, "compound");

        if (compound.hasKey(NbtDataUtil.HUMANOID_TEXTURES_VALUE, NbtDataUtil.TAG_STRING)
                && compound.hasKey(NbtDataUtil.HUMANOID_TEXTURES_SIGNATURE, NbtDataUtil.TAG_STRING)) {
            return ProfileProperty.textures(compound.getString(NbtDataUtil.HUMANOID_TEXTURES_VALUE), compound.getString(NbtDataUtil.HUMANOID_TEXTURES_SIGNATURE));
        } else {
            return null;
        }
    }

    public static void write(NBTTagCompound compound, @Nullable ProfileProperty property) {
        checkNotNull(compound, "compound");

        if (property != null) {
            compound.setString(NbtDataUtil.HUMANOID_TEXTURES_VALUE, property.getValue());
            compound.setString(NbtDataUtil.HUMANOID_TEXTURES_SIGNATURE, checkNotNull(property.getSignature().orElse(null), "signature"));
        } else {
            compound.removeTag(NbtDataUtil.HUMANOID_TEXTURES_VALUE);
            compound.removeTag(NbtDataUtil.HUMANOID_TEXTURES_SIGNATURE);
        }
    }

    public static void migrateLegacyTextureUniqueId(final GameProfile profile, final UUID uniqueId) {
        CompletableFuture<org.spongepowered.api.profile.GameProfile> future = Sponge.getServer().getGameProfileManager().get(uniqueId);
        future.thenAccept(result -> {
            ProfileProperty property = fromProfile((GameProfile) result);
            if (property != null) {
                toPropertyMap(profile.getProperties(), property);
            }
        });
    }

    public static void uniqueIdToProfileProperty(UUID uniqueId, Consumer<org.spongepowered.api.profile.GameProfile> future) {
        Sponge.getServer().getGameProfileManager().fill(org.spongepowered.api.profile.GameProfile.of(uniqueId, null)).thenAccept(future);
    }

}
