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
package org.grible.excel;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.grible.model.Table;
import org.grible.model.json.Key;
import org.grible.model.json.KeyType;
import org.grible.servlets.ServletHelper;

public class ExcelFile {

	private Workbook workbook;
	private List<String> generalKeys;

	public ExcelFile() {
		try {
			this.workbook = new HSSFWorkbook();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ExcelFile(InputStream input, boolean isXlsx) {
		try {
			if (isXlsx) {
				this.workbook = new XSSFWorkbook(input);
			} else {
				this.workbook = new HSSFWorkbook(input);
			}
			setKeys();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String saveToFile(Table table, String filePath) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			Sheet worksheet = workbook.createSheet(table.getName());

			Row row1 = worksheet.createRow(0);

			Font keyFont = workbook.createFont();
			keyFont.setColor(HSSFColor.WHITE.index);
			keyFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

			CellStyle keyCellStyle = workbook.createCellStyle();
			keyCellStyle.setFont(keyFont);
			keyCellStyle.setFillForegroundColor(HSSFColor.BLACK.index);
			keyCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			keyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);

			Key[] keys = null;
			String[][] values = null;
			if (ServletHelper.isJson()) {
				keys = table.getTableJson().getKeys();
				values = table.getTableJson().getValues();
			} else {
				keys = table.getKeys();
				values = table.getValues();
			}
			for (int i = 0; i < keys.length; i++) {
				Cell cell = row1.createCell(i);
				cell.setCellValue(keys[i].getName());
				cell.setCellStyle(keyCellStyle);
			}

			for (int i = 0; i < values.length; i++) {
				Row excelRow = worksheet.createRow(i + 1);
				for (int j = 0; j < values[i].length; j++) {
					Cell cell = excelRow.createCell(j);
					cell.setCellValue(values[i][j]);
				}
			}

			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			return "success";
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}

	public String[][] getValues() {

		Sheet sheet = workbook.getSheetAt(0);

		int keysCount = generalKeys.size();
		int rowCount = sheet.getPhysicalNumberOfRows() - 1;

		String[][] result = new String[rowCount][keysCount];

		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(i + 1);
			for (int j = 0; j < keysCount; j++) {
				Cell cell = row.getCell(j);
				result[i][j] = getStringCellValue(cell);
			}
		}

		return result;
	}

	private String getStringCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			String result = String.valueOf(cell.getNumericCellValue());
			if (result.endsWith(".0")) {
				result = StringUtils.substringBeforeLast(result, ".0");
			}
			return result;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		}
		if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			return cell.getCellFormula();
		}
		return cell.getStringCellValue();
	}

	public Key[] getKeys() {
		Key[] keys = new Key[generalKeys.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = new Key(generalKeys.get(i), KeyType.TEXT, 0);
		}
		return keys;
	}

	private void setKeys() {
		generalKeys = new ArrayList<String>();
		Sheet sheet = workbook.getSheetAt(0);
		Row keysRow = sheet.getRow(0);
		for (int i = 0; i < keysRow.getPhysicalNumberOfCells(); i++) {
			generalKeys.add(keysRow.getCell(i).getStringCellValue());
		}
	}

	public boolean hasPreconditions() {
		return workbook.getSheet("Preconditions") != null;
	}

	public boolean hasPostconditions() {
		return workbook.getSheet("Postconditions") != null;
	}

	public HashMap<String, String> getPrecondition() {
		return getFirstRowHashBySheetName("Preconditions");
	}

	public HashMap<String, String> getPostcondition() {
		return getFirstRowHashBySheetName("Postconditions");
	}

	private HashMap<String, String> getFirstRowHashBySheetName(String sheetName) {
		HashMap<String, String> result = new HashMap<String, String>();
		Sheet sheet = workbook.getSheet(sheetName);
		Row keysRow = sheet.getRow(0);
		Row valuesRow = sheet.getRow(1);
		for (int i = 0; i < keysRow.getPhysicalNumberOfCells(); i++) {
			result.put(getStringCellValue(keysRow.getCell(i)), getStringCellValue(valuesRow.getCell(i)));
		}
		return result;
	}
}
