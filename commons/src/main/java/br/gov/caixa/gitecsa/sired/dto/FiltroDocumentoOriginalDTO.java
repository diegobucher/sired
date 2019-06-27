package br.gov.caixa.gitecsa.sired.dto;

import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;

public class FiltroDocumentoOriginalDTO extends FiltroRequisicaoDTO {
    private SituacaoDocumentoOriginalEnum situacaoDocOriginal;

    public SituacaoDocumentoOriginalEnum getSituacaoDocOriginal() {
        return situacaoDocOriginal;
    }

    public void setSituacaoDocOriginal(SituacaoDocumentoOriginalEnum situacaoDocOriginal) {
        this.situacaoDocOriginal = situacaoDocOriginal;
    }
}
