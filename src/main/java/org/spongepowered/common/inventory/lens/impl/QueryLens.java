package org.spongepowered.common.inventory.lens.impl;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;

import java.util.Collection;

public class QueryLens extends AbstractLens {

    public QueryLens(Collection<Lens> lenses) {
        super(0, lenses.stream().map(Lens::slotCount).mapToInt(i -> i).sum(), BasicInventoryAdapter.class);
        for (Lens match : lenses) {
            this.addSpanningChild(match); // TODO properties?
        }
    }

    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new BasicInventoryAdapter(fabric, this, parent);
    }

}
