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
package org.spongepowered.common.data.processor.multi.entity;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBodyPartRotationalData;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBodyPartRotationalData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.entity.item.EntityArmorStandAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.VecHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ArmorStandBodyPartRotationalDataProcessor
        extends AbstractEntityDataProcessor<EntityArmorStand, BodyPartRotationalData, ImmutableBodyPartRotationalData> {

    public ArmorStandBodyPartRotationalDataProcessor() {
        super(EntityArmorStand.class);
    }

    @Override
    protected BodyPartRotationalData createManipulator() {
        return new SpongeBodyPartRotationalData();
    }

    @Override
    public Optional<BodyPartRotationalData> fill(final DataContainer container, final BodyPartRotationalData data) {
        if (!container.contains(
                Keys.BODY_ROTATIONS.getQuery(),
                Keys.HEAD_ROTATION.getQuery(),
                Keys.CHEST_ROTATION.getQuery(),
                Keys.LEFT_ARM_ROTATION.getQuery(),
                Keys.RIGHT_ARM_ROTATION.getQuery(),
                Keys.LEFT_LEG_ROTATION.getQuery(),
                Keys.RIGHT_LEG_ROTATION.getQuery())) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final Map<BodyPart, Vector3d> bodyRotations = (Map<BodyPart, Vector3d>) container.getMap(Keys.BODY_ROTATIONS.getQuery()).get();
        final Vector3d headRotation = DataUtil.getPosition3d(container, Keys.HEAD_ROTATION.getQuery());
        final Vector3d chestRotation = DataUtil.getPosition3d(container, Keys.CHEST_ROTATION.getQuery());
        final Vector3d leftArmRotation = DataUtil.getPosition3d(container, Keys.LEFT_ARM_ROTATION.getQuery());
        final Vector3d rightArmRotation = DataUtil.getPosition3d(container, Keys.RIGHT_ARM_ROTATION.getQuery());
        final Vector3d leftLegRotation = DataUtil.getPosition3d(container, Keys.LEFT_LEG_ROTATION.getQuery());
        final Vector3d rightLegRotation = DataUtil.getPosition3d(container, Keys.RIGHT_LEG_ROTATION.getQuery());

        data.set(Keys.BODY_ROTATIONS, bodyRotations);
        data.set(Keys.HEAD_ROTATION, headRotation);
        data.set(Keys.CHEST_ROTATION, chestRotation);
        data.set(Keys.LEFT_ARM_ROTATION, leftArmRotation);
        data.set(Keys.RIGHT_ARM_ROTATION, rightArmRotation);
        data.set(Keys.LEFT_LEG_ROTATION, leftLegRotation);
        data.set(Keys.RIGHT_LEG_ROTATION, rightLegRotation);
        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean doesDataExist(final EntityArmorStand dataHolder) {
        return true;
    }

    @Override
    protected boolean set(final EntityArmorStand dataHolder, final Map<Key<?>, Object> keyValues) {
        @SuppressWarnings("unchecked") final Map<BodyPart, Vector3d> bodyRotations = (Map<BodyPart, Vector3d>) keyValues.get(Keys.BODY_ROTATIONS);
        final Vector3d headRotation =
                getValueFromTwoMapsOrUseFallback(keyValues, Keys.HEAD_ROTATION, bodyRotations, BodyParts.HEAD, Constants.Entity.ArmorStand.DEFAULT_HEAD_ROTATION);
        final Vector3d chestRotation = getValueFromTwoMapsOrUseFallback(keyValues, Keys.CHEST_ROTATION, bodyRotations, BodyParts.CHEST,
                Constants.Entity.ArmorStand.DEFAULT_CHEST_ROTATION);
        final Vector3d leftArmRotation = getValueFromTwoMapsOrUseFallback(keyValues, Keys.LEFT_ARM_ROTATION, bodyRotations, BodyParts.LEFT_ARM,
                Constants.Entity.ArmorStand.DEFAULT_LEFT_ARM_ROTATION);
        final Vector3d rightArmRotation = getValueFromTwoMapsOrUseFallback(keyValues, Keys.RIGHT_ARM_ROTATION, bodyRotations, BodyParts.RIGHT_ARM,
                Constants.Entity.ArmorStand.DEFAULT_RIGHT_ARM_ROTATION);
        final Vector3d leftLegRotation = getValueFromTwoMapsOrUseFallback(keyValues, Keys.LEFT_LEG_ROTATION, bodyRotations, BodyParts.LEFT_LEG,
                Constants.Entity.ArmorStand.DEFAULT_LEFT_LEG_ROTATION);
        final Vector3d rightLegRotation = getValueFromTwoMapsOrUseFallback(keyValues, Keys.RIGHT_LEG_ROTATION, bodyRotations, BodyParts.RIGHT_LEG,
                Constants.Entity.ArmorStand.DEFAULT_RIGHT_LEG_ROTATION);

        dataHolder.func_175415_a(VecHelper.toRotation(headRotation));
        dataHolder.func_175424_b(VecHelper.toRotation(chestRotation));
        dataHolder.func_175405_c(VecHelper.toRotation(leftArmRotation));
        dataHolder.func_175428_d(VecHelper.toRotation(rightArmRotation));
        dataHolder.func_175417_e(VecHelper.toRotation(leftLegRotation));
        dataHolder.func_175427_f(VecHelper.toRotation(rightLegRotation));
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final EntityArmorStand dataHolder) {
        final Map<Key<?>, Object> values = Maps.newHashMapWithExpectedSize(6);
        values.put(Keys.HEAD_ROTATION, VecHelper.toVector3d(dataHolder.func_175418_s()));
        values.put(Keys.CHEST_ROTATION, VecHelper.toVector3d(dataHolder.func_175408_t()));
        values.put(Keys.LEFT_ARM_ROTATION, VecHelper.toVector3d(((EntityArmorStandAccessor) dataHolder).accessor$getleftArmRotation()));
        values.put(Keys.RIGHT_ARM_ROTATION, VecHelper.toVector3d(((EntityArmorStandAccessor) dataHolder).accessor$getrightArmRotation()));
        values.put(Keys.LEFT_LEG_ROTATION, VecHelper.toVector3d(((EntityArmorStandAccessor) dataHolder).accessor$getleftLegRotation()));
        values.put(Keys.RIGHT_LEG_ROTATION, VecHelper.toVector3d(((EntityArmorStandAccessor) dataHolder).accessor$getrightLegRotation()));
        final Collection<BodyPart> bodyParts = Sponge.getRegistry().getAllOf(BodyPart.class);
        final Collection<Vector3d> rotations = Arrays.asList(values.values().toArray(new Vector3d[values.values().size()]));
        values.put(Keys.BODY_ROTATIONS, zipCollections(bodyParts, rotations));
        return values;
    }

    /**
     * Creates a map whose keys and values are both provided by collections that
     * are passed to this method.
     *
     * The collections that are passed must be of the same size, otherwise the
     * iterator on the smaller collection will throw a
     * {@link NoSuchElementException} since it would have finished iterating
     * over its elements (as per {@link Iterator#next()}'s contract), whereas,
     * the iterator for the larger collection would still be iterating.
     *
     * @param keys The keys to be used in the new map
     * @param values The values to be used in the new map
     * @return A new map containing keys and values created from the two
     *         collections
     */
    public static <K, V> Map<K, V> zipCollections(final Collection<K> keys, final Collection<V> values) {
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(Math.min(keys.size(), values.size()));
        final Iterator<K> keyIter = keys.iterator();
        final Iterator<V> valueIter = values.iterator();

        while (keyIter.hasNext() || valueIter.hasNext()) {
            map.put(keyIter.next(), valueIter.next());
        }
        return map;
    }

    /**
     * This method tries to retrieve a value from {@code firstMap}. If that
     * value is {@code null}, this method tries to retrieve a value from
     * {@code fallbackMap}. If that value is {@code null}, this method returns
     * {@code fallbackValue}.
     *
     * @return A value using the algorithm above
     */
    @SuppressWarnings("unchecked")
    private static <T> T getValueFromTwoMapsOrUseFallback(
        final Map<?, ?> firstMap, final Object keyForFirst, final Map<?, ?> fallbackMap, final Object keyForFallback,
            final T fallbackValue) {
        return Optional
                .ofNullable((T) firstMap.get(keyForFirst))
                .orElse(Optional
                        .ofNullable((T) fallbackMap.get(keyForFallback))
                        .orElse(fallbackValue));
    }
}
