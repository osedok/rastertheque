package de.rooehler.rastertheque.processing.rendering;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import android.util.Log;
import de.rooehler.rastertheque.core.DataType;
import de.rooehler.rastertheque.core.Raster;
import de.rooehler.rastertheque.core.util.ByteBufferReader;
import de.rooehler.rastertheque.processing.Renderer;

public class MRenderer implements Renderer{
	
	private final static String TAG = MRenderer.class.getSimpleName();
	
	private ColorMap mColorMap;
	
	public MRenderer(final String pFilePath){
		
		final String colorMapFilePath = pFilePath.substring(0, pFilePath.lastIndexOf(".") + 1) + "sld";

		File file = new File(colorMapFilePath);

		if(file.exists()){

			this.mColorMap = SLDColorMapParser.parseColorMapFile(file);

		}
	}
	
	@Override
	public boolean hasColorMap() {
		
		return this.mColorMap != null;
	}
	
	@Override
	public int[] rgbBands(final Raster raster) {
		
		final ByteBufferReader reader = new ByteBufferReader(raster.getData().array(), ByteOrder.nativeOrder());
		final int pixelAmount = (int) raster.getDimension().getWidth() *  (int) raster.getDimension().getHeight();
		
		int [] pixels = new int[pixelAmount];
		
		double[] pixelsR = new double[pixelAmount];
		double[] pixelsG = new double[pixelAmount];
		double[] pixelsB = new double[pixelAmount];
           
		for (int i = 0; i < pixelAmount; i++) {	
			pixelsR[i] =  getValue(reader, raster.getBands().get(0).datatype());
		}
		for (int j = 0; j < pixelAmount; j++) {	
			pixelsG[j] =  getValue(reader, raster.getBands().get(1).datatype());
		}
		for (int k = 0; k < pixelAmount; k++) {	
			pixelsB[k] =  getValue(reader, raster.getBands().get(2).datatype());
		}
		
        for (int l = 0; l < pixelAmount; l++) {	
        	
        	double r = pixelsR[l];
        	double g = pixelsG[l];
        	double b = pixelsB[l];
        	
        	pixels[l] = 0xff000000 | ((((int) r) << 16) & 0xff0000) | ((((int) g) << 8) & 0xff00) | ((int) b);
        }
        
		return pixels;
	}

	/**
	 * generates an array of colored pixels for a buffer of raster pixels according to a priorly loaded ColorMap
	 * if the colorMap is not created priorly by either setting it or by placing a .sld file of the same name as the
	 * raster file in the same directory like the raster file an exception is thrown
	 * @param pBuffer the buffer to read from
	 * @param pixelAmount amount of raster pixels
	 * @param dataType the dataType of the raster pixels
	 * @return the array of color pixels
	 */
	@Override
	public int[] colormap(final Raster raster){
		
		if(mColorMap == null){
			throw new IllegalArgumentException("no colorMap available");
		}
		
		final ByteBufferReader reader = new ByteBufferReader(raster.getData().array(), ByteOrder.nativeOrder());
		final int pixelAmount = (int) raster.getDimension().getWidth() *  (int) raster.getDimension().getHeight();
		
        int[] pixels = new int[pixelAmount];
        
        for (int i = 0; i < pixelAmount; i++) {
        	
        	double d = getValue(reader, raster.getBands().get(0).datatype());

    		pixels[i] = pixelValueForColorMapAccordingToData(d);

        }

        return pixels;
	}
	
	
	
	/**
	 * generates an array of colored gray-scale pixels for a buffer of raster pixels
	 * @param pBuffer the buffer to read from
	 * @param pixelAmount amount of raster pixels
	 * @param dataType the dataType of the raster pixels
	 * @return the array of color pixels
	 */
	@Override
	public int[] grayscale(final Raster raster) {

		final int pixelAmount = (int) raster.getDimension().getWidth() *  (int) raster.getDimension().getHeight();
		int[] pixels = new int[pixelAmount];
	    double[] minMax = new double[2];
			 
		final ByteBufferReader reader = new ByteBufferReader(raster.getData().array(), ByteOrder.nativeOrder());
		
	 	getMinMax(minMax, reader, pixelAmount, raster.getBands().get(0).datatype());
	       
    	Log.d(TAG, "rawdata min "+minMax[0] +" max "+minMax[1]);
    	reader.init();

    	for (int i = 0; i < pixelAmount; i++) {
        	
        	double d = getValue(reader, raster.getBands().get(0).datatype());

    		pixels[i] = pixelValueForGrayScale(d, minMax[0], minMax[1]);

        }

        return pixels;
	}

	/**
	 * returns a (grayscale color) int value according to the @param val inside the range of @param min and @param max  
	 * @param pixel value to calculate a color for
	 * @param min value
	 * @param max value
	 * @return the calculated color value
	 */
	private int pixelValueForGrayScale(double val, double min, double max){

		final double color = (val - min) / (max - min);
		int grey = (int) (color * 256);
		return 0xff000000 | ((((int) grey) << 16) & 0xff0000) | ((((int) grey) << 8) & 0xff00) | ((int) grey);

	}
	
	/**
	 * returns a (color) int value accroding to the @param val in the colorMap
	 * @param pixel value to get a color for
	 * @return the color value according to the value
	 */
	private int pixelValueForColorMapAccordingToData(final double val){

		return mColorMap.getColorAccordingToValue(val);
	}
	/**
	 * retrieve a value from the ByteBufferReader according to its datatype
	 * actually the data is read and for a unified return type is cast to double
	 * @param reader the reader to read from
	 * @param dataType the datatype according to which the data is read
	 * @return the value of the pixel
	 */
	private double getValue(ByteBufferReader reader,final DataType dataType){

		double d = 0.0d;
		try{
			switch(dataType) {
			case CHAR:
				char _char = reader.readChar();
				d = (double) _char;
				break;
			case BYTE:
				byte _byte = reader.readByte();
				d = (double) _byte;
				break;
			case SHORT:
				short _short = reader.readShort();
				d = (double) _short;
				break;
			case INT:
				int _int = reader.readInt();
				d = (double) _int;
				break;
			case LONG:
				long _long = reader.readLong();
				d = (double) _long;
				break;
			case FLOAT:
				float _float = reader.readFloat();
				d = (double) _float;
				break;
			case DOUBLE:
				double _double =  reader.readDouble();
				d = _double;
				break;
			}
		}catch(IOException  e){
			Log.e(TAG, "error reading from byteBufferedReader");
		}

		return d;
	}
	/**
	 * iterates over the pixelsize, determining min and max value of the data in 
	 * the ByteBufferReader according to its datatype
	 * @param result array in order {min, max}
	 * @param reader the reader to read from 	
	 * @param pixelSize the amount of pixels to check
	 * @param dataType the datatype according to which the data is read
	 */
	private void getMinMax(double[] result, ByteBufferReader reader, int pixelSize, final DataType dataType){
		double max =  Double.MIN_VALUE;
		double min =  Double.MAX_VALUE;

		for (int i = 0; i < pixelSize; i++) {
			try{
				switch(dataType) {
				case CHAR:
					char _char = reader.readChar();
					if(_char > max){
						max = _char;
					}
					if(_char < min){
						min = _char;
					}
					break;
				case BYTE:
					byte _byte = reader.readByte();
					if(_byte > max){
						max = _byte;
					}
					if(_byte < min){
						min = _byte;
					}
					break;
				case SHORT:
					short _short = reader.readShort();
					if(_short > max){
						max = _short;
					}
					if(_short < min){
						min = _short;
					}
					break;
				case INT:
					int _int = reader.readInt();
					if(_int > max){
						max = _int;
					}
					if(_int < min){
						min = _int;
					}
					break;
				case LONG:
					long _long = reader.readLong();
					if(_long > max){
						max = _long;
					}
					if(_long < min){
						min = _long;
					}
					break;
				case FLOAT:
					float _float = reader.readFloat();
					if(_float > max){
						max = _float;
					}
					if(_float < min){
						min = _float;
					}
					break;
				case DOUBLE:
					double _double = reader.readDouble();
					if(_double > max){
						max = _double;
					}
					if(_double < min){
						min = _double;
					}
					break;
				}
			}catch(EOFException e){
				break;
			}catch(IOException  e){
				Log.e(TAG, "error reading from byteBufferedReader");
			}
		}
		result[0] = min;
		result[1] = max;
	}
	/**
	 * set a ColorMap to be used by this object
	 * @param pColorMap the colorMap to use
	 */
	public void setColorMap(final ColorMap pColorMap){
		this.mColorMap = pColorMap;
	}


}