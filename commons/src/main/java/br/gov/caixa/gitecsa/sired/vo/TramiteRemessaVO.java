package br.gov.caixa.gitecsa.sired.vo;

import java.text.SimpleDateFormat;
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
@Table(name = "redtbs03_tramite_remessa", schema = Constantes.SCHEMADB_NAME)
public class TramiteRemessaVO extends BaseEntity {

    private static final long serialVersionUID = -2258919554027002558L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_tramite_remessa", nullable = false, columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_situacao_remessa_a13", columnDefinition = "int2")
    private SituacaoRemessaVO situacao;

    @ManyToOne
    @JoinColumn(name = "nu_remessa_c17", columnDefinition = "int4")
    private RemessaVO remessa;

    @Column(name = "co_usuario", nullable = false, length = 60, columnDefinition = "bpchar")
    private String codigoUsuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_tramite_remessa", nullable = false)
    private Date dataTramiteRemessa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_agendamento", nullable = true)
    private Date dataAgendamento;

    @Column(name = "de_observacao", nullable = false, length = 200)
    private String observacao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_tramite_email", nullable = false)
    private Date dataTramiteEmail;

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

    public SituacaoRemessaVO getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoRemessaVO situacao) {
        this.situacao = situacao;
    }

    public RemessaVO getRemessa() {
        return remessa;
    }

    public void setRemessa(RemessaVO remessa) {
        this.remessa = remessa;
    }

    public Date getDataTramiteRemessa() {
        return dataTramiteRemessa;
    }

    public String getDataTramiteRemessaFormatada() {

        if (dataTramiteRemessa != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(dataTramiteRemessa);
        }
        return "";

    }

    public void setDataTramiteRemessa(Date dataTramiteRemessa) {
        this.dataTramiteRemessa = dataTramiteRemessa;
    }

    public Date getDataAgendamento() {
        return dataAgendamento;
    }

    public String getDataAgendamentoFormatada() {
        if (dataAgendamento != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(dataAgendamento);
        }
        return null;
    }

    public void setDataAgendamento(Date dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public Date getDataTramiteEmail() {
        return dataTramiteEmail;
    }

    public void setDataTramiteEmail(Date dataTramiteEmail) {
        this.dataTramiteEmail = dataTramiteEmail;
    }

}
