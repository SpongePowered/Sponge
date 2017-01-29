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
package org.spongepowered.common.item.inventory.query;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensSet;
import org.spongepowered.common.item.inventory.lens.impl.collections.MutableLensSetImpl;
import org.spongepowered.common.item.inventory.query.result.MinecraftResultAdapterProvider;
import org.spongepowered.common.item.inventory.query.result.QueryResult;
import org.spongepowered.common.item.inventory.query.strategy.ClassStrategy;
import org.spongepowered.common.item.inventory.query.strategy.GenericStrategy;
import org.spongepowered.common.item.inventory.query.strategy.ItemStackStrategy;
import org.spongepowered.common.item.inventory.query.strategy.ItemTypeStrategy;
import org.spongepowered.common.item.inventory.query.strategy.NameStrategy;
import org.spongepowered.common.item.inventory.query.strategy.PropertyStrategy;
import org.spongepowered.common.item.inventory.query.strategy.expression.ExpressionStrategy;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

public class Query<TInventory, TStack> {

    public static enum Type {

        CLASS("class", ClassStrategy.class),
        TYPE("type", ItemTypeStrategy.class),
        STACK("stack", ItemStackStrategy.class),
        PROPERTIES("property", PropertyStrategy.class),
        NAME("name", NameStrategy.class),
        EXPRESSION("expr", ExpressionStrategy.class),
        GENERIC("args", GenericStrategy.class);

        private final String key;

        private final Class<? extends QueryStrategy<?, ?, ?>> defaultStrategyClass;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private Type(String key, Class<? extends QueryStrategy> defaultStrategyClass) {
            this.key = key;
            this.defaultStrategyClass = (Class<? extends QueryStrategy<?, ?, ?>>) defaultStrategyClass;
        }

        public String getKey() {
            return this.key;
        }

        public Class<? extends QueryStrategy<?, ?, ?>> getDefaultStrategyClass() {
            return this.defaultStrategyClass;
        }

    }

    public abstract interface ResultAdapterProvider<TInventory, TStack> {

        QueryResult<TInventory, TStack> getResultAdapter(Fabric<TInventory> inventory, MutableLensSet<TInventory, TStack> matches, Inventory parent);

    }

    private static final Map<String, Class<? extends QueryStrategy<?, ?, ?>>> strategies
            = Maps.<String, Class<? extends QueryStrategy<?, ?, ?>>>newHashMap();

    private static ResultAdapterProvider<?, ?> defaultResultProvider;

    static {
        Query.registerDefaultStrategies();
        Query.setDefaultResultProvider(new MinecraftResultAdapterProvider());
    }

    private final InventoryAdapter<TInventory, TStack> adapter;

    private final Fabric<TInventory> inventory;

    private final Lens<TInventory, TStack> lens;

    private final QueryStrategy<TInventory, TStack, ?> strategy;

    protected Query(InventoryAdapter<TInventory, TStack> adapter, QueryStrategy<TInventory, TStack, ?> strategy) {
        this.adapter = adapter;
        this.inventory = adapter.getInventory();
        this.lens = adapter.getRootLens();
        this.strategy = strategy;
    }

    @SuppressWarnings("unchecked")
    public Inventory execute() {
        return this.execute((ResultAdapterProvider<TInventory, TStack>) Query.defaultResultProvider);
    }

    public Inventory execute(ResultAdapterProvider<TInventory, TStack> resultProvider) {
        if (this.strategy.matches(this.lens, null, this.inventory)) {
            return this.lens.getAdapter(this.inventory, null);
        }

        return this.toResult(resultProvider, this.depthFirstSearch(this.lens));
    }

    @SuppressWarnings("unchecked")
    private Inventory toResult(ResultAdapterProvider<TInventory, TStack> resultProvider, MutableLensSet<TInventory, TStack> matches) {
        if (matches.size() == 0) {
            return new EmptyInventoryImpl(this.adapter);
        }
        if (matches.size() == 1) {
            InventoryAdapter<TInventory, TStack> ada = matches.getLens(0).getAdapter(this.inventory, this.adapter);
            return ada;
        }

        if (resultProvider != null) {
            return resultProvider.getResultAdapter(this.inventory, matches, this.adapter);
        }

        return ((ResultAdapterProvider<TInventory, TStack>)Query.defaultResultProvider).getResultAdapter(this.inventory, matches, this.adapter);
    }

    private MutableLensSet<TInventory, TStack> depthFirstSearch(Lens<TInventory, TStack> lens) {
        MutableLensSet<TInventory, TStack> matches = new MutableLensSetImpl<TInventory, TStack>(true);

        for (Lens<TInventory, TStack> child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (child.getChildren().size() > 0) {
                matches.addAll(this.depthFirstSearch(child));
            }
            if (this.strategy.matches(child, lens, this.inventory)) {
                matches.add(child);
            }
        }

        // Only a single match or no matches
        if (matches.size() < 2) {
            return matches;
        }

        return this.reduce(lens, matches);
    }

    private MutableLensSet<TInventory, TStack> reduce(Lens<TInventory, TStack> lens, MutableLensSet<TInventory, TStack> matches) {
        if (lens.getSlots().equals(this.getSlots(matches))) {
            matches.clear();
            matches.add(lens);
            return matches;
        }

        for (Lens<TInventory, TStack> child : lens.getChildren()) {
            if (child == null || !child.isSubsetOf(matches)) {
                continue;
            }
            matches.removeAll(child.getChildren());
            matches.add(child);
        }

        return matches;
    }

//    private boolean allLensesAreSlots(MutableLensSetImpl<TInventory, TStack> lenses) {
//        for (Lens<TInventory, TStack> lens : lenses) {
//            if (!(lens instanceof SlotLens)) {
//                return false;
//            }
//        }
//        return true;
//    }

    private IntSet getSlots(Collection<Lens<TInventory, TStack>> lenses) {
        IntSet slots = new IntOpenHashSet();
        for (Lens<TInventory, TStack> lens : lenses) {
            slots.addAll(lens.getSlots());
        }
        return slots;
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, Class<?>... types) {
        QueryStrategy<TInventory, TStack, Class<?>> strategy = Query.<TInventory, TStack, Class<?>>getStrategy(Type.CLASS).with(types);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, ItemType... types) {
        QueryStrategy<TInventory, TStack, ItemType> strategy = Query.<TInventory, TStack, ItemType>getStrategy(Type.TYPE).with(types);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, ItemStack... types) {
        QueryStrategy<TInventory, TStack, ItemStack> strategy = Query.<TInventory, TStack, ItemStack>getStrategy(Type.STACK).with(types);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, InventoryProperty<?, ?>... props) {
        QueryStrategy<TInventory, TStack, InventoryProperty<?, ?>> strategy = Query.<TInventory, TStack, InventoryProperty<?, ?>>getStrategy(Type.PROPERTIES).with(props);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, Translation... names) {
        QueryStrategy<TInventory, TStack, Translation> strategy = Query.<TInventory, TStack, Translation>getStrategy(Type.NAME).with(names);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, String... expression) {
        QueryStrategy<TInventory, TStack, String> strategy = Query.<TInventory, TStack, String>getStrategy(Type.EXPRESSION).with(expression);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack> Query<TInventory, TStack> compile(InventoryAdapter<TInventory, TStack> adapter, Object... args) {
        QueryStrategy<TInventory, TStack, Object> strategy = Query.<TInventory, TStack, Object>getStrategy(Type.GENERIC).with(args);
        return new Query<TInventory, TStack>(adapter, strategy);
    }

    public static <TInventory, TStack, TArgs> QueryStrategy<TInventory, TStack, TArgs> getStrategy(Type type) {
        return Query.getStrategy(type.getKey());
    }

    public static <TInventory, TStack, TArgs> QueryStrategy<TInventory, TStack, TArgs> getStrategy(String key) {
        @SuppressWarnings("unchecked")
        Class<? extends QueryStrategy<TInventory, TStack, TArgs>> strategyClass = (Class<? extends QueryStrategy<TInventory, TStack, TArgs>>) checkNotNull(Query.strategies.get(key), "The specified query strategy [%s], was not registered", key);
        try {
            return strategyClass.newInstance();
        } catch (Exception ex) {
            throw new InvalidQueryStrategyException("The query strategy class %s does not provide a noargs ctor", strategyClass);
        }
    }

    public static void registerStrategy(String key, Class<? extends QueryStrategy<?, ?, ?>> strategyClass) {
        try {
            @SuppressWarnings({ "unchecked", "unused" })
            Constructor<QueryStrategy<?, ?, ?>> ctor = (Constructor<QueryStrategy<?, ?, ?>>) checkNotNull(strategyClass, "strategyClass").getConstructor();
        } catch (Exception ex) {
            throw new InvalidQueryStrategyException("The query strategy class %s does not provide a noargs ctor", strategyClass);
        }
        Query.strategies.put(key, strategyClass);
    }

    public static void setDefaultResultProvider(ResultAdapterProvider<?, ?> defaultResultProvider) {
        Query.defaultResultProvider = defaultResultProvider;
    }

    private static void registerDefaultStrategies() {
        for (Type type : Query.Type.values()) {
            Query.registerStrategy(type.getKey(), type.getDefaultStrategyClass());
        }
    }

}
