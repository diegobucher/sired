package br.gov.caixa.gitecsa.sired.vo;

import java.util.ArrayList;
import java.util.List;

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
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

@Entity
@Table(name = "redtbc11_ctda_uf", schema = Constantes.SCHEMADB_NAME)
public class CTDAUFVO extends BaseEntity implements Comparable<CTDAUFVO> {

    private static final long serialVersionUID = -7545727055321684075L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_ctda_uf", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sg_uf_a03")
    private UFVO uf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_ctda_c10", columnDefinition = "int2")
    private CTDAVO ctda;

    @OneToMany(mappedBy = "ctdaUF", fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
    private List<CTDAUFRestricaoVO> ctdaUFRestricoes;

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

    public UFVO getUf() {
        return uf;
    }

    public void setUf(UFVO uf) {
        this.uf = uf;
    }

    public CTDAVO getCtda() {
        return ctda;
    }

    public void setCtda(CTDAVO ctda) {
        this.ctda = ctda;
    }

    public boolean existeCtdaUFRestricoes() {
        if (!ObjectUtils.isNullOrEmpty(ctdaUFRestricoes)) {
            return ctdaUFRestricoes.size() > 0;
        }
        return false;
    }

    public List<CTDAUFRestricaoVO> getCtdaUFRestricoes() {
        return ctdaUFRestricoes;
    }

    public void setCtdaUFRestricoes(List<CTDAUFRestricaoVO> ctdaUFRestricoes) {
        this.ctdaUFRestricoes = ctdaUFRestricoes;
    }

    @Override
    public int compareTo(CTDAUFVO o) {
        return this.getUf().getNome().compareToIgnoreCase(o.getUf().getNome());
    }

    @Transient
    public String getResticaoAsString() {

        String retorno = StringUtils.EMPTY;
        List<String> listInclusacao = new ArrayList<String>();
        List<String> listExclusacao = new ArrayList<String>();

        for (CTDAUFRestricaoVO obj : getCtdaUFRestricoes()) {
            if (obj.getTipoResticao().equals(TipoRestricaoEnum.INCLUIR)) {
                listInclusacao.add(obj.getUnidade().getNome());
            } else {
                listExclusacao.add(obj.getUnidade().getNome());
            }
        }

        if (!listInclusacao.isEmpty()) {
            retorno += String.format("- %s: %s", TipoRestricaoEnum.INCLUIR.getDescricaoTela(), StringUtils.join(listInclusacao, ", "));
        }

        if (!listExclusacao.isEmpty()) {
            retorno += String.format("- %s: %s", TipoRestricaoEnum.EXCLUIR.getDescricaoTela(), StringUtils.join(listExclusacao, ", "));
        }

        return retorno;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ctda == null) ? 0 : ctda.hashCode());
        result = prime * result + ((ctdaUFRestricoes == null) ? 0 : ctdaUFRestricoes.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((uf == null) ? 0 : uf.hashCode());
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
        CTDAUFVO other = (CTDAUFVO) obj;
        if (ctda == null) {
            if (other.ctda != null)
                return false;
        } else if (!ctda.equals(other.ctda))
            return false;
        if (ctdaUFRestricoes == null) {
            if (other.ctdaUFRestricoes != null)
                return false;
        } else if (!ctdaUFRestricoes.equals(other.ctdaUFRestricoes))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (!uf.equals(other.uf))
            return false;
        return true;
    }

}
