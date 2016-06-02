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
package org.spongepowered.common.data.builder.manipulator.immutable.block;

import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;

import java.util.Optional;

public class ImmutableSpongeTreeDataBuilder extends AbstractDataBuilder<ImmutableTreeData> implements ImmutableDataManipulatorBuilder<ImmutableTreeData, TreeData> {

    public ImmutableSpongeTreeDataBuilder() {
        super(ImmutableTreeData.class, 1);
    }

    @Override
    public ImmutableTreeData createImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, TreeTypes.OAK);
    }

    @Override
    public Optional<ImmutableTreeData> createFrom(DataHolder dataHolder) {
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableTreeData> createFrom(ImmutableDataHolder<?> dataHolder) {
        return Optional.empty();
    }

    @Override
    public ImmutableDataManipulatorBuilder<ImmutableTreeData, TreeData> reset() {
        return this;
    }

    @Override
    protected Optional<ImmutableTreeData> buildContent(DataView container) throws InvalidDataException {
        checkDataExists(container, Keys.TREE_TYPE.getQuery());
        final String treeTypeId = container.getString(Keys.TREE_TYPE.getQuery()).get();
        final TreeType treeType = SpongeImpl.getRegistry().getType(TreeType.class, treeTypeId).get();
        return Optional.of(ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, treeType));
    }
}
