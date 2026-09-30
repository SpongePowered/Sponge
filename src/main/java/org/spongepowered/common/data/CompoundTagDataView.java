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
package org.spongepowered.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * A {@link DataView} backed directly by a Minecraft {@link CompoundTag}.
 */
public class CompoundTagDataView extends SpongeDataView {

    private final CompoundTag compound;

    protected CompoundTagDataView(final CompoundTag compound, final DataView.SafetyMode safety) {
        super(safety);
        this.compound = Objects.requireNonNull(compound, "compound");
    }

    protected CompoundTagDataView(final CompoundTag compound, final DataView parent, final String key, final DataView.SafetyMode safety) {
        super(parent, key, safety);
        this.compound = Objects.requireNonNull(compound, "compound");
    }

    @Override
    public Stream<String> streamRootKeys() {
        return this.compound.keySet().stream().map(k -> k.replace(NBTTranslator.BOOLEAN_IDENTIFIER, ""));
    }

    @Override
    public Stream<Map.Entry<String, Object>> streamRootValues() {
        return this.compound.entrySet().stream().map(
            e -> Map.entry(e.getKey().replace(NBTTranslator.BOOLEAN_IDENTIFIER, ""), this.fromTagBase(e.getValue(), e.getKey())));
    }

    @Override
    public boolean contains(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> parts = path.parts();
        final String key = parts.getFirst();
        if (parts.size() == 1) {
            return this.compound.contains(key) || this.compound.contains(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        }
        return this.getView(key).map(view -> view.contains(path.popFirst())).orElse(false);
    }

    @Override
    public boolean contains(final DataQuery path, final DataQuery... paths) {
        Objects.requireNonNull(path, "DataQuery cannot be null!");
        Objects.requireNonNull(paths, "DataQuery varargs cannot be null!");
        if (!this.contains(path)) {
            return false;
        }
        for (final DataQuery query : paths) {
            if (!this.contains(Objects.requireNonNull(query, "No null queries!"))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Object> get(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> parts = path.parts();
        if (parts.isEmpty()) {
            return Optional.of(this);
        }

        final String key = parts.getFirst();
        if (parts.size() == 1) {
            return this.get(key);
        }
        return this.getView(key).flatMap(view -> view.get(path.popFirst()));
    }

    private Optional<Object> get(final String key) {
        final @Nullable Tag tag = this.compound.get(key);
        if (tag != null) {
            return Optional.of(this.fromTagBase(tag, key));
        }
        final @Nullable Tag booleanTag = this.compound.get(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        if (booleanTag != null) {
            return Optional.of(this.fromTagBase(booleanTag, key + NBTTranslator.BOOLEAN_IDENTIFIER));
        }
        return Optional.empty();
    }

    @Override
    public DataView set(final DataQuery path, final Object value) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(value, "value");
        Preconditions.checkState(!path.parts().isEmpty(), "The path is empty");
        Preconditions.checkArgument(value != this, "Cannot set a DataView to itself.");

        if (path.parts().size() == 1) {
            return this.set(path.parts().getFirst(), value);
        }
        this.getOrCreateView(path.parts().getFirst()).set(path.popFirst(), value);
        return this;
    }

    @Override
    public DataView set(final DataQuery path, final String value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putString(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final boolean value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst());
            this.compound.putBoolean(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER, value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final byte value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putByte(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final short value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putShort(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final int value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putInt(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final long value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putLong(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final float value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putFloat(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final DataQuery path, final double value) {
        if (path.parts().size() == 1) {
            this.compound.remove(path.parts().getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putDouble(path.parts().getFirst(), value);
            return this;
        }
        return this.set(path, (Object) value);
    }

    @Override
    public DataView set(final String key, final Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Preconditions.checkState(!key.isEmpty(), "The key is empty");
        Preconditions.checkArgument(value != this, "Cannot set a DataView to itself.");

        if (value instanceof final String string) {
            this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putString(key, string);
            return this;
        } else if (value instanceof final Integer integer) {
            this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.putInt(key, integer);
            return this;
        } else if (value instanceof final Boolean bool) {
            this.compound.remove(key);
            this.compound.putBoolean(key + NBTTranslator.BOOLEAN_IDENTIFIER, bool);
            return this;
        }

        DataSerializer.serialize(this.safetyMode(), value, () -> this.createView(key), v -> {
            this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
            this.compound.put(key, NBTTranslator.getBaseFromObject(v));
        });

        return this;
    }

    @Override
    public DataView set(final String key, final String value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putString(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final boolean value) {
        this.compound.remove(key);
        this.compound.putBoolean(key + NBTTranslator.BOOLEAN_IDENTIFIER, value);
        return this;
    }

    @Override
    public DataView set(final String key, final byte value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putByte(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final short value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putShort(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final int value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putInt(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final long value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putLong(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final float value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putFloat(key, value);
        return this;
    }

    @Override
    public DataView set(final String key, final double value) {
        this.compound.remove(key + NBTTranslator.BOOLEAN_IDENTIFIER);
        this.compound.putDouble(key, value);
        return this;
    }

    @Override
    public DataView remove(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> parts = path.parts();
        if (parts.size() == 1) {
            this.compound.remove(parts.getFirst());
            this.compound.remove(parts.getFirst() + NBTTranslator.BOOLEAN_IDENTIFIER);
        } else {
            this.getView(parts.getFirst()).ifPresent(view -> view.remove(path.popFirst()));
        }
        return this;
    }

    @Override
    public DataView createView(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        Preconditions.checkArgument(!path.parts().isEmpty(), "The size of the query must be at least 1");
        if (path.parts().size() == 1) {
            return this.createView(path.parts().getFirst());
        }
        return this.getOrCreateView(path.parts().getFirst()).createView(path.popFirst());
    }

    @Override
    public DataView createView(final String key) {
        Objects.requireNonNull(key, "key");
        Preconditions.checkArgument(!key.isEmpty(), "Key must have at least one part");
        final CompoundTag child = new CompoundTag();
        this.compound.put(key, child);
        return new CompoundTagDataView(child, this, key, this.safetyMode());
    }

    @Override
    public Optional<DataView> getView(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        Optional<DataView> view = Optional.of(this);
        for (final String part : path.parts()) {
            view = view.get().getView(part);
            if (view.isEmpty()) {
                break;
            }
        }
        return view;
    }

    @Override
    public Optional<DataView> getView(final String key) {
        final @Nullable Tag tag = this.compound.get(key);
        if (tag instanceof final CompoundTag compoundTag) {
            return Optional.of(new CompoundTagDataView(compoundTag, this, key, this.safetyMode()));
        }
        return Optional.empty();
    }

    private DataView getOrCreateView(final String key) {
        return this.getView(key).orElseGet(() -> this.createView(key));
    }

    private Object fromTagBase(final Tag base, final String key) {
        return NBTTranslator.fromTagBase(base, key, (k, c) -> {
            if (k != null) {
                return new CompoundTagDataView(c, this, k, this.safetyMode());
            }
            return new CompoundTagDataContainer(c, this.safetyMode());
        });
    }

    @Override
    public DataContainer copy() {
        return this.copy(this.safetyMode());
    }

    @Override
    public DataContainer copy(final DataView.SafetyMode safety) {
        return new CompoundTagDataContainer(this.compound.copy(), this.safetyMode());
    }

    @Override
    public boolean isEmpty() {
        return this.compound.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.compound, this.currentPath());
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return super.equals(obj);
        }
        final CompoundTagDataView other = (CompoundTagDataView) obj;
        return Objects.equals(this.compound, other.compound)
                && Objects.equals(this.currentPath(), other.currentPath());
    }

    @Override
    public String toString() {
        final StringJoiner helper = new StringJoiner(", ", CompoundTagDataView.class.getSimpleName() + "[", "]");
        if (!this.currentPath().toString().isEmpty()) {
            helper.add("path=" + this.currentPath());
        }
        helper.add("safety=" + this.safetyMode().name());
        return helper.add("compound=" + this.compound).toString();
    }
}
