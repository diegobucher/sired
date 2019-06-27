package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

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
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAgrupamentoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentalEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc01_documento", schema = Constantes.SCHEMADB_NAME)
public class DocumentoVO extends BaseEntity {

    private static final long serialVersionUID = 1796281540730647134L;
    
    private static final String ORDER_BY_NOME = "nome";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_documento", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_documento")
    private String nome;

    @Column(name = "de_mensagem")
    private String mensagem;

    @Column(name = "nu_prazo_fragmentacao", columnDefinition = "int2")
    private Long prazoFragmentacao;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_ativo", columnDefinition = "int2")
    private AtivoInativoEnum icAtivo;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_setorial", columnDefinition = "int2")
    private TipoDocumentoEnum icSetorial;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_tipo_agrupamento", columnDefinition = "int2")
    private TipoAgrupamentoDocumentoEnum tipoAgrupamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_grupo_c02", columnDefinition = "int4")
    private GrupoVO grupo;

    @OneToMany(mappedBy = "documento", fetch = FetchType.LAZY)
    private List<RequisicaoVO> requisicoes;
    
    @Column(name = "no_unid_autor_autom", length = 100)
    private String unidadesAutorizadas;
    
    @Column(name = "nu_manual_normativo", length = 5, columnDefinition = "bpchar")
    private String numeroManualNormativo;

    
    @Column(name = "nu_manual_normativo_versao", length = 3, columnDefinition = "bpchar")
    private String numeroManualNormativoVersao;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_tipo_documental", columnDefinition = "int2")
    private TipoDocumentalEnum tipoDocumental = TipoDocumentalEnum.VALOR_PADRAO;
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return ORDER_BY_NOME;
    }

    @Override
    public String getAuditoria() {
       return null;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getPrazoFragmentacao() {
        return prazoFragmentacao;
    }

    public void setPrazoFragmentacao(Long prazoFragmentacao) {
        this.prazoFragmentacao = prazoFragmentacao;
    }

    public AtivoInativoEnum getIcAtivo() {
        return icAtivo;
    }

    public void setIcAtivo(AtivoInativoEnum icAtivo) {
        this.icAtivo = icAtivo;
    }

    public TipoDocumentoEnum getIcSetorial() {
        return icSetorial;
    }

    public void setIcSetorial(TipoDocumentoEnum icSetorial) {
        this.icSetorial = icSetorial;
    }

    public TipoAgrupamentoDocumentoEnum getTipoAgrupamento() {
        return tipoAgrupamento;
    }

    public void setTipoAgrupamento(TipoAgrupamentoDocumentoEnum tipoAgrupamento) {
        this.tipoAgrupamento = tipoAgrupamento;
    }

    public GrupoVO getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoVO grupo) {
        this.grupo = grupo;
    }

    public List<RequisicaoVO> getRequisicoes() {
        return requisicoes;
    }

    public void setRequisicoes(List<RequisicaoVO> requisicoes) {
        this.requisicoes = requisicoes;
    }

    public String getUnidadesAutorizadas() {
        return unidadesAutorizadas;
    }

    public void setUnidadesAutorizadas(String unidadesAutorizadas) {
        this.unidadesAutorizadas = unidadesAutorizadas;
    }

    /**
     * @return the numeroManualNormativo
     */
    public String getNumeroManualNormativo() {
      return numeroManualNormativo;
    }

    /**
     * @param numeroManualNormativo the numeroManualNormativo to set
     */
    public void setNumeroManualNormativo(String numeroManualNormativo) {
      this.numeroManualNormativo = numeroManualNormativo;
    }

    /**
     * @return the numeroManualNormativoVersao
     */
    public String getNumeroManualNormativoVersao() {
      return numeroManualNormativoVersao;
    }

    /**
     * @param numeroManualNormativoVersao the numeroManualNormativoVersao to set
     */
    public void setNumeroManualNormativoVersao(String numeroManualNormativoVersao) {
      this.numeroManualNormativoVersao = numeroManualNormativoVersao;
    }

    /**
     * @return the tipoDocumental
     */
    public TipoDocumentalEnum getTipoDocumental() {
      return tipoDocumental;
    }

    /**
     * @param tipoDocumental the tipoDocumental to set
     */
    public void setTipoDocumental(TipoDocumentalEnum tipoDocumental) {
      this.tipoDocumental = tipoDocumental;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DocumentoVO other = (DocumentoVO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
