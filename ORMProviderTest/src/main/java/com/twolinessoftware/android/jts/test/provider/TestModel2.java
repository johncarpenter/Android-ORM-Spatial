package com.twolinessoftware.android.jts.test.provider;

import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;

public class TestModel2{
	public static final String FIELD_INTTEST = "inttest";

	public static final String FIELD_STRINGTEST = "stringtest";

	public static final String FIELD_FLOATTEST = "floattest";
	
	public static final String FIELD_TESTMODEL2 = "testmodel";

	public TestModel2(){}
	
	public TestModel2(int id, int intTest, String stringTest, float floatTest) {
		super();
		this.id = id;
		this.intTest = intTest;
		this.stringTest = stringTest;
		this.floatTest = floatTest;
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(); 
		sb.append("TestModel2-");
		sb.append(FIELD_INTTEST+":"+intTest);
		sb.append(FIELD_FLOATTEST+":"+floatTest);
		sb.append(FIELD_STRINGTEST+":"+stringTest);
		return sb.toString(); 
	}

	
}
