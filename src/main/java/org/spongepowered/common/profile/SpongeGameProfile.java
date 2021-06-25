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
package org.spongepowered.common.profile;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeGameProfile implements GameProfile {

    private static final Gson GSON = new Gson();
    public static final UUID EMPTY_UUID = new UUID(0, 0);

    public static SpongeGameProfile of(final com.mojang.authlib.GameProfile mcProfile) {
        final UUID uniqueId = mcProfile.getId() == null ? SpongeGameProfile.EMPTY_UUID : mcProfile.getId();
        final String name = mcProfile.getName();
        final List<SpongeProfileProperty> properties = mcProfile.getProperties().values().stream()
                .map(SpongeProfileProperty::new)
                .collect(Collectors.toList());
        return new SpongeGameProfile(uniqueId, name, properties);
    }

    public static SpongeGameProfile basicOf(final com.mojang.authlib.GameProfile mcProfile) {
        final UUID uniqueId = mcProfile.getId() == null ? SpongeGameProfile.EMPTY_UUID : mcProfile.getId();
        final String name = mcProfile.getName();
        return new SpongeGameProfile(uniqueId, name);
    }

    public static com.mojang.authlib.GameProfile toMcProfile(final GameProfile profile) {
        return ((SpongeGameProfile) profile).toMcProfile();
    }

    public static GameProfile unsignedOf(final GameProfile profile) {
        if (profile.properties().isEmpty()) {
            return profile;
        }
        return new SpongeGameProfile(profile.uniqueId(), profile.name().orElse(null),
                profile.properties().stream().map(SpongeGameProfile::withoutSignature)
                        .collect(ImmutableList.toImmutableList()));
    }

    private static String decodeBase64(final String value) {
        return new String(java.util.Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)), Charsets.UTF_8);
    }

    private static String encodeBase64(final String value) {
        return new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)), Charsets.UTF_8);
    }

    private static SpongeProfileProperty withoutSignature(final ProfileProperty property) {
        if (!property.hasSignature()) {
            return (SpongeProfileProperty) property;
        }
        final String decoded = SpongeGameProfile.decodeBase64(property.value());
        final JsonObject json;
        try {
            json = SpongeGameProfile.GSON.fromJson(decoded, JsonObject.class);
        } catch (final Throwable t) {
            // Not valid json, we can't do anything about this
            return (SpongeProfileProperty) property;
        }
        if (!json.has("signatureRequired")) {
            return (SpongeProfileProperty) property;
        }
        json.remove("signatureRequired");
        final String encoded = SpongeGameProfile.encodeBase64(SpongeGameProfile.GSON.toJson(json));
        return new SpongeProfileProperty(property.name(), encoded, null);
    }

    private final UUID uniqueId;
    private final @Nullable String name;
    private final List<SpongeProfileProperty> properties;

    public SpongeGameProfile(final UUID uniqueId, final @Nullable String name) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        this.uniqueId = uniqueId;
        this.name = name;
        this.properties = ImmutableList.of();
    }

    public SpongeGameProfile(final UUID uniqueId, final @Nullable String name, final List<SpongeProfileProperty> properties) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(properties, "properties");
        this.uniqueId = uniqueId;
        this.name = name;
        this.properties = ImmutableList.copyOf(properties);
    }

    public com.mojang.authlib.GameProfile toMcProfile() {
        final UUID uniqueId = this.uniqueId.equals(SpongeGameProfile.EMPTY_UUID) ? null : this.uniqueId;
        final String name = this.name;
        final com.mojang.authlib.GameProfile mcProfile = new com.mojang.authlib.GameProfile(uniqueId, name);
        for (final SpongeProfileProperty property : this.properties) {
            mcProfile.getProperties().put(property.name(), property.asProperty());
        }
        return mcProfile;
    }

    @Override
    @NonNull
    public UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    @NonNull
    public Optional<String> name() {
        return Optional.ofNullable(this.name);
    }

    @Override
    @NonNull
    public GameProfile withName(final @Nullable String name) {
        return new SpongeGameProfile(this.uniqueId, name, this.properties);
    }

    @Override
    @NonNull
    public List<ProfileProperty> properties() {
        return (List) this.properties;
    }

    @Override
    @NonNull
    public GameProfile withoutProperties() {
        return new SpongeGameProfile(this.uniqueId, this.name);
    }

    @Override
    @NonNull
    public GameProfile withProperties(final Iterable<ProfileProperty> properties) {
        return new SpongeGameProfile(this.uniqueId, this.name,
                ImmutableList.<SpongeProfileProperty>builder().addAll(this.properties).addAll((Iterable) properties).build());
    }

    @Override
    @NonNull
    public GameProfile withProperty(final ProfileProperty property) {
        return new SpongeGameProfile(this.uniqueId, this.name,
                ImmutableList.<SpongeProfileProperty>builder().addAll(this.properties).add((SpongeProfileProperty) property).build());
    }

    @Override
    @NonNull
    public GameProfile withoutProperties(final @NonNull Iterable<ProfileProperty> properties) {
        final List<ProfileProperty> toRemove = properties instanceof Collection<?> ?
                (List<ProfileProperty>) properties : ImmutableList.copyOf(properties);
        return new SpongeGameProfile(this.uniqueId, this.name, this.properties.stream()
                .filter(property -> !toRemove.contains(property))
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    @NonNull
    public GameProfile withoutProperty(final ProfileProperty propertyToRemove) {
        return new SpongeGameProfile(this.uniqueId, this.name, this.properties.stream()
                .filter(property -> !propertyToRemove.equals(property))
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    @NonNull
    public GameProfile withoutProperties(final Predicate<ProfileProperty> filter) {
        return new SpongeGameProfile(this.uniqueId, this.name, this.properties.stream()
                .filter(property -> !filter.test(property))
                .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    @NonNull
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Profile.UUID, this.uniqueId().toString());
        if (this.name != null) {
            container.set(Constants.Profile.NAME, this.name);
        }
        if (!this.properties.isEmpty()) {
            final List<DataContainer> entries = new ArrayList<>(this.properties.size());
            for (final ProfileProperty property : this.properties) {
                entries.add(property.toContainer());
            }
            container.set(Constants.Profile.PROPERTIES, entries);
        }
        return container;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeGameProfile that = (SpongeGameProfile) o;
        return this.uniqueId.equals(that.uniqueId) &&
                Objects.equals(this.name, that.name) &&
                this.properties.size() == that.properties.size() &&
                this.properties.containsAll(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId, this.name, Arrays.hashCode(this.properties.toArray()));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeGameProfile.class.getSimpleName() + "[", "]")
                .add("uniqueId=" + this.uniqueId)
                .add("name=" + this.name)
                .toString();
    }

    public static final class Factory implements GameProfile.Factory {

        @Override
        @NonNull
        public GameProfile of(final @NonNull UUID uniqueId, final @Nullable String name) {
            Objects.requireNonNull(uniqueId);

            return new SpongeGameProfile(uniqueId, name);
        }

    }
}
