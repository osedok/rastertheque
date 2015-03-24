/*
 * $RCSfile: RIFUtil.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:41 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;

public class RIFUtil {

    public static ImageLayout getImageLayoutHint(RenderingHints renderHints) {
        if (renderHints == null) {
            return null;
        } else {
            return (ImageLayout)renderHints.get(JAI.KEY_IMAGE_LAYOUT);
        }
    }

    public static TileCache getTileCacheHint(RenderingHints renderHints) {
        if (renderHints == null) {
            return null;
        } else {
            return (TileCache)renderHints.get(JAI.KEY_TILE_CACHE);
        }
    }

    public static BorderExtender
        getBorderExtenderHint(RenderingHints renderHints) {
        if (renderHints == null) {
            return null;
        } else {
            return (BorderExtender)renderHints.get(JAI.KEY_BORDER_EXTENDER);
        }
    }
}
