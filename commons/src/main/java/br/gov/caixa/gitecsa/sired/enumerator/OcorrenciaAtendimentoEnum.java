package br.gov.caixa.gitecsa.sired.enumerator;

import java.math.BigInteger;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum OcorrenciaAtendimentoEnum implements BaseEnumNumberValue {

    DOC_DIGITAL(1L, "DOC DIGITAL", "Documento digital enviado para o sistema e disponível para download."),
    ORIGINAL_UNIDADE(2L, "ORIGINAL UNIDADE", "Enviado via malote monitorado direto para a Unidade Solicitante."),
    COPIA_AUTENTICADA(3L, "CÓPIA AUTENTICADA", "Original enviado para a Base de arquivo autenticar e enviar para a Unidade Solicitante."),
    NAO_LOCALIZADO(4L, "NÃO LOCALIZADO", "Não localizado a partir dos dados fornecidos."),
    NAO_RECEPCIONADO(5L, "NÃO RECEPCIONADO", "Não recepcionado na Base de Arquivo."),
    INCONSISTENCIA(6L, "INCONSISTÊNCIA", "Não localizado devido à inconsistência dos dados."),
    PRAZO_EXPIRADO(7L, "PRAZO EXPIRADO", "Prazo de guarda dos documentos expirado."),
    SEM_MOVIMENTAÇAO(8L,"SEM MOVIMENTAÇÃO", "Conta sem movimentação no período solicitado."),
    SEM_MICROFORMAS(9L, "SEM MICROFORMAS", "Não há microformas no período solicitado.");
    
    private Long valor;
    
    private String label;
    
    private String descricao;

    private OcorrenciaAtendimentoEnum(Long valor, String label, String descricao) {
        this.valor = valor;
        this.label = label;
        this.descricao = descricao;
    }

    public Long getValor() {
        return valor;
    }

    public void setValor(Long valor) {
        this.valor = valor;
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
        return valor;
    }
    
    public static OcorrenciaAtendimentoEnum get(Long index) {
        return OcorrenciaAtendimentoEnum.values()[index.intValue() - BigInteger.ONE.intValue()];
    }
    
    public static OcorrenciaAtendimentoEnum byValor(Long valor) {
        for (OcorrenciaAtendimentoEnum e : OcorrenciaAtendimentoEnum.values()) {
            if (e.valor.equals(valor)) {
                return e;
            }
        }
        
        return OcorrenciaAtendimentoEnum.valueOf(valor.toString());
    }

}
