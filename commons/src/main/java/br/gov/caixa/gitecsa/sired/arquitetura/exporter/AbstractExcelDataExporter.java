package br.gov.caixa.gitecsa.sired.arquitetura.exporter;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;

public abstract class AbstractExcelDataExporter<T> implements DataExportable<T> {

    private HSSFSheet sheet;
    private HSSFWorkbook book;
    private List<String> columnNames;
    private List<T> data;
    
    public AbstractExcelDataExporter() {
        this.book = new HSSFWorkbook();
        this.sheet = this.book.createSheet();
    }
    
    public AbstractExcelDataExporter(String sheet) {
        this.book = new HSSFWorkbook();
        this.sheet = this.book.createSheet(sheet);
    }
    
    public HSSFRow addRowAt(int lineNumber) {
        return sheet.createRow(lineNumber);
    }

    public void addStyleToRegion(CellRangeAddress region, HSSFCellStyle style) {

        for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {

            HSSFRow row = this.sheet.getRow(r);
            if (row == null) {
                this.sheet.createRow(r);
            }

            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                HSSFCell cell = row.getCell(c);
                if (cell == null) {
                    cell = row.createCell(c);
                }
                cell.setCellStyle(style);
            }
        }

    }
    
    public HSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    public HSSFWorkbook getBook() {
        return book;
    }

    public void setBook(HSSFWorkbook book) {
        this.book = book;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
    
    @Override
    public List<T> getData() {
        return data;
    }
    
    @Override
    public void setData(List<T> data) {
        this.data = data;
    }
    
    public abstract void createHeader();
    
    public abstract void createDetail() throws AppException;
    
    public abstract void createFooter();

}
