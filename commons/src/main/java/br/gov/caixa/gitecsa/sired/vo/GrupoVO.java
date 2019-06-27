package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc02_grupo", schema = Constantes.SCHEMADB_NAME)
public class GrupoVO extends BaseEntity implements Comparable<GrupoVO> {

    private static final long serialVersionUID = 8522979305086552340L;
    
    private static final String ORDER_BY_NOME = "nome";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_grupo", columnDefinition = "serial")
    private Long id;

    @Column(name = "de_grupo")
    private String descricao;

    @Column(name = "no_grupo")
    private String nome;

    @Column(name = "ic_tipo_solicitacao", columnDefinition = "int2")
    private TipoSolicitacaoEnum tipoSolicitacao;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_ativo", columnDefinition = "int2")
    private AtivoInativoEnum situacao;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<GrupoCampoVO> grupoCampos;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<UnidadeGrupoVO> unidadeGrupos;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    private Set<DocumentoVO> documentos;
    
    @Column(name = "nu_versao_formulario", columnDefinition = "int2")
    private Integer versaoFormulario = 0;
    
    @Column(name = "co_usro_ultma_altco", columnDefinition = "bpchar", length = 7)
    private String codigoUsuarioAlteracao;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "dh_ultma_altco")
    private Date dtUltimaAlteracao;

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoSolicitacaoEnum getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(TipoSolicitacaoEnum tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public AtivoInativoEnum getSituacao() {
        return situacao;
    }

    public void setSituacao(AtivoInativoEnum situacao) {
        this.situacao = situacao;
    }

    public Set<GrupoCampoVO> getGrupoCampos() {
        return grupoCampos;
    }
    
    public void setGrupoCampos(Set<GrupoCampoVO> grupoCampos) {
        this.grupoCampos = grupoCampos;
    }

    public Set<UnidadeGrupoVO> getUnidadeGrupos() {
        return unidadeGrupos;
    }

    public void setUnidadeGrupos(Set<UnidadeGrupoVO> unidadeGrupos) {
        this.unidadeGrupos = unidadeGrupos;
    }

    public Set<DocumentoVO> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(Set<DocumentoVO> documentos) {
        this.documentos = documentos;
    }

    public String getDocumentoAsString() {
        String documentosStr = StringUtils.EMPTY;

        if (documentos != null && documentos.size() > 0) {
            for (DocumentoVO documento : documentos) {
                documentosStr += !documentosStr.equalsIgnoreCase(StringUtils.EMPTY) ? ", " + documento.getNome() : documento.getNome();
            }
        }

        return documentosStr;
    }

    public Integer getVersaoFormulario() {
        return versaoFormulario;
    }

    public void setVersaoFormulario(Integer versaoFormulario) {
        this.versaoFormulario = versaoFormulario;
    }
    
    public String getCodigoUsuarioAlteracao() {
        return codigoUsuarioAlteracao;
    }

    public void setCodigoUsuarioAlteracao(String codigoUsuarioAlteracao) {
        this.codigoUsuarioAlteracao = codigoUsuarioAlteracao;
    }

    public Date getDtUltimaAlteracao() {
        return dtUltimaAlteracao;
    }

    public void setDtUltimaAlteracao(Date dtUltimaAlteracao) {
        this.dtUltimaAlteracao = dtUltimaAlteracao;
    }

    @Override
    public int compareTo(GrupoVO o) {
        return this.nome.compareTo(o.getNome());
    }

}
