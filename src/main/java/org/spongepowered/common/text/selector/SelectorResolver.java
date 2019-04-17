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
package org.spongepowered.common.text.selector;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.Argument.Invertible;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.SpongeImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * A resolver that acts like Vanilla Minecraft in many regards.
 */
public class SelectorResolver {

    private static final Collection<SelectorType> INFINITE_TYPES = ImmutableSet.of(SelectorTypes.ALL_ENTITIES, SelectorTypes.ALL_PLAYERS);
    private static final Set<ArgumentType<?>> LOCATION_BASED_ARGUMENTS;

    static {
        ImmutableSet.Builder<ArgumentType<?>> builder = ImmutableSet.builder();
        builder.addAll(ArgumentTypes.POSITION.getTypes());
        builder.addAll(ArgumentTypes.DIMENSION.getTypes());
        builder.addAll(ArgumentTypes.RADIUS.getTypes());
        LOCATION_BASED_ARGUMENTS = builder.build();
    }

    private static Vector3d positionFromSource(CommandSource origin) {
        if (origin instanceof Locatable) {
            return ((Locatable) origin).getLocation().getPosition();
        }
        return null;
    }

    @Nullable private final CommandSource origin;
    @Nullable private final Entity entityOrigin;
    private final Collection<Extent> extents;
    private final Vector3d position;
    private final Selector selector;
    private final Predicate<Entity> selectorFilter;

    public SelectorResolver(Selector selector, Collection<? extends Extent> extents) {
        this(selector, extents, null, null);
    }

    public SelectorResolver(Selector selector, CommandSource origin) {
        this(selector, SpongeImpl.getGame().getServer().getWorlds(), origin, positionFromSource(origin));
    }

    private SelectorResolver(Selector selector, Collection<? extends Extent> extents, @Nullable CommandSource origin, @Nullable Vector3d position) {
        this.selector = checkNotNull(selector);
        this.extents = ImmutableSet.copyOf(extents);
        this.origin = origin;
        if (this.origin instanceof Entity) {
            this.entityOrigin = (Entity) origin;
        } else {
            this.entityOrigin = null;
        }
        this.position = position == null ? Vector3d.ZERO : position;
        this.selectorFilter = makeFilter();
    }

    private Predicate<Entity> makeFilter() {
        Vector3d position = getPositionOrDefault(this.position, ArgumentTypes.POSITION);
        ArrayList<Predicate<Entity>> filters = new ArrayList<Predicate<Entity>>();

        addTypeFilters(filters);
        addLevelFilters(filters);
        addGamemodeFilters(filters);
        addTeamFilters(filters);
        addScoreFilters(filters);
        addNameFilters(filters);
        addRadiusFilters(position, filters);
        addDimensionFilters(position, filters);
        addRotationFilters(filters);

        // Pack the list before returning it to improve space efficiency
        filters.trimToSize();
        return Functional.predicateAnd(filters);
    }

    private void addDimensionFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        if (this.selector.has(ArgumentTypes.DIMENSION.x()) || 
                this.selector.has(ArgumentTypes.DIMENSION.y()) || 
                this.selector.has(ArgumentTypes.DIMENSION.z())) return;

        Integer x = this.selector.get(ArgumentTypes.DIMENSION.x()).orElse(0);
        Integer y = this.selector.get(ArgumentTypes.DIMENSION.y()).orElse(0);
        Integer z = this.selector.get(ArgumentTypes.DIMENSION.z()).orElse(0);
        AABB axisalignedbb = getAABB(this.position.toInt(), x, y, z);

        if (this.entityOrigin != null) {
            filters.add(input -> this.entityOrigin.getBoundingBox().map(aabb -> aabb.intersects(axisalignedbb)).orElse(false));
        }
    }

    private void addGamemodeFilters(List<Predicate<Entity>> filters) {
        // TODO: For bleeding, update API to make ArgumentTypes.GAME_MODE invertible
        Optional<Invertible<GameMode>> gamemode = this.selector.getArgument((ArgumentType.Invertible<GameMode>) ArgumentTypes.GAME_MODE);
        if (gamemode.isPresent()) {
            final GameMode actualMode = gamemode.get().getValue();
            // If the gamemode is NOT_SET, that means accept any
            if (actualMode != GameModes.NOT_SET) {
                final boolean inverted = gamemode.get().isInverted();
                filters.add(input -> {
                    Optional<GameModeData> mode = input.get(GameModeData.class);
                    return inverted ^ (mode.isPresent() && mode.get().type().get() == actualMode);
                });
            }
        }
    }

    private void addLevelFilters(List<Predicate<Entity>> filters) {
        Optional<Integer> levelMin = this.selector.get(ArgumentTypes.LEVEL.minimum());
        Optional<Integer> levelMax = this.selector.get(ArgumentTypes.LEVEL.maximum());
        if (levelMin.isPresent()) {
            final int actualMin = levelMin.get();
            filters.add(input -> {
                Optional<ExperienceHolderData> xp = input.get(ExperienceHolderData.class);
                return xp.isPresent() && xp.get().level().get() >= actualMin;
            });
        }
        if (levelMax.isPresent()) {
            final int actualMax = levelMax.get();
            filters.add(input -> {
                Optional<ExperienceHolderData> xp = input.get(ExperienceHolderData.class);
                return xp.isPresent() && xp.get().level().get() <= actualMax;
            });
        }
    }

    private void addNameFilters(List<Predicate<Entity>> filters) {
        Optional<Argument.Invertible<String>> nameOpt = this.selector.getArgument(ArgumentTypes.NAME);
        if (nameOpt.isPresent()) {
            final String name = nameOpt.get().getValue();
            final boolean inverted = nameOpt.get().isInverted();
            filters.add(input -> {
                Optional<DisplayNameData> dispName = input.get(DisplayNameData.class);
                return inverted ^ (dispName.isPresent() && name.equals(dispName.get().displayName().get().toPlain()));
            });
        }
    }

    private void addRadiusFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        Optional<Integer> radiusMin = this.selector.get(ArgumentTypes.RADIUS.minimum());
        Optional<Integer> radiusMax = this.selector.get(ArgumentTypes.RADIUS.maximum());
        if (radiusMin.isPresent()) {
            double radMin = Math.max(radiusMin.get(), 1.0E-4D);
            final double radMinSquared = radMin * radMin;
            filters.add(input -> input.getLocation().getPosition().distanceSquared(position) >= radMinSquared);
        }
        if (radiusMax.isPresent()) {
            double radMax = Math.max(radiusMax.get(), 1.0E-4D);
            final double radMaxSquared = radMax * radMax;
            filters.add(input -> input.getLocation().getPosition().distanceSquared(position) <= radMaxSquared);
        }
    }

    private void addRotationFilters(List<Predicate<Entity>> filters) {
        Optional<Double> rotMinX = this.selector.get(ArgumentTypes.ROTATION.minimum().x());
        if (rotMinX.isPresent()) {
            final double rmx = rotMinX.get();
            filters.add(input -> input.getRotation().getX() >= rmx);
        }
        Optional<Double> rotMinY = this.selector.get(ArgumentTypes.ROTATION.minimum().y());
        if (rotMinY.isPresent()) {
            final double rmy = rotMinY.get();
            filters.add(input -> input.getRotation().getY() >= rmy);
        }
        Optional<Double> rotMaxX = this.selector.get(ArgumentTypes.ROTATION.maximum().x());
        if (rotMaxX.isPresent()) {
            final double rx = rotMaxX.get();
            filters.add(input -> input.getRotation().getX() <= rx);
        }
        Optional<Double> rotMaxY = this.selector.get(ArgumentTypes.ROTATION.maximum().y());
        if (rotMaxY.isPresent()) {
            final double ry = rotMaxY.get();
            filters.add(input -> input.getRotation().getY() <= ry);
        }
    }

    private void addScoreFilters(List<Predicate<Entity>> filters) {
        for (Argument<?> arg : this.selector.getArguments()) {
            String key = arg.getType().getKey();
            if (!key.startsWith("score_")) continue;    

            String objectiveName = key.replaceAll("^score_", "").replaceAll("_min$", "");
            boolean min = key.endsWith("_min");
            filters.add(input -> {                
                Optional<Scoreboard> scoreboard = Sponge.getGame().getServer().getServerScoreboard();
                if (!scoreboard.isPresent()) return false;

                Optional<Objective> objective = scoreboard.get().getObjective(objectiveName);
                if (!objective.isPresent()) return false;

                String name = input instanceof Player ? ((Player) input).getName() : input.getUniqueId().toString();
                Optional<Score> value = objective.get().getScore(Text.of(name));
                if (!value.isPresent()) return false;

                if (min) {
                    return ((Integer) arg.getValue()) <= value.get().getScore();
                } else {
                    return ((Integer) arg.getValue()) >= value.get().getScore();
                }
            });
        }
    }

    private void addTeamFilters(List<Predicate<Entity>> filters) {
        Optional<Invertible<String>> teamOpt = this.selector.getArgument(ArgumentTypes.TEAM);
        if (teamOpt.isPresent()) {
            Invertible<String> teamArg = teamOpt.get();
            final boolean inverted = teamArg.isInverted();
            filters.add(input -> {
                if (!(input instanceof TeamMember)) return teamArg.getValue().isEmpty() && inverted;

                Optional<Scoreboard> scoreboard = Sponge.getGame().getServer().getServerScoreboard();
                if (!scoreboard.isPresent()) return false;

                Optional<Team> team = scoreboard.get().getMemberTeam(((TeamMember) input).getTeamRepresentation());
                if (teamArg.getValue().isEmpty()) {
                    return inverted ^ team.isPresent();
                } else {
                    return inverted ^ (team.isPresent() && team.get().getName().equals(teamArg.getValue()));
                }
            });
        }
    }

    private void addTypeFilters(List<Predicate<Entity>> filters) {
        SelectorType selectorType = this.selector.getType();
        Optional<Argument.Invertible<EntityType>> typeOpt = this.selector.getArgument(ArgumentTypes.ENTITY_TYPE);
        boolean untypedRandom = selectorType == SelectorTypes.RANDOM && !typeOpt.isPresent();
        if (selectorType == SelectorTypes.ALL_PLAYERS || selectorType == SelectorTypes.NEAREST_PLAYER || untypedRandom) {
            filters.add(input -> input instanceof Player);
        } else if (typeOpt.isPresent()) {
            Argument.Invertible<EntityType> typeArg = typeOpt.get();
            final boolean inverted = typeArg.isInverted();
            final EntityType type = typeArg.getValue();
            filters.add(input -> inverted != (input.getType() == type));
        }
    }

    private Vector3d getPositionOrDefault(Vector3d pos, ArgumentHolder.Vector3<?, ? extends Number> vecTypes) {
        Optional<Double> x = this.selector.get(vecTypes.x()).map(Number::doubleValue);
        Optional<Double> y = this.selector.get(vecTypes.y()).map(Number::doubleValue);
        Optional<Double> z = this.selector.get(vecTypes.z()).map(Number::doubleValue);
        return new Vector3d(x.orElse(pos.getX()), y.orElse(pos.getY()), z.orElse(pos.getZ()));
    }

    // This returns an ImmutableSet as we want a guarantee of order. We're also using a set because API 7
    // must return a set, so returning a list here will require another object to needlessly be created.
    //
    // TODO: For API 8, this can be a list instead.
    public ImmutableSet<Entity> resolve() {
        SelectorType selectorType = this.selector.getType();
        if (selectorType == SelectorTypes.SOURCE) {
            if (this.entityOrigin != null && this.selectorFilter.test(this.entityOrigin)) {
                return ImmutableSet.of(this.entityOrigin);
            }
            return ImmutableSet.of();
        }

        int defaultCount = 1;
        if (INFINITE_TYPES.contains(selectorType)) {
            defaultCount = 0;
        }
        int maxToSelect = this.selector.get(ArgumentTypes.COUNT).orElse(defaultCount);
        boolean isReversed = maxToSelect < 0;
        maxToSelect = Math.abs(maxToSelect);
        Set<? extends Extent> extents = getExtentSet();
        Stream<Entity> entityStream = extents.stream()
                .flatMap(ext -> ext.getEntities().stream())
                .filter(this.selectorFilter);

        if (maxToSelect == 0) {
            return entityStream.sorted(distanceSort(isReversed))
                    .collect(ImmutableSet.toImmutableSet());
        }

        if (selectorType == SelectorTypes.RANDOM) {
            List<Entity> holder = entityStream.collect(Collectors.toList());
            if (holder.isEmpty()) return ImmutableSet.of();

            Collections.shuffle(holder);
            return ImmutableSet.copyOf(holder.subList(0, maxToSelect));
        }

        return entityStream.sorted(distanceSort(isReversed))
                .limit(maxToSelect)
                .collect(ImmutableSet.toImmutableSet());
    }

    private Comparator<? super Entity> distanceSort(boolean isReversed) {
        Vector3d position = getPositionOrDefault(this.position, ArgumentTypes.POSITION);
        int multiplier = isReversed ? -1 : 1;
        return (a, b) -> {
            double distToPosA = a.getLocation().getPosition().distanceSquared(position);
            double distToPosB = b.getLocation().getPosition().distanceSquared(position);
            return Double.compare(distToPosA, distToPosB) * multiplier;
        };
    }

    private Set<? extends Extent> getExtentSet() {
        boolean location = this.selector.getArguments().stream()
                .anyMatch(arg -> LOCATION_BASED_ARGUMENTS.contains(arg.getType()));
        if (location && this.origin instanceof Locatable) {
            return ImmutableSet.of(((Locatable) this.origin).getWorld());
        }
        return ImmutableSet.copyOf(this.extents);
    }

    private static AABB getAABB(Vector3i pos, int x, int y, int z) {
        boolean isNegativeX = x < 0;
        boolean isNegativeY = y < 0;
        boolean isNegativeZ = z < 0;

        // First corner co-ordinates (intended to be the minimum co-ordinates)
        int xmin = pos.getX() + (isNegativeX ? x : 0);
        int ymin = pos.getY() + (isNegativeY ? y : 0);
        int zmin = pos.getZ() + (isNegativeZ ? z : 0);

        // Second corner co-ordinates (intended to be the maximum co-ordinates)
        int xmax = pos.getX() + (isNegativeX ? 0 : x) + 1;
        int ymax = pos.getY() + (isNegativeY ? 0 : y) + 1;
        int zmax = pos.getZ() + (isNegativeZ ? 0 : z) + 1;

        return new AABB((double) xmin, (double) ymin, (double) zmin, (double) xmax, (double) ymax, (double) zmax);
    }

}
