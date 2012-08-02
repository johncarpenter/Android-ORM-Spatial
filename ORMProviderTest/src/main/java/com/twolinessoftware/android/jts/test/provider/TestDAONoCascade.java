package com.twolinessoftware.android.jts.test.provider;

import android.content.Context;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.provider.MappedContentProvider;

public class TestDAONoCascade extends DAO<TestModelNoCascade> {

	public TestDAONoCascade(Context context,
			MappedContentProvider<TestModelNoCascade> provider) {
		super(context, provider);
	}
}