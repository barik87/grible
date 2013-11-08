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
package org.grible.servlets.ui.panels;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.grible.dao.Dao;
import org.grible.model.Key;
import org.grible.model.Row;
import org.grible.model.Table;
import org.grible.model.TableType;
import org.grible.model.Value;
import org.grible.servlets.ServletHelper;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/GetTableValues")
public class GetTableValues extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private boolean showUsage;
	private TableType tableType;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetTableValues() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		try {
			StringBuilder responseHtml = new StringBuilder();
			int tableId = Integer.parseInt(request.getParameter("id"));
			Table table = Dao.getTable(tableId);
			tableType = table.getType();
			showUsage = table.isShowUsage();

			List<Key> keys = Dao.getKeys(tableId);
			writeKeys(responseHtml, keys);

			List<Row> rows = Dao.getRows(tableId);
			ArrayList<ArrayList<Value>> values = new ArrayList<ArrayList<Value>>();
			for (Row row : rows) {
				values.add(Dao.getValues(row));
			}
			writeValues(responseHtml, values);
			out.print(responseHtml.toString());
		} catch (Exception e) {
			out.print(e.getLocalizedMessage());
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}

	private void writeKeys(StringBuilder responseHtml, List<Key> keys) {
		responseHtml.append("<div class=\"table-row key-row\">");
		if (tableType == TableType.STORAGE || tableType == TableType.TABLE || tableType == TableType.ENUMERATION) {
			responseHtml.append("<div class=\"table-cell ui-cell index-header-cell\">Index</div>");
		}
		for (Key key : keys) {
			responseHtml.append("<div id=\"").append(key.getId()).append("\" key-order=\"").append(key.getOrder())
					.append("\" class=\"table-cell ui-cell key-cell\">").append(key.getName()).append("</div>");
		}
		if (showUsage) {
			responseHtml.append("<div class=\"table-cell ui-cell info-key-cell\">Used in tables</div>");
			responseHtml.append("<div class=\"table-cell ui-cell info-key-cell\">Used in storages</div>");
		}
		responseHtml.append("</div>");
	}

	private void writeValues(StringBuilder responseHtml, ArrayList<ArrayList<Value>> values) throws SQLException {
		int i = 1;
		String storageCell = "";
		String enumCell = "";
		for (ArrayList<Value> valuesRow : values) {
			responseHtml.append("<div class=\"table-row value-row\">");
			if (tableType == TableType.STORAGE || tableType == TableType.TABLE || tableType == TableType.ENUMERATION) {
				responseHtml.append("<div id=\"").append(valuesRow.get(0).getRowId())
						.append("\" class=\"table-cell ui-cell index-cell\">").append(i++).append("</div>");
			}
			for (Value value : valuesRow) {
				storageCell = (value.isStorage()) ? " storage-cell" : "";
				enumCell = (ServletHelper.isEnumValue(value)) ? " enum-cell" : "";
				responseHtml.append("<div id=\"").append(value.getId()).append("\" keyid=\"").append(value.getKeyId())
						.append("\" rowid=\"").append(value.getRowId())
						.append("\" class=\"table-cell ui-cell value-cell").append(storageCell).append(enumCell)
						.append("\">").append(StringEscapeUtils.escapeHtml4(value.getValue())).append("</div>");
			}
			if (showUsage) {
				if (!valuesRow.isEmpty()) {
					List<Table> tables = Dao.getTablesUsingRow(valuesRow.get(0).getRowId());
					responseHtml.append("<div class=\"table-cell ui-cell info-cell\">")
							.append(getTestTableOccurences(tables)).append("</div>");
					responseHtml.append("<div class=\"table-cell ui-cell info-cell\">")
							.append(getDataStorageOccurences(tables)).append("</div>");
				}
			}
			responseHtml.append("</div>");
		}
	}

	private String getTestTableOccurences(List<Table> tables) throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		for (int i = 0; i < tables.size(); i++) {
			if (TableType.TABLE == tables.get(i).getType()) {
				if (!tableNames.contains(tables.get(i).getName())) {
					tableNames.add(tables.get(i).getName());
				}
			} else if (TableType.PRECONDITION == tables.get(i).getType()
					|| TableType.POSTCONDITION == tables.get(i).getType()) {
				String tableName = Dao.getTable(tables.get(i).getParentId()).getName();
				if (!tableNames.contains(tableName)) {
					tableNames.add(tableName);
				}
			}
		}
		return StringUtils.join(tableNames, ", ");
	}

	private String getDataStorageOccurences(List<Table> tables) throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		for (int i = 0; i < tables.size(); i++) {
			if (TableType.STORAGE == tables.get(i).getType()) {
				tableNames.add(tables.get(i).getName());
			}
		}
		return StringUtils.join(tableNames, ", ");
	}
}
