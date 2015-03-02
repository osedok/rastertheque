package de.rooehler.rastertheque.util;

public class Constants {
	
	
	public static final int EARTH_RADIUS = 6378137;
	
	//public static final int DEGREE_IN_METERS_AT_EQUATOR = 111195;
	
	public static final String EPSG_3857 = "PROJCS[\"WGS 84 / Pseudo-Mercator\","+
			  "GEOGCS[\"WGS 84\","+
			      "DATUM[\"WGS_1984\","+
			          "SPHEROID[\"WGS 84\",6378137,298.257223563,"+
			              "AUTHORITY[\"EPSG\",\"7030\"]],"+
			          "AUTHORITY[\"EPSG\",\"6326\"]],"+
			      "PRIMEM[\"Greenwich\",0,"+
			          "AUTHORITY[\"EPSG\",\"8901\"]],"+
			      "UNIT[\"degree\",0.0174532925199433,"+
			          "AUTHORITY[\"EPSG\",\"9122\"]],"+
			      "AUTHORITY[\"EPSG\",\"4326\"]],"+
			  "PROJECTION[\"Mercator_1SP\"],"+
			  "PARAMETER[\"central_meridian\",0],"+
			  "PARAMETER[\"scale_factor\",1],"+
			  "PARAMETER[\"false_easting\",0],"+
			  "PARAMETER[\"false_northing\",0],"+
			  "UNIT[\"metre\",1,"+
			      "AUTHORITY[\"EPSG\",\"9001\"]],"+
			  "AXIS[\"X\",EAST],"+
			  "AXIS[\"Y\",NORTH],"+
			  "EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"],"+
			  "AUTHORITY[\"EPSG\",\"3857\"]]";
				
	public static final String EPSG_4326 = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
	
	public static final String EPSG_900913 = EPSG_3857;
	
	public static final int COLORMAP_ENTRY_THRESHOLD = 65536;
}