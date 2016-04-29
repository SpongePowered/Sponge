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
package org.spongepowered.common.setting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.setting.Setting;
import org.spongepowered.api.setting.simple.SimpleSetting;
import org.spongepowered.api.setting.type.SettingType;
import org.spongepowered.api.setting.value.SettingValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.GuavaCollectors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javax.annotation.Nullable;

public class SpongeSettingBuilder<T> implements Setting.Builder<T> {

    @Nullable private String id;
    private Collection<String> aliases = Collections.emptySet();
    @Nullable private SettingType<T, SettingValue<T>> type;
    @Nullable private Text name;
    @Nullable private T defaultValue;

    @Override
    public Setting.Builder<T> id(String id) {
        checkNotNull(id, "id");
        id = id.toLowerCase(Locale.ENGLISH);
        checkArgument(Setting.ID_PATTERN.matcher(id).matches(), "id does not match setting id pattern");
        this.id = id;
        return this;
    }

    @Override
    public Setting.Builder<T> aliases(String... aliases) {
        checkNotNull(aliases, "aliases");
        return this.aliases(Arrays.asList(aliases));
    }

    @Override
    public Setting.Builder<T> aliases(Collection<String> aliases) {
        checkNotNull(aliases, "aliases");

        this.aliases = aliases.stream()
                .filter(alias -> {
                    checkNotNull(alias, "null alias");
                    return true;
                })
                .map(alias -> alias.toLowerCase(Locale.ENGLISH))
                .collect(GuavaCollectors.toImmutableSet());
        return this;
    }

    @Override
    public Setting.Builder<T> type(SettingType<T, SettingValue<T>> type) {
        this.type = checkNotNull(type, "type");
        return this;
    }

    @Override
    public Setting.Builder<T> name(Text name) {
        this.name = checkNotNull(name, "name");
        return this;
    }

    @Override
    public Setting.Builder<T> defaultValue(@Nullable T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Setting<T> build() {
        checkState(this.id != null, "id must be set");
        checkState(Setting.ID_PATTERN.matcher(this.id).matches(), "id does not match setting id pattern");
        checkState(this.type != null, "type must be set");
        checkState(this.name != null, "name must be set");

        return new SimpleSetting<>(this.id, this.aliases, this.type, this.name, this.defaultValue);
    }

    @Override
    public Setting.Builder<T> from(Setting<T> value) {
        this.id = value.getId();
        this.aliases = value.getAliases();
        this.type = value.getType();
        this.name = value.getName(null);
        this.defaultValue = value.getDefaultValue().orElse(null);
        return this;
    }

    @Override
    public Setting.Builder<T> reset() {
        this.id = null;
        this.aliases = Collections.emptySet();
        this.type = null;
        this.name = null;
        this.defaultValue = null;
        return this;
    }

}
