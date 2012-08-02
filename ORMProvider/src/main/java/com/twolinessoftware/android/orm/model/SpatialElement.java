/**
 * Copyright 2012 2Lines Software Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twolinessoftware.android.orm.model;

import android.util.Log;

import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SpatialElement {
	
	public static final String FIELD_MBRMINLAT = "mbr_min_lat";

	public static final String FIELD_MBRMAXLAT = "mbr_max_lat";

	public static final String FIELD_MBRMINLNG = "mbr_min_lng";

	public static final String FIELD_MBRMAXLNG = "mbr_max_lng";

	public static final String FIELD_GEOMETRYWKT = "geometry_wkt";

	private static final String LOGNAME = "SpatialElement";

	@DatabaseField(name="id")
	@Index
	private int geometryId; 
	
	@DatabaseField(name=FIELD_MBRMINLAT)
	private float mbrMinLat;
	
	@DatabaseField(name=FIELD_MBRMAXLAT)
	private float mbrMaxLat;
	
	@DatabaseField(name=FIELD_MBRMINLNG)
	private float mbrMinLng;
	
	@DatabaseField(name=FIELD_MBRMAXLNG)
	private float mbrMaxLng;
	
	@DatabaseField(name=FIELD_GEOMETRYWKT)
	private String geometryWkt; 
	
	public static SpatialElement fromPoint(float lat, float lng){
		SpatialElement se = new SpatialElement(); 
		
		Point point = new GeometryFactory().createPoint(new Coordinate(lng,lat));
		se.setGeometry(point);
		return se;
	}
	
	public SpatialElement(){}
		
	private Geometry geometry; 
	 	
	public Geometry getGeometry() {
		if(geometry == null && geometryWkt != null){
			try {
				geometry = new WKTReader().read(geometryWkt);
			} catch (ParseException e) {
				Log.e(LOGNAME, "Unable to parse geometry:"+e.getMessage());
			}
		}
		
		return geometry;
	}
	
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
		this.geometryWkt = geometry.toText();
		setMinimumBoundingRectangle();
	}
		
	private void setMinimumBoundingRectangle() {
		Envelope mbr = geometry.getEnvelopeInternal(); 
		mbrMinLng = (float) mbr.getMinX();
		mbrMaxLng = (float) mbr.getMaxX();
		mbrMinLat = (float) mbr.getMinY();
		mbrMaxLat = (float) mbr.getMaxY();
	}

	public Envelope getMinimumBoundingRectangle(){
		return new Envelope(mbrMinLng, mbrMaxLng, mbrMinLat, mbrMaxLat);
	}
	
}
