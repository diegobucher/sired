package br.gov.caixa.gitecsa.sired.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc14_requisicao", schema = Constantes.SCHEMADB_NAME)
public class RequisicaoVO extends BaseEntity {

    private static final long serialVersionUID = 2594966259927579042L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_requisicao", columnDefinition = "serial")
    private Long id;

    @Column(name = "co_requisicao", columnDefinition = "numeric", updatable = false)
    private Long codigoRequisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_documento_c01", columnDefinition = "int2")
    private DocumentoVO documento;
    
    @OneToOne(cascade= {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "nu_requisicao_documento_c15", columnDefinition = "int4")
    private RequisicaoDocumentoVO requisicaoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_solicitante_a02", updatable = false)
    private UnidadeVO unidadeSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_empresa_contrato_c13", columnDefinition = "int2")
    private EmpresaContratoVO empresaContrato;
    
    @OneToOne
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int2")
    private BaseVO base;
    
    @Column(name = "co_usuario_abertura", columnDefinition = "bpchar")
    private String codigoUsuarioAbertura;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_abertura")
    private Date dataHoraAbertura;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "dt_prazo_atendimento")
    private Date prazoAtendimento;

    @OneToMany(mappedBy = "requisicao", fetch = FetchType.LAZY)
    private Set<TramiteRequisicaoVO> tramiteRequisicoes;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_formato_documento", columnDefinition = "int2")
    private FormatoDocumentoEnum formato;
    
    @Column(name = "qt_solicitada_documento", columnDefinition = "int2")
    private Integer qtSolicitadaDocumento;
    
    @OneToOne
    @JoinColumn(name = "nu_trmte_rqsco_atual_s02", columnDefinition = "int4")
    private TramiteRequisicaoVO tramiteRequisicaoAtual;
    
    @Column(name = "no_arquivo_justificativa", length = 100)
    private String arquivoJustificativa;

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

    public Long getCodigoRequisicao() {
        return codigoRequisicao;
    }

    public void setCodigoRequisicao(Long codigoRequisicao) {
        this.codigoRequisicao = codigoRequisicao;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public RequisicaoDocumentoVO getRequisicaoDocumento() {
        return requisicaoDocumento; 
    }

    public void setRequisicaoDocumento(RequisicaoDocumentoVO requisicaoDocumento) {
        this.requisicaoDocumento = requisicaoDocumento;
    }

    public UnidadeVO getUnidadeSolicitante() {
        return unidadeSolicitante;
    }

    public void setUnidadeSolicitante(UnidadeVO unidadeSolicitante) {
        this.unidadeSolicitante = unidadeSolicitante;
    }

    public EmpresaContratoVO getEmpresaContrato() {
        return empresaContrato;
    }

    public void setEmpresaContrato(EmpresaContratoVO empresaContrato) {
        this.empresaContrato = empresaContrato;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public String getCodigoUsuarioAbertura() {
        return codigoUsuarioAbertura;
    }

    public void setCodigoUsuarioAbertura(String codigoUsuarioAbertura) {
        this.codigoUsuarioAbertura = codigoUsuarioAbertura;
    }

    public Date getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(Date dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public Date getPrazoAtendimento() {
        return prazoAtendimento;
    }

    public void setPrazoAtendimento(Date prazoAtendimento) {
        this.prazoAtendimento = prazoAtendimento;
    }

    public Set<TramiteRequisicaoVO> getTramiteRequisicoes() {
        return tramiteRequisicoes;
    }

    public void setTramiteRequisicoes(Set<TramiteRequisicaoVO> tramiteRequisicoes) {
        this.tramiteRequisicoes = tramiteRequisicoes;
    }

    public FormatoDocumentoEnum getFormato() {
        return formato;
    }

    public void setFormato(FormatoDocumentoEnum formato) {
        this.formato = formato;
    }

    public Integer getQtSolicitadaDocumento() {
        return qtSolicitadaDocumento;
    }

    public void setQtSolicitadaDocumento(Integer qtSolicitadaDocumento) {
        this.qtSolicitadaDocumento = qtSolicitadaDocumento;
    }

    public TramiteRequisicaoVO getTramiteRequisicaoAtual() {
        return tramiteRequisicaoAtual;
    }

    public void setTramiteRequisicaoAtual(TramiteRequisicaoVO tramiteRequisicaoAtual) {
        this.tramiteRequisicaoAtual = tramiteRequisicaoAtual;
    }

    public String getArquivoJustificativa() {
		return arquivoJustificativa;
	}

	public void setArquivoJustificativa(String arquivoJustificativa) {
		this.arquivoJustificativa = arquivoJustificativa;
	}

	public String getDataHoraAberturaFormatada() {

        if (dataHoraAbertura != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(dataHoraAbertura);
        }

        return StringUtils.EMPTY;
    }

}
