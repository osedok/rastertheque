/*
 * $RCSfile: WritableRandomIterCSMDouble.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:44 $
 * $State: Exp $
 */
package com.sun.media.jai.iterator;
import java.awt.Rectangle;
import java.awt.image.WritableRenderedImage;
import javax.media.jai.iterator.WritableRandomIter;

/**
 * @since EA2
 */
public final class WritableRandomIterCSMDouble extends RandomIterCSMDouble
    implements WritableRandomIter {

    public WritableRandomIterCSMDouble(WritableRenderedImage im,
                                       Rectangle bounds) {
        super(im, bounds);
    }

    public void setSample(int x, int y, int b, int val) {
    }

    public void setSample(int x, int y, int b, float val) {
    }

    public void setSample(int x, int y, int b, double val) {
    }

    public void setPixel(int x, int y, int[] iArray) {
    }

    public void setPixel(int x, int y, float[] fArray) {
    }

    public void setPixel(int x, int y, double[] dArray) {
    }
}
