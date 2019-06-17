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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.monster.Slime;

public final class TypeTokenHelperTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testA() {
        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = -2744183853363912523L;
                },
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = 7512706472781562165L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<?>>() {
                    private static final long serialVersionUID = -4715432165291356217L;
                },
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = -9141111645139043586L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = -5084770058972215952L;
                },
                new TypeToken<Key<BaseValue<CatalogType>>>() {
                    private static final long serialVersionUID = 5868333657416959838L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = -6063028554691111654L;
                },
                new TypeToken<Key<BaseValue<? extends CatalogType>>>() {
                    private static final long serialVersionUID = 6475178912364063462L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<?>>>() {
                    private static final long serialVersionUID = 4476319679842566087L;
                },
                new TypeToken<Key<BaseValue<? extends Advancement>>>() {
                    private static final long serialVersionUID = -8991894373263296838L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<Advancement>>>() {
                    private static final long serialVersionUID = -9127427898214080482L;
                },
                new TypeToken<Key<BaseValue<Integer>>>() {
                    private static final long serialVersionUID = 3734740195264045904L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<Slime>>>() {
                    private static final long serialVersionUID = -776294957769877612L;
                },
                new TypeToken<Key<BaseValue<? extends EnderDragon>>>() {
                    private static final long serialVersionUID = -1080598601240787986L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<EnderDragon>>>() {
                    private static final long serialVersionUID = -1685122921179641087L;
                },
                new TypeToken<Key<BaseValue<? extends Living>>>() {
                    private static final long serialVersionUID = -7506005421970616703L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<EnderDragon>>>() {
                    private static final long serialVersionUID = -146766369451278059L;
                },
                new TypeToken<Key<BaseValue<Living>>>() {
                    private static final long serialVersionUID = 7172322752250029262L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<Key<BaseValue<? extends Living>>>() {
                    private static final long serialVersionUID = -7530165981429256750L;
                },
                TypeToken.of(Key.class)));

        assertFalse(TypeTokenHelper.isAssignable(
                TypeToken.of(Key.class),
                new TypeToken<Key<BaseValue<? extends Living>>>() {
                    private static final long serialVersionUID = -1048560465149463858L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration>() {
                    private static final long serialVersionUID = 4490040709077959054L;
                },
                new TypeToken<DataRegistration<?,?>>() {
                    private static final long serialVersionUID = 1966793391819384134L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                TypeToken.of(DataRegistration.class),
                new TypeToken<DataRegistration<LoreData,?>>() {
                    private static final long serialVersionUID = -2471612329609336642L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration>() {
                    private static final long serialVersionUID = 996573824362563041L;
                },
                new TypeToken<DataRegistration<LoreData,?>>() {
                    private static final long serialVersionUID = 7507644590282488025L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration<?,?>>() {
                    private static final long serialVersionUID = 6588698505383159642L;
                },
                new TypeToken<DataRegistration<LoreData,?>>() {
                    private static final long serialVersionUID = 2055481509347300371L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<DataRegistration<LoreData,?>>() {
                    private static final long serialVersionUID = 6146344812465379270L;
                },
                new TypeToken<DataRegistration<?,?>>() {
                    private static final long serialVersionUID = -1644821707399052814L;
                }));
    }

    @Test
    public void testB() {
        // Enclosing classes testing

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object>.B<Value<Double>>>() {
                    private static final long serialVersionUID = -7598875791253244116L;
                },
                new TypeToken<A<Object>.B<Value<? extends Number>>>() {
                    private static final long serialVersionUID = -6473792914038707807L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {
                    private static final long serialVersionUID = -7135507911189558819L;
                },
                new TypeToken<A<Key<BaseValue<Slime>>>.B<Value<? extends Number>>>() {
                    private static final long serialVersionUID = -7720256698994675635L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {
                    private static final long serialVersionUID = 3945720091540514712L;
                },
                new TypeToken<A<Key<BaseValue<? extends Living>>>.B<Value<? extends Number>>>() {
                    private static final long serialVersionUID = -5900476749764702993L;
                }));
    }

    @Test
    public void testC() {
        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object>>() {
                    private static final long serialVersionUID = 210782457160028139L;
                },
                new TypeToken<A<Object[]>>() {
                    private static final long serialVersionUID = 1830207951407728466L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<A<Object[]>>() {
                    private static final long serialVersionUID = 6306557918222324636L;
                },
                new TypeToken<A<Object>>() {
                    private static final long serialVersionUID = 6287092056418459473L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<D>() {
                    private static final long serialVersionUID = -2636161692715322380L;
                },
                new TypeToken<A<Number>>() {
                    private static final long serialVersionUID = -7834521766542990084L;
                }));

        assertTrue(TypeTokenHelper.isAssignable(
                new TypeToken<C>() {
                    private static final long serialVersionUID = -3899375880896785687L;
                },
                new TypeToken<A<Number[]>>() {
                    private static final long serialVersionUID = 3361010897315894472L;
                }));

        assertFalse(TypeTokenHelper.isAssignable(
                new TypeToken<A<Number[]>>() {
                    private static final long serialVersionUID = -8695441578543610047L;
                },
                new TypeToken<C>() {
                    private static final long serialVersionUID = 3553380220811338485L;
                }));
    }

    private static class D extends A<Number> {

    }

    private static class C extends A<Number[]> {

    }

    private static class A<T> {

        private class B<V> {
        }
    }
}
