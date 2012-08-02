package com.twolinessoftware.android.jts.test.provider.spatial;

import android.content.Context;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.provider.MappedContentProvider;

public class SpatialTestDAO extends DAO<SpatialTestModel> {

	public SpatialTestDAO(Context context,
			MappedContentProvider<SpatialTestModel> provider) {
		super(context, provider);
	}
}