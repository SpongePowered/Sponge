package org.spongepowered.common.resource;

import org.spongepowered.api.resource.ResourceLoader;
import org.spongepowered.api.util.Priority;

/**
 * Created by Matthew on 4/13/2017.
 */
public class SortableLoader implements Comparable<SortableLoader> {

    private ResourceLoader loader;
    private Priority priority;

    SortableLoader(ResourceLoader loader, Priority priority) {
        this.loader = loader;
        this.priority = priority;
    }

    public ResourceLoader getLoader() {
        return loader;
    }

    public Priority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(SortableLoader o) {
        return priority.compareTo(o.getPriority());
    }
}
