package com.twolinessoftware.android.jts.test.provider;

import android.content.UriMatcher;
import android.net.Uri;

import com.twolinessoftware.android.orm.provider.MappedContentProvider;

public class TestProvider extends MappedContentProvider<TestModel>{

	public static final String PROVIDER_NAME = "com.twolinessoftware.testprovider";
	
	public static final Uri CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME );
	
	public static final String RETURN_TYPE ="vnd.android.cursor.dir/vnd.twolinessoftware";
	
	private static final int TESTMODEL = 0;
	private static final int TESTMODEL2 = 1;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "testmodel",
				TESTMODEL);
		uriMatcher.addURI(PROVIDER_NAME, "testmodel2",
				TESTMODEL2);
	}
	
	@Override
	public Uri getBaseContentUri() {
		return CONTENT_URI;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case TESTMODEL:
			return RETURN_TYPE+".testmodel";
		case TESTMODEL2:
			return RETURN_TYPE+".testmodel2";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		} 
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

}
