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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

public final class ReflectionUtil {

    public static final Marker REFLECTION_SCANNING = MarkerManager.getMarker("REFLECTION_SCANNING");
    private static final Class<?>[] NEIGHBOR_CHANGED_METHOD_ARGS = {
        BlockState.class,
        Level.class,
        BlockPos.class,
        Block.class,
        BlockPos.class,
        boolean.class
    };
    private static final Class<?>[] ENTITY_INSIDE_METHOD_ARGS = {
        BlockState.class,
        Level.class,
        BlockPos.class,
        Entity.class
    };
    private static final Class<?>[] STEP_ON_METHOD_ARGS = {
        Level.class,
        BlockPos.class,
        BlockState.class,
        Entity.class
    };
    private static final Class<?>[] PLAYER_TOUCH_METHOD_ARGS= {
        Player.class
    };

    public static boolean isNeighborChangedDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            Block.class,
            "neighborChanged",
            ReflectionUtil.NEIGHBOR_CHANGED_METHOD_ARGS,
            void.class
        );
    }

    public static boolean isEntityInsideDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            BlockBehaviour.class,
            "entityInside",
            ReflectionUtil.ENTITY_INSIDE_METHOD_ARGS,
            void.class
        );
    }

    public static boolean isStepOnDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            Block.class,
            "stepOn",
            ReflectionUtil.STEP_ON_METHOD_ARGS,
            void.class
        );
    }

    public static boolean isPlayerTouchDeclared(final Class<?> targetClass) {
        return ReflectionUtil.doesMethodExist(
            targetClass,
            Entity.class,
            "playerTouch",
            ReflectionUtil.PLAYER_TOUCH_METHOD_ARGS,
            void.class
        );
    }

    public static boolean doesMethodExist(
        final Class<?> targetClass,
        final Class<?> ignoredClass,
        final String methodName,
        final Class<?>[] methodParameters,
        final Class<?> returnType
    ) {
        try {

            Class<?> clazz = targetClass;
            while (clazz != null) {
                if (clazz == Object.class || clazz == ignoredClass || clazz.getClassLoader() == null) {
                    return false;
                }
                final InputStream targetClassStream = clazz.getClassLoader().getResourceAsStream(
                    clazz.getName().replace('.', '/') + ".class");
                if (targetClassStream == null) {
                    return true;
                }
                final ClassReader reader = new ClassReader(targetClassStream);
                final MethodCheckerClassVisitor visitor = new MethodCheckerClassVisitor(methodName, methodParameters, returnType);
                reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                final boolean declared  = visitor.wasMethodDeclared();
                if (declared) {
                    return true;
                }
                clazz = clazz.getSuperclass();
            }
            return false;
        } catch (final NoClassDefFoundError e) {
            SpongeCommon.logger().fatal(ReflectionUtil.REFLECTION_SCANNING, String.format("Failed to load class in %s while scanning desired method %s", targetClass, methodName), e);
            return true;
        } catch (final IOException e) {
            SpongeCommon.logger().fatal(ReflectionUtil.REFLECTION_SCANNING, String.format("Class file exception while trying to load %s looking for method %s", targetClass, methodName), e);
            return true;
        }
    }

    private ReflectionUtil() {}

    static class MethodCheckerClassVisitor extends ClassVisitor {

        private final String targetMethod;
        private final String methodDescriptor;
        private boolean declared = false;

        public MethodCheckerClassVisitor(
            final String targetMethodForEnvironment, final Class<?>[] methodParameters, final Class<?> returnType
        ) {
            super(Opcodes.ASM8);
            this.targetMethod = targetMethodForEnvironment;
            final StringJoiner joiner = new StringJoiner("", "(", ")");
            for (final Class<?> clazz : methodParameters) {
                joiner.add(Type.getType(clazz).getDescriptor());
            }
            this.methodDescriptor = joiner + Type.getType(returnType).getDescriptor();
        }

        @Override
        public MethodVisitor visitMethod(
            final int access, final String name, final String descriptor, final String signature, final String[] exceptions
        ) {
            if (this.targetMethod.equals(name) && this.methodDescriptor.equals(descriptor)) {
                this.declared = true;
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        public boolean wasMethodDeclared() {
            return this.declared;
        }
    }

}
