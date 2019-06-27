package br.gov.caixa.gitecsa.sired.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exporter.AbstractExcelDataExporter;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;

public class ExportarModeloGrupoCampoRemessa extends AbstractExcelDataExporter<GrupoCampoVO> {
    
    private static final String MASCARA_MES_ANO = "99/9999";
    
    private static final String MASCARA_MONEY = "###.###.###.###.###.##0,00";
    
    private static final String CELL_TEXT_FORMAT = "@";
    
    private static final String CELL_MES_ANO_FORMAT = "MM/yyyy";

    private static final String CELL_DATE_FORMAT = "dd/MM/yyyy";
    
    private static final String CELL_NUMBER_FORMAT = "#";
    
    private static final String CELL_MONEY_FORMAT = "#,##0.00";

    private static final int PRIMEIRA_LINHA_TEMPLATE = 2;

    private static final int QTD_COLUNAS_FIXAS = 1;

    private static final String FONT_FAMILY = "Arial";

    private int maxNumberOfColumns = 0;
    
    private DocumentoVO documento;

    private transient Logger logger = LogUtils.getLogger(ExportarModeloGrupoCampoRemessa.class.getName());

    private int qtdLinhas = 0;
    
    private List<GrupoCampoVO> datasource; 

    @Override
    public void createHeader() {
        
        try {
            
            HSSFRow rowHeader = this.addRowAt(BigDecimal.ONE.intValue());
            
            this.createHeaderCell(rowHeader, Constantes.UNIDADE_GERADORA, 0, SimNaoEnum.SIM);
            
            HSSFCellStyle headerStyle = this.getDetailHeaderStyle();
            
            for (int colNumber = 0; colNumber < this.datasource.size(); colNumber++) {
                
                GrupoCampoVO grupoCampo = this.datasource.get(colNumber);
                CampoVO campo = grupoCampo.getCampo();
                String nomeCampo = (String)ObjectUtils.defaultIfNull(grupoCampo.getLegenda(), campo.getDescricao());
                
                int cellNumber = (QTD_COLUNAS_FIXAS + colNumber);
                rowHeader.createCell(cellNumber);
                rowHeader.getCell(cellNumber).setCellStyle(headerStyle);
                rowHeader.getCell(cellNumber).setCellValue(this.getContentStyled(nomeCampo, grupoCampo.getCampoObrigatorio()));
            }
                        
            this.maxNumberOfColumns = Math.max(this.maxNumberOfColumns, this.datasource.size()) + QTD_COLUNAS_FIXAS;
            
            //-- Sobre o modelo: Código, Versão e Nome do Documento
            HSSFRow rowDocumentName = this.getSheet().createRow(0);
            rowDocumentName.setHeight((short) 700);

            HSSFCell cellDocumentName = rowDocumentName.createCell(0);
            cellDocumentName.setCellValue(new HSSFRichTextString(String.format("%s|%s|%s", this.documento.getId(),  
                    this.documento.getGrupo().getVersaoFormulario(), this.documento.getNome().toUpperCase())));

            CellRangeAddress regionDocumentName = new CellRangeAddress(0, 0, 0, (this.maxNumberOfColumns - 1));
            HSSFCellStyle titleStyle = this.getHeaderStyle();
            
            this.addStyleToRegion(regionDocumentName, titleStyle);
            rowDocumentName.getSheet().addMergedRegion(regionDocumentName);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void createDetail() throws AppException {
        
        try {
            //-- Template: Linhas em branco
            final HSSFCellStyle detailStyle = this.getDetailStyle();
            
            final HSSFCellStyle dateStyle = this.getDetailStyle();
            final HSSFCellStyle mesAnoStyle = this.getDetailStyle();
            final HSSFCellStyle numberStyle = this.getDetailStyle();
            final HSSFCellStyle moneyStyle = this.getDetailStyle();
            
            CreationHelper helper = this.getBook().getCreationHelper();

            //--Style: Text (evitando a conversao automatica feita pelo Excel).
            Short textFormat = helper.createDataFormat().getFormat(CELL_TEXT_FORMAT);
            // Short textFormat = HSSFDataFormat.getBuiltinFormat("###");
            detailStyle.setDataFormat(textFormat);
            
            //-- Style: dd/MM/yyyy
            Short cellDateFormat = helper.createDataFormat().getFormat(CELL_DATE_FORMAT);
            dateStyle.setDataFormat(cellDateFormat);
            
            //-- Style: MM/yyyy
            Short cellMesAnoFormat = helper.createDataFormat().getFormat(CELL_MES_ANO_FORMAT);
            mesAnoStyle.setDataFormat(cellMesAnoFormat);
            
            //-- Style: #
            Short cellNumberFormat = helper.createDataFormat().getFormat(CELL_NUMBER_FORMAT);
            numberStyle.setDataFormat(cellNumberFormat);
            
            //-- Style: #,##0.00
            Short cellMoneyFormat = helper.createDataFormat().getFormat(CELL_MONEY_FORMAT);
            moneyStyle.setDataFormat(cellMoneyFormat);
            
            int qtdLinhasTemplate = PRIMEIRA_LINHA_TEMPLATE + this.qtdLinhas;
            
            for (int rowNumber = PRIMEIRA_LINHA_TEMPLATE; rowNumber < qtdLinhasTemplate; rowNumber++) {
                HSSFRow rowDetail = this.addRowAt(rowNumber);
                
                //-- Colunas Fixas
                this.createDetailCell(rowDetail, Constantes.UNIDADE_GERADORA, 0, detailStyle);
                
                //-- Colunas Dinâmicas
                for (int colNumber = QTD_COLUNAS_FIXAS; colNumber < this.maxNumberOfColumns; colNumber++) {
                    
                    GrupoCampoVO grupoCampo = this.datasource.get(colNumber - QTD_COLUNAS_FIXAS);
                    CampoVO campo = grupoCampo.getCampo();
                    
                    rowDetail.createCell(colNumber).setCellValue(StringUtils.EMPTY);
                    
                    //-- Campos númericos
                    if (TipoCampoEnum.NUMERICO.equals(campo.getTipo())) {
                        if (campo.getMascara().equals(MASCARA_MES_ANO)) {
                            rowDetail.getCell(colNumber).setCellStyle(mesAnoStyle);
                        } else if (campo.getMascara().equals(MASCARA_MONEY)) {
                            rowDetail.getCell(colNumber).setCellStyle(moneyStyle);
                        } else {
                            rowDetail.getCell(colNumber).setCellStyle(numberStyle);
                        }
                    } else if (TipoCampoEnum.DATA.equals(campo.getTipo())) {
                        rowDetail.getCell(colNumber).setCellStyle(dateStyle);
                    } else {
                        rowDetail.getCell(colNumber).setCellStyle(detailStyle);
                    }
                }
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            throw new AppException(MensagemUtils.obterMensagem("MA012"), e);
        }
    }

    @Override
    public void createFooter() {
        throw new NotImplementedException();
    }

    @Override
    public File export(String filename) throws FileNotFoundException, IOException, AppException {
        
        //-- Colunas fixas e dinâmicas (formulário) ordenadas
        this.datasource = CollectionUtils.asSortedList(this.getData());
        
        this.createHeader();
        this.createDetail();
        
        // TODO: Verificar com Gilberto após a entrega da OS 318 se a proteção irá permanecer.
        
        // UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        // this.getSheet().protectSheet(usuario.getNuMatricula());

        if (!ObjectUtils.isNullOrEmpty(this.maxNumberOfColumns)) {
            for (int column = 0; column < this.maxNumberOfColumns; column++) {
                this.getSheet().autoSizeColumn(column, true);
            }
        }

        File file = new File(filename);
        OutputStream out = new FileOutputStream(file);
        this.getBook().write(out);

        return file;
    }
    
    private void createHeaderCell(HSSFRow rowHeader, String nomeCampo, int colNumber, SimNaoEnum obrigatorio) {
        //-- Header
        rowHeader.createCell(colNumber);
        rowHeader.getCell(colNumber).setCellStyle(this.getDetailHeaderStyle());
        rowHeader.getCell(colNumber).setCellValue(this.getContentStyled(nomeCampo, obrigatorio));
    }
    
    private void createDetailCell(HSSFRow rowDetail, String nomeCampo, int colNumber, final HSSFCellStyle detailStyle) {
        //-- Data
        rowDetail.createCell(colNumber);

            rowDetail.getCell(colNumber).setCellValue(StringUtils.EMPTY);

        
        rowDetail.getCell(colNumber).setCellStyle(detailStyle);
    }

    private HSSFCellStyle getHeaderStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_80_PERCENT.index, (byte) 166, (byte) 166, (byte) 166);
        
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        style.setFont(font);
        
//        style.setLocked(true);

        return style;
    }


    private HSSFCellStyle getDetailHeaderStyle() {

        HSSFCellStyle style = this.getBook().createCellStyle();
        HSSFPalette palette = this.getBook().getCustomPalette();
        palette.setColorAtIndex(HSSFColor.GREY_40_PERCENT.index, (byte) 191, (byte) 191, (byte) 191);
        
        style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        
//        style.setLocked(true);

        return style;
    }

    private HSSFCellStyle getDetailStyle() {

        final HSSFCellStyle style = this.getBook().createCellStyle();
        
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        
//        style.setLocked(false);

        return style;
    }
    
    private RichTextString getContentStyled(String nomeCampo, final SimNaoEnum obrigatorio) {
        
        nomeCampo = (SimNaoEnum.SIM.equals(obrigatorio)) ? nomeCampo + "*" : nomeCampo;
        RichTextString content = new HSSFRichTextString(nomeCampo.toUpperCase());
                        
        HSSFFont font = this.getBook().createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);
        font.setFontName(FONT_FAMILY);
        content.applyFont(0, nomeCampo.toUpperCase().length(), font);
        
        if (SimNaoEnum.SIM.equals(obrigatorio)) {
            HSSFFont fontRequired = this.getBook().createFont();
            fontRequired.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            fontRequired.setColor(HSSFColor.RED.index);
            content.applyFont(nomeCampo.toUpperCase().length() - 1, nomeCampo.toUpperCase().length(), fontRequired);
        }
        
        return content;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public void setQtdLinhas(int qtdLinhas) {
        this.qtdLinhas = qtdLinhas;
    }

}
