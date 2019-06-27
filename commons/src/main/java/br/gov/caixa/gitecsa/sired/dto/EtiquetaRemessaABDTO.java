package br.gov.caixa.gitecsa.sired.dto;

import java.io.Serializable;

public class EtiquetaRemessaABDTO implements Serializable{
  
  /** Serial. */
  private static final long serialVersionUID = 5158336449613316169L;

  private Long caixaArquivo;
  
  private String tipoDocumental;
  
  private String unidadeGeradora;
  
  private String dataAbertura;
  
  private String dataExpurgo;
  
  private String numeroRemessa;
  
  private String responsavel;
  
  private Long codigoBarras;

  /**
   * @return the caixaArquivo
   */
  public Long getCaixaArquivo() {
    return caixaArquivo;
  }

  /**
   * @param caixaArquivo the caixaArquivo to set
   */
  public void setCaixaArquivo(Long caixaArquivo) {
    this.caixaArquivo = caixaArquivo;
  }

  /**
   * @return the tipoDocumental
   */
  public String getTipoDocumental() {
    return tipoDocumental;
  }

  /**
   * @param tipoDocumental the tipoDocumental to set
   */
  public void setTipoDocumental(String tipoDocumental) {
    this.tipoDocumental = tipoDocumental;
  }

  /**
   * @return the unidadeGeradora
   */
  public String getUnidadeGeradora() {
    return unidadeGeradora;
  }

  /**
   * @param unidadeGeradora the unidadeGeradora to set
   */
  public void setUnidadeGeradora(String unidadeGeradora) {
    this.unidadeGeradora = unidadeGeradora;
  }

  /**
   * @return the dataAbertura
   */
  public String getDataAbertura() {
    return dataAbertura;
  }

  /**
   * @param dataAbertura the dataAbertura to set
   */
  public void setDataAbertura(String dataAbertura) {
    this.dataAbertura = dataAbertura;
  }

  /**
   * @return the dataExpurgo
   */
  public String getDataExpurgo() {
    return dataExpurgo;
  }

  /**
   * @param dataExpurgo the dataExpurgo to set
   */
  public void setDataExpurgo(String dataExpurgo) {
    this.dataExpurgo = dataExpurgo;
  }

  /**
   * @return the numeroRemessa
   */
  public String getNumeroRemessa() {
    return numeroRemessa;
  }

  /**
   * @param numeroRemessa the numeroRemessa to set
   */
  public void setNumeroRemessa(String numeroRemessa) {
    this.numeroRemessa = numeroRemessa;
  }

  /**
   * @return the responsavel
   */
  public String getResponsavel() {
    return responsavel;
  }

  /**
   * @param responsavel the responsavel to set
   */
  public void setResponsavel(String responsavel) {
    this.responsavel = responsavel;
  }

  /**
   * @return the codigoBarras
   */
  public Long getCodigoBarras() {
    return codigoBarras;
  }

  /**
   * @param codigoBarras the codigoBarras to set
   */
  public void setCodigoBarras(Long codigoBarras) {
    this.codigoBarras = codigoBarras;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((numeroRemessa == null) ? 0 : numeroRemessa.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EtiquetaRemessaABDTO)) {
      return false;
    }
    EtiquetaRemessaABDTO other = (EtiquetaRemessaABDTO) obj;
    if (numeroRemessa == null) {
      if (other.numeroRemessa != null) {
        return false;
      }
    } else if (!numeroRemessa.equals(other.numeroRemessa)) {
      return false;
    }
    return true;
  }
  
}
