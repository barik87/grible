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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.grible.dao.DataManager;
import org.grible.model.Product;
import org.grible.security.Security;
import org.grible.settings.AppTypes;
import org.grible.settings.GlobalSettings;
import org.grible.uimodel.Section;
import org.grible.uimodel.Sections;

/**
 * Servlet implementation class GetStorageValues
 */
@WebServlet("/AddProduct")
public class AddProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddProduct() {
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

			String name = request.getParameter("name");
			String path = request.getParameter("path");

			if ("".equals(name)) {
				out.print("ERROR: Product name cannot be empty.");
			} else if ("".equals(path)) {
				out.print("ERROR: Product path cannot be empty.");
			} else if (!exists(path)) {
				out.print("ERROR: Directory '" + path + "' does not exist. Please, create this folder first.");
			} else {
				Product product = DataManager.getInstance().getDao().getProduct(name);
				if (product != null) {
					out.print("ERROR: Product with name '" + name + "' already exists.");
				} else {
					try {
						path = (path == null) ? "" : path;
						int id = DataManager.getInstance().getDao().insertProduct(name, path);
						if (isJson()) {
							Product thisProduct = DataManager.getInstance().getDao().getProduct(id);
							File gribleJsonFile = new File(thisProduct.getGribleJson().getFilePath());
							if (gribleJsonFile.exists()) {
								for (Section section : Sections.getSections()) {
									File dir = new File(path + File.separator + section.getDirName());
									if (!dir.exists()) {
										dir.mkdir();
									}
								}
							} else {
								thisProduct.getGribleJson().save();
								for (Section section : Sections.getSections()) {
									File dir = new File(path + File.separator + section.getDirName());
									if (dir.exists()) {
										dir.delete();
									}
									dir.mkdir();
								}
							}
						}
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

	private boolean isJson() throws Exception {
		return GlobalSettings.getInstance().getAppType() == AppTypes.JSON;
	}

	private boolean exists(String path) {
		return new File(path).exists();
	}
}