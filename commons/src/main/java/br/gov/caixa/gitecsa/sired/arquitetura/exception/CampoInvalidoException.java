package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

public class CampoInvalidoException extends AppException {

	private static final long serialVersionUID = -3896662937942270404L;
	
	public CampoInvalidoException () {
		super();
	}
	
	public CampoInvalidoException (String campo) {
		super(MensagemUtils.obterMensagem("MI014", campo.toUpperCase()));
	}

}
