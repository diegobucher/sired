package br.gov.caixa.gitecsa.sired.dto;

import org.apache.commons.csv.CSVRecord;

import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;

public class RemessaLoteDTO {

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RemessaLoteDTO other = (RemessaLoteDTO) obj;
    return this.remessaDocumento.getId().equals(other.remessaDocumento.getId());
  }

  private RemessaDocumentoVO remessaDocumento;

  private CSVRecord registro;

  public CSVRecord getRegistro() {
    return registro;
  }

  public void setRegistro(CSVRecord registro) {
    this.registro = registro;
  }

  /**
   * @return the remessaDocumento
   */
  public RemessaDocumentoVO getRemessaDocumento() {
    return remessaDocumento;
  }

  /**
   * @param remessaDocumento the remessaDocumento to set
   */
  public void setRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
    this.remessaDocumento = remessaDocumento;
  }
}
