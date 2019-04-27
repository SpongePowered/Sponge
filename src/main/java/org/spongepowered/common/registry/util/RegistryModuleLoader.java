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
package org.spongepowered.common.registry.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.RegistryHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public final class RegistryModuleLoader {

    private RegistryModuleLoader() {
    }

    public static boolean tryModulePhaseRegistration(RegistryModule module) {
        try {
            Method method = getCustomRegistration(module);
            if (method != null) {
                if (isCustomProperPhase(method)) {
                    invokeCustomRegistration(module, method);
                    return true;
                } else {
                    return false;
                }
            } else {
                if (isDefaultProperPhase(module)) {
                    module.registerDefaults();
                    RegisterCatalog regAnnot = getRegisterCatalogAnnot(module);
                    if (regAnnot != null) {
                        Map<String, ?> map = getCatalogMap(module);
                        if (map.isEmpty()) {
                            SpongeImpl.getLogger().warn("{} has an empty CatalogMap. Implement registerDefaults() or use the CustomCatalogRegistration annotation", module.getClass().getCanonicalName());
                            return true;
                        }
                        Set<String> ignored = regAnnot.ignoredFields().length == 0 ? null : Sets.newHashSet(regAnnot.ignoredFields());
                        RegistryHelper.mapFields(regAnnot.value(), map, ignored);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error trying to initialize module: " + module.getClass().getCanonicalName(), e);
        }
    }

    @Nullable
    private static Method getCustomRegistration(RegistryModule module) {
        for (Method method : module.getClass().getMethods()) {
            CustomCatalogRegistration registration = method.getDeclaredAnnotation(CustomCatalogRegistration.class);
            if (registration != null) {
                return method;
            }
        }
        return null;
    }

    @Nullable
    private static RegisterCatalog getRegisterCatalogAnnot(RegistryModule module) {
        final RegisterCatalog catalog = module.getClass().getAnnotation(RegisterCatalog.class);
        if (catalog != null) {
            return catalog;
        }
        for (Field field : module.getClass().getDeclaredFields()) {
            RegisterCatalog annotation = field.getAnnotation(RegisterCatalog.class);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static boolean isDefaultProperPhase(RegistryModule module) {
        try {
            Method method = module.getClass().getMethod("registerDefaults");
            DelayedRegistration delay = method.getDeclaredAnnotation(DelayedRegistration.class);
            if (delay == null) {
                return true;
            }
            return SpongeImpl.getRegistry().getPhase() == delay.value();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isCustomProperPhase(Method custom) {
        DelayedRegistration delay = custom.getDeclaredAnnotation(DelayedRegistration.class);
        if (delay == null) {
            return SpongeImpl.getRegistry().getPhase() == RegistrationPhase.PRE_REGISTRY;
        }
        return SpongeImpl.getRegistry().getPhase() == delay.value();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, ?> getCatalogMap(RegistryModule module) {
        if (module instanceof AlternateCatalogRegistryModule) {
            return checkNotNull(((AlternateCatalogRegistryModule) module).provideCatalogMap(), "Provided CatalogMap can't be null");
        }
        for (Field field : module.getClass().getDeclaredFields()) {
            RegisterCatalog annotation = field.getAnnotation(RegisterCatalog.class);
            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    Map<String, ?> map = (Map<String, ?>) field.get(module);
                    return checkNotNull(map);
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("Failed to retrieve a registry field from module: " + module.getClass().getCanonicalName());
                }
            }
        }
        throw new IllegalStateException("Registry module does not have a catalog map! Registry: " + module.getClass().getCanonicalName());
    }

    private static void invokeCustomRegistration(RegistryModule module, Method method) {
        try {
            method.invoke(module);
        } catch (IllegalAccessException | InvocationTargetException e) {
            SpongeImpl.getLogger().error("Error when calling custom catalog registration for module: "
                    + module.getClass().getCanonicalName(), e);
        }
    }

    public static void tryAdditionalRegistration(RegistryModule module) {
        Method additionalRegistration = getAdditionalMethod(module);
        if (additionalRegistration != null) {
            try {
                additionalRegistration.invoke(module);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static Method getAdditionalMethod(RegistryModule module) {
        for (Method method : module.getClass().getMethods()) {
            AdditionalRegistration registration = method.getDeclaredAnnotation(AdditionalRegistration.class);
            if (registration != null) {
                return method;
            }
        }
        return null;
    }
}
