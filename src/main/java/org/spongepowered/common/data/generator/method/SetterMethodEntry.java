package org.spongepowered.common.data.generator.method;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.generator.KeyEntry;

import java.lang.reflect.Method;

public class GetterMethodEntry extends MethodEntry {

    public GetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    void visit(ClassVisitor classVisitor, String implClassName) {
        final MethodVisitor mv = classVisitor.visitMethod(ACC_PUBLIC, this.method.getName(),
                Type.getMethodDescriptor(this.method), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, this.keyEntry.valueFieldName, this.keyEntry.valueFieldDescriptor);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
