package br.gov.caixa.gitecsa.sired.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exporter.AbstractCSVDataExporter;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class ExportarRequisicaoCSV extends AbstractCSVDataExporter<RequisicaoVO> {

    private StringBuilder content;

    private transient Logger logger = LogUtils.getLogger(ExportarRequisicaoCSV.class.getName());

    @Override
    public File export(String filename) throws FileNotFoundException, IOException, AppException {

        this.createContent();

        File file = new File(filename);
        FileOutputStream out = new FileOutputStream(file);
        out.write(this.content.toString().replaceAll(";$", "").getBytes());
        out.close();

        return file;
    }

    @Override
    public void createContent() throws AppException {

        this.content = new StringBuilder();

        for (RequisicaoVO requisicao : this.getData()) {
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_CODIGO, requisicao.getCodigoRequisicao()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_UNIDADE_SOLICITANTE, ExportarUtil.getUnidadeSolicitanteDescricaoCompleta(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_DT_ABERTURA, ExportarUtil.getDataHoraAbertura(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_QTD_SOLIC, requisicao.getQtSolicitadaDocumento()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_DT_PRAZO, ExportarUtil.getPrazoAtendimento(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_DOCUMENTO, SiredUtils.removeCaracteresInvalidos(requisicao.getDocumento().getNome())));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_FORMATO, requisicao.getFormato().getDescricao()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_TIPO_DEMANDA, requisicao.getRequisicaoDocumento().getTipoDemanda().getNome()));
            this.content.append(String.format("%s:%s;", ExportarUtil.NUMERO_DO_PROCESSO, requisicao.getRequisicaoDocumento().getNuDocumentoExigido()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_SITUACAO, requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getNome()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_UNIDADE_GERADORA, ExportarUtil.getUnidadeGeradoraDescricaoCompleta(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_UF_UNIDADE_GERADORA, requisicao.getRequisicaoDocumento().getUnidadeGeradora().getUf()
                    .getDescricao()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_USUARIO_ABERTURA, requisicao.getCodigoUsuarioAbertura()));

            try {
                List<GrupoCampoVO> grupoCampos = CollectionUtils.asSortedList(GrupoCamposHelper.getValorCamposDinamicos(requisicao, requisicao.getDocumento().getGrupo().getGrupoCampos()));

                for (GrupoCampoVO grupoCampo : grupoCampos) {
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

                    this.content.append(String.format("%s:%s;", grupoCampo.getCampo().getNome(), StringUtils.defaultIfEmpty(valor, StringUtils.EMPTY)));
                }

                this.content.append(String.format("%s:%s;", ExportarUtil.DSC_OBSERVACOES, ExportarUtil.getRequisicaoDocumentoObservacao(requisicao)));

                exibirDadosDeAtendimento(requisicao);
            } catch (AppException e) {
                this.logger.error(e.getMessage(), e);
                throw new AppException(MensagemUtils.obterMensagem("MA012"), e);
            }
            this.content.append(System.getProperty("line.separator"));
        }
    }

    private void exibirDadosDeAtendimento(RequisicaoVO requisicao) {
        if (requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.ATENDIDA)
                || requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.REATENDIDA)
                || requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().equals(SituacaoRequisicaoEnum.FECHADA)) {
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_DT_ATENDIMENTO, ExportarUtil.getDataHoraAtendimento(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_OCORRENCIA, requisicao.getTramiteRequisicaoAtual().getOcorrencia().getNome()));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_TIPO_SUPORTE, ExportarUtil.getSuporte(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_QTD_DISP, ExportarUtil.getQtdDisponibilizadaDocumento(requisicao)));
            this.content.append(String.format("%s:%s;", ExportarUtil.DSC_OBSERVACOES_ATENDIMENTO, ExportarUtil.getTramiteObservacao(requisicao)));
        }
    }
}
