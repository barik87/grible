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
package org.pine.servlets.app.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pine.dao.Dao;
import org.pine.excel.ExcelFile;
import org.pine.model.Table;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/ExportToExcel")
public class ExportToExcel extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExportToExcel() {
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
			int tableId = Integer.parseInt(request.getParameter("id"));
			Table table = Dao.getTable(tableId);
			ExcelFile excelFile = new ExcelFile();
			File exportDir = new File(getServletContext().getRealPath("") + "/export");
			if (!exportDir.exists()) {
				exportDir.mkdir();
			} else {
				exportDir.delete();
				exportDir.mkdir();
			}
			String filePath = exportDir + "/" + table.getName() + ".xls";
			out.print(excelFile.saveToFile(table, filePath));
		} catch (Exception e) {
			out.print(e.getLocalizedMessage());
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}
}
