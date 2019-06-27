package br.gov.caixa.gitecsa.sired.exporter;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class ExportarUtil {
    public static final String SEM_INFORMACAO = " - ";
    public static final String DSC_CODIGO = "COD_REQUISIÇÃO";
    public static final String DSC_UNIDADE_SOLICITANTE = "UNIDADE_SOLICITANTE";
    public static final String DSC_UNIDADE_GERADORA = "UNIDADE_GERADORA";
    public static final String DSC_UF_UNIDADE_GERADORA = "UF_UNIDADE_GERADORA";
    public static final String DSC_DT_ABERTURA = "DT_ABERTURA";
    public static final String DSC_DT_PRAZO = "PRAZO";
    public static final String DSC_DOCUMENTO = "DOCUMENTO";
    public static final String DSC_USUARIO_ABERTURA = "USUARIO_ABERTURA";
    public static final String DSC_TIPO_DEMANDA = "TIPO_DEMANDA";
    public static final String NUMERO_DO_PROCESSO = "NÚMERO_DO_PROCESSO";
    public static final String DSC_SITUACAO = "SITUAÇÃO";
    public static final String DSC_FORMATO = "FORMATO";
    public static final String DSC_QTD_SOLIC = "QTD_SOLIC";
    public static final String DSC_OBSERVACOES = "OBSERVAÇÕES";

    // Colunas do Atendimento
    public static final String DSC_OCORRENCIA = "OCORRÊNCIA";
    public static final String DSC_DT_ATENDIMENTO = "DT_ATENDIMENTO";
    public static final String DSC_QTD_DISP = "QTD_DISP";
    public static final String DSC_TIPO_SUPORTE = "SUPORTE";
    public static final String DSC_OBSERVACOES_ATENDIMENTO = "OBS_ATEND";

    public static String getSuporte(RequisicaoVO requisicao) {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getTramiteRequisicaoAtual().getSuporte())) {
            return requisicao.getTramiteRequisicaoAtual().getSuporte().getNome();
        } else {
            return ExportarUtil.SEM_INFORMACAO;
        }
    }

    public static String getTramiteObservacao(RequisicaoVO requisicao) {
        return StringUtils.defaultIfEmpty(requisicao.getTramiteRequisicaoAtual().getObservacao(), SEM_INFORMACAO);
    }

    public static String getDataHoraAtendimento(RequisicaoVO requisicao) {
        return DateUtils.format(requisicao.getTramiteRequisicaoAtual().getDataHoraAtendimento(), DateUtils.DATETIME_FORMAT);
    }

    public static String getRequisicaoDocumentoObservacao(RequisicaoVO requisicao) {
        return StringUtils.defaultIfEmpty(SiredUtils.removeCaracteresInvalidos(requisicao.getRequisicaoDocumento().getObservacao()), SEM_INFORMACAO);
    }

    public static String getDataHoraAbertura(RequisicaoVO requisicao) {
        return DateUtils.format(requisicao.getDataHoraAbertura(), DateUtils.DATETIME_FORMAT);
    }

    public static String getPrazoAtendimento(RequisicaoVO requisicao) {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getPrazoAtendimento())) {
            return DateUtils.format(requisicao.getPrazoAtendimento(), DateUtils.DEFAULT_FORMAT);
        } else {
            return SEM_INFORMACAO;
        }
    }

    public static String getUnidadeSolicitanteDescricaoCompleta(RequisicaoVO requisicao) {
        return SiredUtils.removeCaracteresInvalidos(requisicao.getUnidadeSolicitante().getDescricaoCompleta());
    }

    public static String getUnidadeGeradoraDescricaoCompleta(RequisicaoVO requisicao) {
        return SiredUtils.removeCaracteresInvalidos(requisicao.getRequisicaoDocumento().getUnidadeGeradora().getDescricaoCompleta());
    }

    public static Integer getQtdDisponibilizadaDocumento(RequisicaoVO requisicao) {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getTramiteRequisicaoAtual().getQtdDisponibilizadaDocumento())) {
            return requisicao.getTramiteRequisicaoAtual().getQtdDisponibilizadaDocumento();
        } else {
            return 0;
        }
    }
}
