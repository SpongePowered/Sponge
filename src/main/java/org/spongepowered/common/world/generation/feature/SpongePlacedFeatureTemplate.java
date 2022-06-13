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
package org.spongepowered.common.world.generation.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.FeatureTemplate;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.api.world.generation.feature.PlacedFeatureTemplate;
import org.spongepowered.api.world.generation.feature.PlacementModifier;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record SpongePlacedFeatureTemplate(ResourceKey key, net.minecraft.world.level.levelgen.placement.PlacedFeature represented, DataPack<PlacedFeatureTemplate> pack) implements PlacedFeatureTemplate {

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final JsonElement serialized = SpongePlacedFeatureTemplate.encode(this, SpongeCommon.server().registryAccess());
        try {
            final DataContainer container = DataFormats.JSON.get().read(serialized.toString());
            container.set(Queries.CONTENT_VERSION, this.contentVersion());
            return container;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read deserialized Biome:\n" + serialized, e);
        }
    }

    @Override
    public PlacedFeature feature() {
        return (PlacedFeature) (Object) this.represented;
    }

    public static JsonElement encode(final PlacedFeatureTemplate template, final RegistryAccess registryAccess) {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        return net.minecraft.world.level.levelgen.placement.PlacedFeature.DIRECT_CODEC.encodeStart(ops, (net.minecraft.world.level.levelgen.placement.PlacedFeature) (Object) template.feature()).getOrThrow(false, e -> {});
    }

    public static net.minecraft.world.level.levelgen.placement.PlacedFeature decode(final JsonElement json, final RegistryAccess registryAccess) {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        return net.minecraft.world.level.levelgen.placement.PlacedFeature.DIRECT_CODEC.parse(ops, json).getOrThrow(false, e -> {});
    }

    public static PlacedFeatureTemplate decode(final DataPack<PlacedFeatureTemplate> pack, final ResourceKey key, final JsonElement packEntry, final RegistryAccess registryAccess) {
        final net.minecraft.world.level.levelgen.placement.PlacedFeature parsed = SpongePlacedFeatureTemplate.decode(packEntry, registryAccess);
        return new SpongePlacedFeatureTemplate(key, parsed, pack);
    }

    public static class BuilderImpl extends AbstractResourceKeyedBuilder<PlacedFeatureTemplate, Builder> implements Builder {

        private DataPack<PlacedFeatureTemplate> pack = DataPacks.PLACED_FEATURE;
        private Holder<ConfiguredFeature<?, ?>> feature;
        private List<net.minecraft.world.level.levelgen.placement.PlacementModifier> modifiers = new ArrayList<>();

        @Override
        public Builder reset() {
            this.key = null;
            this.feature = null;
            this.modifiers.clear();
            this.pack = DataPacks.PLACED_FEATURE;
            return this;
        }

        @Override
        public Builder from(final PlacedFeature feature) {
            this.feature(feature.feature());
            this.modifiers.clear();
            this.modifiers.addAll((List) feature.placementModifiers());
            return this;
        }

        @Override
        public Builder feature(final Feature feature) {
            final Registry<ConfiguredFeature<?, ?>> registry = SpongeCommon.server().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            final ResourceLocation key = registry.getKey((ConfiguredFeature<?, ?>) (Object) feature);
            if (key == null) {
                // TODO this gets serialized as inline configured feature - is that an issue?
                this.feature = Holder.direct((ConfiguredFeature<?, ?>) (Object) feature);
            } else {
                this.feature = registry.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, key));
            }
            return this;
        }

        @Override
        public Builder feature(final FeatureTemplate feature) {
            final Registry<ConfiguredFeature<?, ?>> registry = SpongeCommon.server().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            final net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> key =
                    net.minecraft.resources.ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, ((ResourceLocation) (Object) feature.key()));
            this.feature = Holder.Reference.createStandAlone(registry, key);
            return this;
        }

        @Override
        public Builder addModifier(final PlacementModifier modifier) {
            this.modifiers.add((net.minecraft.world.level.levelgen.placement.PlacementModifier) modifier);
            return this;
        }

        @Override
        public Builder from(final PlacedFeatureTemplate value) {
            return this.from(value.feature()).key(value.key()).pack(value.pack());
        }

        @Override
        public Builder fromDataPack(final DataView pack) throws IOException {
            final JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write(pack));
            final net.minecraft.world.level.levelgen.placement.PlacedFeature feature = SpongePlacedFeatureTemplate.decode(json, SpongeCommon.server().registryAccess());
            this.from((PlacedFeature) (Object) feature);
            return this;
        }

        @Override
        public Builder pack(final DataPack<PlacedFeatureTemplate> pack) {
            this.pack = pack;
            return this;
        }

        @Override
        protected PlacedFeatureTemplate build0() {
            var feature = new net.minecraft.world.level.levelgen.placement.PlacedFeature(this.feature, this.modifiers);
            return new SpongePlacedFeatureTemplate(this.key, feature, this.pack);
        }
    }
}
