package de.rooehler.rastertheque.processing.resampling.raw;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Environment;
import android.util.Log;

import com.vividsolutions.jts.geom.Envelope;

import de.rooehler.rastertheque.core.DataType;
import de.rooehler.rastertheque.core.Raster;
import de.rooehler.rastertheque.core.util.ByteBufferReader;
import de.rooehler.rastertheque.processing.RawResampler;

public class OpenCVRawResampler implements RawResampler {

	@Override
	public void resample(Raster raster,Envelope dstDimension, ResampleMethod method) {
	
		final int srcWidth = (int) raster.getBoundingBox().getWidth();
		final int srcHeight = (int) raster.getBoundingBox().getHeight();
		
		if(Double.compare(srcWidth,  dstDimension.getWidth()) == 0 &&
		   Double.compare(srcHeight, dstDimension.getHeight()) == 0){
			return;
		}
		
		final int dstWidth = (int) dstDimension.getWidth();
		final int dstHeight = (int) dstDimension.getHeight();
		
		int i = 0;
		switch (method) {
		case NEARESTNEIGHBOUR:
			i = Imgproc.INTER_NEAREST;
			break;
		case BILINEAR:
			i = Imgproc.INTER_LINEAR;
			break;
		case BICUBIC:
			i = Imgproc.INTER_CUBIC;
			break;
		}	
		long now = System.currentTimeMillis();
		
		final Mat srcMat = matAccordingToDatatype(
					raster.getBands().get(0).datatype(),
					raster.getData(),
					(int) raster.getBoundingBox().getWidth(),
					(int) raster.getBoundingBox().getHeight());

		Log.d(OpenCVRawResampler.class.getSimpleName(), "creating mat took : "+(System.currentTimeMillis() - now));

		Mat dstMat = new Mat();
		
		Imgproc.resize(srcMat, dstMat, new Size(dstWidth, dstHeight), 0, 0, i);
		Log.d(OpenCVRawResampler.class.getSimpleName(), "resizing  took : "+(System.currentTimeMillis() - now));
		
		final int bufferSize = dstWidth * dstHeight * raster.getBands().size() * raster.getBands().get(0).datatype().size();
		
		raster.setDimension(dstDimension);
		
		raster.setData(bytesFromMat(
				dstMat,
				raster.getBands().get(0).datatype(),
				bufferSize));
		Log.d(OpenCVRawResampler.class.getSimpleName(), "reconverting to bytes took : "+(System.currentTimeMillis() - now));
		
	}
	
	/**
	 * converts the bytes from a raster into an OpenCV Mat 
	 * having width * height cells of datatype according to the rasters datatype
	 * @param type the datatype of the raster
	 * @param bytes the data
	 * @param width the width of the raster
	 * @param height the height of the raster
	 * @return the Mat object containing the data in the given format
	 * @throws IOException
	 */
	public Mat matAccordingToDatatype(DataType type, final ByteBuffer buffer, final int width, final int height) {
		
		//dataypes -> http://answers.opencv.org/question/5/how-to-get-and-modify-the-pixel-of-mat-in-java/
		
		switch(type){
		case BYTE:
			
			Mat byteMat = new Mat(height, width, CvType.CV_8U);
			byteMat.put(0, 0, buffer.array());
			//for direct bytebuffer
			//byteMat.put(0, 0, Arrays.copyOfRange(buffer.array(),0, width * height));
			return byteMat;
			
		case CHAR:
			
			Mat charMat = new Mat(height, width, CvType.CV_16UC1);
			
			try{
				ByteBufferReader charReader = new ByteBufferReader(buffer.array(), ByteOrder.nativeOrder());
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width ; x++){
						charMat.put(y, x, charReader.readChar());
					}
				}
			} catch (IOException e) {
				Log.e(OpenCVRawResampler.class.getSimpleName(), "Error creating char mat from raster",e);
			}
			
			return charMat;
			
		case DOUBLE:
			
			Mat doubleMat = new Mat(height, width, CvType.CV_64FC1);
			
			final double[] doubles = new double[height * width];
		    
		    buffer.asDoubleBuffer().get(doubles);

		    doubleMat.put(0,0,doubles);
			
			return doubleMat;

		case FLOAT:
			
			Mat floatMat = new Mat(height, width, CvType.CV_32FC1);
						
			final float[] dst = new float[height * width];
			
		    buffer.asFloatBuffer().get(dst);

		    floatMat.put(0,0,dst);
		    
			return floatMat;

		case INT:
			
			Mat intMat = new Mat(height, width, CvType.CV_32SC1);
			
			final int[] ints = new int[height * width];
		    
		    buffer.asIntBuffer().get(ints);

		    intMat.put(0,0,ints);
			
			return intMat;
			
		case LONG:
			
			Mat longMat = new Mat(height, width, CvType.CV_32SC2);
			try{
				ByteBufferReader longReader = new ByteBufferReader(buffer.array(), ByteOrder.nativeOrder());

				for(int y = 0; y < height; y++){
					for(int x = 0; x < width ; x++){
						longMat.put(y, x, longReader.readLong());
					}
				}
			} catch (IOException e) {
				Log.e(OpenCVRawResampler.class.getSimpleName(), "Error creating long mat from raster",e);
			}
			
			return longMat;
			
		case SHORT:
			
			Mat shortMat = new Mat(height, width, CvType.CV_16SC1);
			
			final short[] shorts = new short[height * width];
		    
		    buffer.asShortBuffer().get(shorts);

		    shortMat.put(0,0,shorts);
			
			return shortMat;
			
		}
		throw new IllegalArgumentException("Invalid datatype");
	}
	/**
	 * converts a Mat (with likely the result of some operation) 
	 * into a ByteBuffer according to the datatype
	 * @param mat the Mat to convert
	 * @param type the datatype of the data
	 * @param bufferSize the size of the ByteBuffer to create
	 * @return a ByteBuffer containing the data of the Mat
	 */
	public ByteBuffer bytesFromMat(Mat mat, DataType type, int bufferSize){
		

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		buffer.order(ByteOrder.nativeOrder());
		final int height = (int) mat.size().height;
		final int width = (int) mat.size().width;
		
		switch(type){
		case BYTE:
			mat.get(0, 0, buffer.array());
			
			break;
			
		case CHAR:
			
			//TODO test char
			int[] _char = new int[1];
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width ; x++){
					
					mat.get(y,x,_char);
					buffer.putChar( (char) _char[0]);
				}
			}
			break;
		case DOUBLE:
			
			final double[] doubles = new double[bufferSize / 8];
			mat.get(0, 0, doubles);
			buffer.asDoubleBuffer().put(doubles);

			break;
		case FLOAT:

			final float[] dst = new float[bufferSize / 4];
			mat.get(0, 0, dst);
			buffer.asFloatBuffer().put(dst);
			
			break;

		case INT:
			
			final int[] ints = new int[bufferSize / 4];
			mat.get(0, 0, ints);
			buffer.asIntBuffer().put(ints);
						
			break;
			
		case LONG:
			
			//TODO test long
			//there seems to be no long support in OpenCV
			int[] _long = new int[1];
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width ; x++){
					
					mat.get(y,x,_long);
					buffer.putLong((long)_long[0]);
				}
			}
			break;
			
		case SHORT:
			
			final short[] shorts = new short[bufferSize / 2];
			mat.get(0, 0, shorts);
			buffer.asShortBuffer().put(shorts);
			
			break;			
		}
		
		return buffer;
	}

	
	@SuppressWarnings("unused")
	private static void testDirectRead(){
		
		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(root + "/rastertheque/HN+24_900913.tif");    
		File writefile = new File(root + "/rastertheque/dem_openCV.tif");    

		Mat m = Highgui.imread(file.getAbsolutePath());

		int depth = m.depth();
		int channels = m.channels();

		Mat dst = new Mat();

		Imgproc.resize(m, dst, new Size(),2,2, Imgproc.INTER_LINEAR);

		Highgui.imwrite(writefile.getAbsolutePath(), dst);
	    
	}

}
