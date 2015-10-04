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
import static org.spongepowered.common.util.OptionalUtils.asSet;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.Argument.Invertible;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.Sponge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A resolver that acts like Vanilla Minecraft in many regards.
 */
// TODO decide if we want selector resolvers as part of the API, ask @kenzierocks for details
public class SelectorResolver {

    private static final Function<CommandSource, String> GET_NAME =
        new Function<CommandSource, String>() {

            @Override
            public String apply(CommandSource input) {
                return input.getName();
            }

        };
    private static final Vector3d ORIGIN = new Vector3d(0, 0, 0);
    private static final Set<ArgumentType<?>> LOCATION_BASED_ARGUMENTS;
    private static final Function<Number, Double> TO_DOUBLE = new Function<Number, Double>() {

        @Override
        public Double apply(Number input) {
            return input.doubleValue();
        }

    };
    private static final Collection<SelectorType> INFINITE_TYPES = ImmutableSet.of(SelectorTypes.ALL_ENTITIES, SelectorTypes.ALL_PLAYERS);

    static {
        ImmutableSet.Builder<ArgumentType<?>> builder = ImmutableSet.builder();
        builder.addAll(ArgumentTypes.POSITION.getTypes());
        builder.addAll(ArgumentTypes.DIMENSION.getTypes());
        builder.addAll(ArgumentTypes.RADIUS.getTypes());
        // Left commented because Vanilla doesn't include it (see field_179666_d)
        // builder.addAll(ArgumentTypes.ROTATION.getTypes());
        LOCATION_BASED_ARGUMENTS = builder.build();
    }

    private static Extent extentFromSource(CommandSource origin) {
        if (origin instanceof LocatedSource) {
            return ((LocatedSource) origin).getWorld();
        }
        return null;
    }

    private static Vector3d positionFromSource(CommandSource origin) {
        if (origin instanceof LocatedSource) {
            return ((LocatedSource) origin).getLocation().getPosition();
        }
        return null;
    }

    private static <I, R> Predicate<I> requireTypePredicate(Class<I> inputType, final Class<R> requiredType) {
        return new Predicate<I>() {

            @Override
            public boolean apply(I input) {
                return requiredType.isInstance(input);
            }

        };
    }

    private final Collection<Extent> extents;
    private final Vector3d position;
    private final Optional<CommandSource> original;
    private final Selector selector;
    private final Predicate<Entity> selectorFilter;
    private final boolean alwaysUsePosition;

    public SelectorResolver(Collection<? extends Extent> extents, Selector selector, boolean force) {
        this(extents, null, null, selector, force);
    }

    public SelectorResolver(Location<World> location, Selector selector, boolean force) {
        this(ImmutableSet.of(location.getExtent()), location.getPosition(), null, selector, force);
    }

    public SelectorResolver(CommandSource origin, Selector selector, boolean force) {
        this(asSet(Optional.ofNullable(extentFromSource(origin))), positionFromSource(origin), origin, selector, force);
    }

    private SelectorResolver(Collection<? extends Extent> extents, Vector3d position,
        CommandSource original, Selector selector, boolean force) {
        this.extents = ImmutableSet.copyOf(extents);
        this.position = position == null ? ORIGIN : position;
        this.original = Optional.ofNullable(original);
        this.selector = checkNotNull(selector);
        this.selectorFilter = makeFilter();
        this.alwaysUsePosition = force;
    }

    private Predicate<Entity> makeFilter() {
        // for easier reading
        final Selector sel = this.selector;
        Vector3d position = getPositionOrDefault(this.position, ArgumentTypes.POSITION);
        List<Predicate<Entity>> filters = Lists.newArrayList();
        addTypeFilters(filters);
        addDimensionFilters(position, filters);
        addRadiusFilters(position, filters);
        addLevelFilters(filters);
        addGamemodeFilters(filters);
        addNameFilters(filters);
        addRotationFilters(filters);
        addTeamFilters(filters);
        addScoreFilters(filters);
        SelectorType selectorType = sel.getType();
        Optional<Invertible<EntityType>> type = sel.getArgument(ArgumentTypes.ENTITY_TYPE);
        // isn't an ALL_ENTITIES selector or it is a RANDOM selector for only players
        boolean isPlayerOnlySelector =
            selectorType == SelectorTypes.ALL_PLAYERS || selectorType == SelectorTypes.NEAREST_PLAYER
                || (selectorType == SelectorTypes.RANDOM && type.isPresent() && !type.get().isInverted()
                && type.get().getValue() != EntityTypes.PLAYER);
        if (isPlayerOnlySelector) {
            // insert at the start so it applies first
            filters.add(0, requireTypePredicate(Entity.class, Player.class));
        }
        return Predicates.and(filters);
    }

    private void addDimensionFilters(final Vector3d position, List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Vector3d boxDimensions = getPositionOrDefault(ORIGIN, ArgumentTypes.DIMENSION);
        Vector3d det1 = position;
        Vector3d det2 = position.add(boxDimensions);
        final Vector3d boxMin = det1.min(det2);
        final Vector3d boxMax = det1.max(det2);
        if (sel.has(ArgumentTypes.DIMENSION.x())) {
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Vector3d pos = input.getLocation().getPosition();
                    return pos.getX() >= boxMin.getX() && pos.getX() <= boxMax.getX();
                }

            });
        }
        if (sel.has(ArgumentTypes.DIMENSION.y())) {
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Vector3d pos = input.getLocation().getPosition();
                    return pos.getY() >= boxMin.getY() && pos.getY() <= boxMax.getY();
                }

            });
        }
        if (sel.has(ArgumentTypes.DIMENSION.z())) {
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Vector3d pos = input.getLocation().getPosition();
                    return pos.getZ() >= boxMin.getZ() && pos.getZ() <= boxMax.getZ();
                }

            });
        }
    }

    private void addGamemodeFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<GameMode> gamemode = sel.get(ArgumentTypes.GAME_MODE);
        // If the gamemode is NOT_SET, that means accept any
        if (gamemode.isPresent() && gamemode.get() != GameModes.NOT_SET) {
            final GameMode actualMode = gamemode.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Optional<GameModeData> mode = input.get(GameModeData.class);
                    return mode.isPresent() && mode.get() == actualMode;
                }

            });
        }
    }

    private void addLevelFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Integer> levelMin = sel.get(ArgumentTypes.LEVEL.minimum());
        Optional<Integer> levelMax = sel.get(ArgumentTypes.LEVEL.maximum());
        if (levelMin.isPresent()) {
            final int actualMin = levelMin.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Optional<ExperienceHolderData> xp = input.get(ExperienceHolderData.class);
                    return xp.isPresent() && xp.get().level().get() >= actualMin;
                }

            });
        }
        if (levelMax.isPresent()) {
            final int actualMax = levelMax.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Optional<ExperienceHolderData> xp = input.get(ExperienceHolderData.class);
                    return xp.isPresent() && xp.get().level().get() <= actualMax;
                }

            });
        }
    }

    private void addNameFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Argument.Invertible<String>> nameOpt = sel.getArgument(ArgumentTypes.NAME);
        if (nameOpt.isPresent()) {
            final String name = nameOpt.get().getValue();
            final boolean inverted = nameOpt.get().isInverted();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    Optional<DisplayNameData> dispName = input.get(DisplayNameData.class);
                    return inverted ^ (dispName.isPresent() && name.equals(Texts.toPlain(dispName.get().displayName().get())));
                }

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
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getLocation().getPosition().distanceSquared(position) >= radMinSquared;
                }

            });
        }
        if (radiusMax.isPresent()) {
            int radMax = radiusMax.get();
            final int radMaxSquared = radMax * radMax;
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getLocation().getPosition().distanceSquared(position) <= radMaxSquared;
                }

            });
        }
    }

    private void addRotationFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        // If the Z's are uncommented, don't forget to implement them
        // Optional<Double> rotMinZ = sel.get(ArgumentTypes.ROTATION.minimum().z());
        // Optional<Double> rotMaxZ = sel.get(ArgumentTypes.ROTATION.maximum().z());
        Optional<Double> rotMinX = sel.get(ArgumentTypes.ROTATION.minimum().x());
        if (rotMinX.isPresent()) {
            final double rmx = rotMinX.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getRotation().getX() >= rmx;
                }

            });
        }
        Optional<Double> rotMinY = sel.get(ArgumentTypes.ROTATION.minimum().y());
        if (rotMinY.isPresent()) {
            final double rmy = rotMinY.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getRotation().getY() >= rmy;
                }

            });
        }
        Optional<Double> rotMaxX = sel.get(ArgumentTypes.ROTATION.maximum().x());
        if (rotMaxX.isPresent()) {
            final double rx = rotMaxX.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getRotation().getX() <= rx;
                }

            });
        }
        Optional<Double> rotMaxY = sel.get(ArgumentTypes.ROTATION.maximum().y());
        if (rotMaxY.isPresent()) {
            final double ry = rotMaxY.get();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return input.getRotation().getY() <= ry;
                }

            });
        }
    }

    private void addScoreFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        sel.getArguments();
    }

    private void addTeamFilters(List<Predicate<Entity>> filters) {
        Selector sel = this.selector;
        Optional<Invertible<String>> teamOpt = sel.getArgument(ArgumentTypes.TEAM);
        Collection<World> worlds = Lists.newArrayList();
        for (Extent e : this.extents) {
            if (e instanceof World) {
                worlds.add((World) e);
            } else if (e instanceof Chunk) {
                worlds.add(((Chunk) e).getWorld());
            }
        }
        if (teamOpt.isPresent() && !worlds.isEmpty()) {
            Invertible<String> teamArg = teamOpt.get();
            final boolean inverted = teamArg.isInverted();
            ImmutableSet.Builder<Team> teamBuilder = ImmutableSet.builder();
            for (World w : worlds) {
                teamBuilder.addAll(asSet(w.getScoreboard().getTeam(teamArg.getValue())));
            }
            final Collection<Team> teams = teamBuilder.build();
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    if (input instanceof TeamMember) {
                        return inverted ^ collectMembers(teams).contains(((TeamMember) input).getTeamRepresentation());
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
            filters.add(new Predicate<Entity>() {

                @Override
                public boolean apply(Entity input) {
                    return inverted ^ input.getType() == type;
                }

            });
        }
    }

    private Vector3d getPositionOrDefault(Vector3d pos, ArgumentHolder.Vector3<?, ? extends Number> vecTypes) {
        Optional<Double> x = this.selector.get(vecTypes.x()).map(TO_DOUBLE);
        Optional<Double> y = this.selector.get(vecTypes.y()).map(TO_DOUBLE);
        Optional<Double> z = this.selector.get(vecTypes.z()).map(TO_DOUBLE);
        return new Vector3d(x.orElse(Double.valueOf(pos.getX())), y.orElse(Double.valueOf(pos.getY())), z.orElse(Double.valueOf(pos.getZ())));
    }

    public String getName() {
        return this.original.map(GET_NAME).orElse("SelectorResolver");
    }

    public Set<Entity> resolve() {
        SelectorType selectorType = this.selector.getType();
        int defaultCount = 1;
        if (INFINITE_TYPES.contains(selectorType)) {
            defaultCount = 0;
        }
        int maxToSelect = this.selector.get(ArgumentTypes.COUNT).orElse(defaultCount);
        Set<? extends Extent> extents = getExtentSet();
        int count = 0;
        ImmutableSet.Builder<Entity> entities = ImmutableSet.builder();
        for (Extent extent : extents) {
            Collection<Entity> allEntities = extent.getEntities();
            if (selectorType == SelectorTypes.RANDOM) {
                List<Entity> entityList = new ArrayList<Entity>(allEntities);
                Collections.shuffle(entityList);
                allEntities = entityList;
            }

            for (Entity e : allEntities) {
                if (!this.selectorFilter.apply(e)) {
                    continue;
                }
                entities.add(e);
                count++;
                if (maxToSelect != 0 && count > maxToSelect) {
                    break;
                }
            }
        }
        return entities.build();
    }

    private Set<? extends Extent> getExtentSet() {
        if (!this.alwaysUsePosition && Collections.disjoint(getArgumentTypes(this.selector.getArguments()), LOCATION_BASED_ARGUMENTS)) {
            return ImmutableSet.copyOf(Sponge.getGame().getServer().getWorlds());
        }
        return ImmutableSet.copyOf(this.extents);
    }

    private Collection<ArgumentType<?>> getArgumentTypes(Collection<Argument<?>> arguments) {
        Collection<ArgumentType<?>> types = Sets.newHashSet();
        for (Argument<?> argument : arguments) {
            types.add(argument.getType());
        }
        return types;
    }

}
