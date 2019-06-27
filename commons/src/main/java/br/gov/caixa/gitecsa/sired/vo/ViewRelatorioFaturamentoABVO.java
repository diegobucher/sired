package br.gov.caixa.gitecsa.sired.vo;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redvw003_relatorio_faturamento_tipoab", schema = Constantes.SCHEMADB_NAME)
public class ViewRelatorioFaturamentoABVO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private static final String ORDER_BY_BASE = "noBase";

    @Id
    @Column(name = "nu_remessa", columnDefinition = "int4")
    private Long id;
    
    @Column(name = "nu_base", columnDefinition = "int4")
    private Long nuBase;
        
    @Column(name = "no_base")
    private String noBase;
    
    @Column(name = "nu_unidade", columnDefinition = "int2")
    private Long nuUnidade;

    @Column(name = "no_unidade")
    private String noUnidade;

    @Column(name = "dh_abertura")
    private Date dataHoraAbertura;

    @Column(name = "dh_agendamento")
    private Date dataHoraAgendamento;
    
    @Column(name = "dh_recebimento")
    private Date dataHoraRecebimento;
    
    @Column(name = "dh_conferencia")
    private Date dataHoraConferencia;
    
    @Column(name = "dh_fechamento")
    private Date dataHoraFechamento;
    
    @Column(name = "qtd_caixa")
    private Long qtdCaixa;
    
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
        return ORDER_BY_BASE;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Long getNuBase() {
        return nuBase;
    }

    public void setNuBase(Long nuBase) {
        this.nuBase = nuBase;
    }

    public String getNoBase() {
        return noBase;
    }

    public void setNoBase(String noBase) {
        this.noBase = noBase;
    }

    public Long getNuUnidade() {
        return nuUnidade;
    }

    public void setNuUnidade(Long nuUnidade) {
        this.nuUnidade = nuUnidade;
    }

    public String getNoUnidade() {
        return noUnidade;
    }

    public void setNoUnidade(String noUnidade) {
        this.noUnidade = noUnidade;
    }

    public Date getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(Date dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public Date getDataHoraAgendamento() {
        return dataHoraAgendamento;
    }

    public void setDataHoraAgendamento(Date dataHoraAgendamento) {
        this.dataHoraAgendamento = dataHoraAgendamento;
    }

    public Date getDataHoraRecebimento() {
        return dataHoraRecebimento;
    }

    public void setDataHoraRecebimento(Date dataHoraRecebimento) {
        this.dataHoraRecebimento = dataHoraRecebimento;
    }

    public Date getDataHoraConferencia() {
        return dataHoraConferencia;
    }

    public void setDataHoraConferencia(Date dataHoraConferencia) {
        this.dataHoraConferencia = dataHoraConferencia;
    }

    public Date getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(Date dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public Long getQtdCaixa() {
        return qtdCaixa;
    }

    public void setQtdCaixa(Long qtdCaixa) {
        this.qtdCaixa = qtdCaixa;
    }
    
    @Transient
    public Boolean isDentroPrazo(Integer prazoEmDias) {
        Calendar dtLimite = Calendar.getInstance();
        dtLimite.setTime(this.dataHoraAbertura);
        dtLimite.setLenient(false);
        dtLimite.add(Calendar.DAY_OF_MONTH, prazoEmDias);
        
        Calendar dtRecebimento = Calendar.getInstance();
        dtLimite.setLenient(false);
        dtRecebimento.setTime(this.dataHoraRecebimento);
        
        return dtLimite.after(dtRecebimento);
    }

}
