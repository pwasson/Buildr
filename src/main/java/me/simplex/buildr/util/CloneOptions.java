/*
 * Copyright 2017 Managing Editor Inc.
 * All rights reserved.
 */

package me.simplex.buildr.util;

/**
 *
 * @author pwasson
 */
public class CloneOptions {
/* NOT YET SUPPORTED
    * force: allow even if source and dest cuboids overlap.
    * move: Clone the source region to the destination region, then replace the source region with air.
        When used in filtered mask mode, only the cloned blocks will be replaced with air.
    * normal: (Default) Don't move or force.
*/
    public enum CloneMode {
        NORMAL,
        MOVE,
        FORCE
    }

    /*
        * filtered: only copy a specified material.
        * masked: copy only non-air blocks.
        * replace: (Default) copy and replace everything.
    
    */
    public enum MaskMode {
        REPLACE,
        FILTER,
        MASK
    }

    private final CloneMode cloneMode;
    private final MaskMode maskMode;
    private final MaterialAndData filterMaterial;
    private final MaterialAndData replaceMaterial;


    public CloneOptions(CloneMode inCloneMode, MaskMode inMaskMode,
            MaterialAndData inFilterMaterial,
            MaterialAndData inReplaceMaterial) {
        this.cloneMode = inCloneMode;
        this.maskMode = inMaskMode;
        this.filterMaterial = inFilterMaterial;
        this.replaceMaterial = inReplaceMaterial;
    }


    public CloneMode getCloneMode() {
        return cloneMode;
    }


    public MaskMode getMaskMode() {
        return maskMode;
    }


    public MaterialAndData getFilterMaterial() {
        return filterMaterial;
    }


    public MaterialAndData getReplaceMaterial() {
        return replaceMaterial;
    }
}
