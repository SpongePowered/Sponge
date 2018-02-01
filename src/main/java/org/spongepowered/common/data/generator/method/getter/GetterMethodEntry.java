package org.spongepowered.common.data.generator.method;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.generator.GeneratorHelper;
import org.spongepowered.common.data.generator.KeyEntry;

import java.lang.reflect.Method;

public class PrimitiveGetterMethodEntry extends MethodEntry {

    public PrimitiveGetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    void visit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        mv.visitVarInsn(ALOAD, 0);
        final Class<?> returnType = this.keyEntry.valueClass;
        mv.visitFieldInsn(GETFIELD, implClassDescriptor,
                this.keyEntry.valueFieldName, Type.getDescriptor(returnType));
        mv.visitInsn(GeneratorHelper.getReturnOpcode(this.keyEntry.valueClass));
        mv.visitMaxs(1, 1);
    }
}
