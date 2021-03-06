/*******************************************************************************
 * Copyright (c) 2013 - 2014 Maksym Barvinskyi.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Maksym Barvinskyi - initial API and implementation
 ******************************************************************************/
package org.grible.servlets.ui.dialogs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grible.dao.DataManager;
import org.grible.dao.PostgresDao;
import org.grible.helpers.StringHelper;
import org.grible.model.Category;
import org.grible.model.Table;
import org.grible.model.TableType;
import org.grible.security.Security;
import org.grible.settings.AppTypes;
import org.grible.settings.GlobalSettings;
import org.grible.settings.Lang;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/GetAddTableDialog")
public class GetAddTableDialog extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetAddTableDialog() {
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
			if (Security.anyServletEntryCheckFailed(request, response)) {
				return;
			}

			Category category = null;
			if (isJson()) {
				Integer productId = Integer.parseInt(request.getParameter("product"));
				TableType tableType = TableType.valueOf(request.getParameter("tabletype").toUpperCase());
				if (tableType == TableType.PRECONDITION || tableType == TableType.POSTCONDITION) {
					tableType = TableType.TABLE;
				}
				String path = StringHelper.getFolderPathWithoutLastSeparator(request.getParameter("path"));
				category = new Category(path, tableType, productId);
			} else {
				int categoryId = Integer.parseInt(request.getParameter("categoryid"));
				category = new PostgresDao().getCategory(categoryId);
			}

			if (!((category.getType() == TableType.TABLE) || (category.getType() == TableType.STORAGE) || (category
					.getType() == TableType.ENUMERATION))) {
				return;
			}
			String tableType = category.getType().toString().toLowerCase();

			StringBuilder responseHtml = new StringBuilder();
			responseHtml.append("<div id=\"add-data-");
			responseHtml.append(tableType);
			responseHtml.append("-dialog\" class=\"ui-dialog\">");
			responseHtml.append("<div class=\"ui-dialog-title\">");

			switch (category.getType()) {
			case TABLE:
				responseHtml.append(Lang.get("addtable"));
				break;
			case STORAGE:
				responseHtml.append(Lang.get("addstorage"));
				break;
			case ENUMERATION:
				responseHtml.append(Lang.get("addenumeration"));
				break;

			default:
				break;
			}

			responseHtml.append("</div>");
			responseHtml.append("<div class=\"ui-dialog-content\">");
			responseHtml.append("<div class=\"table\">");
			responseHtml.append("<div class=\"table-row\"><div class=\"table-cell dialog-cell dialog-label\">");
			responseHtml.append(Lang.get("name"));
			responseHtml.append(":</div><div class=\"table-cell dialog-cell dialog-edit\">");
			responseHtml.append("<input class=\"data-item-name dialog-edit\"></div>");
			responseHtml.append("</div>");

			if (category.getType() == TableType.STORAGE) {
				responseHtml.append("<div class=\"table-row\"><div class=\"table-cell dialog-cell dialog-label\">");
				responseHtml.append(Lang.get("classname"));
				responseHtml.append(":</div><div class=\"table-cell dialog-cell dialog-edit\">");
				responseHtml.append("<input class=\"data-storage-class-name dialog-edit\"></div>");
				responseHtml.append("</div>");
			}

			responseHtml.append("<div class=\"table-row\">");
			responseHtml.append("<div class=\"table-cell dialog-cell dialog-label\">");
			responseHtml.append(Lang.get("copyexisting"));
			responseHtml.append(":</div>");
			responseHtml.append("<div class=\"table-cell dialog-cell\">");
			responseHtml.append("<input class=\"copy-existing\" type=\"checkbox\"></div>");
			responseHtml.append("</div>");

			responseHtml.append("<div class=\"table-row\">");
			responseHtml.append("<div class=\"table-cell dialog-cell dialog-label\">");
			responseHtml.append(Lang.get("whichone"));
			responseHtml.append(":</div>");
			responseHtml.append("<div class=\"table-cell dialog-cell\">");
			responseHtml.append("<select class=\"tables-list dialog-edit\" disabled=\"disabled\"\">");

			List<Table> tables = DataManager.getInstance().getDao()
					.getTablesOfProduct(category.getProductId(), category.getType());
			for (Table table : tables) {
				responseHtml.append("<option value=\"");
				responseHtml.append(table.getId());
				responseHtml.append("\">");
				responseHtml.append(table.getName());
				responseHtml.append("</option>");
			}
			responseHtml.append("</select></div>");
			responseHtml.append("</div>");

			responseHtml.append("<div class=\"table-row\">");
			responseHtml.append("<div class=\"table-cell dialog-cell dialog-label\">");
			responseHtml.append(Lang.get("colsonly"));
			responseHtml.append(":</div>");
			responseHtml.append("<div class=\"table-cell dialog-cell\">");
			responseHtml.append("<input class=\"only-columns\" type=\"checkbox\" disabled=\"disabled\"></div>");
			responseHtml.append("</div>");

			responseHtml.append("</div><br/>");

			switch (category.getType()) {
			case TABLE:
				responseHtml.append(Lang.get("tableaddedto") + " ");
				break;
			case STORAGE:
				responseHtml.append(Lang.get("storageaddedto") + " ");
				break;
			case ENUMERATION:
				responseHtml.append(Lang.get("enumaddedto") + " ");
				break;

			default:
				break;
			}

			responseHtml.append(category.getName());
			responseHtml.append("\".");
			responseHtml.append("<div class=\"dialog-buttons right\">");
			responseHtml.append("<button id=\"dialog-btn-add-data-item\" category-id=\"");
			responseHtml.append(category.getId());
			responseHtml.append("\" category-path=\"");
			responseHtml.append(category.getPath());
			responseHtml.append("\" class=\"ui-button\">");
			responseHtml.append(Lang.get("add"));
			responseHtml.append("</button> <button class=\"ui-button btn-cancel\">");
			responseHtml.append(Lang.get("cancel"));
			responseHtml.append("</button>");
			responseHtml.append("</div></div></div>");

			out.print(responseHtml.toString());
		} catch (Exception e) {
			e.printStackTrace();
			out.print(e.getLocalizedMessage());
		} finally {
			out.flush();
			out.close();
		}
	}

	private boolean isJson() throws Exception {
		return GlobalSettings.getInstance().getAppType() == AppTypes.JSON;
	}
}
