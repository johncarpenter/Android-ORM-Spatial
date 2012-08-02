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
package com.twolinessoftware.android.orm.dto;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.android.orm.provider.DatabaseInfo;
import com.twolinessoftware.android.orm.provider.SessionFactory;
import com.twolinessoftware.android.orm.provider.TableInfo;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;

public abstract class DAO<T> implements GenericDAO<T> {

	private static final String LOGNAME = "DAO";

	private AbstractContentProvider<T> provider;
	private Context context;

	public DAO(Context context, AbstractContentProvider<T> provider) {
		this.provider = provider;
		this.context = context;
	}

	protected AbstractContentProvider<T> getProvider() {
		return provider;
	}

	protected Context getContext() {
		return context;
	}

	private int getIndexValue(Object t) {
		Field[] fieldList = t.getClass().getDeclaredFields();
		try {

			for (Field field : fieldList) {
				Index indexAnnot = (Index) field.getAnnotation(Index.class);
				if (indexAnnot != null)
					return (Integer) getValueFromObject(field, t);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to access index value in "
							+ t.getClass().getSimpleName() + " Class");
		}

		throw new IllegalArgumentException(
				"No Mapping Element with @Index found in class "
						+ t.getClass().getSimpleName());

	}

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#save(T)
	 */
	public int save(T t) throws DAOException {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				t.getClass());

		TableInfo primaryTable = info.getPrimaryTable();

		try {
			return saveIterative(primaryTable, t);
		} catch (Exception e) {
			throw new DAOException("Unable to save object:Cause:"
					+ e.getMessage());
		}

	}

	private int saveIterative(TableInfo tableInfo, Object t)
			throws DAOException {

		HashMap<String, Integer> linkedFields = new HashMap<String, Integer>();

		Class elementClass = t.getClass();

		Field[] fieldList = elementClass.getDeclaredFields();

		for (Field field : fieldList) {
			// Check for one to one mapping
			OneToOne oneToOneAnnot = (OneToOne) field
					.getAnnotation(OneToOne.class);

			if (oneToOneAnnot != null) {
				TableInfo info = tableInfo
						.getJoinedTable(oneToOneAnnot.table());

				Object object = getValueFromObject(field, t);

				// @TODO check for nullable
				if (object != null) {
					int result = saveIterative(info, object);
					linkedFields.put(oneToOneAnnot.joinField(), result);
				}
			}
		}

		return save(tableInfo, linkedFields, t);

	}

	private int save(TableInfo info, HashMap<String, Integer> linkedFields,
			Object t) {

		ContentValues values = null;
		try {
			values = toContentValues(t);
		} catch (Exception e) {
			Log.e(LOGNAME, "Unable to map to contentvalues:" + e.getMessage());
			return -1;
		}

		for (String field : linkedFields.keySet())
			values.put(field, linkedFields.get(field));

		long id = getIndexValue(t);

		String where = info.getName() + "."
				+ info.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		Uri appendedProvider = Uri.parse(provider.getBaseContentUri()
				.toString() + "/" + info.getName());

		if (id == 0
				|| context.getContentResolver().update(appendedProvider,
						values, where, whereArgs) == 0) {

			Uri insertUri = context.getContentResolver().insert(
					appendedProvider, values);
			if (insertUri != null)
				id = (int) ContentUris.parseId(insertUri);
		}

		return (int) id;
	}

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#delete(long)
	 */
	public boolean delete(long id) throws DAOException {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		TableInfo primaryTable = info.getPrimaryTable(); 

		String where = primaryTable.getName() + "."
				+ primaryTable.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		
		if (context.getContentResolver().delete(
				provider.getBaseContentUri(), where, whereArgs) == 0)
			return false;
		
		return true; 
	}
	
	

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#findById(long)
	 */
	public T findById(long id) {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		TableInfo primaryTable = info.getPrimaryTable();

		String where = primaryTable.getName() + "."
				+ primaryTable.getPrimaryKey() + " = " + id;

		String[] whereArgs = null;

		T element = null;

		Cursor cursor = null;
		try {

			cursor = context.getContentResolver().query(
					provider.getBaseContentUri(), null, where, whereArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				element = fromCursor(cursor);
			}

		} catch (Exception e) {
			throw new SQLException("Unable to map class:"
					+ Log.getStackTraceString(e));
		} finally {
			if (cursor != null)
				cursor.close();
		}

		return element;

	}

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#findByCriteria(java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public List<T> findByCriteria(String[] fields, String where,
			String[] whereArgs, String sort) {

		List<T> elements = new ArrayList<T>();

		Cursor cursor = null;

		try {

			cursor = context.getContentResolver().query(
					provider.getBaseContentUri(), fields, where, whereArgs,
					sort);

			if (cursor != null && cursor.moveToFirst()) {

				do {
					elements.add(fromCursor(cursor));
				} while (cursor.moveToNext());
			}

		} catch (Exception e) {
			throw new SQLException("Unable to map class:" + e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();
		}

		return elements;
	}

	/* (non-Javadoc)
	 * @see com.twolinessoftware.android.jts.dto.GenericDAO#findAll()
	 */
	public List<T> findAll() {
		return findByCriteria(null, null, null, null);
	}

	protected ContentValues toContentValues(Object t) throws DAOException {

		ContentValues values = new ContentValues();

		Class elementClass = t.getClass();

		Field[] fieldList = elementClass.getDeclaredFields();

		for (Field field : fieldList) {
			DatabaseField fieldAnnot = (DatabaseField) field
					.getAnnotation(DatabaseField.class);

			Index indexAnnot = (Index) field.getAnnotation(Index.class);

			boolean exclude = (indexAnnot != null && indexAnnot.autoIncrement());

			if (fieldAnnot != null && !exclude) {
				String columnName = fieldAnnot.name();

				Object value = getValueFromObject(field, t);

				if (field.getType() == Long.TYPE)
					values.put(columnName, (Long) value);
				else if (field.getType() == Integer.TYPE)
					values.put(columnName, (Integer) value);
				else if (field.getType() == Float.TYPE)
					values.put(columnName, (Float) value);
				else if (field.getType() == Double.TYPE)
					values.put(columnName, (Double) value);
				else if (field.getType() == Boolean.TYPE)
					values.put(columnName, ((Boolean) value) ? 1 : 0);
				else
					values.put(columnName, (String) value.toString());

				Log.d(LOGNAME, "ContentValue field:" + field.getName()
						+ " to column " + columnName);
			}

		}

		return values;
	}

	protected T fromCursor(Cursor cursor) throws IllegalAccessException,
			InstantiationException {

		DatabaseInfo info = SessionFactory.getInstance().getDatabaseInfo(
				getSuperClass());

		Class elementClass = getSuperClass();

		T element = getSuperClass().newInstance();

		element = (T) populateObject(info.getPrimaryTable().getName(),
				elementClass, cursor);

		return element;
	}

	private Object populateObject(String tableName, Class clazz, Cursor cursor)
			throws InstantiationException, IllegalAccessException {

		Object element = clazz.newInstance();
		Field[] fieldList = clazz.getDeclaredFields();

		for (Field field : fieldList) {
			DatabaseField fieldAnnot = (DatabaseField) field
					.getAnnotation(DatabaseField.class);

			boolean accessible = field.isAccessible();

			if (!accessible)
				field.setAccessible(true);
			
			if (fieldAnnot != null) {

				String sqlFieldName = tableName + "" + fieldAnnot.name();

				if (field.getType() == Long.TYPE)
					field.setLong(element,
							cursor.getLong(cursor.getColumnIndex(sqlFieldName)));
				else if (field.getType() == Integer.TYPE)
					field.setInt(element,
							cursor.getInt(cursor.getColumnIndex(sqlFieldName)));
				else if (field.getType() == Float.TYPE)
					field.setFloat(element, cursor.getFloat(cursor
							.getColumnIndex(sqlFieldName)));
				else if (field.getType() == Double.TYPE)
					field.setDouble(element, cursor.getDouble(cursor
							.getColumnIndex(sqlFieldName)));
				else
					field.set(element, cursor.getString(cursor
							.getColumnIndex(sqlFieldName)));

			}
			OneToOne oneToOneAnnot = (OneToOne) field
					.getAnnotation(OneToOne.class);
			if (oneToOneAnnot != null) {
				
				String tName = oneToOneAnnot.table();
				String joinField = oneToOneAnnot.joinField();
				
				int joinedId = cursor.getInt(cursor.getColumnIndex(tableName+joinField));
		
				if(joinedId != 0)
					field.set(element,
							populateObject(tName, field.getType(), cursor));
				
			}
			field.setAccessible(accessible);

		}
		return element;
	}

	private Class<T> getSuperClass() {
		ParameterizedType superclass = (ParameterizedType) getClass()
				.getGenericSuperclass();

		Class<T> clazz = (Class<T>) ((ParameterizedType) superclass)
				.getActualTypeArguments()[0];

		return clazz;

	}

	private Object getValueFromObject(Field field, Object t)
			throws DAOException {
		Object object = null;

		boolean accessible = field.isAccessible();

		if (!accessible)
			field.setAccessible(true);

		try {
			object = field.get(t);
		} catch (IllegalArgumentException e) {
			throw new DAOException("Unable to access member:" + field.getName()+":"+Log.getStackTraceString(e));
		} catch (IllegalAccessException e) {
			throw new DAOException("Unable to access member:" + field.getName()+":"+Log.getStackTraceString(e));
		}

		field.setAccessible(accessible);


		return object;
	}

}
