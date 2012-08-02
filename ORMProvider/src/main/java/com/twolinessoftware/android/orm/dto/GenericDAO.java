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

import java.util.List;

public interface GenericDAO<T> {

	public abstract int save(T t) throws DAOException;

	/**
	 * 
	 * @TODO needs to handle different types of Cascades
	 * 
	 * 
	 * @param id
	 * @return
	 * @throws DAOException 
	 */
	public abstract boolean delete(long id) throws DAOException;

	public abstract T findById(long id);

	public abstract List<T> findByCriteria(String[] fields, String where,
			String[] whereArgs, String sort);

	public abstract List<T> findAll();

}