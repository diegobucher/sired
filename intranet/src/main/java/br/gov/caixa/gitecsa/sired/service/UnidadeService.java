package br.gov.caixa.gitecsa.sired.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.UnidadeDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoUnidadeEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless(name = "unidadeGestoraService")
public class UnidadeService extends AbstractService<UnidadeVO> {

    private static final long serialVersionUID = 4860119789270226754L;

    @Inject
    private UnidadeDAO dao;

    @Override
    protected void validaCamposObrigatorios(UnidadeVO entity) {

    }

    @Override
    protected void validaRegras(UnidadeVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(UnidadeVO entity) {

    }

    @Override
    protected GenericDAO<UnidadeVO> getDAO() {
        return this.dao;
    }

    /**
     * Obtém a unidade de lotação do usuário logado
     * 
     * @return Unidade de lotação do usuário
     * @throws DataBaseException 
     */
    public UnidadeVO findUnidadeLotacaoUsuarioLogado() throws DataBaseException {

        UnidadeVO unidade = new UnidadeVO();
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        unidade = this.dao.findByIdEager(usuario.getCoUnidade().longValue());

        return unidade;
    }

    /**
     * Caso a unidade não seja área-meio retorna <code>true</code>. Caso não existam restrições para a unidade retorna <code>true</code>.
     * Caso contrário, caso o grupo do documento esteja na relação das restrições para a unidade retorna <code>true</code>. Caso contrário retorna
     * <code>false</code> .
     * 
     * @param documento
     * @param unidade
     * @return
     * @throws DataBaseException 
     */
    public Boolean hasPermissaoDocumentoUnidade(DocumentoVO documento, UnidadeVO unidade) throws DataBaseException {
        if (unidade.getTipoUnidade().getIndicadorUnidade().equals(TipoUnidadeEnum.PV.name())) {
            return true;
        }

        if (!this.dao.hasRestricaoUnidadeConfigurada(unidade)) {
            return true;
        }

        return this.dao.hasPermissaoDocumentoUnidade(documento, unidade);
    }

    /**
     * Obtém uma unidade de acordo com as regras de visibilidade previstas pela RN017 - Visualização das requisições e remessas
     * 
     * @param unidade
     * @param usuario
     * @return A unidade requisitada ou null caso a unidade não seja encontrada
     * @throws BusinessException
     *             Caso algum item da regra de negócio não seja atendida
     * @throws DataBaseException 
     */
    public UnidadeVO findUnidadeAutorizada(UnidadeVO unidade, UsuarioLdap usuario) throws BusinessException, DataBaseException {

        if (unidade != null && unidade.getId() != null) {

            // Caso a unidade pesquisada seja a mesma do usuário autenticado,
            // retornar demais dados da unidade
            if (((Long) unidade.getId()).equals(usuario.getCoUnidade().longValue())) {
                return this.findById((Long) unidade.getId());
            } else {

                // Verifica se a unidade pesquisada é subordinada à unidade do
                // usuário autenticado
                UnidadeVO unidadeUsuario = new UnidadeVO();
                unidadeUsuario.setId(usuario.getCoUnidade().longValue());

                UnidadeVO unidadePesquisada = this.dao.findByUnidadeVinculadora(unidade, unidadeUsuario);
                if (unidadePesquisada != null)
                    return unidadePesquisada;

                // Verifica se a unidade pesquisada está incluída na abrangência
                // da unidade do usuário autenticado
                unidade = this.dao.findById((Long) unidade.getId());
                if (unidade != null) {
                    // Verifica se:
                    // A unidade do usuário é uma base e uma CTDA
                    // A unidade pesquisada está incluída na abrangência (UFs)
                    // da CTDA
                    // Existe restrição de inclusão ou exclusão para a unidade
                    // pesquisada
                    unidadePesquisada = this.dao.findByAbrangenciaCtda(unidade, unidadeUsuario);
                    if (unidadePesquisada != null) {
                        return unidadePesquisada;
                    }

					// Caso a unidade solicitada não tenha UF, não é possível requisitar documentos para ela.
					if (ObjectUtils.isNullOrEmpty(unidade.getUf())) {
						throw new BusinessException(MensagemUtils.obterMensagem("MA063", String.format("%s - %s", unidade.getId(), unidade.getNome())));
					}
					
                    // Verifica se:
                    // A unidade do usuário é uma área-meio
                    // Caso positivo, se a unidade pesquisada está relacionada a essa área-meio
                    unidadePesquisada = this.dao.findByAbrangenciaUf(unidadeUsuario);
                    if (unidadePesquisada != null && unidadePesquisada.getUfs().contains(unidade.getUf())) {
                        return unidade;
                    }

                    throw new BusinessException(MensagemUtils.obterMensagem("MA040", String.format("%s - %s", unidade.getId(), unidade.getNome())));
                } else {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA020"));
                }
            }
        }

        return null;
    }

    /**
     * Lista todas as unidades permitidas para o perfil do usuário
     * 
     * @param usuario
     * @return A lista de unidades permitidas para o usuário
     * @throws DataBaseException 
     */
    public List<UnidadeVO> findAllByPerfil(UsuarioLdap usuario) throws DataBaseException {

        // Gestores e Auditores: podem ver dados de qualquer unidade desde que
        // relacionadas à uma base
        if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR) || JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {
            return this.dao.findAllComBase();
        }

        // Os demais usuários podem ver dados das unidade subordinadas à sua
        // unidade de lotação
        UnidadeVO unidadeUsuario = new UnidadeVO();
        unidadeUsuario.setId(usuario.getCoUnidade().longValue());

        return this.dao.findAllByUnidadeVinculadora(unidadeUsuario);
    }

    /**
     * Obtém uma unidade de acordo com as regras de visibilidade previstas pela RN016 - Subordinação
     * 
     * @param unidade
     * @param usuario
     * @return A unidade requisitada ou null caso a unidade não seja encontrada
     * @throws BusinessException
     *             Caso algum item da regra de negócio não seja atendida
     * @throws DataBaseException 
     */
    public UnidadeVO findUnidadePV(UnidadeVO unidade, UsuarioLdap usuario) throws BusinessException, DataBaseException {

        if (unidade != null && unidade.getId() != null) {

            // Caso a unidade pesquisada seja a mesma do usuário autenticado,
            // retornar demais dados da unidade
            if (((Long) unidade.getId()).equals(usuario.getCoUnidade().longValue())) {
                return this.findById((Long) unidade.getId());
            } else {

                // Verifica se a unidade pesquisada é subordinada à unidade do
                // usuário autenticado
                UnidadeVO unidadeUsuario = new UnidadeVO();
                unidadeUsuario.setId(usuario.getCoUnidade().longValue());

                UnidadeVO unidadePesquisada = this.dao.findByUnidadeVinculadora(unidade, unidadeUsuario);
                if (unidadePesquisada != null) {
                    return unidadePesquisada;
                } else {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA020"));
                }
            }
        }

        return null;
    }

    /**
     * Verifica se a unidade consultada está permitida para o usuário.
     * 
     * @param unidade
     * @param usuario
     * @return <b>True</b> se a unidade está autorizada para o usuário e <b>false</b>.
     */
    public Boolean isUnidadeAutorizada(UnidadeVO unidade, UsuarioLdap usuario) {
        try {
            this.findUnidadeAutorizada(unidade, usuario);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<UnidadeVO> findAllByEager() throws DataBaseException {
        return this.dao.findAllByEager();
    }

    @Override
    public Map<Object, UnidadeVO> findAllAsMap() throws DataBaseException {

        List<UnidadeVO> listEntity = this.findAllByEager();
        Map<Object, UnidadeVO> mapEntity = new HashMap<Object, UnidadeVO>();

        for (UnidadeVO entity : listEntity) {
            mapEntity.put(entity.getId(), entity);
        }

        return mapEntity;
    }
}
