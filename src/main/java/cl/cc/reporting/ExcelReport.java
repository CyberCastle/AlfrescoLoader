package cl.cc.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author CyberCastle
 */
public class ExcelReport extends AbstractReport {

    private final static String DEFAULT_DATE_CELL_FORMAT = "dd-mmm-yyyy";

    private Workbook book;
    private Sheet workingSheet;
    private Integer firstRow;
    private Integer firstColumn;
    private String dateCellFormat;
    private DataFormat dataFormat;

    public ExcelReport() {
        super();
        this.firstRow = 0;
        this.firstColumn = 0;
    }

    public void setDateCellFormat(String dateCellFormat) {
        this.dateCellFormat = dateCellFormat;
    }

    @Override
    public void loadBase() throws ReportException {
        try {
            this.book = new XSSFWorkbook(this.baseDocument);
            this.dataFormat = book.createDataFormat();
        } catch (IOException e) {
            throw new ReportException("Error to load base document.", "Rprt - 02", e);
        }

    }

    public void setWorkingSheet(Integer sheet) {
        this.workingSheet = this.book.getSheetAt(sheet);
    }

    public void setFirstRow(Integer firstRow) {
        this.firstRow = firstRow;
    }

    public void setFirstColumn(Integer firstColumn) {
        this.firstColumn = firstColumn;
    }

    public void addSheet(String name) {
        this.workingSheet = this.book.createSheet(name);
    }

    public void addSheetHeader(List<String> header) {
        Row row = this.workingSheet.createRow(this.firstRow);
        for (Integer column = 0; column < header.size(); column++) {
            Cell cell = row.createCell(firstColumn + column);
            this.setCellValue(cell, header.get(column), null, null);
            this.workingSheet.autoSizeColumn(firstColumn + column);
        }
    }

    // Completamos la hoja
    public void fillSheet(List<?> beans, List<String> fieldNames) throws ReportException {

        Integer rowCount = this.firstRow;
        String _dateCellFormat = DEFAULT_DATE_CELL_FORMAT;
        if (this.dateCellFormat != null) {
            _dateCellFormat = this.dateCellFormat;
        }

        // Se setea el formato que tendrá la fecha
        CellStyle cs = this.createCellStyle(_dateCellFormat);
        try {
            for (Object bean : beans) {
                Row row = this.workingSheet.createRow(rowCount++);
                for (Integer column = 0; column < fieldNames.size(); column++) {
                    Cell cell = row.createCell(firstColumn + column);
                    Object cellValue = PropertyUtils.getProperty(bean, fieldNames.get(column));
                    this.setCellValue(cell, cellValue, cs, null);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ReportException("Error to fill the document.", "Rprt - 03", e);
        }
    }

    public <T> List<T> readSheet(List<String> fieldNames, Class<T> classObj) throws ReportException {
        List<T> results = new ArrayList<>();
        for (Integer rowCount = this.firstRow; rowCount <= this.workingSheet.getLastRowNum(); rowCount++) {
            try {
                Row row = this.workingSheet.getRow(rowCount);
                T bean = classObj.newInstance();
                for (Integer column = 0; column < fieldNames.size(); column++) {
                    Cell cell = row.getCell(column);
                    Class<?> beanType = PropertyUtils.getPropertyType(bean, fieldNames.get(column));
                    if (beanType == Date.class) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            PropertyUtils.setProperty(bean, fieldNames.get(column), cell.getDateCellValue());
                        } else {
                            throw new ReportException("Error to get cell value. Please verify data type of cell", "Rprt - 05");
                        }
                    } else if (beanType == Double.class) {
                        PropertyUtils.setProperty(bean, fieldNames.get(column), cell.getNumericCellValue());
                    } else if (beanType == Integer.class) {
                        PropertyUtils.setProperty(bean, fieldNames.get(column), new Double(cell.getNumericCellValue()).intValue());
                    } else if (beanType == Long.class) {
                        PropertyUtils.setProperty(bean, fieldNames.get(column), new Double(cell.getNumericCellValue()).longValue());
                    } else {
                        PropertyUtils.setProperty(bean, fieldNames.get(column), cell.getStringCellValue());
                    } 
                }
                results.add(bean);
            } catch (IllegalStateException e) {
                throw new ReportException("Error to get cell value. Please verify data type of cell", "Rprt - 05", e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new ReportException("Error to read the document.", "Rprt - 04", e);
            }
        }
        return results;
    }

    public void addFormula(String cellPosition, String formula) {
        Cell cell = this.getCellByPosition(cellPosition);
        cell.setCellFormula(formula);
    }

    public void setCellValue(String cellPosition, Object cellValue, String dateFormat, String numberFormat) {
        CellStyle dateCellStyle = null;
        CellStyle numberCellStyle = null;

        // Se añade estilo para las fechas
        if (dateFormat != null) {
            dateCellStyle = this.createCellStyle(dateFormat);
        }

        // Se añade estilo para los números
        if (numberFormat != null) {
            numberCellStyle = this.createCellStyle(numberFormat);
        }
        this.setCellValue(this.getCellByPosition(cellPosition), cellValue, dateCellStyle, numberCellStyle);
    }

    @Override
    public void save(OutputStream out) throws ReportException {
        try {
            this.book.write(out);
        } catch (IOException e) {
            throw new ReportException("Error to save document.", "Rprt - 02", e);
        }
    }

    private Cell getCellByPosition(String cellPosition) {
        CellReference cellReference = new CellReference(cellPosition);
        Row row = this.workingSheet.getRow(cellReference.getRow());

        if (row == null) {
            row = this.workingSheet.createRow(this.workingSheet.getLastRowNum());
        }

        Cell cell = row.getCell(cellReference.getCol());
        // No existe la celda, hay que crearla
        if (cell == null) {
            cell = row.createCell(this.firstColumn + row.getLastCellNum());
        }
        return cell;
    }

    private void setCellValue(Cell cell, Object cellValue, CellStyle dateCellStyle, CellStyle numberCellStyle) {
        if (cellValue instanceof Date) {
            // Guardamos el valor como una fecha, con formato pre-establecido
            cell.setCellValue((Date) cellValue);
            if (dateCellStyle != null) {
                cell.setCellStyle(dateCellStyle);
            }
        } else if (cellValue instanceof Number) {
            // Guardamos el valor como un número, con formato pre-establecido
            cell.setCellValue(((Number) cellValue).doubleValue());
            if (numberCellStyle != null) {
                cell.setCellStyle(numberCellStyle);
            }
        } else {
            // Guardamos el resto como texto
            cell.setCellValue(cellValue.toString());
        }
    }

    private CellStyle createCellStyle(String dataFormat) {
        CellStyle cs = this.book.createCellStyle();
        cs.setDataFormat(this.dataFormat.getFormat(dataFormat));
        return cs;
    }

    //TODO: Definir custom data
}
