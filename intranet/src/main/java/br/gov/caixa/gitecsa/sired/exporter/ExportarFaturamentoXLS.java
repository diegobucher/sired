package br.gov.caixa.gitecsa.sired.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

import br.gov.caixa.gitecsa.dto.RelatorioFaturamentoDTO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exporter.AbstractExcelDataExporter;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.visitor.RelatorioVisitor;

public class ExportarFaturamentoXLS extends AbstractExcelDataExporter<RelatorioFaturamentoDTO> {

    private static final String DSC_FECHAMENTO = "FECHAMENTO";
    private static final String DSC_CODIGO = "COD. REQ.";
    private static final String DSC_SUPORTE = "SUPORTE";
    private static final String DSC_DOCUMENTO = "DOCUMENTO";
    private static final String DSC_DT_ABERTURA = "ABERT.";
    private static final String DSC_PRAZO_ATENDIMENTO = "PRAZO";
    private static final String DSC_DT_ATENDIMENTO = "ATEND.";

    private static final String DSC_QTD_SOLICITADA = "QTD.SOLIC.";
    private static final String DSC_QTD_DISPONIBILIZADA = "QTD.DISP.";
    private static final String DSC_QTD_DISPONIBILIZADA_PRAZO = "QTD.DISP.PR.";
    private static final String DSC_QTD_NAO_LOCALIZADOS = "QTD.Ñ.LOC.";
    private static final String DSC_IDLP = "IDLP(%)";
    private static final String DSC_IDNL = "IDNL(%)";

    private static final String TITULO_RELATORIO = "RELATÓRIO DE FATURAMENTO DE REQUISIÇÃO";
    private static final String FONT_FAMILY = "Calibri";
    private static final String TITULO_TOTAL = "TOTAL";
    private static final String TITULO_SUBTOTAL = "SUB-TOTAL";

    private static final int COLUNA_SUMARIO = 6;

    private int rowNumber = 0;
    private int maxNumberOfColumns = 0;

    private Map<Long, Map<Long, RelatorioFaturamentoDTO>> mapSubtotalFaturamento;

    private int qtdTotalSolicitada;
    private int qtdTotalDisponibilizada;
    private int qtdTotalDisponibilizadaNoPrazo;
    private int qtdTotalNaoLocalizada;
    private Double idlpTotal;
    private Double idnlTotal;
    private RelatorioVisitor relatorioVisitor;

    @Override
    public void createHeader() {

        HSSFRow rowAppName = this.getSheet().createRow(0);
        rowAppName.setHeight((short) 700);

        HSSFCell cellAppName = rowAppName.createCell(0);
        cellAppName.setCellValue(new HSSFRichTextString(MensagemUtils.obterMensagem("geral.nomeSistemaCompleto")));

        CellRangeAddress regionAppName = new CellRangeAddress(0, 0, 0, (this.maxNumberOfColumns - 1));
        this.addStyleToRegion(regionAppName, this.getHeaderStyle());
        rowAppName.getSheet().addMergedRegion(regionAppName);

        HSSFRow rowReportName = this.addRowAt(1);
        HSSFCell cellReportName = rowReportName.createCell(0);
        /** Criado um método para a adição do subtitulo. */
        cellReportName.setCellValue(new HSSFRichTextString(montarSubTitulo()));
        cellReportName.setCellStyle(this.getReportNameStyle());

        CellRangeAddress regionReportName = new CellRangeAddress(1, 1, 0, (this.maxNumberOfColumns - 1));
        rowReportName.getSheet().addMergedRegion(regionReportName);
    }

    private String montarSubTitulo() {
      SimpleDateFormat sdfCompleta = new SimpleDateFormat("dd/MM/yyyy HH:mm");
      String subTitulo = TITULO_RELATORIO;
      
      subTitulo += " - ";
      subTitulo += relatorioVisitor.getBase().getNome(); 
      subTitulo += " - REQUISIÇÕES FECHADAS ENTRE "; 
      subTitulo += DateUtils.format(relatorioVisitor.getDataInicio(), DateUtils.DEFAULT_FORMAT);
      subTitulo += " E ";
      subTitulo += DateUtils.format(relatorioVisitor.getDataFim(), DateUtils.DEFAULT_FORMAT);
      subTitulo += " - GERADO EM ";
      subTitulo += sdfCompleta.format(new Date());
      return subTitulo;
    }

    @Override
    public void createDetail() throws AppException {

        this.rowNumber = 2;
        HSSFRow rowHeader = this.addRowAt(rowNumber);
        HSSFCellStyle detailHeaderStyle = this.getDetailHeaderStyle();

        int celula = 0;
        rowHeader.createCell(celula).setCellValue(DSC_CODIGO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_SUPORTE);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_DOCUMENTO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_DT_ABERTURA);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_PRAZO_ATENDIMENTO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_DT_ATENDIMENTO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);
        
        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_FECHAMENTO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_QTD_SOLICITADA);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_QTD_DISPONIBILIZADA);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_QTD_DISPONIBILIZADA_PRAZO);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_QTD_NAO_LOCALIZADOS);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_IDLP);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        celula++;
        rowHeader.createCell(celula).setCellValue(DSC_IDNL);
        rowHeader.getCell(celula).setCellStyle(detailHeaderStyle);

        int numberOfColumns = rowHeader.getPhysicalNumberOfCells();
        Long idBase = null;
        Long idSuporte = null;

        for (int index = 0; index < this.getData().size(); index++) {

            RelatorioFaturamentoDTO item = this.getData().get(index);

            if (!ObjectUtils.isNullOrEmpty(idSuporte) && !idSuporte.equals((Long) item.getSuporte().getId())) {
                this.createGroupSummary(idBase, idSuporte);
            }

            idBase = (Long) item.getBase().getId();
            idSuporte = (Long) item.getSuporte().getId();

            this.rowNumber++;
            HSSFRow rowDetail = this.addRowAt(this.rowNumber);
            HSSFCellStyle detailStyle = this.getDetailStyle();

            celula = 0;
            rowDetail.createCell(celula).setCellValue(item.getRequisicao().getCodigoRequisicao());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getSuporte().getNome());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getDocumento().getNome());
            rowDetail.getCell(celula).setCellStyle(this.getDetailAlignToLeftStyle());

            celula++;
            rowDetail.createCell(celula).setCellValue(DateUtils.format(item.getDataAbertura(), DateUtils.DATETIME_FORMAT));
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getPrazoAtendimentoFormatado());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(DateUtils.format(item.getDataAtendimento(), DateUtils.DEFAULT_FORMAT));
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(DateUtils.format(item.getRequisicao().getTramiteRequisicaoAtual().getDataHora(),DateUtils.DEFAULT_FORMAT));
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getQtdSolicitada());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getQtdDisponibilizada());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getQtdDispNoPrazo());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(item.getQtdNaoLocalizada());
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            NumberFormat percFormat = new DecimalFormat("#0.00");
            celula++;
            rowDetail.createCell(celula).setCellValue(percFormat.format(item.getIdlp()));
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            celula++;
            rowDetail.createCell(celula).setCellValue(percFormat.format(item.getIdnl()));
            rowDetail.getCell(celula).setCellStyle(detailStyle);

            this.maxNumberOfColumns = Math.max(this.maxNumberOfColumns, numberOfColumns);
        }

        this.createGroupSummary(idBase, idSuporte);
    }

    @Override
    public void createFooter() {

        this.rowNumber++;
        HSSFRow rowFooter = this.addRowAt(this.rowNumber);
        rowFooter.setHeight((short) 400);

        HSSFCellStyle footerStyle = this.getFooterStyle();

        int colNumber = 0;

        for (; colNumber < COLUNA_SUMARIO; colNumber++) {
            rowFooter.createCell(colNumber).setCellValue(StringUtils.EMPTY);
        }

        rowFooter.createCell(colNumber).setCellValue(TITULO_TOTAL);
        colNumber++;

        rowFooter.createCell(colNumber).setCellValue(this.qtdTotalSolicitada);
        colNumber++;

        rowFooter.createCell(colNumber).setCellValue(this.qtdTotalDisponibilizada);
        colNumber++;

        rowFooter.createCell(colNumber).setCellValue(this.qtdTotalDisponibilizadaNoPrazo);
        colNumber++;

        rowFooter.createCell(colNumber).setCellValue(this.qtdTotalNaoLocalizada);
        colNumber++;

        NumberFormat percFormat = new DecimalFormat("#0.00");

        rowFooter.createCell(colNumber).setCellValue(percFormat.format(this.idlpTotal));
        colNumber++;

        rowFooter.createCell(colNumber).setCellValue(percFormat.format(this.idnlTotal));
        colNumber++;

        for (int index = 0; index < this.maxNumberOfColumns; index++) {
            rowFooter.getCell(index).setCellStyle(footerStyle);
        }
    }

    private void createGroupSummary(Long idBase, Long idSuporte) {

        Map<Long, RelatorioFaturamentoDTO> mapSubtotal = mapSubtotalFaturamento.get(idBase);
        if (!ObjectUtils.isNullOrEmpty(mapSubtotal)) {

            RelatorioFaturamentoDTO item = mapSubtotal.get(idSuporte);

            this.rowNumber++;
            HSSFRow rowSummary = this.addRowAt(this.rowNumber);
            rowSummary.setHeight((short) 400);

            HSSFCellStyle summaryStyle = this.getFooterStyle();

            int colNumber = 0;

            for (; colNumber < COLUNA_SUMARIO; colNumber++) {
                rowSummary.createCell(colNumber).setCellValue(StringUtils.EMPTY);
            }

            rowSummary.createCell(colNumber).setCellValue(TITULO_SUBTOTAL);
            colNumber++;

            rowSummary.createCell(colNumber).setCellValue(item.getQtdSolicitada());
            colNumber++;

            rowSummary.createCell(colNumber).setCellValue(item.getQtdDisponibilizada());
            colNumber++;

            rowSummary.createCell(colNumber).setCellValue(item.getQtdDispNoPrazo());
            colNumber++;

            rowSummary.createCell(colNumber).setCellValue(item.getQtdNaoLocalizada());
            colNumber++;

            NumberFormat percFormat = new DecimalFormat("#0.00");

            Double idlp = ((new Double(item.getQtdDispNoPrazo()) / new Double(item.getQtdDisponibilizada())) * 100);
            rowSummary.createCell(colNumber).setCellValue(percFormat.format((Double.isNaN(idlp)) ? BigDecimal.ZERO.doubleValue() : idlp));
            colNumber++;

            Double idln = (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.doubleValue() : ((new Double(item.getQtdNaoLocalizada()) / new Double(
                    item.getQtdSolicitada())) * 100);
            rowSummary.createCell(colNumber).setCellValue(percFormat.format((Double.isNaN(idln)) ? BigDecimal.ZERO.doubleValue() : idln));
            colNumber++;

            for (int index = 0; index < this.maxNumberOfColumns; index++) {
                rowSummary.getCell(index).setCellStyle(summaryStyle);
            }
        }
    }

    @Override
    public File export(String filename) throws FileNotFoundException, IOException, AppException {

        this.createDetail();
        this.createFooter();
        this.createHeader();

        if (!ObjectUtils.isNullOrEmpty(this.maxNumberOfColumns)) {
            for (int column = 0; column < this.maxNumberOfColumns; column++) {
                this.getSheet().autoSizeColumn(column);
            }
        }

        File file = new File(filename);
        OutputStream out = new FileOutputStream(file);
        this.getBook().write(out);

        return file;
    }

    private HSSFCellStyle getHeaderStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_80_PERCENT.index, (byte) 166, (byte) 166, (byte) 166);
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        style.setFont(font);

        return style;
    }

    private HSSFCellStyle getReportNameStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_50_PERCENT.index, (byte) 217, (byte) 217, (byte) 217);
        style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        style.setFont(font);

        return style;
    }

    private HSSFCellStyle getDetailHeaderStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_40_PERCENT.index, (byte) 191, (byte) 191, (byte) 191);
        style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setTopBorderColor(HSSFColor.BLACK.index);

        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        style.setFont(font);

        return style;
    }

    private HSSFCellStyle getDetailStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 242, (byte) 242, (byte) 242);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_JUSTIFY);

        return style;
    }

    private HSSFCellStyle getDetailAlignToLeftStyle() {
        HSSFCellStyle style = this.getDetailStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);

        return style;
    }

    private HSSFCellStyle getFooterStyle() {
        HSSFCellStyle style = this.getDetailStyle();

        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        style.setFont(font);

        return style;
    }

    public void setQtdTotalSolicitada(int qtdTotalSolicitada) {
        this.qtdTotalSolicitada = qtdTotalSolicitada;
    }

    public void setQtdTotalDisponibilizada(int qtdTotalDisponibilizada) {
        this.qtdTotalDisponibilizada = qtdTotalDisponibilizada;
    }

    public void setQtdTotalDisponibilizadaNoPrazo(int qtdTotalDisponibilizadaNoPrazo) {
        this.qtdTotalDisponibilizadaNoPrazo = qtdTotalDisponibilizadaNoPrazo;
    }

    public void setQtdTotalNaoLocalizada(int qtdTotalNaoLocalizada) {
        this.qtdTotalNaoLocalizada = qtdTotalNaoLocalizada;
    }

    public void setIdlpTotal(Double idlpTotal) {
        this.idlpTotal = idlpTotal;
    }

    public void setIdnlTotal(Double idnlTotal) {
        this.idnlTotal = idnlTotal;
    }

    public void setMapSubtotalFaturamento(Map<Long, Map<Long, RelatorioFaturamentoDTO>> mapSubtotalFaturamento) {
        this.mapSubtotalFaturamento = mapSubtotalFaturamento;
    }

    /**
     * @return the relatorioVisitor
     */
    public RelatorioVisitor getRelatorioVisitor() {
      return relatorioVisitor;
    }

    /**
     * @param relatorioVisitor the relatorioVisitor to set
     */
    public void setRelatorioVisitor(RelatorioVisitor relatorioVisitor) {
      this.relatorioVisitor = relatorioVisitor;
    }

}
