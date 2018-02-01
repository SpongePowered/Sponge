package org.spongepowered.common.data.generator;

import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Method;

public abstract class MethodEntry {

    final Method method;
    final KeyEntry keyEntry;

    public MethodEntry(Method method, KeyEntry keyEntry) {
        this.keyEntry = keyEntry;
        this.method = method;
    }

    abstract void visit(ClassVisitor classVisitor);
}
