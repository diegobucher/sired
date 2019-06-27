package br.gov.caixa.gitecsa.sired.enumerator;

public enum TipoAuditoriaEnum {
	
	DOWNLOAD_EFETUADO (1, "Download Efetuado"),
	CADASTRO_AREA_MEIO (2, "Cadastro de Area-Meio"),
	CADASTRO_BASE (3, "Cadastro de Base"),
	CADASTRO_DOCUMENTO (4, "Cadastro de Documento"),
	CADASTRO_EMPRESA (5, "Cadastro de Empresa"),
	CADASTRO_GRUPO (6, "Cadastro de Grupo"),
	CADASTRO_CTDA (7, "Cadastro de CTDA"),
	REQUISICAO (8, "Requisição"),
	REMESSA (9, "Remessa"),
	REMESSA_ETIQUETA (10, "Remessa Etiqueta");
	
	private Integer id;
	
	private String label;
	
	private TipoAuditoriaEnum(final Integer id, final String label) {
		this.id = id;
		this.label = label;
	}

	public Integer getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
}
