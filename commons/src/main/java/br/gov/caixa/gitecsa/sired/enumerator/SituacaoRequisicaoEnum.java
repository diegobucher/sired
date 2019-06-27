package br.gov.caixa.gitecsa.sired.enumerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

public enum SituacaoRequisicaoEnum implements BaseEnumNumberValue, Comparator<SituacaoRequisicaoEnum> {

    RASCUNHO(1L, "RASCUNHO", "Usuário Caixa após abrir a Requisição."),
    EM_AUTORIZACAO(2l, "EM AUTORIZAÇÃO", "Estado quando a Requisição solicita um documento ORIGINAL."),
    CANCELADA(3L, "CANCELADA", "A Base de Arquivo pode cancelar uma requisição que esteja EM AUTORIZAÇÃO caso não concorde com uma Requisição de documento ORIGINAL."),
    ABERTA(4l, "ABERTA", "Disponível para atendimento por parte da Terceirizada."),
    EM_TRATAMENTO(5L, "EM TRATAMENTO", "Pendente para a base de arquivo recepcionar o documento e encaminhar para a unidade solicitante."),
    PENDENTE_UPLOAD(6L, "PEND UPLOAD", "Atendimento foi realizado com ocorrência como DOC DIGITAL, mas o documento digital ainda não foi enviado."),
    ATENDIDA(7L, "ATENDIDA", "Terceirizada prestou o atendimento, ficando disponível para a Caixa obter o arquivo."),
    REABERTA(8L, "REABERTA", "Disponível para reatendimento por parte da Terceirizada."),
    REABERTA_EM_TRATAMENTO(9L, "EM TRATAMENTO (REABERTA)", "Pendente para a base de arquivo recepcionar o documento e encaminhar para a unidade solicitante."),
    REABERTA_PENDENTE_UPLOAD(10L, "PEND UPLOAD (REABERTA)", "Reatendimento foi realizado com ocorrência como DOC DIGITAL, mas o documento digital ainda não foi enviado."),
    REATENDIDA(11L, "REATENDIDA", "Terceirizada prestou o reatendimento, ficando disponível para a Caixa obter o arquivo."),
    FECHADA(12L, "FECHADA", "Requisição finalizada."),
    PEND_DADOS_FAT(13L, "PEND DADOS FAT", "Terceirizada enviou o documento digital após abertura da requisição, mas não as informações de atendimento. Após envio das informações de atendimento a situação é atualizada para ATENDIDA."),
    REABERTA_PEND_DADOS_FAT(14L, "PEND DADOS FAT (REABERTA)", "Terceirizada enviou o documento digital após reabertura da requisição, mas não as informações de atendimento. Após envio das informações de atendimento a situação é atualizada para REATENDIDA.");

    private Long id;

    private String label;

    private String descricao;

    /**
     * Variável estática que possui a relação de todas as situações em ordem alfabética.
     */
    private static SituacaoRequisicaoEnum[] listaSituacoesOrdemAlfabetica;
    /**
     * Variável estática que possui a relação de todas as situações permitidas às empresas Terceirizadas em ordem alfabética.
     */
    private static SituacaoRequisicaoEnum[] listaSituacoesTerceirizadaOrdemAlfabetica;

    private SituacaoRequisicaoEnum(Long id, String label, String descricao) {
        this.id = id;
        this.label = label;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public Long getValue() {
        return id;
    }

    public static SituacaoRequisicaoEnum get(Long index) {
        return SituacaoRequisicaoEnum.values()[index.intValue() - BigInteger.ONE.intValue()];
    }

    public static SituacaoRequisicaoEnum[] getListaSituacoesOrdemAlfabetica() {
        if (ObjectUtils.isNullOrEmpty(listaSituacoesOrdemAlfabetica)) {
            listaSituacoesOrdemAlfabetica = SituacaoRequisicaoEnum.values();
            Arrays.sort(listaSituacoesOrdemAlfabetica);
        }
        return listaSituacoesOrdemAlfabetica;
    }

    public static SituacaoRequisicaoEnum[] getListaSituacoesTerceirizadaOrdemAlfabetica() {
        if (ObjectUtils.isNullOrEmpty(listaSituacoesTerceirizadaOrdemAlfabetica)) {
            listaSituacoesTerceirizadaOrdemAlfabetica = new SituacaoRequisicaoEnum[8];
            listaSituacoesTerceirizadaOrdemAlfabetica[0] = SituacaoRequisicaoEnum.ABERTA;
            listaSituacoesTerceirizadaOrdemAlfabetica[1] = SituacaoRequisicaoEnum.EM_TRATAMENTO;
            listaSituacoesTerceirizadaOrdemAlfabetica[2] = SituacaoRequisicaoEnum.PENDENTE_UPLOAD;
            listaSituacoesTerceirizadaOrdemAlfabetica[3] = SituacaoRequisicaoEnum.PEND_DADOS_FAT;
            listaSituacoesTerceirizadaOrdemAlfabetica[4] = SituacaoRequisicaoEnum.ATENDIDA;
            listaSituacoesTerceirizadaOrdemAlfabetica[5] = SituacaoRequisicaoEnum.REABERTA;
            listaSituacoesTerceirizadaOrdemAlfabetica[6] = SituacaoRequisicaoEnum.REATENDIDA;
            listaSituacoesTerceirizadaOrdemAlfabetica[7] = SituacaoRequisicaoEnum.FECHADA;

            Arrays.sort(listaSituacoesTerceirizadaOrdemAlfabetica);

        }
        return listaSituacoesTerceirizadaOrdemAlfabetica;
    }

    @Override
    public int compare(SituacaoRequisicaoEnum o1, SituacaoRequisicaoEnum o2) {
        return o1.getLabel().compareTo(o2.getLabel());
    }
}
