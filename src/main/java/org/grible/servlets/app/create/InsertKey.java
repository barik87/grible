/*******************************************************************************
 * Copyright (c) 2013 Maksym Barvinskyi.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Maksym Barvinskyi - initial API and implementation
 ******************************************************************************/
package org.grible.servlets.app.create;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.grible.dao.DataManager;
import org.grible.dao.JsonDao;
import org.grible.model.Key;
import org.grible.model.Row;
import org.grible.model.Table;
import org.grible.model.json.KeyJson;
import org.grible.model.json.KeyType;
import org.grible.security.Security;
import org.grible.servlets.ServletHelper;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/InsertKey")
public class InsertKey extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public InsertKey() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try {
			if (Security.anyServletEntryCheckFailed(request, response)) {
				return;
			}
			String result = null;

			if (ServletHelper.isJson()) {
				int tableId = Integer.parseInt(request.getParameter("tableid"));
				int productId = Integer.parseInt(request.getParameter("product"));
				int keyOrder = Integer.parseInt(request.getParameter("keyorder")) - 1;

				JsonDao dao = new JsonDao();
				Table table = dao.getTable(tableId, productId);
				KeyJson[] keys = table.getTableJson().getKeys();
				KeyJson[] newKeys = new KeyJson[keys.length + 1];
				String[][] values = table.getTableJson().getValues();
				String[][] newValues = new String[values.length][keys.length + 1];
				for (int i = newKeys.length - 1; i >= 0; i--) {
					if (i == keyOrder) {
						newKeys[i] = new KeyJson("editme", KeyType.TEXT, 0);
						for (int j = 0; j < newValues.length; j++) {
							newValues[j][i] = "";
						}
					} else if (i < keyOrder) {
						newKeys[i] = keys[i];
						for (int j = 0; j < newValues.length; j++) {
							newValues[j][i] = values[j][i];
						}
					} else {
						newKeys[i] = keys[i - 1];
						for (int j = 0; j < newValues.length; j++) {
							newValues[j][i] = values[j][i - 1];
						}
					}
				}
				table.getTableJson().setKeys(newKeys);
				table.getTableJson().setValues(newValues);
				table.save();

				result = "success";
			} else {
				int keyId = Integer.parseInt(request.getParameter("keyid"));

				Key currentKey = DataManager.getInstance().getDao().getKey(keyId);
				int currentKeyNumber = currentKey.getOrder();
				int tableId = currentKey.getTableId();
				List<Integer> keyIds = new ArrayList<Integer>();
				List<Integer> keyNumbers = new ArrayList<Integer>();
				List<Key> keys = DataManager.getInstance().getDao().getKeys(tableId);
				for (int i = keys.size() - 1; i >= 0; i--) {
					keyIds.add(keys.get(i).getId());
					if (keys.get(i).getOrder() >= currentKeyNumber) {
						keyNumbers.add(i + 2);
					} else {
						keyNumbers.add(i + 1);
					}
				}
				DataManager.getInstance().getDao().updateKeys(keyIds, keyNumbers);
				currentKey.setOrder(currentKeyNumber);
				int newKeyId = DataManager.getInstance().getDao()
						.insertKey(tableId, "editme", currentKey.getOrder(), 0);

				List<Row> rows = DataManager.getInstance().getDao().getRows(tableId);
				List<Integer> ids = DataManager.getInstance().getDao().insertValuesEmptyWithKeyId(newKeyId, rows);

				result = newKeyId + ";" + StringUtils.join(ids, ";");
			}

			out.print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}
}