package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs10_auditoria", schema = Constantes.SCHEMADB_NAME)
public class AuditoriaVO extends BaseEntity {

  private static final long serialVersionUID = 49786883127123901L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "nu_auditoria", columnDefinition = "serial")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nu_tipo_auditoria_a16", columnDefinition = "int4")
  private TipoAuditoriaVO tipo;

  @Column(name = "co_usuario", length = 60)
  private String codigoUsuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "dh_ocorrencia")
  private Date dataHoraOcorrencia;

  @Column(name = "nu_identificador", columnDefinition = "int4")
  private Integer identificador;

  @Column(name = "de_ocorrencia", length = 100)
  private String descricao;

  @Override
  public Object getId() {
    return id;
  }

  @Override
  public void setId(Object id) {
    this.id = (Long) id;
  }

  @Override
  public String getColumnOrderBy() {
    return null;
  }

  @Override
  public String getAuditoria() {
    return null;
  }

  public TipoAuditoriaVO getTipo() {
    return tipo;
  }

  public void setTipo(TipoAuditoriaVO tipo) {
    this.tipo = tipo;
  }

  public String getCodigoUsuario() {
    return codigoUsuario;
  }

  public void setCodigoUsuario(String codigoUsuario) {
    this.codigoUsuario = codigoUsuario;
  }

  public Date getDataHoraOcorrencia() {
    return dataHoraOcorrencia;
  }

  public void setDataHoraOcorrencia(Date dataHoraOcorrencia) {
    this.dataHoraOcorrencia = dataHoraOcorrencia;
  }

  public Integer getIdentificador() {
    return identificador;
  }

  public void setIdentificador(Integer identificador) {
    this.identificador = identificador;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public void setId(Long id) {
    this.id = id;
  }

}
