/*
 * $RCSfile: NotCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:37 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import java.util.Map;

/**
 * A <code>CRIF</code> supporting the "Not" operation in the
 * rendered and renderable image layers.
 *
 * @since EA2
 * @see javax.media.jai.operator.NotDescriptor
 * @see NotOpImage
 *
 */
public class NotCRIF extends CRIFImpl {

     /** Constructor. */
    public NotCRIF() {
        super("not");
    }

    /**
     * Creates a new instance of <code>NotOpImage</code> in the
     * rendered layer. This method satisifies the implementation of RIF.
     *
     * @param paramBlock   The source image to perform the logical "not"
     *                     operation on.
     * @param renderHints  Optionally contains destination image layout.     
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        return new NotOpImage(paramBlock.getRenderedSource(0),
                              renderHints,
			      layout);
    }
}
