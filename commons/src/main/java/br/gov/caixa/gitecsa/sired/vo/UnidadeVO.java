package br.gov.caixa.gitecsa.sired.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cascade;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;

@Entity
@Table(name = "redtba02_unidade", schema = Constantes.SCHEMADB_NAME)
public class UnidadeVO extends BaseEntity implements Comparable<UnidadeVO> {

    private static final long serialVersionUID = -9007958133585289961L;

    private static final String ORDER_BY_NOME = "nome";

    @Id
    @Column(name = "nu_unidade", columnDefinition = "int2")
    private Long id;

    @Column(name = "ic_ativo", columnDefinition = "int2")
    private Integer icAtivo;

    @Column(name = "no_unidade")
    private String nome;

    @Column(name = "sg_localizacao", columnDefinition = "bpchar")
    private String siglaLocalizacao;

    @Column(name = "sg_unidade", columnDefinition = "bpchar")
    private String siglaUnidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_vncra_a02")
    private UnidadeVO unidadeVinculadora;

    @OneToMany(mappedBy = "unidadeVinculadora", fetch = FetchType.LAZY)
    private List<UnidadeVO> unidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sg_uf_a03", columnDefinition = "bpchar")
    private UFVO uf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_municipio_a08", columnDefinition = "int2")
    private MunicipioVO municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_tipo_unidade_a10", columnDefinition = "int2")
    private TipoUnidadeVO tipoUnidade;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "redtbc07_unidade_uf", schema = Constantes.SCHEMADB_NAME, joinColumns = { @JoinColumn(name = "nu_unidade_a02") }, inverseJoinColumns = { @JoinColumn(name = "sg_uf_a03") })
    private Set<UFVO> ufs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "unidade", fetch = FetchType.LAZY)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL })
    private Set<UnidadeGrupoVO> unidadeGrupos;

    @OneToMany(mappedBy = "unidade", fetch = FetchType.LAZY)
    private List<BaseVO> bases;

    @OneToMany(mappedBy = "unidadeSolicitante", fetch = FetchType.LAZY)
    private List<RequisicaoVO> requisicoes;

    @OneToMany(mappedBy = "unidadeGeradora", fetch = FetchType.LAZY)
    private List<RequisicaoDocumentoVO> requisicaoDocumentos;

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
        return ORDER_BY_NOME;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Integer getIcAtivo() {
        return icAtivo;
    }

    public void setIcAtivo(Integer icAtivo) {
        this.icAtivo = icAtivo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSiglaLocalizacao() {
        return siglaLocalizacao;
    }

    public void setSiglaLocalizacao(String siglaLocalizacao) {
        this.siglaLocalizacao = siglaLocalizacao;
    }

    public String getSiglaUnidade() {
        return siglaUnidade;
    }

    public void setSiglaUnidade(String siglaUnidade) {
        this.siglaUnidade = siglaUnidade;
    }

    public UnidadeVO getUnidadeVinculadora() {
        return unidadeVinculadora;
    }

    public void setUnidadeVinculadora(UnidadeVO unidadeVinculadora) {
        this.unidadeVinculadora = unidadeVinculadora;
    }

    public List<UnidadeVO> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<UnidadeVO> unidades) {
        this.unidades = unidades;
    }

    public UFVO getUf() {
        return uf;
    }

    public void setUf(UFVO uf) {
        this.uf = uf;
    }

    public MunicipioVO getMunicipio() {
        return municipio;
    }

    public void setMunicipio(MunicipioVO municipio) {
        this.municipio = municipio;
    }

    public TipoUnidadeVO getTipoUnidade() {
        return tipoUnidade;
    }

    public void setTipoUnidade(TipoUnidadeVO tipoUnidade) {
        this.tipoUnidade = tipoUnidade;
    }

    public Set<UFVO> getUfs() {
        return ufs;
    }

    // TODO #RC_SIRED Avaliar necessidade
    public String getUFAsString() {
        String retonoStr = "";
        try {
            List<UFVO> list = new ArrayList<UFVO>(ufs);
            Collections.sort(list);
            if (list != null && list.size() > 0) {
                for (UFVO obj : list) {
                    String nome = obj.getNome() + " (" + obj.getId() + ")";
                    retonoStr += !retonoStr.equalsIgnoreCase("") ? ", " + nome : nome;

                }
            }
        } catch (LazyInitializationException e) {
            return "";
        }

        return retonoStr;
    }

    public void setUfs(Set<UFVO> ufs) {
        this.ufs = ufs;
    }

    public Set<UnidadeGrupoVO> getUnidadeGrupos() {
        return unidadeGrupos;
    }

    public void setUnidadeGrupos(Set<UnidadeGrupoVO> unidadeGrupos) {
        this.unidadeGrupos = unidadeGrupos;
    }

    public String getGrupoAsString() {
        String retonoStr = "";
        this.getUnidadeGrupos();
        try {
            List<UnidadeGrupoVO> list = new ArrayList<UnidadeGrupoVO>(unidadeGrupos);
            Collections.sort(list);
            if (list != null && list.size() > 0) {
                for (UnidadeGrupoVO obj : list) {
                    String nome = obj.getGrupo() != null ? obj.getGrupo().getNome().toString() : "";
                    retonoStr += !retonoStr.equalsIgnoreCase("") ? ", " + nome : nome;

                }
            }
        } catch (LazyInitializationException e) {
            return "";
        }
        return retonoStr;
    }

    public List<BaseVO> getBases() {
        return bases;
    }

    public void setBases(List<BaseVO> bases) {
        this.bases = bases;
    }

    public List<RequisicaoVO> getRequisicoes() {
        return requisicoes;
    }

    // TODO #RC_SIRED Avaliar necessidade
    public String getRetricoesAsString() {
        String str = "";
        try {
            List<UnidadeGrupoVO> list = new ArrayList<UnidadeGrupoVO>(unidadeGrupos);
            Collections.sort(list);
            for (UnidadeGrupoVO unidade : unidadeGrupos) {
                unidade.getOperacao().toArray();
                if (unidade.getOperacao() != null && unidade.getOperacao().size() > 0) {
                    for (OperacaoVO obj : unidade.getOperacao()) {
                        String descricao = unidade.getGrupo().getNome() + " - " + obj.getDescricaoCompleta();
                        str += !str.equalsIgnoreCase("") ? ", " + descricao : descricao;
                    }
                }
            }
        } catch (LazyInitializationException e) {
            return "";
        }

        return str;
    }

    public void setRequisicoes(List<RequisicaoVO> requisicoes) {
        this.requisicoes = requisicoes;
    }

    public List<RequisicaoDocumentoVO> getRequisicaoDocumentos() {
        return requisicaoDocumentos;
    }

    public void setRequisicaoDocumentos(List<RequisicaoDocumentoVO> requisicaoDocumentos) {
        this.requisicaoDocumentos = requisicaoDocumentos;
    }

    @Override
    public int compareTo(UnidadeVO o) {
        return this.nome.compareTo(o.getNome());
    }

    // TODO #RC_SIRED Avaliar necessidade
    public String getDescricaoCompleta() {
        try {
            return this.getId() + " - " + this.getNome();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }

    }

    public String getEnderecoEmail() {

        String email = StringUtils.EMPTY;

        if (tipoUnidade.getId().equals(Long.valueOf(SiredUtils.NUM_SUPERINTENDENCIA))) {
            email = SiredUtils.ABREV_SUPERINTENDENCIA + StringUtils.leftPad(id.toString(), SiredUtils.NUM_CHARS_CGC, '0') + uf.getId().toString().trim();
        } else if (tipoUnidade.getId().equals(Long.valueOf(SiredUtils.NUM_CHARS_AGENCIA))) {
            email = SiredUtils.ABREV_AGENCIA + StringUtils.leftPad(id.toString(), SiredUtils.NUM_CHARS_CGC, '0') + uf.getId().toString().trim();
        } else if (tipoUnidade.getId().equals(Long.valueOf(SiredUtils.NUM_CHARS_PAB))) {
            email = SiredUtils.ABREV_PAB + StringUtils.leftPad(id.toString(), SiredUtils.NUM_CHARS_CGC, '0') + uf.getId().toString().trim();
        } else if (tipoUnidade.getId().equals(Long.valueOf(SiredUtils.NUM_CHARS_PA))) {
            email = SiredUtils.ABREV_PA + StringUtils.leftPad(id.toString(), SiredUtils.NUM_CHARS_CGC, '0') + uf.getId().toString().trim();
        } else if (!ObjectUtils.isNullOrEmpty(siglaUnidade) && (!ObjectUtils.isNullOrEmpty(siglaLocalizacao))) {
            email = siglaUnidade.trim() + siglaLocalizacao.trim();
        } else {
            return null;
        }

        return email += SiredUtils.DOMINIO_EMAIL;
    }
    
    @Deprecated
    public boolean equalsViewSiico(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UnidadeVO other = (UnidadeVO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (municipio == null) {
            if (other.municipio != null)
                return false;
        } else if (other.municipio == null) {
            return false;
        } else if (!municipio.getId().equals(other.municipio.getId()))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (!nome.trim().equals(other.nome.trim()))
            return false;
        if (siglaLocalizacao == null) {
            if (other.siglaLocalizacao != null)
                return false;
        } else if (other.siglaLocalizacao == null) {
            return false;
        } else if (!siglaLocalizacao.trim().equals(other.siglaLocalizacao.trim()))
            return false;
        if (siglaUnidade == null) {
            if (other.siglaUnidade != null)
                return false;
        } else if (other.siglaUnidade == null) {
            return false;
        } else if (!siglaUnidade.trim().equals(other.siglaUnidade.trim()))
            return false;
        if (icAtivo == null) {
            if (other.icAtivo != null)
                return false;
        } else if (!icAtivo.equals(other.icAtivo))
            return false;
        if (tipoUnidade == null) {
            if (other.tipoUnidade != null)
                return false;
        } else if (other.tipoUnidade == null) {
            return false;
        } else if (!tipoUnidade.getId().equals(other.tipoUnidade.getId()))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (other.uf == null) {
            return false;
        } else if (!uf.getId().equals(other.uf.getId()))
            return false;
        if (unidadeVinculadora == null) {
            if (other.unidadeVinculadora != null)
                return false;
        } else if (other.unidadeVinculadora == null) {
            return false;
        } else if (!unidadeVinculadora.getId().equals(other.unidadeVinculadora.getId()))
            return false;
        return true;
    }

    public int hashCodeSync() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((icAtivo == null) ? 0 : icAtivo.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((municipio == null) ? 0 : municipio.getId().hashCode());
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        result = prime * result + ((siglaLocalizacao == null) ? 0 : siglaLocalizacao.hashCode());
        result = prime * result + ((siglaUnidade == null) ? 0 : siglaUnidade.hashCode());
        result = prime * result + ((tipoUnidade == null) ? 0 : tipoUnidade.getId().hashCode());
        result = prime * result + ((uf == null) ? 0 : uf.getId().hashCode());
        result = prime * result + ((unidadeVinculadora == null) ? 0 : unidadeVinculadora.getId().hashCode());
        return result;
    }

    public boolean equalsForSync(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof UnidadeVO))
            return false;
        UnidadeVO other = (UnidadeVO) obj;
        if (icAtivo == null) {
            if (other.icAtivo != null)
                return false;
        } else if (!icAtivo.equals(other.icAtivo))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (municipio == null) {
            if (other.municipio != null)
                return false;
        } else if (!municipio.equals(other.municipio))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (!nome.equals(other.nome))
            return false;
        if (siglaLocalizacao == null) {
            if (other.siglaLocalizacao != null)
                return false;
        } else if (!siglaLocalizacao.equals(other.siglaLocalizacao))
            return false;
        if (siglaUnidade == null) {
            if (other.siglaUnidade != null)
                return false;
        } else if (!siglaUnidade.equals(other.siglaUnidade))
            return false;
        if (tipoUnidade == null) {
            if (other.tipoUnidade != null)
                return false;
        } else if (!tipoUnidade.equals(other.tipoUnidade))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (!uf.equals(other.uf))
            return false;
        if (unidadeVinculadora == null) {
            if (other.unidadeVinculadora != null)
                return false;
        } else if (!unidadeVinculadora.equals(other.unidadeVinculadora))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.getDescricaoCompleta();
    }
}
