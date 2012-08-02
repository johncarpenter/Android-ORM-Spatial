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
package com.twolinessoftware.android.orm.provider;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.twolinessoftware.android.orm.provider.DatabaseInfo.TableMapping;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne.Cascade;

public abstract class MappedContentProvider<T> extends AbstractContentProvider {

	private static final String LOGNAME = "MappedContentProvider";

	private SQLiteDatabase db;

	private static final String NULLHACK = "null";

	@Override
	public boolean onCreate() {

		DatabaseInfo databaseInfo = SessionFactory.getInstance()
				.getDatabaseInfo(getSuperClass());

		DatabaseHelper dbHelper = new DatabaseHelper(getContext(), databaseInfo);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			db = null;
			Log.e(LOGNAME, "Database Opening exception:" + e.getMessage());
		}

		return (db == null) ? false : true;
	}

	private Class<T> getSuperClass() {
		ParameterizedType superclass = (ParameterizedType) getClass()
				.getGenericSuperclass();

		Class<T> clazz = (Class<T>) ((ParameterizedType) superclass)
				.getActualTypeArguments()[0];

		return clazz;

	}

	private DatabaseInfo getDatabaseInfo() {
		return SessionFactory.getInstance().getDatabaseInfo(getSuperClass());
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private DatabaseInfo databaseInfo;

		public DatabaseHelper(Context context, DatabaseInfo databaseInfo) {
			super(context, databaseInfo.getDatabaseName(), null, databaseInfo
					.getDatabaseVersion());
			this.databaseInfo = databaseInfo;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			Log.d(LOGNAME, "Creating New Database");

			TableInfo tableInfo = databaseInfo.getPrimaryTable();

			createTable(db, tableInfo);

		}

		private void createTable(SQLiteDatabase db, TableInfo tableInfo) {
			String tableCreate = createTableQuery(tableInfo);
			Log.d(LOGNAME, "Create:" + tableCreate);
			db.execSQL(tableCreate);

			for (TableJoinInfo tableJoinInfo:tableInfo.getAllJoinedTables()){
				createTable(db, tableJoinInfo.getTableInfo());
			
				if(tableJoinInfo.getCascadeType()==Cascade.All)
					addTriggers(db,tableInfo,tableJoinInfo);
			}
		}

				
		private void addTriggers(SQLiteDatabase db,TableInfo originTable, TableJoinInfo tableJoinInfo) {

			TableInfo tableInfo = tableJoinInfo.getTableInfo(); 
			
			String databaseTrigger =  "CREATE TRIGGER fkd_"+tableInfo.getName()+"_"+originTable.getName()
				       +" BEFORE DELETE ON "+originTable.getName()
				       +" FOR EACH ROW BEGIN "
				       +" DELETE FROM "+tableInfo.getName()+" WHERE "+tableInfo.getPrimaryKey()+" = OLD."+tableJoinInfo.getJoinField()+"; "
				       +" UPDATE "+originTable.getName()+" SET "+tableJoinInfo.getJoinField() +"= 0 WHERE "+tableJoinInfo.getJoinField()+"= OLD."+tableJoinInfo.getJoinField()+";"
				       +" END;";
			
			db.execSQL(databaseTrigger);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOGNAME, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");

			TableInfo tableInfo = databaseInfo.getPrimaryTable();

			dropTable(db, tableInfo);

			onCreate(db);
		}

		private void dropTable(SQLiteDatabase db, TableInfo tableInfo) {
			db.execSQL("DROP TABLE IF EXISTS " + tableInfo.getName());

			for (TableInfo info : tableInfo.getJoinedTables())
				dropTable(db, info);
		}

		private String createTableQuery(TableInfo tableInfo) {

			StringBuilder sb = new StringBuilder("CREATE TABLE ").append(
					tableInfo.getName()).append("(");

			for (String columnName : tableInfo.getFields().keySet()) {
				FieldInfo fieldInfo = tableInfo.getFields().get(columnName);
				sb.append(fieldInfo.getSqlCreate()).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(");");
			return sb.toString();
		}

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		Log.d(LOGNAME, "Starting Query");

		DatabaseInfo info = getDatabaseInfo();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(info.fromJoinedTablesString());

		if (projection == null) {
			ArrayList<String> projectionList = new ArrayList<String>();

			HashMap<String, String> projectionMap = info.getFullProjectionMap();
			for (String fullName : projectionMap.keySet()) {
				String fieldName = projectionMap.get(fullName);

				projectionList.add(fieldName + " " + fullName);

			}
			projection = projectionList.toArray(new String[projectionList
					.size()]);
		}

		Log.d(LOGNAME,
				"Running:"
						+ SQLiteQueryBuilder.buildQueryString(false,
								qb.getTables(), projection, selection, null,
								null, sort, null));

		// Apply the query to the underlying database.
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sort);

		Log.d(LOGNAME, "Query results:" + c.getCount());

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		DatabaseInfo info = getDatabaseInfo();

		String table;
		if (uri.getPathSegments() == null || uri.getPathSegments().size() == 0)
			table = info.getPrimaryTable().getName();
		else
			table = uri.getPathSegments().get(0);

		int count;
		count = db.update(table, values, where, whereArgs);

		Log.d(LOGNAME, "Updating " + count + " records");

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Uri insert(Uri _uri, ContentValues cv) {

		DatabaseInfo info = getDatabaseInfo();

		String table;
		if (_uri.getPathSegments() == null
				|| _uri.getPathSegments().size() == 0)
			table = info.getPrimaryTable().getName();
		else
			table = _uri.getPathSegments().get(0);

		// Insert the new row, will return the row number if successful.
		long rowID = db.insert(table, NULLHACK, cv);

		Log.d(LOGNAME, "Inserting record table:" + table + " id:" + rowID);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {

			Uri uri = ContentUris.withAppendedId(_uri, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + _uri + ":"
				+ rowID);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		DatabaseInfo info = getDatabaseInfo();

		String table;
		if (uri.getPathSegments() == null
				|| uri.getPathSegments().size() == 0)
			table = info.getPrimaryTable().getName();
		else
			table = uri.getPathSegments().get(0);
		
		Log.d(LOGNAME, "Deleting record from table:" + table + " where:" + where);
		
		int count = db.delete(table, where, whereArgs);

		Log.d(LOGNAME, "Deleted" + count + " records");

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
