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
import com.google.common.collect.ImmutableList;
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
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.Argument.Invertible;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
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

    private static Set<Extent> extentFromSource(CommandSource origin) {
        if (origin instanceof Locatable) {
            return ImmutableSet.of(((Locatable) origin).getWorld());
        }
        // System
        return ImmutableSet.copyOf(SpongeImpl.getGame().getServer().getWorlds());
    }

    private static Vector3d positionFromSource(CommandSource origin) {
        if (origin instanceof Locatable) {
            return ((Locatable) origin).getLocation().getPosition();
        }
        return null;
    }

    private static <I, R extends I> Predicate<I> requireTypePredicate(Class<I> inputType, final Class<R> requiredType) {
        return requiredType::isInstance;
    }

    private final CommandSource origin;
    private final Collection<Extent> extents;
    private final Vector3d position;
    private final Selector selector;
    private final Predicate<Entity> selectorFilter;

    public SelectorResolver(Collection<? extends Extent> extents, Selector selector) {
        this(null, extents, null, selector);
    }

    public SelectorResolver(Location<World> location, Selector selector) {
        this(null, ImmutableSet.of(location.getExtent()), location.getPosition(), selector);
    }

    public SelectorResolver(CommandSource origin, Selector selector) {
        this(origin, extentFromSource(origin), positionFromSource(origin), selector);
    }

    private SelectorResolver(@Nullable CommandSource origin, Collection<? extends Extent> extents, @Nullable Vector3d position, Selector selector) {
        this.origin = origin;
        this.extents = ImmutableSet.copyOf(extents);
        this.position = position == null ? Vector3d.ZERO : position;
        this.selector = checkNotNull(selector);
        this.selectorFilter = makeFilter();
    }

    private Predicate<Entity> makeFilter() {
        final Selector sel = this.selector;
        Vector3d position = getPositionOrDefault(this.position, ArgumentTypes.POSITION);
        ArrayList<Predicate<Entity>> filters = new ArrayList<>(sel.getArguments().size());

        if (isPlayerOnlySelector()) {
            filters.add(requireTypePredicate(Entity.class, Player.class));
        }

        addTypeFilters(filters);
        addDimensionFilters(position, filters);
        addRadiusFilters(position, filters);
        addLevelFilters(filters);
        addGamemodeFilters(filters);
        addNameFilters(filters);
        addRotationFilters(filters);
        addTeamFilters(filters);
        addScoreFilters(filters);

        // Pack the list before returning it to improve space efficiency
        filters.trimToSize();
        return Functional.predicateAnd(filters);
    }

    private boolean isPlayerOnlySelector() {
        SelectorType type = this.selector.getType();
        boolean untypedRandom = type == SelectorTypes.RANDOM && !this.selector.get(ArgumentTypes.ENTITY_TYPE).isPresent();
        return type == SelectorTypes.ALL_PLAYERS || type == SelectorTypes.NEAREST_PLAYER || untypedRandom;
    }

    private void addDimensionFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Vector3d boxDimensions = getPositionOrDefault(Vector3d.ZERO, ArgumentTypes.DIMENSION);
        Vector3d det1 = position;
        Vector3d det2 = position.add(boxDimensions);
        final Vector3d boxMin = det1.min(det2);
        final Vector3d boxMax = det1.max(det2);
        if (sel.has(ArgumentTypes.DIMENSION.x())) {
            filters.add(input -> {
                Vector3d pos = input.getLocation().getPosition();
                return pos.getX() >= boxMin.getX() && pos.getX() <= boxMax.getX();
            });
        }
        if (sel.has(ArgumentTypes.DIMENSION.y())) {
            filters.add(input -> {
                Vector3d pos = input.getLocation().getPosition();
                return pos.getY() >= boxMin.getY() && pos.getY() <= boxMax.getY();
            });
        }
        if (sel.has(ArgumentTypes.DIMENSION.z())) {
            filters.add(input -> {
                Vector3d pos = input.getLocation().getPosition();
                return pos.getZ() >= boxMin.getZ() && pos.getZ() <= boxMax.getZ();
            });
        }
    }

    private void addGamemodeFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<GameMode> gamemode = sel.get(ArgumentTypes.GAME_MODE);
        // If the gamemode is NOT_SET, that means accept any
        if (gamemode.isPresent() && gamemode.get() != GameModes.NOT_SET) {
            final GameMode actualMode = gamemode.get();
            filters.add(input -> {
                Optional<GameModeData> mode = input.get(GameModeData.class);
                return mode.isPresent() && mode.get() == actualMode;
            });
        }
    }

    private void addLevelFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Integer> levelMin = sel.get(ArgumentTypes.LEVEL.minimum());
        Optional<Integer> levelMax = sel.get(ArgumentTypes.LEVEL.maximum());
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
        Selector sel = this.selector;
        Optional<Argument.Invertible<String>> nameOpt = sel.getArgument(ArgumentTypes.NAME);
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
        final Selector sel = this.selector;
        Optional<Integer> radiusMin = sel.get(ArgumentTypes.RADIUS.minimum());
        Optional<Integer> radiusMax = sel.get(ArgumentTypes.RADIUS.maximum());
        if (radiusMin.isPresent()) {
            int radMin = radiusMin.get();
            final int radMinSquared = radMin * radMin;
            filters.add(input -> input.getLocation().getPosition().distanceSquared(position) >= radMinSquared);
        }
        if (radiusMax.isPresent()) {
            int radMax = radiusMax.get();
            final int radMaxSquared = radMax * radMax;
            filters.add(input -> input.getLocation().getPosition().distanceSquared(position) <= radMaxSquared);
        }
    }

    private void addRotationFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Double> rotMinX = sel.get(ArgumentTypes.ROTATION.minimum().x());
        if (rotMinX.isPresent()) {
            final double rmx = rotMinX.get();
            filters.add(input -> input.getRotation().getX() >= rmx);
        }
        Optional<Double> rotMinY = sel.get(ArgumentTypes.ROTATION.minimum().y());
        if (rotMinY.isPresent()) {
            final double rmy = rotMinY.get();
            filters.add(input -> input.getRotation().getY() >= rmy);
        }
        Optional<Double> rotMaxX = sel.get(ArgumentTypes.ROTATION.maximum().x());
        if (rotMaxX.isPresent()) {
            final double rx = rotMaxX.get();
            filters.add(input -> input.getRotation().getX() <= rx);
        }
        Optional<Double> rotMaxY = sel.get(ArgumentTypes.ROTATION.maximum().y());
        if (rotMaxY.isPresent()) {
            final double ry = rotMaxY.get();
            filters.add(input -> input.getRotation().getY() <= ry);
        }
    }

    private void addScoreFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        sel.getArguments();
    }

    private void addTeamFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Invertible<String>> teamOpt = sel.getArgument(ArgumentTypes.TEAM);
        if (teamOpt.isPresent()) {
            Invertible<String> teamArg = teamOpt.get();
            final boolean inverted = teamArg.isInverted();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean test(Entity input) {
                    Collection<Team> teams = Sponge.getGame().getServer().getServerScoreboard().get().getTeams();
                    if (input instanceof TeamMember) {
                        return inverted != collectMembers(teams).contains(((TeamMember) input).getTeamRepresentation());
                    }
                    return false;
                }

                private Collection<Text> collectMembers(Collection<Team> teams) {
                    ImmutableSet.Builder<Text> users = ImmutableSet.builder();
                    for (Team t : teams) {
                        users.addAll(t.getMembers());
                    }
                    return users.build();
                }

            });
        }
    }

    private void addTypeFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Argument.Invertible<EntityType>> typeOpt = sel.getArgument(ArgumentTypes.ENTITY_TYPE);
        if (typeOpt.isPresent()) {
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

    public List<Entity> resolve() {
        SelectorType selectorType = this.selector.getType();
        
        if (selectorType == SelectorTypes.SOURCE) {
            if (this.origin != null && this.origin instanceof Entity && this.selectorFilter.test((Entity) this.origin)) {
                return ImmutableList.of((Entity) this.origin);
            } else {
                return ImmutableList.of();
            }
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
                    .collect(ImmutableList.toImmutableList());
        }
        
        if (selectorType == SelectorTypes.RANDOM) {
            List<Entity> holder = entityStream.collect(Collectors.toList());
            if (holder.isEmpty()) return ImmutableList.of();
            
            Collections.shuffle(holder);
            return ImmutableList.copyOf(holder.subList(0, maxToSelect));
        }
        
        return entityStream.sorted(distanceSort(isReversed))
                .limit(maxToSelect)
                .collect(ImmutableList.toImmutableList());
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
        return ImmutableSet.copyOf(this.extents);
    }

}
