package br.gov.caixa.gitecsa.sired.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
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

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exporter.AbstractExcelDataExporter;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class ExportarRequisicaoXLS extends AbstractExcelDataExporter<RequisicaoVO> {
    private static final String TITULO_RELATORIO = "CONSULTA DE REQUISIÇÃO";

    private static final String FONT_FAMILY = "Arial";

    private int maxNumberOfColumns = 0;

//    private transient Logger logger = LogUtils.getLogger(ExportarRequisicaoXLS.class.getName());

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
        cellReportName.setCellValue(new HSSFRichTextString(TITULO_RELATORIO));
        cellReportName.setCellStyle(this.getReportNameStyle());

        CellRangeAddress regionReportName = new CellRangeAddress(1, 1, 0, (this.maxNumberOfColumns - 1));
        rowReportName.getSheet().addMergedRegion(regionReportName);
    }

    @Override
    public void createDetail() throws AppException {

        int rowNumber = 2;
        for (int index = 0; index < this.getData().size(); index++) {

            RequisicaoVO requisicao = this.getData().get(index);

            rowNumber++;
            HSSFRow rowDetail = this.addRowAt(rowNumber);
            HSSFRow rowHeader = this.addRowAt((rowNumber - 1));

            int ultimaColunaUtilizada = 0;
            
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_CODIGO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getCodigoRequisicao());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_UNIDADE_SOLICITANTE);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getUnidadeSolicitante().getDescricaoCompleta());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_DT_ABERTURA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getDataHoraAbertura(requisicao));

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_QTD_SOLIC);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getQtSolicitadaDocumento());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_DT_PRAZO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getPrazoAtendimento(requisicao));

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_DOCUMENTO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getDocumento().getNome());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_FORMATO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getFormato().getDescricao());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_TIPO_DEMANDA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getRequisicaoDocumento().getTipoDemanda().getNome());
            
            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.NUMERO_DO_PROCESSO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getRequisicaoDocumento().getNuDocumentoExigido());
           
            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_SITUACAO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getNome());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_UNIDADE_GERADORA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getUnidadeGeradoraDescricaoCompleta(requisicao));

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_UF_UNIDADE_GERADORA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getRequisicaoDocumento().getUnidadeGeradora().getUf().getDescricao());
            
            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_USUARIO_ABERTURA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getCodigoUsuarioAbertura());

            try {

                List<GrupoCampoVO> listGrupoCampo = CollectionUtils.asSortedList(GrupoCamposHelper.getValorCamposDinamicos(requisicao, requisicao.getDocumento().getGrupo().getGrupoCampos()));
                
                for (GrupoCampoVO grupoCampo : listGrupoCampo) {

                    String caption = StringUtils.defaultIfEmpty(grupoCampo.getLegenda(), grupoCampo.getCampo().getDescricao());
                    String valor = StringUtils.EMPTY;

                    if (TipoCampoEnum.DATA.equals(grupoCampo.getCampo().getTipo())) {
                        valor = DateUtils.format(grupoCampo.getValorData(), DateUtils.DEFAULT_FORMAT);
                    } else if (grupoCampo.getCampo().getNome().equals("NU_OPERACAO_A11")
                            && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getOperacao())) {
                        valor = requisicao.getRequisicaoDocumento().getOperacao().getDescricaoCompleta();
                    } else {
                        valor = grupoCampo.getValor();
                        if (!ObjectUtils.isNullOrEmpty(valor)) {
                            valor = valor.trim();
                        }
                    }

                    ultimaColunaUtilizada++;
                    rowHeader.createCell(ultimaColunaUtilizada).setCellValue(caption);
                    rowDetail.createCell(ultimaColunaUtilizada).setCellValue(valor);
                }
                ultimaColunaUtilizada++;
                rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_OBSERVACOES);
                rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getRequisicaoDocumentoObservacao(requisicao));

                exibirDadosDeAtendimento(requisicao, rowHeader, rowDetail, ultimaColunaUtilizada);

                int numberOfColumns = rowHeader.getPhysicalNumberOfCells();
                HSSFCellStyle detailStyle = this.getDetailStyle();
                HSSFCellStyle detailHeaderStyle = this.getDetailHeaderStyle();
                for (int i = 0; i < numberOfColumns; i++) {
                    rowDetail.getCell(i).setCellStyle(detailStyle);
                    rowHeader.getCell(i).setCellStyle(detailHeaderStyle);
                }

                rowNumber++;

                // adiciona linha em branco.
                HSSFRow rowEmpty = this.addRowAt(rowNumber);
                rowEmpty.createCell(0).setCellValue(StringUtils.EMPTY);
                this.getSheet().addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, numberOfColumns - 1));

                this.maxNumberOfColumns = Math.max(this.maxNumberOfColumns, numberOfColumns);

                rowNumber++;
            }
            catch (AppException e) {
//                this.logger.error(e.getMessage(), e);
//                throw new AppException(MensagemUtils.obterMensagem("MA012"), e);
            } 
        }
    }

    private void exibirDadosDeAtendimento(RequisicaoVO requisicao, HSSFRow rowHeader, HSSFRow rowDetail, int ultimaColunaUtilizada) {
        if (requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.ATENDIDA)
                || requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.REATENDIDA)
                || requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.FECHADA)) {

        	ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_DT_ATENDIMENTO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getDataHoraAtendimento(requisicao));
            
            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_OCORRENCIA);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(requisicao.getTramiteRequisicaoAtual().getOcorrencia().getNome());

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_TIPO_SUPORTE);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getSuporte(requisicao));

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_QTD_DISP);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getQtdDisponibilizadaDocumento(requisicao));

            ultimaColunaUtilizada++;
            rowHeader.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.DSC_OBSERVACOES_ATENDIMENTO);
            rowDetail.createCell(ultimaColunaUtilizada).setCellValue(ExportarUtil.getTramiteObservacao(requisicao));
        }
    }

    @Override
    public void createFooter() {
        throw new NotImplementedException();
    }

    @Override
    public File export(String filename) throws FileNotFoundException, IOException, AppException {

        this.createDetail();
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

}
