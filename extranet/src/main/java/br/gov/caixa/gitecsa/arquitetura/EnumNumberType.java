package br.gov.caixa.gitecsa.arquitetura;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

public class EnumNumberType implements UserType, ParameterizedType {

    private Class<?> enumClass;

    public int[] sqlTypes() {
        return new int[] { StandardBasicTypes.LONG.sqlType() };
    }

    public Class<?> returnedClass() {
        return enumClass;
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] ids, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return rs.wasNull() || Long.valueOf(rs.getLong(ids[0])) == null ? null : BaseEnumNumberValue.class.getEnumConstants()[rs.getInt(ids[0]) - 1];
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, StandardBasicTypes.LONG.sqlType());
        } else {
            st.setLong(index, ((BaseEnumNumberValue) value).getValue());
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof BaseEnumNumberValue) {
            BaseEnumNumberValue enumVal = (BaseEnumNumberValue) value;
            return BaseEnumNumberValue.class.getEnumConstants()[enumVal.getValue().intValue() - 1];
        }
        return null;
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        Object deepCopy = deepCopy(value);

        if (!(deepCopy instanceof Serializable))
            return (Serializable) deepCopy;

        return null;

    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public void setParameterValues(Properties parameters) {

        String className = parameters.getProperty("enumClassName");

        try {
            enumClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y)
            return true;
        if (null == x || null == y)
            return false;
        return x.equals(y);
    }
}
