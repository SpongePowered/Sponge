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
package org.spongepowered.common.world.generation.config.noise;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.material.EndMaterialRules;
import net.minecraft.data.worldgen.material.NetherMaterialRules;
import net.minecraft.data.worldgen.material.OverworldMaterialRules;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.api.world.generation.config.noise.Noise;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.data.worldgen.material.OverworldMaterialRulesAccessor;

import java.util.Arrays;
import java.util.List;

public final class SpongeSurfaceRulesFactory implements SurfaceRule.Factory {

    private static MaterialRule rule(final net.minecraft.resources.ResourceKey<MaterialRule> key) {
        return MaterialRules.getRule(SpongeCommon.vanillaRegistry(Registries.MATERIAL_RULE), key);
    }

    private static MaterialCondition condition(final net.minecraft.resources.ResourceKey<MaterialCondition> key) {
        return MaterialRules.getCondition(SpongeCommon.vanillaRegistry(Registries.MATERIAL_CONDITION), key);
    }

    @Override
    public SurfaceRule overworld() {
        return (SurfaceRule) SpongeSurfaceRulesFactory.rule(OverworldMaterialRules.OVERWORLD);
    }

    @Override
    public SurfaceRule overworldLike(final boolean nearSurface, final boolean bedrockRoof, final boolean bedrockFloor) {
        return (SurfaceRule) OverworldMaterialRulesAccessor.invoker$createOverworldLike(
            SpongeCommon.vanillaRegistry(Registries.MATERIAL_RULE),
            nearSurface,
            bedrockRoof,
            bedrockFloor,
            SpongeSurfaceRulesFactory.rule(OverworldMaterialRulesAccessor.accessor$SURFACE()),
            SpongeSurfaceRulesFactory.rule(OverworldMaterialRulesAccessor.accessor$UNDERGROUND()),
            List.of() // TODO
        );
    }

    @Override
    public SurfaceRule nether() {
        return (SurfaceRule) SpongeSurfaceRulesFactory.rule(NetherMaterialRules.NETHER);
    }

    @Override
    public SurfaceRule end() {
        return (SurfaceRule) SpongeSurfaceRulesFactory.rule(EndMaterialRules.END);
    }

    @Override
    public SurfaceRule firstOf(final List<SurfaceRule> surfaceRules) {
        return (SurfaceRule) MaterialRules.sequence(surfaceRules.stream().map(MaterialRule.class::cast).toArray(MaterialRule[]::new));
    }

    @Override
    public SurfaceRule firstOf(final SurfaceRule... surfaceRules) {
        return this.firstOf(Arrays.asList(surfaceRules));
    }

    @Override
    public SurfaceRule badlands() {
        return (SurfaceRule) MaterialRules.bandlands();
    }

    @Override
    public SurfaceRule block(final BlockState blockState) {
        return (SurfaceRule) MaterialRules.state((net.minecraft.world.level.block.state.BlockState) blockState);
    }

    @Override
    public SurfaceRule test(final List<SurfaceRule.Condition> conditions, final SurfaceRule rule) {
        MaterialRule mcRule = (MaterialRule) rule;
        for (final SurfaceRule.Condition condition : conditions) {
            mcRule = MaterialRules.ifTrue((MaterialCondition) condition, mcRule);
        }
        return (SurfaceRule) mcRule;
    }

    @Override
    public SurfaceRule test(final SurfaceRule.Condition condition, final SurfaceRule rule) {
        return this.test(List.of(condition), rule);
    }

    @Override
    public SurfaceRule.Condition liquidDepth(final int offset, final int depthMultiplier) {
        return (SurfaceRule.Condition) MaterialRules.waterBlockCheck(offset, depthMultiplier);
    }

    @Override
    public SurfaceRule.Condition liquidDepthFromSurface(final int offset, final int depthMultiplier) {
        return (SurfaceRule.Condition) MaterialRules.waterStartCheck(offset, depthMultiplier);
    }

    @Override
    public SurfaceRule.Condition biome(final List<RegistryReference<Biome>> biomes) {
        final var stream = biomes.stream()
                .map(r -> net.minecraft.resources.ResourceKey.create(Registries.BIOME, ((Identifier) (Object) r.location())));
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>[] keys = stream.toArray(net.minecraft.resources.ResourceKey[]::new);
        return (SurfaceRule.Condition) MaterialRules.isBiome(SpongeCommon.vanillaRegistry(Registries.BIOME), keys);
    }

    @Override
    public SurfaceRule.Condition nearSurface() {
        return (SurfaceRule.Condition) MaterialRules.abovePreliminarySurface();
    }

    @Override
    public SurfaceRule.Condition hole() {
        return (SurfaceRule.Condition) MaterialRules.hole();
    }

    @Override
    public SurfaceRule.Condition verticalGradient(final String randomSource, final int fromY, final int toY) {
        final VerticalAnchor trueAtAndBelowAnchor = new VerticalAnchor.Absolute(fromY);
        final VerticalAnchor falseAtAndAboveAnchor = new VerticalAnchor.Absolute(toY);
        return (SurfaceRule.Condition) MaterialRules.verticalGradient(randomSource, trueAtAndBelowAnchor, falseAtAndAboveAnchor);
    }

    @Override
    public SurfaceRule.Condition verticalGradient(final String randomSource, final SurfaceRule.VerticalAnchor trueAtAndBelow, final SurfaceRule.VerticalAnchor falseAtAndAbove) {
        return (SurfaceRule.Condition) MaterialRules.verticalGradient(randomSource, (VerticalAnchor) trueAtAndBelow, (VerticalAnchor) falseAtAndAbove);
    }

    @Override
    public SurfaceRule.Condition steep() {
        return (SurfaceRule.Condition) MaterialRules.steep();
    }

    @Override
    public SurfaceRule.Condition onFloor() {
        return (SurfaceRule.Condition) SpongeSurfaceRulesFactory.condition(VanillaMaterialConditions.ON_FLOOR);
    }

    @Override
    public SurfaceRule.Condition underFloor(final int depth) {
        return (SurfaceRule.Condition) MaterialRules.stoneDepthCheck(0, true, depth, CaveSurface.FLOOR);
    }

    @Override
    public SurfaceRule.Condition floor(final int offset, final boolean useDepth, final int secondaryDepth) {
        return (SurfaceRule.Condition) MaterialRules.stoneDepthCheck(offset, useDepth, secondaryDepth, CaveSurface.FLOOR);
    }

    @Override
    public SurfaceRule.Condition onCeiling() {
        return (SurfaceRule.Condition) SpongeSurfaceRulesFactory.condition(VanillaMaterialConditions.ON_CEILING);
    }

    @Override
    public SurfaceRule.Condition underCeiling(final int depth) {
        return (SurfaceRule.Condition) MaterialRules.stoneDepthCheck(0, true, depth, CaveSurface.CEILING);
    }

    @Override
    public SurfaceRule.Condition ceiling(final int offset, final boolean useDepth, final int secondaryDepth) {
        return (SurfaceRule.Condition) MaterialRules.stoneDepthCheck(offset, useDepth, secondaryDepth, CaveSurface.CEILING);
    }

    @Override
    public SurfaceRule.Condition not(SurfaceRule.Condition condition) {
        return (SurfaceRule.Condition) MaterialRules.not((MaterialCondition) condition);
    }

    @Override
    public SurfaceRule.Condition snowyTemperature() {
        return (SurfaceRule.Condition) MaterialRules.temperature();
    }

    @Override
    public SurfaceRule.Condition blockAbove(final SurfaceRule.VerticalAnchor anchor, final int depthMultiplier) {
        return (SurfaceRule.Condition) MaterialRules.yBlockCheck((VerticalAnchor) anchor, depthMultiplier);
    }

    @Override
    public SurfaceRule.Condition surfaceAbove(final SurfaceRule.VerticalAnchor anchor, final int depthMultiplier) {
        return (SurfaceRule.Condition) MaterialRules.yStartCheck((VerticalAnchor) anchor, depthMultiplier);
    }

    @Override
    public SurfaceRule.Condition noiseThreshold(final RegistryReference<Noise> noise, final double min, final double max) {
        final net.minecraft.resources.ResourceKey<NormalNoise> key = net.minecraft.resources.ResourceKey.create(Registries.NOISE, ((Identifier) (Object) noise.location()));
        return (SurfaceRule.Condition) MaterialRules.noiseCondition2d(key, min, max);
    }

    // Anchors

    @Override
    public SurfaceRule.VerticalAnchor absolute(final int y) {
        return (SurfaceRule.VerticalAnchor) net.minecraft.world.level.levelgen.VerticalAnchor.absolute(y);
    }

    @Override
    public SurfaceRule.VerticalAnchor belowTop(final int blocks) {
        return (SurfaceRule.VerticalAnchor) net.minecraft.world.level.levelgen.VerticalAnchor.belowTop(blocks);
    }

    @Override
    public SurfaceRule.VerticalAnchor top() {
        return (SurfaceRule.VerticalAnchor) net.minecraft.world.level.levelgen.VerticalAnchor.top();
    }

    @Override
    public SurfaceRule.VerticalAnchor aboveBottom(final int blocks) {
        return (SurfaceRule.VerticalAnchor) net.minecraft.world.level.levelgen.VerticalAnchor.aboveBottom(blocks);
    }

    @Override
    public SurfaceRule.VerticalAnchor bottom() {
        return (SurfaceRule.VerticalAnchor) net.minecraft.world.level.levelgen.VerticalAnchor.bottom();
    }
}
