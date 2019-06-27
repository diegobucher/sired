package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum SituacaoRemessaEnum implements BaseEnumNumberValue {

	TODAS(0L, "Todas"),
	RASCUNHO(1L, "Rascunho"), // intra
	ABERTA(2L, "Aberta"),
	BLOQUEADA(3L, "Bloqueada"), // intra	
	AGENDADA(4L, "Agendada"),
	RECEBIDA(5L, "Recebida"),
	CONFERIDA(6L, "Conferida"),	
	FECHADA(7L, "Fechada"), 
	EM_ALTERACAO(8L, "Em alteração"),
	ALTERADA(9L, "Alterada"),
	EM_DISPUTA(10L, "Em disputa"),
	INVALIDA(11L, "Inválida"),	
	ALTERACAO_CONFIRMADA(12L, "Alteração confirmada"),
	ALTERACAO_DESFEITA(13L, "Alteração desfeita"),
	FECHADA_INCONSISTENTE(14L, "Fechada inconsistente"); // intra

	private Long id;

	private String descricao;

	private SituacaoRemessaEnum(Long id, String descricao) {
		this.id = id;
		this.descricao = descricao;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public static SituacaoRemessaEnum[] valuesSituacaoOrdenados() {
		return new SituacaoRemessaEnum[]{ TODAS, ABERTA, AGENDADA, ALTERADA, ALTERACAO_CONFIRMADA, ALTERACAO_DESFEITA, BLOQUEADA, CONFERIDA, EM_ALTERACAO, EM_DISPUTA, FECHADA, FECHADA_INCONSISTENTE, INVALIDA, RASCUNHO, RECEBIDA };
	}
	
	public static SituacaoRemessaEnum[] valuesIntranet() {
            return new SituacaoRemessaEnum[] {TODAS, RASCUNHO, BLOQUEADA, FECHADA_INCONSISTENTE};
        }
	
	public static SituacaoRemessaEnum[] valuesPendenteIntranet() {
	    return new SituacaoRemessaEnum[] {RASCUNHO, BLOQUEADA};
	}
	
	public static SituacaoRemessaEnum[] valuesExtranet() {
            return new SituacaoRemessaEnum[] {TODAS, ABERTA, AGENDADA, RECEBIDA, CONFERIDA, FECHADA,
                INVALIDA, EM_ALTERACAO, ALTERADA, EM_DISPUTA, ALTERACAO_CONFIRMADA, ALTERACAO_DESFEITA};
	}
	
	public static SituacaoRemessaEnum[] valuesPendenteExtranet() {
            return new SituacaoRemessaEnum[] {ABERTA, AGENDADA, RECEBIDA, CONFERIDA, ALTERACAO_CONFIRMADA, ALTERACAO_DESFEITA};
        }
	
	/**
	 * Metodo que retorna array com as situações da remessa definidas
	 * como PENDENTES no momento de filtrar a consulta de remessa na
	 * tela de Manutenção de Remessa
	 * PENDENTES (RASCUNHO, BLOQUEADA, DEVOLVIDA PARA CORREÇÃO ou INVÁLIDA(INCONSISTENTE)).
	 * @return
	 */
	@Deprecated
	public static Long[] valuesSituacoesPendentes() {
	    return new Long[]{ BLOQUEADA.getId(), INVALIDA.getId(), RASCUNHO.getId(), ALTERADA.getId() };
	}

}