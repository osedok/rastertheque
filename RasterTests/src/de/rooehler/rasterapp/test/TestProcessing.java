package de.rooehler.rasterapp.test;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.vividsolutions.jts.geom.Envelope;

import de.rooehler.rastertheque.core.Raster;
import de.rooehler.rastertheque.core.RasterQuery;
import de.rooehler.rastertheque.io.gdal.GDALDataset;
import de.rooehler.rastertheque.io.gdal.GDALDriver;
import de.rooehler.rastertheque.processing.RasterOps;
import de.rooehler.rastertheque.processing.Render;
import de.rooehler.rastertheque.processing.Resampler.ResampleMethod;
import de.rooehler.rastertheque.processing.Resize;
import de.rooehler.rastertheque.processing.rendering.MRenderer;
import de.rooehler.rastertheque.processing.resampling.JAIResampler;
import de.rooehler.rastertheque.processing.resampling.MResampler;
import de.rooehler.rastertheque.processing.resampling.OpenCVResampler;

public class TestProcessing extends android.test.AndroidTestCase  {
	
	/**
	 * tests and compares interpolations
	 */
	public void testBilinearInterpolation() throws IOException{		
		
		final GDALDriver driver = new GDALDriver();
		
		assertTrue(driver.canOpen(TestIO.GRAY_50M_BYTE));
		
		final GDALDataset dataset = driver.open(TestIO.GRAY_50M_BYTE);
		
		final Envelope  dim = dataset.getDimension();
		final int height = (int) dim.getHeight();
		final int width =  (int)dim.getWidth();
		
		final int tileSize = Math.min(width, height) / 10;
		
		final Envelope env = new Envelope(0, tileSize, 0, tileSize);
			     
        final RasterQuery query = new RasterQuery(
        		env,
        		dataset.getCRS(),
        		dataset.getBands(),
        		new Envelope(0, env.getWidth(), 0, env.getHeight()),
        		dataset.getBands().get(0).datatype());
        
        final Raster raster = dataset.read(query);
        
        final MRenderer renderer = new MRenderer(TestIO.GRAY_50M_BYTE, true);
        
        final int[] pixels  = renderer.render(raster);
        
        assertNotNull(pixels);
        
        final int resamplingFactor = 3;
        
        final int resampledSize = tileSize * resamplingFactor;
        
        final int[] mResampled = new int[resampledSize * resampledSize];
        final int[] jaiResampled = new int[resampledSize * resampledSize];
        final int[] openCVResampled = new int[resampledSize * resampledSize];
        
        final Envelope targetEnv = new Envelope(0, resampledSize, 0, resampledSize);
        
        /////// MImp ///////
        
        long now = System.currentTimeMillis();
        
        new MResampler().resample(raster,targetEnv,ResampleMethod.BILINEAR, null);
        
        Log.d(TestProcessing.class.getSimpleName(), "MInterpolation took "+ (System.currentTimeMillis() - now));
           
        /////// JAI ///////
        
        now = System.currentTimeMillis();
        
        new JAIResampler().resample(raster,targetEnv,ResampleMethod.BILINEAR, null);
        
        Log.d(TestProcessing.class.getSimpleName(), "JAI took "+ (System.currentTimeMillis() - now));
        
        assertTrue(jaiResampled.length == pixels.length * resamplingFactor * resamplingFactor);
        
        /////// OpenCV ///////
        
        now = System.currentTimeMillis();
        
        new OpenCVResampler().resample(raster,targetEnv,ResampleMethod.BILINEAR, null);
        
        Log.d(TestProcessing.class.getSimpleName(), "OpenCV took "+ (System.currentTimeMillis() - now));
        
        assertTrue(openCVResampled.length == pixels.length * resamplingFactor * resamplingFactor);
        
        //check a pixel
        final int mPixel = mResampled[resampledSize / 2];
        final int jaiPixel = jaiResampled[resampledSize / 2];
        
        final int mRed = (mPixel >> 16) & 0xff;
        final int mGreen = (mPixel >> 8) & 0xff;
        final int mBlue = (mPixel) & 0xff;
        
        final int jaiRed = (jaiPixel >> 16) & 0xff;
        final int jaiGreen = (jaiPixel >> 8) & 0xff;
        final int jaiBlue = (jaiPixel) & 0xff;
        
        //valid values
        assertTrue(mRed >= 0 && mRed <= 255);
        assertTrue(mGreen >= 0 && mGreen <= 255);
        assertTrue(mBlue >= 0 && mBlue <= 255);
        
        assertTrue(mRed == jaiRed);
        assertTrue(mGreen == jaiGreen);
        assertTrue(mBlue == jaiBlue);
        
        dataset.close();
	}
	
	/**
	 * tests and compares resampling methods
	 */
	public void testResampling() throws IOException {
				
		final GDALDriver driver = new GDALDriver();
		
		assertTrue(driver.canOpen(TestIO.GRAY_50M_BYTE));
		
		final GDALDataset dataset = driver.open(TestIO.GRAY_50M_BYTE);

		final int readSize = 256;
		final int targetSize = 756;
		
		final Envelope env = new Envelope(0, 256, 0, 256);

        //1. Manually ///////////////
		
		int manual = resampleManually(env, dataset, readSize, targetSize);

        //2. with gdal ////////////
        int gdal = resampleWithGDAL(env, dataset, targetSize);
       
        assertEquals(manual, gdal);
              
		
	}
	
	public int resampleWithGDAL(final Envelope env, final GDALDataset dataset, final int targetSize){
		
        final RasterQuery gdalResampleQuery = new RasterQuery(
        		env,
        		dataset.getCRS(),
        		dataset.getBands(),
        		new Envelope(0, targetSize, 0 ,targetSize),
        		dataset.getBands().get(0).datatype());
        
        final long gdalNow = System.currentTimeMillis();
        
        final Raster raster = dataset.read(gdalResampleQuery);    
		final MRenderer renderer = new MRenderer(TestIO.GRAY_50M_BYTE, true);
		
        final int[] gdalResampledPixels  = renderer.render(raster);
        assertNotNull(gdalResampledPixels);
        Log.d(TestProcessing.class.getSimpleName(), "GDAL resampling took "+ (System.currentTimeMillis() - gdalNow)+" ms");

        return gdalResampledPixels.length;
	}
	
	public int resampleManually(final Envelope env, final GDALDataset dataset,final int readSize, final int targetSize){
		
        final RasterQuery manualResamplingQuery = new RasterQuery(
        		env,
        		dataset.getCRS(),
        		dataset.getBands(),
        		new Envelope(0 , readSize, 0, readSize),
        		dataset.getBands().get(0).datatype());
        
        final long manualNow = System.currentTimeMillis();
        
        final Raster manualRaster = dataset.read(manualResamplingQuery);
		
        Log.d(TestProcessing.class.getSimpleName(), "gdal read took "+ (System.currentTimeMillis() - manualNow)+" ms");
        
        new MResampler().resample(
        		manualRaster,
        		new Envelope(0, targetSize, 0, targetSize),
        		ResampleMethod.BILINEAR, 
        		null);
        
        Log.d(TestProcessing.class.getSimpleName(), "manual resampling took "+ (System.currentTimeMillis() - manualNow)+" ms");
        
        
        
        return (int) (manualRaster.getDimension().getHeight() * manualRaster.getDimension().getWidth());
	}
	
	@SuppressWarnings("unchecked")
	public void testProcessingDrivers(){
		
		ArrayList<Resize> resizers = (ArrayList<Resize>) RasterOps.getRasterOps("org/rastertheque/processing/raster/",Resize.class);
		
		ArrayList<Render> renderers = (ArrayList<Render>) RasterOps.getRasterOps("org/rastertheque/processing/raster/", Render.class);
		
		assertNotNull(resizers);
		
		assertNotNull(renderers);
		
		//there is currently one resampler impl
		assertTrue(resizers.size() == 1);
		
		//there is one real and one test renderer impl
		assertTrue(renderers.size() == 2);
	}

}
