package br.gov.caixa.gitecsa.arquitetura.service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.FilterVisitor;

public class AppService<T extends BaseEntity> implements Serializable {

    /**
	 */
    private static final long serialVersionUID = 5746962210345825045L;

    @PersistenceContext(unitName = "siredPU")
    protected EntityManager entityManager;

    protected Criteria createCriteria(T object, FilterVisitor visitor) throws AppException {
        Criteria criteria = getSession().createCriteria(object.getClass());
        if (object.getId() != null) {
            criteria.add(Restrictions.idEq(object.getId()));
        }
        Example example = createExample(object);
        criteria.add(example);
        createSubCriteria(criteria, object, visitor);
        if (visitor != null) {
            visitor.visitCriteria(criteria);
        }

        return criteria;
    }

    @SuppressWarnings("rawtypes")
    private void createSubCriteria(Criteria criteria, BaseEntity object, FilterVisitor visitor) throws AppException {
        try {
            Field[] fields = object.getClass().getDeclaredFields();

            for (Field field : fields) {

                if (field != null
                        && (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)
                                || field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class))) {
                    String propertyName = field.getName();
                    Object sub = PropertyUtils.getProperty(object, propertyName);

                    if (sub != null && sub instanceof ArrayList && (((ArrayList) sub).size()) > 0) {
                        sub = ((ArrayList) sub).get(0);
                    }

                    if (sub != null && sub instanceof BaseEntity) {
                        Criteria subCriteria = null;
                        subCriteria = criteria.createCriteria(propertyName);
                        Object id = ((BaseEntity) sub).getId();

                        if (id != null) {
                            subCriteria.add(Restrictions.idEq(id));

                        } else {
                            Example ex = createExample(sub);
                            subCriteria.add(ex);
                            createSubCriteria(subCriteria, (BaseEntity) sub, visitor);
                            if (visitor != null) {
                                visitor.visitSubCriteria(subCriteria);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    private Example createExample(Object object) {

        Example example = Example.create(object).excludeZeroes().enableLike(MatchMode.ANYWHERE).ignoreCase();

        PropertySelector notNullOrEmptySelector = new PropertySelector() {

            private static final long serialVersionUID = 3872103103165444592L;

            public boolean include(Object object, String propertyName, org.hibernate.type.Type type) {
                return object != null && (!(object instanceof String) || !((String) object).equals(""));
            }
        };
        example.setPropertySelector(notNullOrEmptySelector);
        return example;
    }

    protected Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
}
