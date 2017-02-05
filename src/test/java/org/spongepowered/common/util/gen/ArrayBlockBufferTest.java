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
package org.spongepowered.common.util.gen;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.GlobalPalette;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

public class ArrayBlockBufferTest {

    private static BlockState AIR;
    private static BlockState TNT;
    private static Cause CAUSE;

    @BeforeClass
    public static void init() {
        try {
            // Create fake block states
            AIR = createBlockState("AIR");
            TNT = createBlockState("TNT");
            for (int i = 0; i < 4000; i++) {
                createBlockState();
            }

            // Make BlockTypes.AIR.getDefaultState() work
            BlockType airType = mock(BlockType.class);
            when(airType.getDefaultState()).thenReturn(AIR);
            setField(BlockTypes.class, null, "AIR", airType);

            //Hack Sponge so GlobalPalette does not explode when we touch it
            GameRegistry registry = mock(GameRegistry.class);
            when(registry.getAllOf(BlockState.class)).thenReturn(
                    (Collection<BlockState>) (Object) Lists.newArrayList(Block.BLOCK_STATE_IDS.iterator()));
            Game game = mock(Game.class);
            when(game.getRegistry()).thenReturn(registry);
            setField(Sponge.class, null, "game", game);

        } catch (Exception e) {
            fail(e.toString());
        }

        CAUSE = Cause.of(NamedCause.of("array_block_buffer_test", ArrayBlockBufferTest.class));
    }

    private static void setField(Class clazz, @Nullable Object obj, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            Field modifiers = field.getClass().getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(obj, value);

        } catch (Exception e) {
            fail(e.toString());
        }
    }

    private static BlockState createBlockState() {
        return createBlockState("block_state_G" + Block.BLOCK_STATE_IDS.size());
    }

    private static BlockState createBlockState(String name) {
        FakeBlockState bs = mock(FakeBlockState.class, name);
        Block.BLOCK_STATE_IDS.put(bs, Block.BLOCK_STATE_IDS.size());
        return bs;
    }

    private abstract class FakeBlockState implements BlockState, IBlockState {}

    @Test
    public void testLargeAirDefault() {
        Vector3i size = new Vector3i(10, 20, 30);
        ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(Vector3i.ZERO, size);
        TestBuffer testBuffer = new TestBuffer(size);

        testBuffer.assertEqualz(buffer);

        BlockPalette nonZeroAirId = new BimapPalette();
        nonZeroAirId.getOrAssign(TNT);

        buffer = new ArrayMutableBlockBuffer(nonZeroAirId, Vector3i.ZERO, size);

        testBuffer.assertEqualz(buffer);
    }

    @Test
    public void testSmallAirDefault() {
        Vector3i size = new Vector3i(1, 2, 3);
        ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(Vector3i.ZERO, size);
        TestBuffer testBuffer = new TestBuffer(size);

        testBuffer.assertEqualz(buffer);

        BlockPalette nonZeroAirId = new BimapPalette();
        nonZeroAirId.getOrAssign(TNT);

        buffer = new ArrayMutableBlockBuffer(nonZeroAirId, Vector3i.ZERO, size);

        testBuffer.assertEqualz(buffer);
    }

    @Test
    public void testBlockStates() {
        Vector3i size = new Vector3i(10, 10, 10);
        ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(Vector3i.ZERO, size);
        TestBuffer testBuffer = new TestBuffer(size);

        Iterator<BlockState> statesIter = GlobalPalette.instance.getEntries().iterator();
        buffer.getBlockWorker(CAUSE).iterate((volume, x, y, z) -> {
            if (statesIter.hasNext()) {
                testBuffer.setInBoth(volume, x, y, z, statesIter.next());
                testBuffer.assertEqualz(volume);
            }
        });
    }

    private class TestBuffer {

        private BlockState[][][] blocks;
        private Vector3i size;

        private TestBuffer(Vector3i size) {
            this.size = size;
            this.blocks = new BlockState[size.getX()][size.getY()][size.getZ()];
        }

        private void set(int x, int y, int z, BlockState block) {
            blocks[x][y][z] = block;
        }

        private void setInBoth(MutableBlockVolume volume, int x, int y, int z, BlockState block) {
            set(x, y, z, block);
            volume.setBlock(volume.getBlockMin().add(x, y, z), block, CAUSE);
        }

        private BlockState get(int x, int y, int z) {
            return blocks[x][y][z] != null ? blocks[x][y][z] : AIR;
        }

        private void assertEqualz(BlockVolume volume) {
            assertEquals(size, volume.getBlockSize());
            for (int x = 0; x < size.getX(); x++) {
                for (int y = 0; y < size.getY(); y++) {
                    for (int z = 0; z < size.getZ(); z++) {
                        assertEquals(get(x, y, z), volume.getBlock(volume.getBlockMin().add(x, y, z)));
                    }
                }
            }
        }
    }
}
