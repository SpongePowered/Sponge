package org.spongepowered.common.item.inventory.query.result;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.impl.AbstractLens;

import java.util.Set;

public class QueryLens extends AbstractLens {

    private InventoryAdapter adapter;

    public QueryLens(Set<Lens> resultSet) {
        super(0, getResultSize(resultSet), QueryResultAdapter.class);
        resultSet.forEach(lens -> this.addSpanningChild(lens));
    }

    private static int getResultSize(Set<Lens> resultSet) {
        return resultSet.stream().map(Lens::slotCount).mapToInt(i -> i).sum();
    }

    public void setAdapter(InventoryAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return adapter;
    }

}
