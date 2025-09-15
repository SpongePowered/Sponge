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
package org.spongepowered.common.util.transformation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.common.world.volume.buffer.archetype.AbstractReferentArchetypeVolume;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class VolumeTransformationTest {

    private static final Vector3i INVALID_STUB_POSITION = Vector3i.from(
        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private static final DataQuery POS = DataQuery.of("pos");

    private static Stream<Arguments> testTransformationsOfPositions() {
        return Stream.of(
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.ZERO,
                Vector3i.from(1, 1, 1),
                0,
                Rotations.NONE.get()
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.UNIT_X,
                Vector3i.from(1, 1, 1),
                1,
                Rotations.CLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.from(-1, 1, 4),
                Vector3i.from(1, 1, 1),
                0,
                Rotations.NONE.get()
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.from(-1, 1, 4),
                Vector3i.from(1, 1, 1),
                1,
                Rotations.CLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                1,
                Rotations.CLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                2,
                Rotations.COUNTERCLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                4,
                Rotations.CLOCKWISE_180.get()
            ),
            Arguments.of(
                Vector3i.from(-4, -1, -5),
                Vector3i.from(10, 7, 9),
                Vector3i.ZERO,
                Vector3i.from(1, -3, -1),
                8,
                Rotations.CLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.from(-4, -1, -5),
                Vector3i.from(10, 7, 9),
                Vector3i.from(-8, 3, -7),
                Vector3i.from(1, -3, -1),
                8,
                Rotations.CLOCKWISE_90.get()
            ),
            Arguments.of(
                Vector3i.from(-11, -4, -10),
                Vector3i.from(21, 11, 17),
                Vector3i.from(-6, 2, -4),
                Vector3i.from(1, -3, -1),
                8,
                Rotations.CLOCKWISE_90.get()
            )
        );
    }

    private static SpongeArchetypeVolume fillVolume(final Vector3i min, final Vector3i max, final Vector3i origin) {
        final Vector3i rawMin = min.min(max);
        final Vector3i rawMax = max.max(min);
        final Vector3i size = rawMax.sub(rawMin).add(Vector3i.ONE);
        final Vector3i relativeMin = rawMin.sub(origin);
        final RegistryHolder holder = Sponge.server();
        final SpongeArchetypeVolume volume = new SpongeArchetypeVolume(relativeMin, size, holder);

        final Vector3i volMax = volume.max().add(Vector3i.ONE);
        for (int x = relativeMin.x(); x < volMax.x(); x++) {
            for (int z = relativeMin.z(); z < volMax.z(); z++) {
                for (int y = relativeMin.y(); y < volMax.y(); y++) {
                    volume.addBlockEntity(x, y, z, BlockEntityArchetype.builder()
                        .state(BlockTypes.CHEST.get().defaultState())
                        .blockEntity(BlockEntityTypes.CHEST.get())
                        .blockEntityData(DataContainer.createNew().set(VolumeTransformationTest.POS, new Vector3i(x, y, z)))
                        .build());
                }
            }
        }
        return volume;
    }

    @MethodSource
    @ParameterizedTest
    void testTransformationsOfPositions(
        final Vector3i min, final Vector3i max, final Vector3i origin, final Vector3i testForRoundTrip,
        final int rotationCount, final Rotation wanted
    ) {
        final SpongeArchetypeVolume volume = VolumeTransformationTest.fillVolume(min, max, origin);
        final Vector3i size = volume.size();
        final Vector3i relativeMin = volume.min();

        final Vector3d center = volume.logicalCenter();

        ArchetypeVolume intermediary = volume;
        for (int i = 0; i < rotationCount; i++) {
            intermediary = intermediary.transform(Transformation.builder()
                .origin(center)
                .rotate(wanted)
                .build());
        }
        Rotation expected = Rotations.NONE.get();
        for (int i = 0; i < rotationCount; i++) {
            expected = expected.and(wanted);
        }
        final Transformation expectedTransform = Transformation.builder()
            .origin(center)
            .rotate(expected)
            .build();
        final Transformation inverse = expectedTransform.inverse();
        final ArchetypeVolume rotated = intermediary;
        if (rotationCount > 0) {
            final Vector3d preliminaryTransformed = expectedTransform.transformPosition(testForRoundTrip.toDouble());
            Vector3i unTransformed = preliminaryTransformed.round().toInt();
            for (int i = 0; i < rotationCount; i++) {
                unTransformed = ((AbstractReferentArchetypeVolume) rotated).inverseTransform(
                    unTransformed.x(), unTransformed.y(), unTransformed.z());
            }
            Assertions.assertEquals(testForRoundTrip, unTransformed);
        }
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                for (int z = 0; z < size.z(); z++) {
                    final int relativeX = x + relativeMin.x();
                    final int relativeY = y + relativeMin.y();
                    final int relativeZ = z + relativeMin.z();
                    final Vector3d rawRelativePosition = new Vector3d(relativeX, relativeY, relativeZ);
                    final Optional<BlockEntityArchetype> untransformedEntity = volume.blockEntityArchetype(relativeX, relativeY, relativeZ);
                    final Vector3i transformedPosition = expectedTransform.transformPosition(
                        rawRelativePosition).toInt();
                    final Optional<BlockEntityArchetype> transformedEntity = rotated.blockEntityArchetype(
                        transformedPosition.x(), transformedPosition.y(), transformedPosition.z());
                    Assertions.assertEquals(untransformedEntity, transformedEntity, () -> String.format(
                        "Block entity check failed!\nOriginal(%d, %d, %d): %s\nTransformed(%d, %d, %d): %s\n",
                        relativeX, relativeY, relativeZ, untransformedEntity,
                        transformedPosition.x(), transformedPosition.y(),
                        transformedPosition.z(), transformedEntity
                    ));
                }
            }
        }
        if (rotationCount < 0) {
            return;
        }
        // At this point, we should have an abstract referent volume at least

        rotated.blockEntityArchetypeStream(rotated.min(), rotated.max(), StreamOptions.lazily())
            .forEach((rotatedRef, entity, x, y, z) -> {
                final Vector3d transformedPos = new Vector3d(x, y, z);
                // We have this offset in the stream, so we have to undo it here.
                final Vector3d invertedTransformedPos = inverse
                    .transformPosition(transformedPos.add(VolumePositionTranslators.BLOCK_OFFSET))
                    .sub(VolumePositionTranslators.BLOCK_OFFSET);
                final Vector3i invertedBlockPos = invertedTransformedPos.toInt();

                final DataView posView = entity.blockEntityData().getView(VolumeTransformationTest.POS).get();
                final Vector3i expectedPos = new Vector3i(posView.getInt(DataQuery.of("x")).get(), posView.getInt(DataQuery.of("y")).get(), posView.getInt(DataQuery.of("z")).get());

                Assertions.assertNotEquals(
                    VolumeTransformationTest.INVALID_STUB_POSITION,
                    expectedPos,
                    () -> String.format("expected to have a positioned block entity: [%f, %f, %f] but got %s", x, y, z,
                        entity
                    )
                );

                Assertions.assertEquals(expectedPos, invertedBlockPos,
                    () -> String.format(
                        "expected untransformed position %s for block entity %s does not match reverse transformed position: %s",
                        expectedPos, entity, invertedBlockPos
                    )
                );

                final BlockEntityArchetype originalEntity = volume.blockEntityArchetype(expectedPos.x(), expectedPos.y(), expectedPos.z()).orElse(null);
                Assertions.assertEquals(entity, originalEntity,
                    () -> String.format(
                        "Expected deduced block entity to be equal from the original target volume but had a mismatch: Original target %s does not match %s",
                        originalEntity, entity
                    )
                );
            });
    }

}
