package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.UnidadeDAO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class UnidadeService extends AbstractService<UnidadeVO> {

    private static final long serialVersionUID = 8116241403393445697L;

    @Inject
    private UnidadeDAO unidadeDAO;

    public UnidadeVO findByIdEager(Long id) {
        return unidadeDAO.findByIdEager(id);
    }

    @Override
    protected void validaRegras(UnidadeVO entity) {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(UnidadeVO entity) {
        // do nothing
    }

    @Override
    protected void validaCamposObrigatorios(UnidadeVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected GenericDAO<UnidadeVO> getDAO() {
        return unidadeDAO;
    }
    
    @Deprecated
    public UnidadeVO findUnidadeLotacaoUsuarioLogado() {
        UsuarioLdap usuario = JSFUtil.getUsuario();

        return unidadeDAO.findByIdEager(usuario.getCoUnidade().longValue());
    }

    public UnidadeVO findUnidadeAutorizada(UnidadeVO unidade, UsuarioLdap usuario) throws BusinessException {

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

                UnidadeVO unidadePesquisada = this.unidadeDAO.findByUnidadeVinculadora(unidade, unidadeUsuario);
                if (unidadePesquisada != null)
                    return unidadePesquisada;

                // Verifica se a unidade pesquisada está incluída na abrangência
                // da unidade do usuário autenticado
                unidade = this.unidadeDAO.findById((Long) unidade.getId());
                if (unidade != null) {
                    // Verifica se:
                    // A unidade do usuário é uma base e uma CTDA
                    // A unidade pesquisada está incluída na abrangência (UFs)
                    // da CTDA
                    // Existe restrição de inclusão ou exclusão para a unidade
                    // pesquisada
                    unidadePesquisada = this.unidadeDAO.findByAbrangenciaCtda(unidade, unidadeUsuario);
                    if (unidadePesquisada != null) {
                        return unidadePesquisada;
                    }

                    // Verifica se:
                    // A unidade do usuário é uma área-meio
                    // Caso positivo, se a unidade pesquisada está relacionada a essa área-meio
                    unidadePesquisada = this.unidadeDAO.findByAbrangenciaUf(unidadeUsuario);
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
    
    public Boolean isUnidadeAutorizada(UnidadeVO unidade, UsuarioLdap usuario) {
        try {
            this.findUnidadeAutorizada(unidade, usuario);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<UnidadeVO> findAllByPerfil(UsuarioLdap usuario) {

        // Gestores e Auditores: podem ver dados de qualquer unidade desde que
        // relacionadas à uma base
        if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR) || JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {
            return this.unidadeDAO.findAllComBase();
        }

        // Os demais usuários podem ver dados das unidade subordinadas à sua
        // unidade de lotação
        UnidadeVO unidadeUsuario = new UnidadeVO();
        unidadeUsuario.setId(usuario.getCoUnidade().longValue());

        return this.unidadeDAO.findAllByUnidadeVinculadora(unidadeUsuario);
    }

}
