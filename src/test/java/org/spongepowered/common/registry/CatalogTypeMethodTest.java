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
import net.minecraft.stats.StatBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.translation.Translatable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.interfaces.MojangTranslatable;
import org.spongepowered.lwts.runner.LaunchWrapperParameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

@RunWith(LaunchWrapperParameterized.class)
public class CatalogTypeMethodTest {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogTypeMethodTest.class);

    private static final boolean verbose = false;

    private static String last = "";

    @Parameterized.Parameters(name = "{index} Catalog Type: {0} | Key: {3} | Method: {5} | ({6})")
    public static Iterable<Object[]> data() throws Exception {
        return RegistryTestUtil.generateCatalogTypeMethodTestObjects();
    }

    private static final Set<String> ignoredMojangTranslations = ImmutableSet.<String>builder()
            .add("tile.stone.name", "tile.sapling.name", "tile.prismarine.name", "tile.sponge.name",
                    "tile.cobbleWall.name", "tile.banner.name", "tile.stoneSlab2.name"
            )
            .build();

    @Parameterized.Parameter(0)
    public String name;
    @Parameterized.Parameter(1)
    public Class<? extends CatalogType> catalogClass;
    @Parameterized.Parameter(2)
    public CatalogType catalogType;
    @Parameterized.Parameter(3)
    public String catalogKey;
    @Parameterized.Parameter(4)
    public Method method;
    @Parameterized.Parameter(5)
    public String methodName;
    @Parameterized.Parameter(6)
    public String implementationClass;

    @Test
    public void testCatalogMethodImpl() throws Throwable {
        if (!last.equals(this.catalogKey)) {
            last = this.catalogKey;
            testKeyAndName(this.catalogType);

            if (this.catalogType instanceof Translatable) {
                testTranslation((Translatable) this.catalogType);
            }
        }

        try {
            testResult(checkNotNull(this.method.invoke(this.catalogType), "return value"));
        } catch (InvocationTargetException e) {
            // Unwrap exception to avoid useless stacktrace entries
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }

    private void testResult(Object object) {
        if (object instanceof Optional) {
            if (((Optional) object).isPresent()) {
                this.testResult(((Optional) object).get());
            }
        }

        if (object instanceof Iterable) {
            int index = 0;
            for (Object elem : (Iterable<?>) object) {
                try {
                    checkNotNull(elem, "contained value");
                    testResult(elem);
                    index++;
                } catch (Throwable t) {
                    throw new RuntimeException("Failed on sub-element: " + index, t);
                }
            }
        }

        if (object instanceof CatalogType) {
            testKeyAndName((CatalogType) object);
        }

        if (object instanceof Translatable) {
            testTranslation((Translatable) object);
        }
    }

    private void testKeyAndName(CatalogType object) {
        String name = object.getName();
        CatalogKey key = object.getKey();

        if (key.getValue().indexOf(':') != -1) {
            LOG.warn("Duplicate namespace. Catalog Type: {} | Key: {} | Name: {} | ({})", object.getClass().getSimpleName(), key, name, this.implementationClass);
        }

        if (name.equals(key.toString())) {
            LOG.warn("Keys and names should NEVER be the same. Catalog Type: {} | Key: {} | Name: {} | ({})", object.getClass().getSimpleName(), key, name, this.implementationClass);
        }

        if (key.getValue().chars().anyMatch(c -> Character.isLetter(c) && Character.isUpperCase(c))) {
            LOG.warn("Key is not lowercase. Catalog Type: {} | Key: {} | ({})", object.getClass().getSimpleName(), key, this.implementationClass);
        }

    }

    private void testTranslation(Translatable object) {
        Translation translation = object.getTranslation();

        if (translation != object.getTranslation()) {
            LOG.warn("Translation is not lazyloaded. Catalog Type: {} | Translation: {} | ({})", object.getClass().getSimpleName(), object.getTranslation(), this.implementationClass);
        }

        String mojang;
        if (object instanceof MojangTranslatable) {
            mojang = ((MojangTranslatable) object).getMojangTranslation();
            if (!translation.get().equals(mojang) && !ignoredMojangTranslations.contains(mojang)) {
                if (object instanceof StatBase) {
                    //Wait for statistics PR
                    return;
                }
                LOG.warn("Inconsistency with Mojang translation. Catalog Type: {} | Mojang: {} | Sponge: {} | ({})", object.getClass().getSimpleName(), mojang, translation.get(), this.implementationClass);
            }

        } else {
            if (verbose) {
                LOG.warn("Unable to retrieve Mojang translation for Catalog Type: {} | Translation id: {} | Sponge: {} | ({})", object.getClass().getSimpleName(), translation.getId(), translation.get(), this.implementationClass);
            }
        }

    }

}
