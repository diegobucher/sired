package br.gov.caixa.gitecsa.sired.enumerator;

import java.math.BigInteger;
import java.util.Comparator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum SituacaoDocumentoOriginalEnum implements BaseEnumNumberValue, Comparator<SituacaoDocumentoOriginalEnum> {

    ENVIADO(1L, "ENVIADO", "Enviado pela Empresa Terceirizada para a Unidade Solicitante."),
    RECEPCIONADO_UNIDADE(2L, "RECEPCIONADO UNIDADE", "Unidade Solicitante atestou o recebimento."),
    DEVOLVIDO(3L, "DEVOLVIDO", "Devolvido pela Unidade Solicitante para a Empresa Terceirizada."),
    RECEPCIONADO_TERCEIRIZADA(4L, "RECEPCIONADO TERCEIRIZADA", "Empresa Terceirizada atestou o recebimento do documento devolvido."),
    EXTRAVIADO(5L, "EXTRAVIADO", "Base de Arquivo registrou extravio do documento.");

    private Long id;

    private String label;

    private String descricao;

    private SituacaoDocumentoOriginalEnum(Long id, String label, String descricao) {
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
    
    public static SituacaoDocumentoOriginalEnum get(Long index) {
        return SituacaoDocumentoOriginalEnum.values()[index.intValue() - BigInteger.ONE.intValue()];
    }

    @Override
    public int compare(SituacaoDocumentoOriginalEnum o1, SituacaoDocumentoOriginalEnum o2) {
        return o1.getLabel().compareTo(o2.getLabel());
    }
}
