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
package org.grible.servlets.app.create;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grible.dao.DataManager;
import org.grible.helpers.StringHelper;
import org.grible.model.TableType;
import org.grible.security.Security;
import org.grible.settings.Lang;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/AddCategory")
public class AddCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddCategory() {
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

			Integer productId = Integer.parseInt(request.getParameter("product"));

			Integer parentId = null;
			if (request.getParameter("parent") != null) {
				parentId = Integer.parseInt(request.getParameter("parent"));
			}

			String parentPath = "";
			if (request.getParameter("parentpath") != null) {
				parentPath = StringHelper.getFolderPath(request.getParameter("parentpath"));
			}

			String name = request.getParameter("name");
			TableType tableType = TableType.valueOf(request.getParameter("tabletype").toUpperCase());
			if (tableType == TableType.PRECONDITION || tableType == TableType.POSTCONDITION) {
				tableType = TableType.TABLE;
			}

			if ("".equals(name)) {
				out.print(Lang.get("error") + ": " + Lang.get("categorynameempty"));
			} else {
				Integer categoryId = DataManager.getInstance().getDao().getCategoryId(name, productId, tableType, parentId, parentPath);
				if (categoryId != null) {
					out.print(Lang.get("error") + ": "+Lang.get("categorywithname")+" '" + name + "' " + Lang.get("alreadyexists"));
				} else {
					try {
						DataManager.getInstance().getDao().insertCategory(tableType, productId, name, parentId, parentPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
					out.print("success");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			out.print(e.getLocalizedMessage());
		} finally {
			out.flush();
			out.close();
		}
	}
}
