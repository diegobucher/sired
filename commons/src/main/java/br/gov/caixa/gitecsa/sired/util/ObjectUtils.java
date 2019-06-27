package br.gov.caixa.gitecsa.sired.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;

public class ObjectUtils {

    @SuppressWarnings("rawtypes")
    public static boolean isNullOrEmpty(Object pObjeto) {

        if (pObjeto == null) {
            return true;
        } else if (pObjeto instanceof Collection) {
            return ((Collection) pObjeto).isEmpty();
        } else if (pObjeto instanceof List) {
            return ((List) pObjeto).isEmpty();
        } else if (pObjeto instanceof Map) {
            return ((Map) pObjeto).isEmpty();
        } else if (pObjeto instanceof String) {
            return StringUtils.isBlank((String) pObjeto);
        } else if (pObjeto instanceof UploadedFile) {
            return (pObjeto == null || ((UploadedFile) pObjeto).getSize() <= 0L);
        }

        return false;
    }

    public static Object defaultIfNull(Object object, Object value) {
        return org.apache.commons.lang.ObjectUtils.defaultIfNull(object, value);
    }

    /**
     * Invoca um método set de um atributo por reflexão.
     * 
     * @param field
     *            Atributo
     * @param value
     *            Valor do atributo
     * @return Object
     * @throws AppException
     *             Dispara um <b>AppException</b> caso não seja possível executar o método informado
     */
    public static Object invokeSet(Object instance, final Field field, final Object value) throws AppException {

        Class<?> objClass = instance.getClass();
        String method = String.format("set%s", StringUtils.capitalize(field.getName()));

        try {
            Method objMethod = objClass.getMethod(method, field.getType());
            objMethod.setAccessible(true);
            return objMethod.invoke(instance, value);

        } catch (NoSuchMethodException nsme) {
            throw new AppException(MensagemUtils.obterMensagem("geral.exception.NoSuchMethod", method), nsme);
        } catch (IllegalArgumentException iae) {
            throw new AppException(MensagemUtils.obterMensagem("geral.exception.IllegalArgument", method), iae);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    /**
     * Invoca um método get de um atributo por reflexão.
     * 
     * @param field
     *            Atributo
     * @return Object
     * @throws AppException
     *             Dispara um <b>AppException</b> caso não seja possível executar o método informado
     */
    public static Object invokeGet(Object instance, final Field field) throws AppException {

        Class<?> objClass = instance.getClass();
        String method = String.format("get%s", StringUtils.capitalize(field.getName()));

        try {
            Method objMethod = objClass.getMethod(method);
            objMethod.setAccessible(true);
            return objMethod.invoke(instance);

        } catch (NoSuchMethodException nsme) {
            throw new AppException(MensagemUtils.obterMensagem("geral.exception.NoSuchMethod", method), nsme);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    /**
     * Realiza a cópia de um objeto informado.
     * 
     * @param o
     *            Objeto à ser copiado
     * @return Uma nova instância com a cópia dos atributos do objeto passado como parâmetro.
     */
    public static Object clone(Serializable o) {
        try {
            return SerializationUtils.clone(o);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tenta obter uma instância de uma classe através de lookup
     * 
     * @param application
     * @param name
     * @param className
     * @return Object
     * @throws NamingException
     */
    public static Object lookup(final String application, final String name, final String className) throws NamingException {

        NameClassPair obj = null;
        Context context = new InitialContext();
        NamingEnumeration<NameClassPair> contextList = context.list(name);

        while (contextList.hasMore()) {
            obj = contextList.next();
            if (obj.getName().toLowerCase().contains(application.toLowerCase())) {
                return context.lookup(name + obj.getName() + "/" + className);
            }
        }

        return null;
    }

    /**
     * Verifica se o campo é do tipo Data/Calendar tags: #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo Data/Calendar e <b>False</b> caso contrário
     */
    public static Boolean isCampoData(CampoVO campo) {
        if (campo.getTipo().equals(TipoCampoEnum.DATA)) {
            return true;
        }
        return false;
    }

    /**
     * Verifica se o campo é do tipo Numérico tags: #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo Numérico e <b>False</b> caso contrário
     */
    public static Boolean isCampoNumerico(CampoVO campo) {
        if (campo.getTipo().equals(TipoCampoEnum.NUMERICO)) {
            return true;
        }
        return false;
    }
}
