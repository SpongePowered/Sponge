package org.spongepowered.common.item.inventory.query.result;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;

import java.util.Set;

public class QueryResultAdapter extends BasicInventoryAdapter implements InventoryAdapter {

    private QueryResultAdapter(Fabric fabric, Inventory parent, org.spongepowered.common.item.inventory.query.result.QueryLens lens) {
        super(fabric, lens, parent); // null root lens calls initRootLens();
        lens.setAdapter(this);
    }

    public QueryResultAdapter(Fabric fabric, Inventory parent, Set<Lens> lenses) {
        this(fabric, parent, new org.spongepowered.common.item.inventory.query.result.QueryLens(lenses));
    }

}
