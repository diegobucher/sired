package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc16_avaliacao_requisicao", schema = Constantes.SCHEMADB_NAME)
public class AvaliacaoRequisicaoVO extends BaseEntity implements Comparable<AvaliacaoRequisicaoVO> {

    private static final long serialVersionUID = 5611154091873644041L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_avaliacao_requisicao", columnDefinition = "serial")
    private Long id;

    @Column(name = "co_usuario_avaliacao", columnDefinition = "bpchar")
    private String codigoUsuario;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_avaliacao")
    private Date dataHora;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_motivo_avaliacao_a12", columnDefinition = "int2")
    private MotivoAvaliacaoVO motivoAvaliacao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_tramite_requisicao_s01", columnDefinition = "int4")
    private TramiteRequisicaoVO tramite;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_reabertura", columnDefinition = "int2")
    private SimNaoEnum icReabertura;

    @Column(name = "de_observacao", columnDefinition = "bpchar")
    private String observacao;

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

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public MotivoAvaliacaoVO getMotivoAvaliacao() {
        return motivoAvaliacao;
    }

    public void setMotivoAvaliacao(MotivoAvaliacaoVO motivoAvaliacao) {
        this.motivoAvaliacao = motivoAvaliacao;
    }

    public TramiteRequisicaoVO getTramite() {
        return tramite;
    }

    public void setTramite(TramiteRequisicaoVO tramite) {
        this.tramite = tramite;
    }

    public SimNaoEnum getIcReabertura() {
        return icReabertura;
    }

    public void setIcReabertura(SimNaoEnum icReabertura) {
        this.icReabertura = icReabertura;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public int compareTo(AvaliacaoRequisicaoVO o) {
        return this.dataHora.compareTo(o.dataHora);
    }

}
