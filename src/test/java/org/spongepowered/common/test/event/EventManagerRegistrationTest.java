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
package org.spongepowered.common.test.event;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.common.test.TestEventManager;
import org.spongepowered.common.test.UnitTestExtension;
import org.spongepowered.common.util.DefinableClassLoader;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.InvocationTargetException;

@Disabled
@ExtendWith(UnitTestExtension.class)
public class EventManagerRegistrationTest {

    @Test
    public void successRegistration() {
        final EventManager eventManager = new TestEventManager();
        final PluginContainer mock = Mockito.mock(PluginContainer.class);
        eventManager.registerListeners(mock, new Dummy());
    }

    @Test
    public void wildcardCanRegister() {
        final EventManager eventManager = new TestEventManager();
        final PluginContainer mock = Mockito.mock(PluginContainer.class);
        eventManager.registerListeners(mock, new Wildcard());
    }

    @Test
    public void successfulRegistrationWithAsmDefinedClass() throws
        NoSuchMethodException,
        InvocationTargetException,
        InstantiationException,
        IllegalAccessException {
        final var loader = new DefinableClassLoader(Thread.currentThread().getContextClassLoader());
        final EventManager eventManager = new TestEventManager(loader);
        final PluginContainer mock = Mockito.mock(PluginContainer.class);
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "org/spongepowered/common/test/event/BombDummy",
            null,
            "java/lang/Object",
            null
        );
        final MethodVisitor ctor = writer.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null
        );
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        );
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(1, 1);
        final MethodVisitor el = writer.visitMethod(
            Opcodes.ACC_PUBLIC,
            "onBlockChange",
            "(Lorg/spongepowered/api/event/block/ChangeBlockEvent;)V",
            null,
            null
        );
        el.visitCode();
        el.visitAnnotation(Type.getDescriptor(Listener.class), true);
        el.visitInsn(Opcodes.RETURN);

        final MethodVisitor bomb = writer.visitMethod(
            Opcodes.ACC_PUBLIC,
            "throwup",
            "(Lcom/example/doesnt/Exist;)V",
            null,
            null
        );
        bomb.visitCode();
        bomb.visitInsn(Opcodes.RETURN);
        final Class<?> clazz = loader.defineClass("org.spongepowered.common.test.event.BombDummy", writer.toByteArray());
        final Object o = clazz.getConstructor().newInstance();

        eventManager.registerListeners(mock, o);
    }


    public static class Dummy {

        @Listener
        public void onEnable(final StoppedGameEvent event) {

        }

        @Listener
        public void onRegister(final ProvideServiceEvent<EconomyService> e) {

        }

        @Listener
        public void onArray(final ProvideServiceEvent<Integer[]> e) {

        }

        @Include(DamageEntityEvent.class)
        @Listener(order = Order.FIRST)
        public void onFirst(
            final DamageEntityEvent event,
            final @First(inverse = true) DamageSource source,
            final @Getter("entity") Player target
        ) {

        }
    }

    static final class Wildcard {

        @Listener
        public void onCriteria(final CriterionEvent.Trigger<?> e) {

        }
    }
}
