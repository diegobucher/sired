package br.gov.caixa.gitecsa.sired.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

public class ExportarRemessaXLS {

  public void exportarTabelaXLS(OutputStream outputStream, List<RemessaVO> listaRemessa) throws IOException, AppException {

    HSSFWorkbook wb = new HSSFWorkbook();
    HSSFSheet sheet = wb.createSheet("Sheet0");
    HSSFCellStyle estiloTituloColuna = RelatorioSiredUtil.getEstiloTituloColuna(wb);
    HSSFCellStyle estiloCelula = RelatorioSiredUtil.getEstiloCelulaCentralizado(wb);
    HSSFCellStyle estiloCelulaEsquerda = RelatorioSiredUtil.getEstiloCelula(wb);
    HSSFCellStyle estiloCelulaNegrito = RelatorioSiredUtil.getEstiloCelulaNegrito(wb);

    int linha = 2;
    int numeroCelula;
    int maximoCelulas = 6; // valor inicial igual às células do cabeçalho
    for (int i = 0; i < listaRemessa.size(); i++) {
      HSSFRow linhaCabecalho = sheet.createRow(linha);
      numeroCelula = 0;
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "Nº OU CÓD. REMESSA");
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "UNIDADE SOLICITANTE");
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "ABERTURA");
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "AGENDAMENTO");
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "BASE");
      RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "SITUAÇÃO");

      linha++;

      HSSFRow rowLineRequisicao = sheet.createRow(linha);
      numeroCelula = 0;
      if (listaRemessa.get(i).getTipoRemessaMoviMentoDiario()) {
        RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelulaNegrito,
            listaRemessa.get(i).getCodigoRemessaTipoC().toString());
      } else {
        RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelulaNegrito,
            listaRemessa.get(i).getId().toString());
      }
      RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelula,
          listaRemessa.get(i).getUnidadeSolicitante().getDescricaoCompleta());
      RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelula,
          listaRemessa.get(i).getDataHoraAberturaFormatada());
      RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelula,
          listaRemessa.get(i).getDataAgendamentoFormatada() != null ? listaRemessa.get(i).getDataAgendamentoFormatada() : " -- ");
      RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelula,
          listaRemessa.get(i).getEmpresaContrato() != null ? listaRemessa.get(i).getEmpresaContrato().getBase().getNome()
              : " -- ");
      RelatorioSiredUtil.adicionaCelula(rowLineRequisicao, numeroCelula++, estiloCelula,
          listaRemessa.get(i).getTramiteRemessaAtual().getSituacao().getNome());

      linha++;
      if (listaRemessa.get(i).getTipoRemessaMoviMentoDiario()) {
        List<MovimentoDiarioRemessaCDTO> listaMovDiario = listaRemessa.get(i).getDataMovimentosList();
        for (MovimentoDiarioRemessaCDTO mov : listaMovDiario) {
        HSSFRow rowHeadDetalhe = sheet.createRow(linha);
        numeroCelula = 3;
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "DATA MOVIMENTO");
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "UNIDADE GERADORA");
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "Nº DE TERMINAIS");
          linha++;
          HSSFRow rowLineDetalhe = sheet.createRow(linha);
          numeroCelula = 3;
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, mov.getDataFormatada());
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelulaEsquerda, mov.getNomeUnidadeFormatada());
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
              String.valueOf(mov.tamanhoRemessaTratada()));

          linha++;
          HSSFRow rowLineDetalhe2 = sheet.createRow(linha);
          numeroCelula = 1;
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloTituloColuna, "TF/LOTÉRICO");
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloTituloColuna, "NÚMERO TF/LOT");
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloTituloColuna, "QTD. ENVELOPES GRUPO 1");
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloTituloColuna, "QTD. ENVELOPES GRUPO 2");
          RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloTituloColuna, "QTD. ENVELOPES GRUPO 3");

          List<RemessaMovimentoDiarioVO> listaMovDiarioVO = mov.getRemessaMovDiarioList();
          
          for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaMovDiarioVO) {
            if(remessaMovimentoDiarioVO.getDataFormatada().equals(mov.getDataFormatada())) {
              linha++;
              rowLineDetalhe2 = sheet.createRow(linha);
              numeroCelula = 1;
              RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloCelulaEsquerda, remessaMovimentoDiarioVO.getIcLoterico().getDescricao());
              RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloCelula, remessaMovimentoDiarioVO.getNuTerminal().toString());
              RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloCelula, remessaMovimentoDiarioVO.getGrupo1().toString());
              RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloCelula, remessaMovimentoDiarioVO.getGrupo2().toString());
              RelatorioSiredUtil.adicionaCelula(rowLineDetalhe2, numeroCelula++, estiloCelula, remessaMovimentoDiarioVO.getGrupo3().toString());
            }
          }
          linha++;
        }
        linha++;
        HSSFRow rowRodape = sheet.createRow(linha);
        rowRodape.createCell(0).setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(linha, linha, 0, maximoCelulas - 1));
        linha++;

      } else {
        List<RemessaDocumentoVO> listaDocumentos = listaRemessa.get(i).getRemessaDocumentos();
        HSSFRow rowHeadDetalhe = sheet.createRow(linha);
        numeroCelula = 2;
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "CAIXA ARQUIVO");
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "UNIDADE GERADORA");
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "DOCUMENTO");
        RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "DATA DE GERAÇÃO / PERÍODO");
        for (RemessaDocumentoVO doc : listaDocumentos) {
          if (!doc.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)
              && !doc.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_BASE_ARQUIVO)) {
            linha++;
            HSSFRow rowLineDetalhe = sheet.createRow(linha);
            numeroCelula = 2;
            RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, doc.getCodigoRemessa().toString());
            RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
                doc.getUnidadeGeradora().getDescricaoCompleta());
            RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, doc.getDocumento().getNome());
            RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, doc.getDataGeracaoFormatada());
          }
        }
        linha++;
        HSSFRow rowRodape = sheet.createRow(linha);
        rowRodape.createCell(0).setCellValue("");
        sheet.addMergedRegion(new CellRangeAddress(linha, linha, 0, maximoCelulas - 1));

        linha++;
      }
    }

    RelatorioSiredUtil.criarCabecalhoPadraoRelatorio(wb, sheet, maximoCelulas);
    RelatorioSiredUtil.criarCabecalhoNomeRelatorio(wb, sheet, maximoCelulas, "MANUTENÇÃO DE REMESSA");
    RelatorioSiredUtil.definirTamanhoCelulas(wb, sheet, maximoCelulas);
    wb.write(outputStream);
  }
}
