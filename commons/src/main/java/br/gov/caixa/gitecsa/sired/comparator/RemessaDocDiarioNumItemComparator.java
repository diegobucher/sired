package br.gov.caixa.gitecsa.sired.comparator;

import java.util.Comparator;

import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;

public class RemessaDocDiarioNumItemComparator implements Comparator<RemessaMovimentoDiarioVO>{

  @Override
  public int compare(RemessaMovimentoDiarioVO o1, RemessaMovimentoDiarioVO o2) {
    return o1.getNuItem().compareTo(o2.getNuItem());
  }

  
}
