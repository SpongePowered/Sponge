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

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.data.Keys;
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
import org.spongepowered.api.entity.selector.Argument;
import org.spongepowered.api.entity.selector.Argument.Invertible;
import org.spongepowered.api.entity.selector.ArgumentHolder;
import org.spongepowered.api.entity.selector.ArgumentType;
import org.spongepowered.api.entity.selector.ArgumentTypes;
import org.spongepowered.api.entity.selector.Selector;
import org.spongepowered.api.entity.selector.SelectorType;
import org.spongepowered.api.entity.selector.SelectorTypes;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
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

/**
 * A resolver that acts like Vanilla Minecraft in many regards.
 */
public class SelectorResolver {

    private static final Collection<SelectorType> INFINITE_TYPES = ImmutableSet.of(SelectorTypes.ALL_ENTITIES.get(), SelectorTypes.ALL_PLAYERS.get());
    private static final Set<ArgumentType<?>> LOCATION_BASED_ARGUMENTS;

    static {
        ImmutableSet.Builder<ArgumentType<?>> builder = ImmutableSet.builder();
        builder.addAll(ArgumentTypes.POSITION.get().getTypes());
        builder.addAll(ArgumentTypes.DIMENSION.get().getTypes());
        builder.addAll(ArgumentTypes.RADIUS.get().getTypes());
        LOCATION_BASED_ARGUMENTS = builder.build();
    }

    private static Vector3d positionFromSource(CommandCause origin) {
        if (origin instanceof Locatable) {
            return ((Locatable) origin).getLocation().getPosition();
        }
        return null;
    }

    @Nullable private final CommandCause origin;
    @Nullable private final Entity entityOrigin;
    private final Iterable<ServerWorld> serverWorlds;
    private final Vector3d position;
    private final Selector selector;
    private final Predicate<Entity> selectorFilter;

    public SelectorResolver(Selector selector, Iterable<ServerWorld> serverWorlds) {
        this(selector, serverWorlds, null, null);
    }

    public SelectorResolver(Selector selector, CommandCause origin) {
        this(selector, SpongeCommon.getServer().getWorlds(), origin, positionFromSource(origin));
    }

    private SelectorResolver(Selector selector, Iterable<? extends ServerWorld> serverWorlds, @Nullable CommandCause origin, @Nullable Vector3d position) {
        this.selector = checkNotNull(selector);
        this.serverWorlds = ImmutableSet.copyOf(serverWorlds);
        this.origin = origin;
        if (this.origin instanceof Entity) {
            this.entityOrigin = (Entity) origin;
        } else {
            this.entityOrigin = null;
        }
        this.position = position == null ? Vector3d.ZERO : position;
        this.selectorFilter = this.makeFilter();
    }

    private Predicate<Entity> makeFilter() {
        Vector3d position = this.getPositionOrDefault(this.position, ArgumentTypes.POSITION.get());
        ArrayList<Predicate<Entity>> filters = new ArrayList<>();

        this.addTypeFilters(filters);
        this.addLevelFilters(filters);
        this.addGamemodeFilters(filters);
        this.addTeamFilters(filters);
        this.addScoreFilters(filters);
        this.addNameFilters(filters);
        this.addRadiusFilters(position, filters);
        this.addDimensionFilters(position, filters);
        this.addRotationFilters(filters);

        // Pack the list before returning it to improve space efficiency
        filters.trimToSize();
        return Functional.predicateAnd(filters);
    }

    private void addDimensionFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        if (this.selector.has(ArgumentTypes.DIMENSION.get().x()) ||
                this.selector.has(ArgumentTypes.DIMENSION.get().y()) ||
                this.selector.has(ArgumentTypes.DIMENSION.get().z())) return;

        Integer x = this.selector.get(ArgumentTypes.DIMENSION.get().x()).orElse(0);
        Integer y = this.selector.get(ArgumentTypes.DIMENSION.get().y()).orElse(0);
        Integer z = this.selector.get(ArgumentTypes.DIMENSION.get().z()).orElse(0);
        AABB axisalignedbb = getAABB(this.position.toInt(), x, y, z);

        if (this.entityOrigin != null) {
            filters.add(input -> this.entityOrigin.getBoundingBox().map(aabb -> aabb.intersects(axisalignedbb)).orElse(false));
        }
    }

    private void addGamemodeFilters(List<Predicate<Entity>> filters) {
        // TODO: For bleeding, update API to make ArgumentTypes.GAME_MODE invertible
        Optional<Invertible<GameMode>> gamemode = this.selector.getArgument(ArgumentTypes.GAME_MODE.get());
        if (gamemode.isPresent()) {
            final GameMode actualMode = gamemode.get().getValue();
            // If the gamemode is NOT_SET, that means accept any
            if (actualMode != GameModes.NOT_SET) {
                final boolean inverted = gamemode.get().isInverted();
                filters.add(input -> {
                    Optional<GameMode> mode = input.get(Keys.GAME_MODE);
                    return inverted ^ (mode.isPresent() && mode.get() == actualMode);
                });
            }
        }
    }

    private void addLevelFilters(List<Predicate<Entity>> filters) {
        Optional<Integer> levelMin = this.selector.get(ArgumentTypes.LEVEL.get().minimum());
        Optional<Integer> levelMax = this.selector.get(ArgumentTypes.LEVEL.get().maximum());
        if (levelMin.isPresent()) {
            final int actualMin = levelMin.get();
            filters.add(input -> {
                Optional<Integer> xp = input.get(Keys.EXPERIENCE);
                return xp.isPresent() && xp.get() >= actualMin;
            });
        }
        if (levelMax.isPresent()) {
            final int actualMax = levelMax.get();
            filters.add(input -> {
                Optional<Integer> xp = input.get(Keys.EXPERIENCE);
                return xp.isPresent() && xp.get() <= actualMax;
            });
        }
    }

    private void addNameFilters(List<Predicate<Entity>> filters) {
        Optional<Argument.Invertible<String>> nameOpt = this.selector.getArgument(ArgumentTypes.NAME.get());
        if (nameOpt.isPresent()) {
            final String name = nameOpt.get().getValue();
            final boolean inverted = nameOpt.get().isInverted();
            filters.add(input -> {
                Optional<Component> dispName = input.get(Keys.DISPLAY_NAME);
                return inverted ^ (dispName.isPresent() && name.equals(dispName.get().toString()));
            });
        }
    }

    private void addRadiusFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        Optional<Integer> radiusMin = this.selector.get(ArgumentTypes.RADIUS.get().minimum());
        Optional<Integer> radiusMax = this.selector.get(ArgumentTypes.RADIUS.get().maximum());
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
        Optional<Double> rotMinX = this.selector.get(ArgumentTypes.ROTATION.get().minimum().x());
        if (rotMinX.isPresent()) {
            final double rmx = rotMinX.get();
            filters.add(input -> input.getRotation().getX() >= rmx);
        }
        Optional<Double> rotMinY = this.selector.get(ArgumentTypes.ROTATION.get().minimum().y());
        if (rotMinY.isPresent()) {
            final double rmy = rotMinY.get();
            filters.add(input -> input.getRotation().getY() >= rmy);
        }
        Optional<Double> rotMaxX = this.selector.get(ArgumentTypes.ROTATION.get().maximum().x());
        if (rotMaxX.isPresent()) {
            final double rx = rotMaxX.get();
            filters.add(input -> input.getRotation().getX() <= rx);
        }
        Optional<Double> rotMaxY = this.selector.get(ArgumentTypes.ROTATION.get().maximum().y());
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
                Optional<Scoreboard> scoreboard = (Optional<Scoreboard>) Sponge.getServer().getServerScoreboard();
                if (!scoreboard.isPresent()) return false;

                Optional<Objective> objective = scoreboard.get().getObjective(objectiveName);
                if (!objective.isPresent()) return false;

                String name = input instanceof Player ? ((Player) input).getName() : input.getUniqueId().toString();
                Optional<Score> value = objective.get().getScore(TextComponent.of(name));
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
        Optional<Invertible<String>> teamOpt = this.selector.getArgument(ArgumentTypes.TEAM.get());
        if (teamOpt.isPresent()) {
            Invertible<String> teamArg = teamOpt.get();
            final boolean inverted = teamArg.isInverted();
            filters.add(input -> {
                if (!(input instanceof TeamMember)) return teamArg.getValue().isEmpty() && inverted;

                Optional<Scoreboard> scoreboard = (Optional<Scoreboard>) Sponge.getServer().getServerScoreboard();
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
        Optional<Invertible<EntityType<?>>> typeOpt = this.selector.getArgument(ArgumentTypes.ENTITY_TYPE.get());
        boolean untypedRandom = selectorType == SelectorTypes.RANDOM && !typeOpt.isPresent();
        if (selectorType == SelectorTypes.ALL_PLAYERS || selectorType == SelectorTypes.NEAREST_PLAYER || untypedRandom) {
            filters.add(input -> input instanceof Player);
        } else if (typeOpt.isPresent()) {
            Invertible<EntityType<?>> typeArg = typeOpt.get();
            final boolean inverted = typeArg.isInverted();
            final ArgumentType<EntityType<?>> type = typeArg.getType();
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
        int maxToSelect = this.selector.get(ArgumentTypes.COUNT.get()).orElse(defaultCount);
        boolean isReversed = maxToSelect < 0;
        maxToSelect = Math.abs(maxToSelect);
        AABB aabb = new AABB(255,255,255,255,255,255);
        Set<? extends World> worlds = (Set<? extends World>) this.getExtentSet();
        Stream<Entity> entityStream = worlds.stream().flatMap(ext -> ext.getEntities(aabb,this.selectorFilter).parallelStream());

        if (maxToSelect == 0) {
            return entityStream.sorted(this.distanceSort(isReversed))
                    .collect(ImmutableSet.toImmutableSet());
        }

        if (selectorType == SelectorTypes.RANDOM) {
            List<Entity> holder = entityStream.collect(Collectors.toList());
            if (holder.isEmpty()) return ImmutableSet.of();

            Collections.shuffle(holder);
            return ImmutableSet.copyOf(holder.subList(0, maxToSelect));
        }

        return entityStream.sorted(this.distanceSort(isReversed))
                .limit(maxToSelect)
                .collect(ImmutableSet.toImmutableSet());
    }

    private Comparator<? super Entity> distanceSort(boolean isReversed) {
        Vector3d position = this.getPositionOrDefault(this.position, ArgumentTypes.POSITION.get());
        int multiplier = isReversed ? -1 : 1;
        return (a, b) -> {
            double distToPosA = a.getLocation().getPosition().distanceSquared(position);
            double distToPosB = b.getLocation().getPosition().distanceSquared(position);
            return Double.compare(distToPosA, distToPosB) * multiplier;
        };
    }

    private Set<? extends net.minecraft.world.World> getExtentSet() {
        boolean location = this.selector.getArguments().stream()
                .anyMatch(arg -> LOCATION_BASED_ARGUMENTS.contains(arg.getType()));
        if (location && this.origin instanceof Locatable) {
            return ImmutableSet.of((net.minecraft.world.World) ((Locatable) this.origin).getWorld());
        }

        Set<net.minecraft.world.World> set = Collections.EMPTY_SET;
        this.serverWorlds.forEach(serverWorld -> set.add(serverWorld.getWorld()));

        return set;
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
