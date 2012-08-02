package com.twolinessoftware.android.jts.test.provider.spatial;

import com.twolinessoftware.android.orm.model.SpatialElement;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;


@Database(name = "spatialtestmodel", version = 2)
public class SpatialTestModel{
	public static final String FIELD_INTTEST = "inttest";

	public static final String FIELD_STRINGTEST = "stringtest";

	public static final String FIELD_FLOATTEST = "floattest";
	
	public static final String FIELD_TESTMODEL2 = "testmodel2";

	
	public SpatialTestModel(){}
	
	public SpatialTestModel(int id, int intTest, String stringTest, float floatTest,
			TestModel2 testModel2Test) {
		super();
		this.id = id;
		this.intTest = intTest;
		this.stringTest = stringTest;
		this.floatTest = floatTest;
		this.setTestModel2Test(testModel2Test);
	}



	@DatabaseField(name="id")
	@Index
	private int id;
	
	@DatabaseField(name=FIELD_INTTEST)
	private int intTest; 
	
	@DatabaseField(name=FIELD_STRINGTEST)
	private String stringTest; 
	
	@DatabaseField(name=FIELD_FLOATTEST)
	private float floatTest;
	
	@OneToOne(joinField = "testModelId", table = FIELD_TESTMODEL2)
	private TestModel2 testModel2Test; 
	
	@OneToOne(joinField = "geometryId", table = "geometry")
	private SpatialElement spatialElement;
	
	
	public TestModel2 getTestModel2Test() {
		return testModel2Test;
	}

	public void setTestModel2Test(TestModel2 testModel2Test) {
		this.testModel2Test = testModel2Test;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIntTest() {
		return intTest;
	}

	public void setIntTest(int intTest) {
		this.intTest = intTest;
	}

	public String getStringTest() {
		return stringTest;
	}

	public void setStringTest(String stringTest) {
		this.stringTest = stringTest;
	}

	public float getFloatTest() {
		return floatTest;
	}

	public void setFloatTest(float floatTest) {
		this.floatTest = floatTest;
	}

	public SpatialElement getSpatialElement() {
		return spatialElement;
	}

	public void setSpatialElement(SpatialElement spatialElement) {
		this.spatialElement = spatialElement;
	}
	
	
	
}
