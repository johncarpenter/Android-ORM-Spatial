package com.twolinessoftware.android.jts.test.provider.spatial;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.util.Log;

import com.twolinessoftware.android.orm.dto.DAOException;
import com.twolinessoftware.android.orm.model.SpatialElement;
import com.twolinessoftware.android.orm.provider.MappedContentProvider;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SpatialOneToOneProviderTest extends AndroidTestCase {

	private static final String LOGNAME = "SpatialOneToOneProviderTest";
	private SpatialTestDAO dao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setIsolatedContext(); 
		dao = new SpatialTestDAO(getContext(),new SpatialTestProvider());

	}

	private void setIsolatedContext() {

		final String filenamePrefix = "test.";
		RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(new MockContext(),
				getContext(), filenamePrefix);
		
		MockContentResolver contentResolver = new MockContentResolver();
		
		IsolatedContext isolatedContext = new IsolatedContext(contentResolver,
				targetContextWrapper);

		SpatialTestProvider provider = new SpatialTestProvider();
		provider.attachInfo(isolatedContext, null);
		assertNotNull(provider);

		contentResolver.addProvider(SpatialTestProvider.PROVIDER_NAME, provider);
		
		setContext(isolatedContext);

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCheckOneToOneMapping() {

		TestModel2 test2 = new TestModel2(1, 10, "model2", 2.0f); 
		SpatialTestModel test = new SpatialTestModel(1, 5, "model", 1.0f,test2);
		
		SpatialElement se = SpatialElement.fromPoint(51, -114);
		test.setSpatialElement(se);
		
		int id = -1;
		try {
			id = dao.save(test);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		
		SpatialTestModel testResult = dao.findById(id);
		
		// Test Result
		assertNotNull(testResult);
		assertEquals(testResult.getFloatTest(), test.getFloatTest());
		assertEquals(testResult.getIntTest(), test.getIntTest());
		assertEquals(testResult.getStringTest(), test.getStringTest());
		
		// Test Spatial Element
		assertNotNull(testResult.getSpatialElement());
		SpatialElement se2 = testResult.getSpatialElement();
		Log.d(LOGNAME, "Spatial Element:"+se2.getGeometry().toText());
		assertEquals(se.getGeometry().toText(),se2.getGeometry().toText() );
		
		
		
		// Test OneToOne Mapping
		assertNotNull(testResult.getTestModel2Test());
		TestModel2 testResult2 = testResult.getTestModel2Test(); 
		
		assertEquals(testResult2.getFloatTest(), test2.getFloatTest());
		assertEquals(testResult2.getIntTest(), test2.getIntTest());
		assertEquals(testResult2.getStringTest(), test2.getStringTest());
	
		
	}

	
	
}
