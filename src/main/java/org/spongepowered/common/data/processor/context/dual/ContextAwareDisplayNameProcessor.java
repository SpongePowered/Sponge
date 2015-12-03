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
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.processor.context.common.AbstractContextAwareDualProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.context.SpongeEntityContext;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public class ContextAwareDisplayNameProcessor extends
        AbstractContextAwareDualProcessor<Entity, SpongeEntityContext, Text, Value<Text>, DisplayNameData, ImmutableDisplayNameData> {

    public ContextAwareDisplayNameProcessor() {
        super(Entity.class, SpongeEntityContext.class, Keys.DISPLAY_NAME);
    }

    @Override
    protected Optional<Text> getVal(Entity container, ContextViewer viewer, SpongeEntityContext context) {
        return Optional.ofNullable(((IMixinEntityContext) container).getContextStore().getDisplayName(viewer));
    }

    @Override
    protected boolean set(Entity container, ContextViewer viewer, SpongeEntityContext context, Text value) {
        ((IMixinEntityContext) container).getContextStore().setDisplayName(viewer, value);
        return true;
    }

    @Override
    protected Value<Text> constructValue(Text actualValue) {
        return new SpongeValue<>(this.key, Text.of(), actualValue);
    }

    @Override
    protected boolean set(Entity container, Text value) {
        ((IMixinEntityContext) container).getContextStore().setDisplayName(value);

        return true;
    }

    @Override
    protected Optional<Text> getVal(Entity container) {
        return Optional.ofNullable(((IMixinEntityContext) container).getContextStore().getDisplayName());
    }

    @Override
    protected ImmutableValue<Text> constructImmutableValue(Text value) {
        return new ImmutableSpongeValue<>(this.key, Text.of(), value);
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, DataContext context) {
        return contextual instanceof Entity && context instanceof SpongeEntityContext;
    }

    @Override
    public Optional<DisplayNameData> from(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (contextual instanceof IMixinEntityContext) {
            Text name = ((IMixinEntityContext) contextual).getContextStore().getDisplayName();
            if (name != null) {
                return Optional.of(new SpongeDisplayNameData(name));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> createFrom(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (context instanceof IMixinEntityContext) {
            Text name = ((IMixinEntityContext) contextual).getContextStore().getDisplayName();
            if (name != null) {
                return this.from(contextual, viewer, context);
            } else {
                return Optional.of(new SpongeDisplayNameData());
            }
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataContextual contextual, ContextViewer viewer, DataContext context, DisplayNameData manipulator, MergeFunction function) {
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult remove(DataContextual contextual, ContextViewer viewer, DataContext context) {
        if (context instanceof SpongeEntityContext) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = this.from(contextual, viewer, context);
            if (optional.isPresent()) {
                try {
                    ((IMixinEntityContext) contextual).getContextStore().setDisplayName(viewer, null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the display name on an entity!", e);
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
        final Optional<Text> optional = this.getValueFromContainer(contextual, viewer, container);
        if (optional.isPresent()) {
            try {
                ((IMixinEntityContext) contextual).getContextStore().setDisplayName(viewer, null);
                return builder.replace(new ImmutableSpongeValue<>(this.key, optional.get())).result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().error("There was an issue resetting the display name on an entity!", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else {
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }

    @Override
    public boolean supports(DataHolder holder) {
        return holder instanceof Entity;
    }

    @Override
    public boolean supports(EntityType type) {
        return Entity.class.isAssignableFrom(type.getEntityClass());
    }

    @Override
    public Optional<DisplayNameData> from(DataHolder dataHolder) {
        if (dataHolder instanceof Entity && ((Entity) dataHolder).hasCustomName()) {
            final String displayName = ((Entity) dataHolder).getCustomNameTag();
            final DisplayNameData data = new SpongeDisplayNameData(SpongeTexts.fromLegacy(displayName));
            return Optional.of(data);
        }

        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                return this.from(dataHolder);
            } else {
                return Optional.of(new SpongeDisplayNameData());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(DataHolder holder, DisplayNameData manipulator, MergeFunction overlap) {
        if (this.supports(holder)) {
            final DisplayNameData data = this.from(holder).orElse(null);
            final DisplayNameData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Text display = newData.displayName().get();
            return Optional.of(manipulator.set(Keys.DISPLAY_NAME, display));
        }
        return Optional.empty();
    }

    @Override
    public Optional<DisplayNameData> fill(DataContainer container, DisplayNameData displayNameData) {
        final String json = DataUtil.getData(container, Keys.DISPLAY_NAME, String.class);
        final Text displayName = TextSerializers.JSON.deserialize(json);
        return Optional.of(displayNameData.set(Keys.DISPLAY_NAME, displayName));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, DisplayNameData manipulator, MergeFunction function) {
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableDisplayNameData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableDisplayNameData immutable) {
        if (key == Keys.DISPLAY_NAME) {
            return Optional.of(new ImmutableSpongeDisplayNameData((Text) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder holder) {
        if (holder instanceof Entity) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<DisplayNameData> optional = from(holder);
            if (optional.isPresent()) {
                try {
                    ((IMixinEntityContext) holder).getContextStore().setDisplayName(null);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue resetting the custom name on an entity!", e);
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
        final Optional<Text> optional = getValueFromContainer(container);
        if (optional.isPresent()) {
            try {
                ((IMixinEntityContext) container).getContextStore().setDisplayName(null);
                return builder.replace(new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, optional.get()))
                        .result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().error("There was an issue resetting the custom name on an entity!", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else {
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }
}
