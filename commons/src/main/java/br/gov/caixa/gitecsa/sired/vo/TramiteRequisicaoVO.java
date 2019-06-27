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
import br.gov.caixa.gitecsa.sired.util.DateUtils;

@Entity
@Table(name = "redtbs02_tramite_requisicao", schema = Constantes.SCHEMADB_NAME)
public class TramiteRequisicaoVO extends BaseEntity {

    private static final long serialVersionUID = -6377516299686887075L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_tramite_requisicao", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_requisicao_c14", columnDefinition = "int4")
    private RequisicaoVO requisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_situacao_requisicao_a05")
    private SituacaoRequisicaoVO situacaoRequisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_suporte_a09", columnDefinition = "int2")
    private SuporteVO suporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_arquivo_lote_s04", columnDefinition = "int4")
    private ArquivoLoteVO arquivoLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_ocrna_atnto_a06")
    private OcorrenciaAtendimentoVO ocorrencia;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_tramite_requisicao")
    private Date dataHora;

    @Column(name = "co_usuario", length = 60, columnDefinition = "bpchar")
    private String codigoUsuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_tramite_email")
    private Date dataHoraTramiteEmail;

    @Column(name = "qt_disponibilizada_documento", columnDefinition = "int2")
    private Integer qtdDisponibilizadaDocumento;

    @Column(name = "no_arquivo_disponibilizado")
    private String arquivoDisponibilizado;

    @Column(name = "de_observacao", length = 200, columnDefinition = "bpchar")
    private String observacao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_atendimento")
    private Date dataHoraAtendimento;

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

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public SituacaoRequisicaoVO getSituacaoRequisicao() {
        return situacaoRequisicao;
    }

    public void setSituacaoRequisicao(SituacaoRequisicaoVO situacaoRequisicao) {
        this.situacaoRequisicao = situacaoRequisicao;
    }

    public SuporteVO getSuporte() {
        return suporte;
    }

    public void setSuporte(SuporteVO suporte) {
        this.suporte = suporte;
    }

    public ArquivoLoteVO getArquivoLote() {
        return arquivoLote;
    }

    public void setArquivoLote(ArquivoLoteVO arquivoLote) {
        this.arquivoLote = arquivoLote;
    }

    public OcorrenciaAtendimentoVO getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(OcorrenciaAtendimentoVO ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public Date getDataHoraTramiteEmail() {
        return dataHoraTramiteEmail;
    }

    public void setDataHoraTramiteEmail(Date dataHoraTramiteEmail) {
        this.dataHoraTramiteEmail = dataHoraTramiteEmail;
    }

    public Integer getQtdDisponibilizadaDocumento() {
        return qtdDisponibilizadaDocumento;
    }

    public void setQtdDisponibilizadaDocumento(Integer qtdDisponibilizadaDocumento) {
        this.qtdDisponibilizadaDocumento = qtdDisponibilizadaDocumento;
    }

    public String getArquivoDisponibilizado() {
        return arquivoDisponibilizado;
    }

    public void setArquivoDisponibilizado(String arquivoDisponibilizado) {
        this.arquivoDisponibilizado = arquivoDisponibilizado;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Date getDataHoraAtendimento() {
      return dataHoraAtendimento;
    }

    public void setDataHoraAtendimento(Date dataHoraAtendimento) {
      this.dataHoraAtendimento = dataHoraAtendimento;
    }

    /**
     * Utilizar o método <b>DateUtils.format</b>
     * 
     * @return
     */
    @Deprecated
    public String getDataHoraFormatada() {
        return DateUtils.format(this.getDataHora(), DateUtils.DATETIME_FORMAT);
    }

}
