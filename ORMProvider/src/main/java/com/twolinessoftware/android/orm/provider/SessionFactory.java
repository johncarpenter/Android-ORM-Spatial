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
import java.util.HashMap;

import android.net.Uri;
import android.util.Log;

import com.twolinessoftware.android.orm.model.SpatialElement;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne;
import com.twolinessoftware.android.orm.provider.annotation.OneToOne.Cascade;

public class SessionFactory {

	private static final String LOGNAME = "SessionFactory";
	private static SessionFactory instance;

	private HashMap<Class, DatabaseInfo> mappingClasses = new HashMap<Class, DatabaseInfo>();

	public static SessionFactory getInstance() {
		if (instance == null)
			instance = new SessionFactory();

		return instance;
	}

	private SessionFactory() {
	}

	public DatabaseInfo getDatabaseInfo(Class clazz) {

		Log.d(LOGNAME,
				"Getting Details Mapping for:" + clazz.getCanonicalName());

		if (!mappingClasses.containsKey(clazz)){
			DatabaseInfo info = buildDatabaseDetails(clazz);

			mappingClasses.put(clazz, info);
		}
			

		return mappingClasses.get(clazz);
	}

	
	private TableInfo addTables( String name, Class clazz) {

		Log.d(LOGNAME, "Adding Mapping for:" + clazz.getCanonicalName());

		TableInfo tableInfo = new TableInfo();
		
		tableInfo.setClazz(clazz);
		
		tableInfo.setName(name);

		Field[] fieldList = clazz.getDeclaredFields();
					
		for (Field field : fieldList) {

			Index indexAnnot = (Index) field.getAnnotation(Index.class);

			DatabaseField fieldAnnot = (DatabaseField) field
					.getAnnotation(DatabaseField.class);

			if (fieldAnnot != null) {

				FieldInfo fi = new FieldInfo();

				fi.setName(fieldAnnot.name());

				fi.setType(field.getType());

				StringBuffer sb = new StringBuffer();
				
				sb.append(fieldAnnot.name());
				sb.append(" ");
				sb.append(getSqlNameForField(field));
				
				if (indexAnnot != null) {
					if (tableInfo.hasPrimary())
						throw new SessionInstantiationException(
								"Mapping classes cannot have multiple @INDEX calls");
				
					tableInfo.setPrimaryKey(fieldAnnot.name());
					sb.append(" primary key ");

					fi.setPrimary(true);

					fi.setAutoIncrement(indexAnnot.autoIncrement());
					
					if(indexAnnot.autoIncrement())
						sb.append("autoincrement ");

				}

				fi.setSqlCreate(sb.toString());
				
				tableInfo.addField(field.getName(), fi );

				Log.d(LOGNAME,
						"Mapping field:" + field.getName() + " to column "
								+ fieldAnnot.name() + "("
								+ fi.getType().getCanonicalName() + ")("
								+ fi.getSqlCreate() + ") in Table:"
								+ tableInfo.getName());
			}

			OneToOne oneToOneAnnot = (OneToOne) field
					.getAnnotation(OneToOne.class);
	
			if (oneToOneAnnot != null) {
				String tName = oneToOneAnnot.table();
				String joinField = oneToOneAnnot.joinField();
				Cascade cascadeType = oneToOneAnnot.cascade();
				// Add the one to one mapping
				FieldInfo fi = new FieldInfo();
				
				fi.setAutoIncrement(false);
				fi.setName(joinField);
				fi.setType(Integer.TYPE);
				fi.setSqlCreate(joinField+" INTEGER");
				
				tableInfo.addField(joinField, fi );
				
				Class claz = field.getType();
				Log.d(LOGNAME, "OneToOne Table:" + claz.getSimpleName());
				
				tableInfo.addJoinedTable(new TableJoinInfo(cascadeType ,  joinField,	addTables(tName, claz)));
			}
		}

		return tableInfo;
	}

	private DatabaseInfo buildDatabaseDetails(Class clazz)
			throws SessionInstantiationException {

		DatabaseInfo info = new DatabaseInfo();

		Database annot = (Database) clazz.getAnnotation(Database.class);

		if (annot == null)
			throw new SessionInstantiationException(
					"No @Database element included");

		info.setDatabaseName(annot.name() + ".db");
		info.setDatabaseVersion(annot.version());
		info.setPrimaryTable(addTables(annot.name(), clazz));


		return info;
	}

	private String getSqlNameForField(Field field) {
		if (field.getType() == Long.TYPE || field.getType() == Integer.TYPE)
			return "INTEGER";
		else if (field.getType() == Float.TYPE
				|| field.getType() == Double.TYPE)
			return "REAL";
		else
			return "TEXT";

	}

	public class SessionInstantiationException extends RuntimeException {

		public SessionInstantiationException(String string) {
			super(string);
		}
	}

}
