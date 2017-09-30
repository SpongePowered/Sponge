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

import static org.junit.Assume.assumeFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistryException;
import org.spongepowered.lwts.runner.LaunchWrapperParameterized;

import java.lang.reflect.Field;

@RunWith(LaunchWrapperParameterized.class)
public class CatalogTypeClassesTest {

    @Parameterized.Parameters(name = "{index} Catalog Type: {1} Field : {0}")
    public static Iterable<Object[]> data() throws Exception {
        return RegistryTestUtil.generateCatalogContainerTestObjects();
    }

    @Parameterized.Parameter(0) public String fieldName;
    @Parameterized.Parameter(1) public Class<? extends CatalogType> catalogClass;
    @Parameterized.Parameter(2) public Class<?> catalogContainerClass;
    @Parameterized.Parameter(3) public CatalogRegistryModule<?> registryModule;
    @Parameterized.Parameter(4) public Field targetedField;

    private boolean isDummy = false;

    @Test
    public void testCatalogFieldExists() throws Exception {
        try {
            final CatalogType o = (CatalogType) this.targetedField.get(null);
            o.getId(); // Validates that the field is not a dummy object. If it is, it will throw an exception.
        } catch (Exception e) {
            this.isDummy = true;
            throw e;
        }
    }

    @Test
    public void testCatalogCanBeRetrieved() throws Exception {
        assumeFalse(this.isDummy);
        final CatalogType o = (CatalogType) this.targetedField.get(null);
        this.registryModule.getById(o.getId())
            .orElseThrow(() -> new RegistryException("Could not locate " + this.fieldName + " in the registry: " + this.registryModule));
    }

}
