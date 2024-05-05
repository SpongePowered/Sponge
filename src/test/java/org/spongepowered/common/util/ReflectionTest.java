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
package org.spongepowered.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.common.test.UnitTestExtension;
import org.spongepowered.common.test.block.SpongeBlock;

import java.lang.reflect.Method;

@Disabled
@ExtendWith(UnitTestExtension.class)
public class ReflectionTest {

    @Test
    public void testCheckNeighborChangedDeclared() {
        final boolean isNeighborChangedDeclared = ReflectionUtil.isNeighborChangedDeclared(SpongeBlock.class);
        Assertions.assertTrue(isNeighborChangedDeclared, "NeighborChanged should be declared on SpongeBlock");
    }

    @Test
    public void testEntityInsideNotDeclared() {
        final boolean entityInsideDeclared = ReflectionUtil.isEntityInsideDeclared(SpongeBlock.class);
        Assertions.assertFalse(entityInsideDeclared, "isEntityInsideDeclared is not declared on SpongeBlock");
    }

    @Test
    public void testNonExistentMethodOnCustomClass() {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "org/spongepowered/common/test/block/BombDummy", null, "net/minecraft/world/level/block/Block", null);
        {
            final MethodVisitor ctor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V",
                null,
                null
            );
            ctor.visitCode();
            ctor.visitVarInsn(Opcodes.ALOAD, 0);
            ctor.visitVarInsn(Opcodes.ALOAD, 1);
            ctor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "net/minecraft/world/level/block/Block",
                "<init>",
                "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V",
                false
            );
            ctor.visitInsn(Opcodes.RETURN);
            ctor.visitMaxs(0, 0);
        }
        {
            final MethodVisitor el = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "neighborChanged",
                "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
                null,
                null
            );
            el.visitCode();
            el.visitInsn(Opcodes.RETURN);
            el.visitMaxs(0,0);
        }
        {
            final MethodVisitor bomb = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "throwup",
                "(Lcom/example/doesnt/Exist;)V",
                null,
                null
            );
            bomb.visitCode();
            bomb.visitInsn(Opcodes.RETURN);
            bomb.visitMaxs(0, 0);
        }

        final DefinableClassLoader loader = new DefinableClassLoader(this.getClass().getClassLoader());
        final byte[] bombClazzBytes = writer.toByteArray();
        final Class<?> clazz = loader.defineClass("org.spongepowered.common.test.block.BombDummy", bombClazzBytes);
        final boolean neighborChanged = ReflectionUtil.isNeighborChangedDeclared(clazz);
        Assertions.assertTrue(neighborChanged, "NeighborChanged should have been defined on BombDummy");
        final boolean entityInside = ReflectionUtil.isEntityInsideDeclared(clazz);
        Assertions.assertFalse(entityInside, "isEntityInsideDeclared is not defined on BombDummy");
        NoClassDefFoundError e = null;
        try {
            ReflectionTest.getNeighborChanged(clazz);
        } catch (final Exception ex) {
            Assertions.fail("Expected a class not found exception for com/example/doesnt/Exist");
        } catch (final NoClassDefFoundError ee) {
            e = ee;
        }
        Assertions.assertNotNull(e, "Should have gotten a class exception");
    }

    private static void getNeighborChanged(final Class<?> clazz) throws Exception, Error {
        final Method m = clazz.getMethod("neighborChanged", BlockState.class,
            Level.class,
            BlockPos.class,
            Block.class,
            BlockPos.class,
            boolean.class);
    }

}
