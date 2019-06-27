package br.gov.caixa.gitecsa.sired.arquitetura.vo;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 3155141040043694691L;

    public abstract Object getId();

    public abstract void setId(Object id);

    public abstract String getColumnOrderBy();

    public abstract String getAuditoria();

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	
	if (obj == null) {
	    return false;
	}
	
	if (getClass() != obj.getClass()) {
	    return false;
	}
	
	BaseEntity other = (BaseEntity) obj;
	
	if (getId() == null) {
	    if (other.getId() != null) {
		return false;
	    }
	} else if (!getId().equals(other.getId())) {
	    return false;
	}
	
	return true;
    }

}
