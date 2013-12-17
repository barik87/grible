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
package org.grible.servlets.ui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grible.dao.Dao;
import org.grible.model.Table;
import org.grible.model.User;
import org.grible.servlets.ServletHelper;
import org.grible.settings.GlobalSettings;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/tables/")
public class Tables extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Tables() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		try {
			if (!GlobalSettings.getInstance().init(getServletContext().getRealPath(""))) {
				response.sendRedirect("/grible/firstlaunch");
				return;
			}
			StringBuilder responseHtml = new StringBuilder();

			if (request.getSession(false) == null) {
				response.sendRedirect("/grible/?url=" + request.getRequestURI() + "?" + request.getQueryString());
			} else if (request.getSession(false).getAttribute("userName") == null) {
				response.sendRedirect("/grible/?url=" + request.getRequestURI() + "?" + request.getQueryString());
			} else if ((request.getParameter("product") == null) && (request.getParameter("id") == null)) {
				response.sendRedirect("/grible");
			} else {

				responseHtml.append("<!DOCTYPE html>");
				responseHtml.append("<html>");
				responseHtml.append("<head>");
				responseHtml.append("<title>Test Tables - Grible</title>");
				responseHtml.append(ServletHelper.getIncludes());

				String userName = (String) request.getSession(false).getAttribute("userName");
				User user = Dao.getUserByName(userName);

				int productId = 0;
				int tableId = 0;
				String tableType = "table";
				if (request.getParameter("id") != null) {
					tableId = Integer.parseInt(request.getParameter("id"));
					Table table = Dao.getTable(tableId);
					switch (table.getType()) {
					case TABLE:
						productId = Dao.getProductIdByPrimaryTableId(tableId);
						break;

					case PRECONDITION:
						productId = Dao.getProductIdBySecondaryTableId(tableId);
						break;

					case POSTCONDITION:
						productId = Dao.getProductIdBySecondaryTableId(tableId);
						break;

					default:
						break;
					}
					tableType = table.getType().toString().toLowerCase();
				} else {
					productId = Integer.parseInt(request.getParameter("product"));
				}

				if (!user.hasAccessToProduct(productId)) {
					responseHtml.append("<a href=\".\"><span id=\"home\" class=\"header-text\">Home</span></a>");
					responseHtml.append("<br/><br/>"
							+ "<div class=\"error-message\">You do not have permissions to access this page.</div>");
				} else {

					responseHtml.append("<script type=\"text/javascript\">");
					responseHtml.append("var productId = \"").append(productId).append("\";");
					responseHtml.append("var tableId = \"").append(tableId).append("\";");
					responseHtml.append("var tableType = \"").append(tableType).append("\";");
					responseHtml.append("var isTooltipOnClick = ").append(user.isTooltipOnClick()).append(";");
					responseHtml.append("</script>");
					responseHtml.append("<script type=\"text/javascript\" src=\"../js/dataCenter.js\"></script>");
					ServletHelper.showImportResult(request, responseHtml, tableId);
					ServletHelper.showAdvancedImportDialog(request, responseHtml);

					responseHtml.append("</head>");
					responseHtml.append("<body>");
					responseHtml.append(ServletHelper.getUserPanel(user));
					responseHtml.append(ServletHelper.getBreadCrumb("tables", Dao.getProduct(productId), "../img"));
					responseHtml.append(ServletHelper.getMain());
					responseHtml.append(ServletHelper.getContextMenus("table"));
					responseHtml.append(ServletHelper.getLoadingGif());
				}
				responseHtml.append(ServletHelper.getFooter(getServletContext().getRealPath("")));
				responseHtml.append("</body>");
				responseHtml.append("</html>");
				out.print(responseHtml.toString());
			}
		} catch (Exception e) {
			out.print(e.getLocalizedMessage());
			e.printStackTrace();
			request.getSession(false).setAttribute("importResult", null);
			request.getSession(false).setAttribute("importedTable", null);
			request.getSession(false).setAttribute("importedFile", null);
		}
		out.flush();
		out.close();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}