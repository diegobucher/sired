package br.gov.caixa.gitecsa.sired.vo;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redvw001_relatorio_pendencia", schema = Constantes.SCHEMADB_NAME)
public class ViewPendenciaVO extends BaseEntity {

    private static final long serialVersionUID = 1069987389659124663L;
    
    private static final String ORDER_BY_BASE = "noBase";

    @Id
    @Column(name = "nu_requisicao_documento", columnDefinition = "int4")
    private Long idRequisicaoDocumento;
        
    @Column(name = "co_requisicao", columnDefinition = "numeric")
    private Long codigoRequisicao;

    @Column(name = "nu_base", columnDefinition = "int4")
    private Long idBase;

    @Column(name = "no_base")
    private String noBase;

    @Column(name = "no_tipo_demanda")
    private String nomeDemanda;

    @Column(name = "dh_tramite_abertura")
    private Date dataHoraRegistro;

    @Column(name = "dt_prazo_atendimento", columnDefinition = "date")
    private Date dataPrazoAtendimento;

    @Column(name = "atraso")
    private Integer atraso;
    
    @Override
    public Object getId() {
        return idRequisicaoDocumento;
    }

    @Override
    public void setId(Object id) {
        this.idRequisicaoDocumento = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return ORDER_BY_BASE;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Long getIdRequisicaoDocumento() {
        return idRequisicaoDocumento;
    }

    public void setIdRequisicaoDocumento(Long idRequisicaoDocumento) {
        this.idRequisicaoDocumento = idRequisicaoDocumento;
    }

    public Long getCodigoRequisicao() {
        return codigoRequisicao;
    }

    public void setCodigoRequisicao(Long codigoRequisicao) {
        this.codigoRequisicao = codigoRequisicao;
    }

    public Long getIdBase() {
        return idBase;
    }

    public void setIdBase(Long idBase) {
        this.idBase = idBase;
    }

    public String getNoBase() {
        return noBase;
    }

    public void setNoBase(String noBase) {
        this.noBase = noBase;
    }

    public String getNomeDemanda() {
        return nomeDemanda;
    }

    public void setNomeDemanda(String nomeDemanda) {
        this.nomeDemanda = nomeDemanda;
    }

    public Date getDataHoraRegistro() {
        return dataHoraRegistro;
    }

    public void setDataHoraRegistro(Date dataHoraRegistro) {
        this.dataHoraRegistro = dataHoraRegistro;
    }

    public Date getDataPrazoAtendimento() {
        return dataPrazoAtendimento;
    }

    public void setDataPrazoAtendimento(Date dataPrazoAtendimento) {
        this.dataPrazoAtendimento = dataPrazoAtendimento;
    }

    public Integer getAtraso() {
        return atraso;
    }

    public void setAtraso(Integer atraso) {
        this.atraso = atraso;
    }

    public String getDataHoraRegistroFormatada() {
        if (dataHoraRegistro != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataHoraRegistro);
        }
        return StringUtils.EMPTY;
    }

    public String getDataPrazoAtendimentoFormatada() {
        if (dataPrazoAtendimento != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataPrazoAtendimento);
        }
        return StringUtils.EMPTY;
    }

}
