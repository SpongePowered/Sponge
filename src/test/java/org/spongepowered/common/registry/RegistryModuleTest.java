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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.lwts.runner.LaunchWrapperParameterized;

@RunWith(LaunchWrapperParameterized.class)
public class RegistryModuleTest {

    @Parameterized.Parameters(name = "{index} Catalog Type: {0} Id : {4}")
    public static Iterable<Object[]> data() throws Exception {
        return RegistryTestUtil.generateRegistryTestObjects();
    }

    @Parameterized.Parameter(0) public String name;
    @Parameterized.Parameter(1) public Class<? extends CatalogType> catalogClass;
    @Parameterized.Parameter(2) public CatalogRegistryModule<?> registryModule;
    @Parameterized.Parameter(3) public CatalogType catalogType;
    @Parameterized.Parameter(4) public String catalogId;

//    @Test
//    @Ignore
//    public void testIdValidity() {
//        assertThat("The CatalogType " + this.catalogType.getName() + " has an invalid id", this.catalogId.startsWith("minecraft:") || this.catalogId.startsWith("sponge:"));
//        assertThat("The CatalogType " + this.catalogType.getName() + " has spaces! It should never be spaced!", !this.catalogId.contains(" "));
//    }

    @Test
    public void testGetCatalogTypeFromAll() {
        final boolean contains = this.registryModule.getAll().contains(this.catalogType);
        assertThat("The CatalogType " + this.catalogType.getId() + " could not be located in the \n"
                   + "all collection of " + this.registryModule.getClass().getSimpleName() + ".",
            contains, is(true));
    }

    @Test
    public void testGetCatalogTypeById() {
        assertThat("The CatalogType " + this.catalogType.getId() + " could not be retrieved by id \n"
                   + "from " + this.registryModule.getClass().getSimpleName() + " with the following id: \n"
                   + this.catalogId,
            this.registryModule.getById(this.catalogType.getId()).isPresent(), is(true));
    }


}
