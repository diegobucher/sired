package br.gov.caixa.gitecsa.arquitetura.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.exception.ConstraintViolationException;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.interceptor.AppLogInterceptor;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.FilterVisitor;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

/**
 * Classe utilizada por todo serviço EJB que faz operação de CRUD e/ou precisa implementar regras de campos obrigatórios e regras negócio.
 * Contém métodos comuns que devem ser implementados, assim como métodos utilitários.
 * 
 * @param <T>
 */
@Stateless
@Interceptors(AppLogInterceptor.class)
public abstract class AbstractService<T extends BaseEntity> extends AppService<T> {

    /**
	 */

    private static final long serialVersionUID = 297699908122496343L;

    @Inject
    protected Logger logger;

    @Inject
    protected FacesMensager facesMessager;

    protected List<String> mensagens;

    private boolean editando;

    // =======================================================================================================
    // Template methods
    // =======================================================================================================

    protected abstract void validaCampos(T entity);

    protected abstract void validaRegras(T entity);

    protected abstract void validaRegrasExcluir(T entity) throws AppException;

    // =======================================================================================================
    // Implements methods
    // =======================================================================================================

    /**
     * Lista todas as entidades cadastradas na tabela que reprensenta a entidade anotada nesta interface.
     * 
     * @return Coleção contendo todas as entidades cadastradas na4 tabela que reprensenta a entidade anotada nesta interface.
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<T> findAll() {
        String campoOrderBy = null;
        try {
            Method m = getTypeClass().getDeclaredMethod("getColumnOrderBy");
            Class c = Class.forName(getTypeClass().getName());
            Object t = c.newInstance();
            campoOrderBy = (String) m.invoke(t);
        } catch (Exception e) {
            String men = "Erro de Ordenação do Campo";
            logger.info(men);
            new AppException(e);
        }

        try {
            if (campoOrderBy == null)
                return entityManager.createQuery(("FROM " + getTypeClass().getName())).getResultList();
            else
                return entityManager.createQuery(("FROM " + getTypeClass().getName()) + " ORDER BY " + campoOrderBy).getResultList();
        } catch (Exception e) {
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), LogUtils.getNomeFuncionalidade(getTypeClass().getName()), "findAll"));
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));

        }
        return null;

    }

    /**
     * Recupera a entidade a partir de seu identificador
     * 
     * @param id
     *            O identificador da entidade
     * @return A entidade com seus dados preenchidos a partir do banco de dados
     */
    @SuppressWarnings("unchecked")
    public T getById(Object id) {
        try {
            return (T) entityManager.find(getTypeClass(), id);
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            return null;

        }

    }

    /**
     * Insere a entidade no banco de dados.
     * 
     * @param entidade
     *            Entidade a ser inserida.
     * @return A entidade que está persistida no banco.
     * @throws AppException
     */

    public T save(T entity) throws AppException {
        processValidations(entity, false);
        try {
            return saveImpl(entity);
        } catch (Exception e) {
            throw new AppException(e.getMessage(), e);
        }
    }

    /**
     * Insere a entidade no banco de dados.
     * 
     * @param entidade
     *            Entidade a ser inserida.
     * @return A entidade que está persistida no banco.
     * @throws AppException
     */

    public T saveOrUpdate(T entity) throws AppException {
        processValidations(entity, false);
        try {
            if (entity.getId() == null)
                return saveImpl(entity);
            else
                return updateImpl(entity);
        } catch (Exception e) {
            throw new AppException(e.getMessage(), e);
        }
    }

    /**
     * Atualiza os valores da entidade no banco de dados.
     * 
     * @param entidade
     *            Entidade que terá os valores atualizados.
     * @return A entidade que está persistida no banco.
     * @throws AppException
     */
    public T update(T entity) throws AppException {
        processValidations(entity, true);
        try {

            return updateImpl(entity);
        } catch (Exception e) {
            throw new AppException(e.getMessage(), e);
        }

    }

    /**
     * Atualiza os valores de uma lista de objetos de determinada entidade.
     * 
     * @param listEntity
     * @return Retorna a lista das entidades atualizadas.
     * @throws RequiredException
     * @throws BusinessException
     * @throws Exception
     */
    public List<T> update(Collection<T> listEntity) throws AppException {
        processValidations(listEntity, true);
        try {

            return updateLote(listEntity);
        } catch (Exception e) {
            throw new AppException(e.getMessage(), e);
        }

    }

    /**
     * Exclui a entidade do banco de dados.
     * 
     * @param entidade
     *            Entidade a ser excluída.
     * @throws BusinessException
     * @throws AppException
     */
    public void delete(T entity) throws AppException {
        try {
            processDeleteValidations(entity);
            deleteImpl(entity);
        } catch (ConstraintViolationException cve) {
            throw new BusinessException(MensagemUtils.obterMensagem("MA006"));
        } catch (EJBTransactionRolledbackException be) {
            throw new BusinessException(MensagemUtils.obterMensagem("MA006"));
        } catch (BusinessException e) {
            throw new BusinessException(mensagens);
        } catch (AppException e) {
            throw new AppException(e.getMessage(), e);
        }

    }

    @SuppressWarnings("unchecked")
    public List<T> findByParameters(T object) throws AppException {
        Criteria criteria = createCriteria(object, null);

        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByParameters(T object, FilterVisitor visitor) throws AppException {
        Criteria criteria = createCriteria(object, visitor);

        return criteria.list();
    }

    // =======================================================================================================
    // IMPLEMENTAÇÕES DEFAULT
    // =======================================================================================================
    /**
     * Método que salva uma entidade. Se necessário, ele será sobrescrito para realizar algo antes e/ou depois da operação de save.
     * 
     * @param entity
     * @return
     * @throws AppException
     */
    protected T saveImpl(T entity) throws AppException {
        try {
            entityManager.persist(entity);
        } catch (Exception e) {
            mensagens.add(e.getMessage());
            if (!Util.isNullOuVazio(mensagens))
                throw new BusinessException(e.getMessage());
        }

        return entity;
    }

    /**
     * Método que atualiza uma entidade. Se necessário, ele será sobrescrito para realizar algo antes e/ou depois da operação de update.
     * 
     * @param entity
     * @return
     * @throws AppException
     */
    protected T updateImpl(T entity) throws AppException {

        try {

            entityManager.merge(entity);
            entityManager.flush();

        } catch (Exception e) {
            throw new AppException(e);
        }

        return entity;
    }

    /**
     * Implementação default do do método para fazer update em lote
     * 
     * @param listEntity
     * @return Retorna uma lista das entidades salvas.
     * @throws AppException
     */
    protected List<T> updateLote(Collection<T> listEntity) throws AppException {
        List<T> listReturn = new ArrayList<T>();
        for (T entity : listEntity) {
            listReturn.add(updateImpl(entity));
        }
        return listReturn;
    }

    /**
     * Método que remove uma entidade. Se necessário, ele será sobrescrito para realizar algo antes e/ou depois da operação de delete.
     * 
     * @param entity
     * @throws BusinessException
     * @throws Exception
     */
    protected void deleteImpl(T entity) throws AppException {

        T attachedEntity = entityManager.merge(entity);
        entityManager.remove(attachedEntity);

    }

    /**
     * Processa todas as validações implementadas no validaCamposObrigatorios e no validaRegras durante o save e o update.
     * 
     * @param entity
     *            Entidade a ser validada.
     * @param editando
     *            Parametro que será usado nos métodos das RNs.
     * @throws RequiredException
     *             Quando algum campo obrigatório não foi preenchido.
     * @throws BusinessException
     *             Quando alguma RN não foi atendida.
     */
    protected void processValidations(T entity, boolean editando) throws RequiredException, BusinessException {

        setEditando(editando);
        mensagens = new ArrayList<String>();

        validaCampos(entity);
        if (!Util.isNullOuVazio(mensagens))
            throw new RequiredException(mensagens);

        this.validaRegras(entity);
        if (!Util.isNullOuVazio(mensagens))
            throw new BusinessException(mensagens);
    }

    /**
     * Processa todas as validações implementadas no validaCamposObrigatorios e no validaRegras durante o save e o update.
     * 
     * @param listEntity
     *            lista da Entidade a ser validada.
     * @param editando
     *            Parametro que será usado nos métodos das RNs.
     * @throws RequiredException
     *             Quando algum campo obrigatório não foi preenchido.
     * @throws BusinessException
     *             Quando alguma RN não foi atendida.
     */
    protected void processValidations(Collection<T> listEntity, boolean editando) throws RequiredException, BusinessException {
        setEditando(editando);
        mensagens = new ArrayList<String>();

        for (T entity : listEntity) {
            validaCampos(entity);
        }
        if (!Util.isNullOuVazio(mensagens))
            throw new RequiredException("Campos Obrigatórios não preenchidos.", mensagens);

        for (T entity : listEntity) {
            validaRegras(entity);
        }
        if (!Util.isNullOuVazio(mensagens))
            throw new BusinessException("Regras de negócio não atendidas.", mensagens);
    }

    /**
     * Processa as RNs implementadas no validaRegrasExcluir durante o remove.
     * 
     * @param entity
     *            Entidade a ser validada.
     * @throws BusinessException
     *             Se alguma regra não foi atendida.
     */
    protected void processDeleteValidations(T entity) throws AppException {
        mensagens = new ArrayList<String>();
        try {
            validaRegrasExcluir(entity);
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }

        if (!Util.isNullOuVazio(mensagens))
            throw new BusinessException("Regras de negócio não atendidas.", mensagens);
    }

    private Class<?> getTypeClass() {
        Class<?> clazz = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return clazz;
    }

    // =======================================================================================================
    // MÉT0D0S DE ACESSO
    // =======================================================================================================
    protected boolean isEditando() {
        return editando;
    }

    protected void setEditando(boolean editando) {
        this.editando = editando;
    }

}
