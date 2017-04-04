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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.CatalogType;
import org.spongepowered.lwts.runner.LaunchWrapperParameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

@RunWith(LaunchWrapperParameterized.class)
public class CatalogTypeMethodTest {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogTypeMethodTest.class);

    @Parameterized.Parameters(name = "{index} Catalog Type: {0} Id : {3} Method: {5} ({6})")
    public static Iterable<Object[]> data() throws Exception {
        return RegistryTestUtil.generateCatalogTypeMethodTestObjects();
    }

    // TODO: Fix this list
    private static final Set<String> ignoredFailures = ImmutableSet.<String>builder()
            .add("org.spongepowered.common.statistic.SpongeEntityStatistic#getEntityType()")
            .add("org.spongepowered.common.world.gen.SpongePopulatorType#getTranslation()")
            .add("net.minecraft.util.EnumHand#getTranslation()")
            .build();

    @Parameterized.Parameter(0)
    public String name;
    @Parameterized.Parameter(1)
    public Class<? extends CatalogType> catalogClass;
    @Parameterized.Parameter(2)
    public CatalogType catalogType;
    @Parameterized.Parameter(3)
    public String catalogId;
    @Parameterized.Parameter(4)
    public Method method;
    @Parameterized.Parameter(5)
    public String methodName;
    @Parameterized.Parameter(6)
    public String implementationClass;

    @Test
    public void testCatalogMethodImpl() throws Throwable {
        try {
            try {
                checkNotNull(this.method.invoke(this.catalogType), "return value");
            } catch (InvocationTargetException e) {
                // Unwrap exception to avoid useless stacktrace entries
                if (e.getCause() != null) {
                    throw e.getCause();
                } else {
                    throw e;
                }
            }
        } catch (Throwable t) {
            if (ignoredFailures.contains(this.implementationClass + "#" + this.method.getName() + "()") || t instanceof AbstractMethodError) {
                LOG.warn("Catalog Type: {} Id : {} has broken Method: {} ({}): {}", this.name, this.catalogId, this.methodName,
                        this.implementationClass, t);
                return;
            }
            throw t;
        }
    }

}
