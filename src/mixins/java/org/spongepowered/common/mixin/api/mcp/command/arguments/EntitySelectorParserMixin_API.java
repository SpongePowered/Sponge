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
package org.spongepowered.common.mixin.api.mcp.command.arguments;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBoundsWrapped;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.advancements.criterion.MinMaxBounds_FloatBoundAccessor;
import org.spongepowered.common.accessor.advancements.criterion.MinMaxBounds_IntBoundAccessor;
import org.spongepowered.common.bridge.command.arguments.EntitySelectorParserBridge;
import org.spongepowered.common.command.selector.SpongeSelectorSortAlgorithm;
import org.spongepowered.math.vector.Vector3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin_API implements Selector.Builder {

    @Shadow @Final private StringReader reader;
    @Shadow private int limit;
    @Shadow private BiConsumer<Vec3d, List<? extends Entity>> sorter;
    @Shadow private MinMaxBounds.FloatBound distance;
    @Shadow private MinMaxBounds.IntBound level;
    @Shadow private boolean includeNonPlayers;
    @Shadow private boolean currentWorldOnly;
    @Shadow @Nullable private Double x;
    @Shadow @Nullable private Double y;
    @Shadow @Nullable private Double z;
    @Shadow @Nullable private Double dx;
    @Shadow @Nullable private Double dy;
    @Shadow @Nullable private Double dz;
    @Shadow private MinMaxBoundsWrapped xRotation = MinMaxBoundsWrapped.UNBOUNDED;
    @Shadow private MinMaxBoundsWrapped yRotation = MinMaxBoundsWrapped.UNBOUNDED;
    @Shadow private Predicate<Entity> filter;
    @Shadow private boolean self;
    @Shadow @Nullable private String username;
    @Shadow private int cursorStart;
    @Shadow @Nullable private UUID uuid;
    @Shadow private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionHandler;
    @Shadow private boolean hasNameEquals;
    @Shadow private boolean hasNameNotEquals;
    @Shadow private boolean isLimited;
    @Shadow private boolean isSorted;
    @Shadow private boolean hasGamemodeEquals;
    @Shadow private boolean hasGamemodeNotEquals;
    @Shadow private boolean hasTeamEquals;
    @Shadow private boolean hasTeamNotEquals;
    @Shadow private net.minecraft.entity.@Nullable EntityType<?> type;
    @Shadow private boolean typeInverse;
    @Shadow private boolean hasScores;
    @Shadow private boolean hasAdvancements;
    @Shadow private boolean checkPermission;

    @Shadow public abstract void shadow$setLimited(boolean value);
    @Shadow public abstract void shadow$setSorted(boolean value);
    @Shadow public abstract void shadow$setX(double xIn);
    @Shadow public abstract void shadow$setY(double yIn);
    @Shadow public abstract void shadow$setZ(double zIn);
    @Shadow public abstract void shadow$setDx(double dxIn);
    @Shadow public abstract void shadow$setDy(double dyIn);
    @Shadow public abstract void shadow$setDz(double dzIn);
    @Shadow public abstract void shadow$setIncludeNonPlayers(boolean includeNonPlayers);
    @Shadow public abstract EntitySelector shadow$build();
    @Shadow public abstract void shadow$addFilter(Predicate<Entity> p_197401_1_);

    @Nullable private Map<String, Range<@NonNull Integer>> api$scores;
    @Nullable private Object2BooleanOpenHashMap<String> api$advancement;
    @Nullable private Map<String, Object2BooleanOpenHashMap<String>> api$criterion;
    private boolean api$forceSelf;

    @Override
    public Selector.@NonNull Builder applySelectorType(final Supplier<SelectorType> selectorType) {
        return this.applySelectorType(selectorType.get());
    }

    @Override
    public Selector.@NonNull Builder applySelectorType(@NonNull final SelectorType selectorType) {
        try {
            ((EntitySelectorParserBridge) this).bridge$parseSelector(selectorType);
        } catch (final CommandSyntaxException commandSyntaxException) {
            throw new IllegalArgumentException("Could not parse provided SelectorType", commandSyntaxException);
        }
        return this;
    }

    @Override
    public Selector.@NonNull Builder includeSelf() {
        this.api$forceSelf = true;
        return this;
    }

    @Override
    public Selector.@NonNull Builder setLimit(final int limit) {
        this.limit = limit;
        this.shadow$setLimited(limit != Integer.MAX_VALUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setDistance(@NonNull final Range<@NonNull Double> range) {
        Preconditions.checkArgument(range.getMin() == null || range.getMin() >= 0, "min must be non-negative");
        Preconditions.checkArgument(range.getMax() == null || range.getMax() >= 0, "max must be non-negative");
        this.distance = MinMaxBounds_FloatBoundAccessor.accessor$init(
                this.api$floatFromDouble(range.getMin(), Function.identity()),
                this.api$floatFromDouble(range.getMax(), Function.identity()));
        return this;
    }

    @Override
    public Selector.@NonNull Builder setVolume(@NonNull final Vector3d corner1, @NonNull final Vector3d corner2) {
        final Vector3d minPoint = corner1.min(corner2);
        final Vector3d distance = corner1.max(corner2).sub(minPoint);
        this.shadow$setX(minPoint.getX());
        this.shadow$setY(minPoint.getY());
        this.shadow$setZ(minPoint.getZ());
        this.shadow$setDx(distance.getX());
        this.shadow$setDy(distance.getY());
        this.shadow$setDz(distance.getZ());
        return this;
    }

    @Override
    public Selector.@NonNull Builder setSortAlgorithm(@NonNull final Supplier<SelectorSortAlgorithm> algorithm) {
        return this.setSortAlgorithm(algorithm.get());
    }

    @Override
    public Selector.@NonNull Builder setSortAlgorithm(@NonNull final SelectorSortAlgorithm algorithm) {
        Preconditions.checkArgument(algorithm instanceof SpongeSelectorSortAlgorithm, "Must be a SpongeSelectorSortAlgorithm");
        this.shadow$setSorted(true);
        this.sorter = ((SpongeSelectorSortAlgorithm) algorithm).getSortAlgorithm();
        return this;
    }

    @Override
    public Selector.@NonNull Builder advancement(@NonNull final Advancement advancement) {
        return this.api$advancement(advancement, false);
    }

    @Override
    public Selector.@NonNull Builder notAdvancement(@NonNull final Advancement advancement) {
        return this.api$advancement(advancement, true);
    }

    @Override
    public Selector.@NonNull Builder advancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion) {
        return this.api$advancementCriterion(advancement, criterion, false);
    }

    @Override
    public Selector.@NonNull Builder notAdvancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion) {
        return this.api$advancementCriterion(advancement, criterion, true);
    }

    @Override
    public Selector.@NonNull Builder setDataView(@NonNull final DataView view) {
        try {
            // TODO: ensure this works as expected
            final String json = DataFormats.JSON.get().write(view);
            this.api$handle("nbt", json);
            return this;
        } catch (@NonNull final IOException e) {
            throw new RuntimeException("Could not create JSON representation of DataView", e);
        }
    }

    @Override
    public Selector.@NonNull Builder notEntityType(@NonNull final Supplier<@NonNull EntityType<@NonNull ?>> type) {
        return this.notEntityType(type.get());
    }

    @Override
    public Selector.@NonNull Builder notEntityType(@NonNull final EntityType<@NonNull ?> type) {
        this.api$handle("type", "!" + type.getKey().asString());
        return this;
    }

    @Override
    public Selector.@NonNull Builder entityType(@NonNull final Supplier<@NonNull EntityType<@NonNull ?>> type, final boolean inherit) {
        return this.entityType(type.get(), inherit);
    }

    @Override
    public Selector.@NonNull Builder entityType(@NonNull final EntityType<@NonNull ?> type, final boolean inherit) {
        this.api$handle("type", String.format("%s%s", inherit ? "#" : "", type.getKey().asString()));
        return this;
    }

    @Override
    public Selector.@NonNull Builder setExperienceLevel(@NonNull final Range<@NonNull Integer> range) {
        Preconditions.checkArgument(range.getMin() == null || range.getMin() >= 0, "min must be non-negative");
        Preconditions.checkArgument(range.getMax() == null || range.getMax() >= 0, "max must be non-negative");
        this.level = MinMaxBounds_IntBoundAccessor.accessor$init(range.getMin(), range.getMax());
        this.shadow$setIncludeNonPlayers(false);
        return this;
    }

    @Override
    public Selector.@NonNull Builder gameMode(@NonNull final Supplier<@NonNull GameMode> mode) {
        return this.gameMode(mode.get());
    }

    @Override
    public Selector.@NonNull Builder gameMode(@NonNull final GameMode mode) {
        this.api$handle("gamemode", mode.getKey().getValue(), Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notGameMode(@NonNull final Supplier<@NonNull GameMode> mode) {
        return this.notGameMode(mode.get());
    }

    @Override
    public Selector.@NonNull Builder notGameMode(@NonNull final GameMode mode) {
        this.api$handle("gamemode", mode.getKey().getValue(), Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder noTeam() {
        this.api$handle("team", "", Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder anyTeam() {
        this.api$handle("team", "", Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder team(@NonNull final Team team) {
        this.api$handle("team", team.getName(), Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notTeam(@NonNull final Team team) {
        this.api$handle("team", team.getName(), Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder name(@NonNull final String name) {
        this.api$handle("name", name, Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notName(@NonNull final String name) {
        this.api$handle("name", name, Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder score(@NonNull final Score score, @NonNull final Range<@NonNull Integer> range) {
        if (this.api$scores == null) {
            this.api$scores = new HashMap<>();
        }
        // TODO: Check this is right.
        this.api$scores.put(score.getName().toString(), range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder tag(@NonNull final String tag) {
        this.api$handle("tag", tag, Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notTag(@NonNull final String tag) {
        this.api$handle("tag", tag, Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setPitch(@NonNull final Range<@NonNull Double> range) {
        this.xRotation = this.api$getWrappedBounds(range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setYaw(@NonNull final Range<@NonNull Double> range) {
        this.yRotation = this.api$getWrappedBounds(range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder filter(@NonNull final Predicate<org.spongepowered.api.entity.@NonNull Entity> filter) {
        this.shadow$addFilter((Predicate<Entity>) (Object) filter);
        return this;
    }

    @Override
    @NonNull
    public Selector build() throws IllegalStateException {
        // Advancements and criteria
        if (this.api$advancement != null || this.api$criterion != null) {
            final List<String> entries = new ArrayList<>();
            if (this.api$advancement != null) {
                this.api$advancement.object2BooleanEntrySet().fastForEach(x -> entries.add(x.getKey() + "=" + x.getBooleanValue()));
            }
            if (this.api$criterion != null) {
                this.api$criterion.forEach((key, value) ->
                        value.object2BooleanEntrySet().fastForEach(x -> entries.add(key + "={" + x.getKey() + "=" + x.getBooleanValue() + "}")));
            }
            this.api$handle("advancements", "{" + String.join(",", entries) + "}");
            this.api$advancement = null;
            this.api$criterion = null;
        }

        if (this.api$scores != null) {
            final List<String> entries = new ArrayList<>();
            this.api$scores.forEach((key, range) -> entries.add(key + "=" + this.api$intRangeToStringRepresentation(range)));
            this.api$handle("scores", "{" + String.join(",", entries) + "}");
            this.api$scores = null;
        }

        if (this.api$forceSelf) {
            this.self = true;
        }

        return (Selector) this.shadow$build();
    }

    @Override
    public Selector.@NonNull Builder reset() {
        this.sorter = EntitySelectorParser.ARBITRARY;
        this.distance = MinMaxBounds.FloatBound.UNBOUNDED;
        this.level = MinMaxBounds.IntBound.UNBOUNDED;
        this.includeNonPlayers = false;
        this.currentWorldOnly = false;
        this.x = null;
        this.y = null;
        this.z = null;
        this.dx = null;
        this.dy = null;
        this.dz = null;
        this.xRotation = MinMaxBoundsWrapped.UNBOUNDED;
        this.yRotation = MinMaxBoundsWrapped.UNBOUNDED;
        this.filter = x -> true;
        this.self = false;
        this.username = null;
        this.cursorStart = 0;
        this.uuid = null;
        this.hasNameEquals = false;
        this.hasNameNotEquals = false;
        this.isLimited = false;
        this.isSorted = false;
        this.hasGamemodeEquals = false;
        this.hasGamemodeNotEquals = false;
        this.hasTeamEquals = false;
        this.hasTeamNotEquals = false;
        this.type = null;
        this.typeInverse = false;
        this.hasScores = false;
        this.hasAdvancements = false;
        this.checkPermission = false;
        this.reader.setCursor(0);
        this.suggestionHandler = EntitySelectorParser.SUGGEST_NONE;
        this.api$forceSelf = false;
        return this;
    }

    private Selector.@NonNull Builder api$advancement(@NonNull final Advancement advancement, final boolean inverted) {
        if (this.api$advancement == null) {
            this.api$advancement = new Object2BooleanOpenHashMap<>();
        }
        this.api$advancement.put(advancement.getKey().asString(), inverted);
        return this;
    }

    private Selector.@NonNull Builder api$advancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion,
            final boolean inverted) {
        if (this.api$criterion == null) {
            this.api$criterion = new HashMap<>();
        }
        this.api$criterion.computeIfAbsent(advancement.getKey().toString(), k -> new Object2BooleanOpenHashMap<>()).put(criterion.getName(), inverted);
        return this;
    }

    private void api$handle(@NonNull final String name, @NonNull final String value) {
        this.api$handle(name, value, Tristate.UNDEFINED);
    }

    private void api$handle(@NonNull final String name, @NonNull final String value, @NonNull final Tristate invert) {
        try {
            ((EntitySelectorParserBridge) this).bridge$handleValue(name, value, invert);
        } catch (final CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not create selector criteria based on input", ex);
        }
    }

    @Nullable
    private Float api$floatFromDouble(@Nullable final Double d, final Function<Float, Float> mapping) {
        if (d == null) {
            return null;
        }
        return mapping.apply(d.floatValue());
    }


    private MinMaxBoundsWrapped api$getWrappedBounds(final Range<@NonNull Double> range) {
        final Float a = this.api$floatFromDouble(range.getMin(), MathHelper::wrapDegrees);
        final Float b = this.api$floatFromDouble(range.getMax(), MathHelper::wrapDegrees);
        if (a == null) {
            return new MinMaxBoundsWrapped(null, b);
        }
        if (b == null) {
            return new MinMaxBoundsWrapped(a, null);
        }
        if (a <= b) {
            return new MinMaxBoundsWrapped(a, b);
        }
        return new MinMaxBoundsWrapped(b, a);
    }

    private String api$intRangeToStringRepresentation(@NonNull final Range<@NonNull Integer> range) {
        if (range.getMin() != null && range.getMax() != null && range.getMin().intValue() == range.getMax().intValue()) {
            return String.valueOf(range.getMax().intValue());
        }

        return String.format("%s..%s",
                range.getMin() == null ? "" : String.valueOf(range.getMin().intValue()),
                range.getMax() == null ? "" : String.valueOf(range.getMax().intValue()));
    }

}
