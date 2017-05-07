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
package org.spongepowered.common.item.inventory.observer;

import org.spongepowered.common.util.observer.EventArgs;

/**
 * Observable event payload class for inventory slot actions.
 */
public class InventoryEventArgs extends EventArgs {
    
    /**
     * Type of thing what did properly happen.
     */
    public enum Type {
        
        /**
         * The contents of a "slot" was changed.
         */
        SLOT_CONTENT_CHANGED, //(true),
        
        /**
         * A property of a "slot" (in <em>this</em> scope) was changed. 
         */
        SLOT_PROPERTY_CHANGED, //(false),
        
        /**
         * InventoryView added to scope.
         */
        LENS_ADDED, //(false),
        
        /**
         * InventoryView in this scope was invalidated (<tt>invalidate</tt>
         * was called) 
         */
        LENS_INVALIDATED, //(false),
        
        /**
         * InventoryView in the scope was removed.
         */
        LENS_REMOVED; //(false);
        
//        private final boolean propagate;
//
//        Type(boolean propagate) {
//            this.propagate = propagate;
//        }
//        
//        public boolean isPropagated() {
//            return this.propagate;
//        }
        
    }
    
    public final Type type;
    
    public final int index;
    
    public final Object source;
    
    public InventoryEventArgs(Type type, Object source) {
        this.type = type;
        this.index = -1;
        this.source = source;
    }
    
    public InventoryEventArgs(Type type, int index) {
        this.type = type;
        this.index = index;
        this.source = null;
    }
    
    public InventoryEventArgs(Type type) {
        this(type, -1);
    }
    
    public int getIndex() {
        return this.index;
    }

    public Type getType() {
        return this.type;
    }
}
