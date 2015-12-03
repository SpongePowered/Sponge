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
package org.spongepowered.common.data.processor.context.dual;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFakeNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.FakeNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFakeNameData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFakeNameData;
import org.spongepowered.common.data.processor.context.common.AbstractContextAwareDualProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.entity.context.SpongeEntityContext;
import org.spongepowered.common.entity.context.store.HumanoidContextStore;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;

import java.util.Optional;

public final class FakeNameDualProcessor extends AbstractContextAwareDualProcessor<Entity, SpongeEntityContext, Optional<String>,
        OptionalValue<String>, FakeNameData, ImmutableFakeNameData> {

    public FakeNameDualProcessor() {
        super(Entity.class, SpongeEntityContext.class, Keys.FAKE_NAME);
    }

    @Override
    protected Optional<Optional<String>> getVal(Entity container, ContextViewer viewer, SpongeEntityContext context) {
        return Optional.of(Optional.ofNullable(((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).getFakeName(viewer)));
    }

    @Override
    protected boolean set(Entity container, ContextViewer viewer, SpongeEntityContext context, Optional<String> value) {
        ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setFakeName(viewer, value.orElse(null));
        return true;
    }

    @Override
    protected OptionalValue<String> constructValue(Optional<String> actualValue) {
        return new SpongeOptionalValue<>(this.key, actualValue);
    }

    @Override
    protected boolean set(Entity container, Optional<String> value) {
        ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setFakeName(value.orElse(null));
        return false;
    }

    @Override
    protected Optional<Optional<String>> getVal(Entity container) {
        return Optional.of(Optional.ofNullable(((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).getFakeName()));
    }

    @Override
    protected ImmutableValue<Optional<String>> constructImmutableValue(Optional<String> value) {
        return new ImmutableSpongeOptionalValue<>(this.key, value);
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, DataContext context) {
        return contextual instanceof EntityPlayer || contextual instanceof EntityHuman;
    }

    @Override
    public Optional<FakeNameData> from(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (contextual instanceof IMixinEntityContext) {
            String name = ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).getFakeName(viewer);
            if (name != null) {
                return Optional.of(new SpongeFakeNameData(Optional.ofNullable(name)));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<FakeNameData> createFrom(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (context instanceof IMixinEntityContext) {
            String name = ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).getFakeName(viewer);
            if (name != null) {
                return this.from(contextual, viewer, context);
            } else {
                return Optional.of(new SpongeFakeNameData());
            }
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataContextual contextual, ContextViewer viewer, DataContext context, FakeNameData manipulator, MergeFunction function) {
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult remove(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (contextual instanceof IMixinEntityContext) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<FakeNameData> optional = this.from(contextual, viewer, context);
            if (optional.isPresent()) {
                try {
                    ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).setFakeName(viewer, null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the fake name on an entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final Optional<String> optional = this.getValueFromContainer(contextual, viewer, container).get();
        if (optional.isPresent()) {
            try {
                ((HumanoidContextStore) ((IMixinEntityContext) contextual).getContextStore()).setFakeName(viewer, null);
                return builder.replace(new ImmutableSpongeOptionalValue<>(this.key, optional)).result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().error("There was an issue resetting the display name on an entity!", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else {
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityPlayer || dataHolder instanceof EntityHuman;
    }

    @Override
    public boolean supports(EntityType type) {
        return type.getEntityClass().isAssignableFrom(EntityPlayer.class) || type.getEntityClass().isAssignableFrom(EntityHuman.class);
    }

    @Override
    public Optional<FakeNameData> from(DataHolder dataHolder) {
        if (dataHolder instanceof IMixinEntityContext) {
            String name = ((HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore()).getFakeName();
            return Optional.of(new SpongeFakeNameData(Optional.ofNullable(name)));
        }

        return Optional.empty();
    }

    @Override
    public Optional<FakeNameData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            String name = ((HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore()).getFakeName();
            if (name != null) {
                return this.from(dataHolder);
            } else {
                return Optional.of(new SpongeFakeNameData());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<FakeNameData> fill(DataHolder dataHolder, FakeNameData manipulator, MergeFunction overlap) {
        if (this.supports(dataHolder)) {
            final FakeNameData data = this.from(dataHolder).orElse(null);
            final FakeNameData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Optional<String> name = newData.fakeName().get();
            return Optional.of(manipulator.set(Keys.FAKE_NAME, name));
        }
        return Optional.empty();
    }

    @Override
    public Optional<FakeNameData> fill(DataContainer container, FakeNameData fakeNameData) {
        final String name = DataUtil.getData(container, Keys.FAKE_NAME, String.class);
        return Optional.of(fakeNameData.set(Keys.FAKE_NAME, Optional.ofNullable(name)));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, FakeNameData manipulator, MergeFunction function) {
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableFakeNameData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableFakeNameData immutable) {
        if (key == Keys.FAKE_NAME) {
            return Optional.of(new ImmutableSpongeFakeNameData((Optional<String>) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof IMixinEntityContext) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<FakeNameData> optional = this.from(dataHolder);
            if (optional.isPresent()) {
                try {
                    ((HumanoidContextStore) ((IMixinEntityContext) dataHolder).getContextStore()).setFakeName(null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the fake name on an entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final Optional<String> optional = this.getValueFromContainer(container).get();
        if (optional.isPresent()) {
            try {
                ((HumanoidContextStore) ((IMixinEntityContext) container).getContextStore()).setFakeName(null);
                return builder.replace(new ImmutableSpongeOptionalValue<>(this.key, optional)).result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().error("There was an issue resetting the display name on an entity!", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else {
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }

}
