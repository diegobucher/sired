package br.gov.caixa.gitecsa.sired.comparator;

import java.util.Comparator;

import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;

public class RemessaDocumentoCodigoRemessaComparator implements Comparator<RemessaDocumentoVO>{

  @Override
  public int compare(RemessaDocumentoVO o1, RemessaDocumentoVO o2) {
    // TODO Auto-generated method stub
    return o1.getCodigoRemessa().compareTo(o2.getCodigoRemessa());
  }
  
}
