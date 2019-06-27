package br.gov.caixa.gitecsa.sired.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

public class RelatorioSiredUtil {

    private String nomeRelatorio;
    private Object document;
    private int quantidadeColunas = 0;

    public RelatorioSiredUtil(String nomeRelatorio, Object document) {
        this.nomeRelatorio = nomeRelatorio;
        this.document = document;
    }

    public RelatorioSiredUtil(String nomeRelatorio, Object document, int quantidadeColunas) {
        this.nomeRelatorio = nomeRelatorio;
        this.document = document;
        this.quantidadeColunas = quantidadeColunas;
    }

    public void preExportar() {
        HSSFWorkbook wb = (HSSFWorkbook) document;

        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow header = sheet.getRow(0);

        // HEAD DA COLUNA
        for (int i = 0; i < header.getLastCellNum(); i++) {
            for (int j = 0; j < header.getPhysicalNumberOfCells(); j++) {
                HSSFCell cell = header.getCell(j);
                cell.setCellStyle(getEstiloTituloColuna(wb));
            }
        }

        // DEFINICAO DO HEAD DA TABELA
        HSSFRow cabecalhoTabela = sheet.getRow(0);
        if (quantidadeColunas == 0)
            quantidadeColunas = cabecalhoTabela.getPhysicalNumberOfCells();
        for (int j = 0; j < quantidadeColunas; j++) {
            HSSFCell cell = cabecalhoTabela.getCell(j);
            if (cell != null)
                cell.setCellStyle(getEstiloTituloColuna(wb));
        }

        sheet.shiftRows(0, sheet.getLastRowNum(), 2, true, false);

        definirEstiloCelulas(wb, sheet);
        definirTamanhoCelulas(wb, sheet);

        criarCabecalhoPadraoRelatorio(wb, sheet, quantidadeColunas);

        criarCabecalhoNomeRelatorio(wb, sheet, quantidadeColunas, nomeRelatorio);
    }

    public static void definirTamanhoCelulas(HSSFWorkbook wb, HSSFSheet sheet) {
        definirTamanhoCelulas(wb, sheet, 10);
    }

    public static void definirTamanhoCelulas(HSSFWorkbook wb, HSSFSheet sheet, int maximoCelulas) {
        sheet = wb.getSheetAt(0);
        for (int i = 0; i < maximoCelulas; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static void definirEstiloCelulas(HSSFWorkbook wb, HSSFSheet sheet) {
        for (int iRow = 3; iRow <= sheet.getLastRowNum(); iRow++) {
            HSSFRow row = sheet.getRow(iRow);
            for (int iCell = row.getFirstCellNum(); iCell < row.getLastCellNum(); iCell++) {
                HSSFCell cell = row.getCell(iCell);
                if (cell != null) {
                    HSSFCellStyle estilo = getEstiloCelula(wb);
                    String valor = cell.getStringCellValue();
                    if (NumberUtils.isNumber(valor)) { // alinhar à direita.
                        cell.setCellValue(Double.parseDouble(valor));
                        estilo.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
                    } else {// alinhar à esquerda
                        estilo.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                    }
                    cell.setCellStyle(estilo);
                }
            }
        }
    }

    public void preExportarSemEstilo() {
        HSSFWorkbook wb = (HSSFWorkbook) document;

        HSSFSheet sheet = wb.getSheetAt(0);

        sheet.shiftRows(0, sheet.getLastRowNum(), 2, true, false);

        criarCabecalhoPadraoRelatorio(wb, sheet, quantidadeColunas);

        criarCabecalhoNomeRelatorio(wb, sheet, quantidadeColunas, nomeRelatorio);

        sheet = wb.getSheetAt(0);
        for (int i = 0; i < quantidadeColunas; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static HSSFCellStyle getEstiloTituloColuna(HSSFWorkbook wb) {
        HSSFCellStyle headerEstilo = wb.createCellStyle();
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_40_PERCENT.index, (byte) 191, // RGB
                                                                             // red
                (byte) 191, // RGB green
                (byte) 191 // RGB blue
        );
        headerEstilo.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        headerEstilo.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        headerEstilo.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerEstilo.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        headerEstilo.setBottomBorderColor(HSSFColor.BLACK.index);
        headerEstilo.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        headerEstilo.setLeftBorderColor(HSSFColor.BLACK.index);
        headerEstilo.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        headerEstilo.setRightBorderColor(HSSFColor.BLACK.index);
        headerEstilo.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        headerEstilo.setTopBorderColor(HSSFColor.BLACK.index);
        headerEstilo.setFont(getFonteCalibriNegritoTamanho11(wb));
        return headerEstilo;
    }

    public static HSSFCellStyle getEstiloCelulaCentralizado(HSSFWorkbook wb) {
        return getEstiloCelula(wb, HSSFCellStyle.ALIGN_CENTER);
    }

    public static HSSFCellStyle getEstiloCelulaDireita(HSSFWorkbook wb) {
        return getEstiloCelula(wb, HSSFCellStyle.ALIGN_RIGHT);
    }

    public static HSSFCellStyle getEstiloCelula(HSSFWorkbook wb) {
        return getEstiloCelula(wb, HSSFCellStyle.ALIGN_LEFT);
    }

    public static HSSFCellStyle getEstiloCelulaNegrito(HSSFWorkbook wb) {
        HSSFFont fonteCalibriNegritoTamanho11 = RelatorioSiredUtil.getFonteCalibriNegritoTamanho11(wb);
        HSSFCellStyle estiloCelulaNegrito = getEstiloCelula(wb, HSSFCellStyle.ALIGN_LEFT);
        estiloCelulaNegrito.setFont(fonteCalibriNegritoTamanho11);
        return estiloCelulaNegrito;
    }

    public static HSSFCellStyle getEstiloCelula(HSSFWorkbook wb, short alinhamento) {
        HSSFCellStyle linhaEstiloBrancoSemNegrito = wb.createCellStyle();
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 242, // RGBred
                (byte) 242, // RGB green
                (byte) 242 // RGB blue
        );
        linhaEstiloBrancoSemNegrito.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);

        linhaEstiloBrancoSemNegrito.setAlignment(alinhamento);
        linhaEstiloBrancoSemNegrito.setVerticalAlignment(HSSFCellStyle.VERTICAL_JUSTIFY);

        // bordas
        linhaEstiloBrancoSemNegrito.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        linhaEstiloBrancoSemNegrito.setLeftBorderColor(HSSFColor.BLACK.index);

        linhaEstiloBrancoSemNegrito.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        linhaEstiloBrancoSemNegrito.setBottomBorderColor(HSSFColor.BLACK.index);

        linhaEstiloBrancoSemNegrito.setBorderRight(HSSFCellStyle.BORDER_THIN);
        linhaEstiloBrancoSemNegrito.setRightBorderColor(HSSFColor.BLACK.index);

        linhaEstiloBrancoSemNegrito.setBorderTop(HSSFCellStyle.BORDER_THIN);
        linhaEstiloBrancoSemNegrito.setTopBorderColor(HSSFColor.BLACK.index);

        return linhaEstiloBrancoSemNegrito;
    }

    public static HSSFFont getFonteCalibriNegritoTamanho11(HSSFWorkbook wb) {
        HSSFFont fontCalibriNegritoTamanho11 = wb.createFont();
        fontCalibriNegritoTamanho11.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        fontCalibriNegritoTamanho11.setFontHeightInPoints((short) 11);
        fontCalibriNegritoTamanho11.setFontName("Calibri");
        return fontCalibriNegritoTamanho11;
    }

    public static void criarCabecalhoNomeRelatorio(HSSFWorkbook wb, HSSFSheet sheet, int quantidadeColunas, String nomeReport) {
        HSSFRow row2 = sheet.createRow(1);
        HSSFCell cell2 = row2.createCell(0);
        cell2.setCellValue(new HSSFRichTextString(nomeReport));
        cell2.setCellStyle(getEstiloHeadNomeRelatorio(wb));

        for (int j = 1; j < row2.getPhysicalNumberOfCells(); j++) {
            HSSFCell celula2 = row2.getCell(j);
            celula2.setCellStyle(getEstiloHeadNomeRelatorio(wb));
        }

        CellRangeAddress region2 = new CellRangeAddress(1, 1, 0, (quantidadeColunas - 1));
        cleanBeforeMergeOnValidCells(row2.getSheet(), region2, getEstiloHeadNomeRelatorio(wb));
        row2.getSheet().addMergedRegion(region2);
    }

    public static void criarCabecalhoPadraoRelatorio(HSSFWorkbook wb, HSSFSheet sheet, int quantidadeColunas) {

        HSSFRow row = sheet.createRow(0);
        row.setHeight((short) 700);

        HSSFCell cell1 = row.createCell(0);
        cell1.setCellValue(new HSSFRichTextString(MensagemUtils.obterMensagem("geral.nomeSistemaCompleto")));
        cell1.setCellStyle(getEstiloHeadNomeSistema(wb));

        for (int j = 1; j < row.getPhysicalNumberOfCells(); j++) {
            HSSFCell celula = row.getCell(j);
            celula.setCellStyle(getEstiloHeadNomeSistema(wb));
        }

        /* MESCLA AS COLUNAS */
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, (quantidadeColunas - 1));
        cleanBeforeMergeOnValidCells(row.getSheet(), region, getEstiloHeadNomeSistema(wb));
        row.getSheet().addMergedRegion(region);
    }

    public static HSSFCellStyle getEstiloHeadNomeSistema(HSSFWorkbook wb) {
        HSSFCellStyle nomeSistemaEstilo = wb.createCellStyle();
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_80_PERCENT.index, (byte) 166, // RGB
                                                                             // red
                (byte) 166, // RGB green
                (byte) 166 // RGB blue
        );
        nomeSistemaEstilo.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        nomeSistemaEstilo.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        nomeSistemaEstilo.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        nomeSistemaEstilo.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setBottomBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setLeftBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setRightBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setTopBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setFont(getFonteCalibriNegritoTamanho11(wb));
        nomeSistemaEstilo.setAlignment(CellStyle.ALIGN_CENTER);
        nomeSistemaEstilo.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return nomeSistemaEstilo;
    }

    public static HSSFCellStyle getEstiloHeadNomeRelatorio(HSSFWorkbook wb) {
        HSSFCellStyle nomeSistemaEstilo = wb.createCellStyle();
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_50_PERCENT.index, (byte) 217, // RGB
                                                                             // red
                (byte) 217, // RGB green
                (byte) 217 // RGB blue
        );
        nomeSistemaEstilo.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
        nomeSistemaEstilo.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        nomeSistemaEstilo.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        nomeSistemaEstilo.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setBottomBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setLeftBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setRightBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        nomeSistemaEstilo.setTopBorderColor(HSSFColor.BLACK.index);
        nomeSistemaEstilo.setFont(getFonteCalibriNegritoTamanho11(wb));
        nomeSistemaEstilo.setAlignment(CellStyle.ALIGN_CENTER);
        nomeSistemaEstilo.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return nomeSistemaEstilo;
    }

    public static void cleanBeforeMergeOnValidCells(HSSFSheet sheet, CellRangeAddress region, HSSFCellStyle cellStyle) {
        for (int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++) {
            HSSFRow row = sheet.getRow(rowNum);
            if (row == null) {
                sheet.createRow(rowNum);
            }
            for (int colNum = region.getFirstColumn(); colNum <= region.getLastColumn(); colNum++) {
                HSSFCell currentCell = row.getCell(colNum);
                if (currentCell == null) {
                    currentCell = row.createCell(colNum);
                }

                currentCell.setCellStyle(cellStyle);

            }
        }

    }

    public String getNOME_RELATORIO() {
        return nomeRelatorio;
    }

    public void setNOME_RELATORIO(String nOME_RELATORIO) {
        nomeRelatorio = nOME_RELATORIO;
    }

    /**
     * Cria a célula na linha linhaPlanilha, na posição numeroCelula, com o valor do atributo conteudo.
     * 
     * @param linhaPlanilha
     * @param numeroCelula
     * @param estiloCelula
     * @param conteudo
     * @return a célula recém criada.
     */
    public static HSSFCell adicionaCelula(HSSFRow linhaPlanilha, int numeroCelula, HSSFCellStyle estiloCelula, String conteudo) {
        HSSFCell cell = linhaPlanilha.createCell(numeroCelula);
        if (!ObjectUtils.isNullOrEmpty(estiloCelula)) {
            cell.setCellStyle(estiloCelula);
        }
        if (!ObjectUtils.isNullOrEmpty(conteudo) && StringUtils.isNumeric(conteudo)) {
            cell.setCellValue(Double.parseDouble(conteudo));
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        } else { // caso não seja negrito (título) define alinhado à esquerda.
            cell.setCellValue(conteudo);
            if (cell.getCellStyle().getFont(cell.getSheet().getWorkbook()).getBoldweight() != HSSFFont.BOLDWEIGHT_BOLD) {
                cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_LEFT);
            }
        }
        return cell;
    }
}
