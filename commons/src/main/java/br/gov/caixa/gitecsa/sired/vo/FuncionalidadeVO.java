package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs06_funcionalidade", schema = Constantes.SCHEMADB_NAME)
public class FuncionalidadeVO extends BaseEntity {

    private static final long serialVersionUID = 1110139479790431765L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_funcionalidade", nullable = false, columnDefinition = "serial")
    private Long id;

    @Column(name = "no_funcionalidade")
    private String nome;

    @Column(name = "no_link_funcionalidade", length = 255, columnDefinition = "bpchar")
    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_funcionalidade_s06", columnDefinition = "int2", nullable = true)
    private FuncionalidadeVO funcionalidadePai;
    
    @Column(name = "ic_tipo_acesso", columnDefinition = "int2")
    private Integer tipoAcesso;

    @OneToMany(mappedBy = "funcionalidade", fetch = FetchType.LAZY)
    private List<PerfilFuncionalidadeVO> listaPerfilFuncionalidadeVO;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public FuncionalidadeVO getFuncionalidadePai() {
        return funcionalidadePai;
    }

    public void setFuncionalidadePai(FuncionalidadeVO funcionalidadePai) {
        this.funcionalidadePai = funcionalidadePai;
    }

    public Integer getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(Integer tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }

    public List<PerfilFuncionalidadeVO> getListaPerfilFuncionalidadeVO() {
        return listaPerfilFuncionalidadeVO;
    }

    public void setListaPerfilFuncionalidadeVO(List<PerfilFuncionalidadeVO> listaPerfilFuncionalidadeVO) {
        this.listaPerfilFuncionalidadeVO = listaPerfilFuncionalidadeVO;
    }

    public String getNomeFuncionalidadePaiFilha() {
        return (this.funcionalidadePai != null && this.funcionalidadePai.getNome() != null ? this.funcionalidadePai.getNome() + " - " : "").concat(this.nome);
    }

}
