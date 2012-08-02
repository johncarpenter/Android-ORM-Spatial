package com.twolinessoftware.android.jts.test.provider;

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
import com.twolinessoftware.android.orm.provider.MappedContentProvider;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class OneToOneProviderTest extends AndroidTestCase {

	private static final String LOGNAME = "OneToOneProviderTest";
	private TestDAO dao;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setIsolatedContext(); 
		dao = new TestDAO(getContext(),new TestProvider());

	}

	private void setIsolatedContext() {

		final String filenamePrefix = "test.";
		RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(new MockContext(),
				getContext(), filenamePrefix);
		
		MockContentResolver contentResolver = new MockContentResolver();
		
		IsolatedContext isolatedContext = new IsolatedContext(contentResolver,
				targetContextWrapper);

		isolatedContext.deleteDatabase("testmodel");
		
		TestProvider provider = new TestProvider();
		provider.attachInfo(isolatedContext, null);
		assertNotNull(provider);

		contentResolver.addProvider(TestProvider.PROVIDER_NAME, provider);
		
		
		TestProviderNoCascade provider2 = new TestProviderNoCascade();
		provider.attachInfo(isolatedContext, null);
		assertNotNull(provider2);
		
		contentResolver.addProvider(TestProviderNoCascade.PROVIDER_NAME, provider2);
		
		setContext(isolatedContext);

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCheckOneToOneMapping() {

		TestModel2 test2 = new TestModel2(1, 10, "model2", 2.0f); 
		TestModel test = new TestModel(1, 5, "model", 1.0f,test2);
		int id = -1;
		try {
			id = dao.save(test);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		
		TestModel testResult = dao.findById(id);
		
		assertNotNull(testResult);
		
		assertEquals(testResult.getFloatTest(), test.getFloatTest());
		assertEquals(testResult.getIntTest(), test.getIntTest());
		assertEquals(testResult.getStringTest(), test.getStringTest());
		assertNotNull(testResult.getTestModel2Test());
		TestModel2 testResult2 = testResult.getTestModel2Test(); 
		
		assertEquals(testResult2.getFloatTest(), test2.getFloatTest());
		assertEquals(testResult2.getIntTest(), test2.getIntTest());
		assertEquals(testResult2.getStringTest(), test2.getStringTest());
	
		testResult.setFloatTest(3.0f);
		testResult.setIntTest(15);
		testResult.setStringTest("model3");
		
		int id2 = -1;
		try {
			id2 = dao.save(testResult);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		assertEquals(id2,id);

	}

	
	public void testCascadeAllDelete(){
		
		TestModel2 test2 = new TestModel2(1, 10, "model2", 2.0f); 
		TestModel test = new TestModel(1, 5, "model", 1.0f,test2);
		int id = -1;
		try {
			id = dao.save(test);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		TestModel test3 = new TestModel(2, 6, "model3", 3.0f,test2);
		int id3 = -1;
		try {
			id3 = dao.save(test3);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		// Removed the test and test2 (Cascade.ALL) 
		try {
			dao.delete(id);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		TestModel testResult = dao.findById(id3);
		
		assertNotNull(testResult);
		
		assertEquals(testResult.getFloatTest(), test3.getFloatTest());
		assertEquals(testResult.getIntTest(), test3.getIntTest());
		assertEquals(testResult.getStringTest(), test3.getStringTest());
		
		assertNull(testResult.getTestModel2Test());
		
	}

	public void testCascadeNoneDelete(){
		
		TestDAONoCascade tdao = new TestDAONoCascade(getContext(),new TestProviderNoCascade());
		
		TestModel2 test2 = new TestModel2(1, 10, "model2", 2.0f); 
		TestModelNoCascade test = new TestModelNoCascade(1, 5, "model", 1.0f,test2);
		int id = -1;
		try {
			id = tdao.save(test);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		TestModelNoCascade test3 = new TestModelNoCascade(2, 6, "model3", 3.0f,test2);
		int id3 = -1;
		try {
			id3 = tdao.save(test3);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		// Removed the test and test2 (Cascade.ALL) 
		try {
			tdao.delete(id);
		} catch (DAOException e) {
			fail(e.getMessage());
		}
		
		TestModelNoCascade testResult = tdao.findById(id3);
		
		assertNotNull(testResult);
		
		assertEquals(testResult.getFloatTest(), test3.getFloatTest());
		assertEquals(testResult.getIntTest(), test3.getIntTest());
		assertEquals(testResult.getStringTest(), test3.getStringTest());
		
		assertNotNull(testResult.getTestModel2Test());
		
		TestModel2 testResult2 = testResult.getTestModel2Test(); 
		
		assertEquals(testResult2.getFloatTest(), test2.getFloatTest());
		assertEquals(testResult2.getIntTest(), test2.getIntTest());
		assertEquals(testResult2.getStringTest(), test2.getStringTest());
		
	}
}
