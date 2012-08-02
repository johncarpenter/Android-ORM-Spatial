package com.twolinessoftware.android.jts.test.provider;

import android.content.Context;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.provider.MappedContentProvider;

public class TestDAO extends DAO<TestModel> {

	public TestDAO(Context context,
			MappedContentProvider<TestModel> provider) {
		super(context, provider);
	}
}