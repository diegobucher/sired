package br.gov.caixa.gitecsa.sired.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc10_ctda", schema = Constantes.SCHEMADB_NAME)
public class CTDAVO extends BaseEntity {

    private static final long serialVersionUID = 1440555875900789259L;

    private static final String ORDER_BY_NOME = "nome";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_ctda", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_ctda")
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_municipio_a08", columnDefinition = "int2")
    private MunicipioVO municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int2")
    private BaseVO base;

    @OneToMany(orphanRemoval = true, mappedBy = "ctda", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
    private Set<CTDAUFVO> ctdaUF;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public MunicipioVO getMunicipio() {
        return municipio;
    }

    public void setMunicipio(MunicipioVO municipio) {
        this.municipio = municipio;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public Set<CTDAUFVO> getCtdaUF() {
        return ctdaUF;
    }

    public void setCtdaUF(Set<CTDAUFVO> ctdaUF) {
        this.ctdaUF = ctdaUF;
    }

    @Transient
    public String getUFAsString() {
        List<String> listUf = new ArrayList<String>();
        List<CTDAUFVO> listOrdenada = new ArrayList<CTDAUFVO>(this.ctdaUF);
        Collections.sort(listOrdenada);

        for (CTDAUFVO item : listOrdenada) {
            // listUf.add(String.format("%s (%s%s)", item.getUf().getNome(), item.getUf().getId(), item.getResticaoAsString()));

            StringBuilder abrangencia = new StringBuilder();
            abrangencia.append(String.format("%s (%s)", item.getUf().getNome(), item.getUf().getId()));
            if (item.existeCtdaUFRestricoes()) {
                abrangencia.append("*");
            }
            listUf.add(abrangencia.toString());
        }
        return StringUtils.join(listUf, ", ");
    }

}
