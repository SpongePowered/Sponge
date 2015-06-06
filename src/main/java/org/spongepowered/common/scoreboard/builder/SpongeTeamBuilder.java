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

import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.TeamBuilder;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.scoreboard.SpongeTeam;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class SpongeTeamBuilder implements TeamBuilder {

    private String name;
    private Text displayName;
    private TextColor color = TextColors.RESET;
    private Text prefix = Texts.of();
    private Text suffix = Texts.of();
    private boolean allowFriendlyFire = false;
    private boolean showFriendlyInvisibles = false;
    private Visibility nameTagVisibility = Visibilities.ALL;
    private Visibility deathMessageVisibility = Visibilities.ALL;
    private Set<User> users = new HashSet<User>();

    @Override
    public TeamBuilder name(String name) {
        this.name = checkNotNull(name, "Name cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder color(TextColor color) throws IllegalArgumentException {
        checkNotNull(color, "Color cannot be null!");
        if (color == TextColors.RESET) {
            throw new IllegalArgumentException("Color cannot be TextColors.RESET!");
        }
        this.color = color;
        return this;
    }

    @Override
    public TeamBuilder displayName(Text displayName) throws IllegalArgumentException {
        this.displayName = checkNotNull(displayName, "DisplayName cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder prefix(Text prefix) {
        this.prefix = checkNotNull(prefix, "Prefix cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder suffix(Text suffix) {
        this.suffix = checkNotNull(suffix, "Suffix cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder allowFriendlyFire(boolean enabled) {
        this.allowFriendlyFire = enabled;
        return this;
    }

    @Override
    public TeamBuilder canSeeFriendlyInvisibles(boolean enabled) {
        this.showFriendlyInvisibles = enabled;
        return this;
    }

    @Override
    public TeamBuilder nameTagVisibility(Visibility visibility) {
        this.nameTagVisibility = checkNotNull(visibility, "Visibility cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder deathTextVisibility(Visibility visibility) {
        this.deathMessageVisibility = checkNotNull(visibility, "Visibility cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder users(Set<User> users) {
        this.users = checkNotNull(users, "Users cannot be null!");
        return this;
    }

    @Override
    public TeamBuilder reset() {
        return null;
    }

    @Override
    public Team build() throws IllegalStateException {
        checkState(this.name != null, "Name cannot be null!");
        checkState(this.displayName != null, "DisplayName cannot be null!");

        return new SpongeTeam(name, displayName, color, prefix, suffix, allowFriendlyFire, showFriendlyInvisibles, nameTagVisibility, deathMessageVisibility, users);
    }
}
