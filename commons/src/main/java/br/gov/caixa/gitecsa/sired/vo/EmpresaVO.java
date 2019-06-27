package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;

@Entity
@Table(name = "redtbc12_empresa", schema = Constantes.SCHEMADB_NAME)
public class EmpresaVO extends BaseEntity {

    private static final long serialVersionUID = 4216868414958656789L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_empresa", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_caixa_postal")
    private String caixaPostal;

    @Column(name = "no_empresa")
    private String nome;

    @Column(name = "no_site")
    private String site;

    @Column(name = "nu_cnpj", columnDefinition = "numeric", length = 14)
    private Long cnpj;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<EmpresaContratoVO> empresaContratos;

    @Transient
    private EmpresaContratoVO empresaContratoExcluir;

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

    public String getCaixaPostal() {
        return caixaPostal;
    }

    public void setCaixaPostal(String caixaPostal) {
        this.caixaPostal = caixaPostal;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Long getCnpj() {
        return cnpj;
    }

    public void setCnpj(Long cnpj) {
        this.cnpj = cnpj;
    }

    public List<EmpresaContratoVO> getEmpresaContratos() {
        return empresaContratos;
    }

    public void setEmpresaContratos(List<EmpresaContratoVO> empresaContratos) {
        this.empresaContratos = empresaContratos;
    }

    public EmpresaContratoVO getEmpresaContratoExcluir() {
        return empresaContratoExcluir;
    }

    public void setEmpresaContratoExcluir(EmpresaContratoVO empresaContratoExcluir) {
        this.empresaContratoExcluir = empresaContratoExcluir;
    }
    
    @Transient
    public String getCnpjFormatado() {
        if (!ObjectUtils.isNullOrEmpty(this.cnpj)) {
            return SiredUtils.formatarCnpj(this.cnpj.toString());
        } else {
            return StringUtils.EMPTY;
        }
    }

}
