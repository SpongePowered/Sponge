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
package org.spongepowered.common.scoreboard.builder;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.scores.PlayerTeam;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.common.util.Preconditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


public final class SpongeTeamBuilder implements Team.Builder {

    private @Nullable String name;
    private @Nullable Component displayName;
    private NamedTextColor color;
    private Component prefix;
    private Component suffix;
    private boolean allowFriendlyFire;
    private boolean showFriendlyInvisibles;
    private Supplier<? extends Visibility> nameTagVisibility;
    private Supplier<? extends Visibility> deathMessageVisibility;
    private Supplier<? extends CollisionRule> collisionRule;
    private Set<Component> members;

    public SpongeTeamBuilder() {
        this.reset();
    }

    @Override
    public Team.Builder name(final String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null!");
        if (this.displayName == null) {
            this.displayName = Component.text(this.name);
        }
        return this;
    }

    @Override
    public Team.Builder color(final NamedTextColor color) {
        Objects.requireNonNull(color, "Color cannot be null!");
        this.color = color;
        return this;
    }

    @Override
    public Team.Builder displayName(final Component displayName) throws IllegalArgumentException {
        this.displayName = Objects.requireNonNull(displayName, "DisplayName cannot be null!");
        return this;
    }

    @Override
    public Team.Builder prefix(final Component prefix) {
        this.prefix = Objects.requireNonNull(prefix, "Prefix cannot be null!");
        return this;
    }

    @Override
    public Team.Builder suffix(final Component suffix) {
        this.suffix = Objects.requireNonNull(suffix, "Suffix cannot be null!");
        return this;
    }

    @Override
    public Team.Builder allowFriendlyFire(final boolean enabled) {
        this.allowFriendlyFire = enabled;
        return this;
    }

    @Override
    public Team.Builder canSeeFriendlyInvisibles(final boolean enabled) {
        this.showFriendlyInvisibles = enabled;
        return this;
    }

    @Override
    public Team.Builder nameTagVisibility(final Visibility visibility) {
        Objects.requireNonNull(visibility, "Visibility cannot be null!");
        this.nameTagVisibility = () -> visibility;
        return this;
    }

    @Override
    public Team.Builder deathTextVisibility(final Visibility visibility) {
        Objects.requireNonNull(visibility, "Visibility cannot be null!");
        this.deathMessageVisibility = () -> visibility;
        return this;
    }

    @Override
    public Team.Builder collisionRule(final CollisionRule rule) {
        Objects.requireNonNull(rule, "Collision rule cannot be null!");
        this.collisionRule = () -> rule;
        return this;
    }

    @Override
    public Team.Builder members(final Set<Component> members) {
        this.members = new HashSet<>(Objects.requireNonNull(members, "Members cannot be null!"));
        return this;
    }

    @Override
    public Team.Builder from(final Team value) {
        this.name(value.name())
            .displayName(value.displayName())
            .prefix(value.prefix())
            .color(value.color())
            .allowFriendlyFire(value.allowFriendlyFire())
            .canSeeFriendlyInvisibles(value.canSeeFriendlyInvisibles())
            .suffix(value.suffix())
            .nameTagVisibility(value.nameTagVisibility())
            .deathTextVisibility(value.deathMessageVisibility())
            .collisionRule(value.collisionRule())
            .members(value.members());
        return this;
    }

    @Override
    public SpongeTeamBuilder reset() {
        this.name = null;
        this.displayName = null;
        this.color = NamedTextColor.WHITE;
        this.prefix = Component.empty();
        this.suffix = Component.empty();
        this.allowFriendlyFire = false;
        this.showFriendlyInvisibles = false;
        this.nameTagVisibility = Visibilities.ALWAYS;
        this.deathMessageVisibility = Visibilities.ALWAYS;
        this.collisionRule = CollisionRules.ALWAYS;
        this.members = new HashSet<>();
        return this;
    }

    @Override
    public Team build() throws IllegalStateException {
        Preconditions.checkState(this.name != null, "Name cannot be null!");
        Preconditions.checkState(this.displayName != null, "DisplayName cannot be null!");

        final Team team = (Team) new PlayerTeam(null, this.name);
        team.setDisplayName(this.displayName);
        team.setColor(this.color);
        team.setPrefix(this.prefix);
        team.setSuffix(this.suffix);
        team.setAllowFriendlyFire(this.allowFriendlyFire);
        team.setCanSeeFriendlyInvisibles(this.showFriendlyInvisibles);
        team.setNameTagVisibility(this.nameTagVisibility.get());
        team.setDeathMessageVisibility(this.deathMessageVisibility.get());
        team.setCollisionRule(this.collisionRule.get());
        for (final Component member : this.members) {
            team.addMember(member);
        }

        return team;
    }
}
