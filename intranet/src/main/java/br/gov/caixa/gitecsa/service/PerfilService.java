package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilFuncionalidadeVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilVO;

@Stateless
public class PerfilService extends AbstractService<PerfilVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    private PerfilFuncionalidadeService perfilFuncService;

    public PerfilFuncionalidadeService getPerfilFuncService() {
        return perfilFuncService;
    }

    public void setPerfilFuncService(PerfilFuncionalidadeService perfilFuncService) {
        this.perfilFuncService = perfilFuncService;
    }

    @Override
    protected void validaCampos(PerfilVO entity) {
    }

    @Override
    protected void validaRegras(PerfilVO entity) {
        validaDuplicidade(entity);

    }

    public void salvarFuncionalidade(PerfilFuncionalidadeVO pfVO) throws AppException {
        perfilFuncService.save(pfVO);
    }

    private void validaDuplicidade(PerfilVO entity) {
        List<PerfilVO> lista = findAll();
        for (PerfilVO objVo : lista) {
            if (entity.getId() == null) {
                if (objVo.getDescricao().equalsIgnoreCase(entity.getDescricao()) || objVo.getSigla().equalsIgnoreCase(entity.getSigla())) {
                    mensagens.add(MensagemUtils.obterMensagem("MI018"));
                    break;
                }
            } else {
                if ((!objVo.getId().equals(entity.getId()))
                        && (objVo.getDescricao().equalsIgnoreCase(entity.getDescricao()) || objVo.getSigla().equalsIgnoreCase(entity.getSigla()))) {
                    mensagens.add(MensagemUtils.obterMensagem("MI018"));
                    break;
                }

            }
        }
    }

    @Override
    protected void validaRegrasExcluir(PerfilVO entity) {
    }

    public PerfilVO getPerfilFuncionalidade(PerfilVO perfil) {
        try {
            Criteria criteria = getSession().createCriteria(PerfilVO.class).createAlias("perfilFuncionalidade", "perfilFuncionalidade",
                    JoinType.LEFT_OUTER_JOIN);

            if (!Util.isNullOuVazio(perfil.getId())) {
                criteria.add(Restrictions.eq("this.id", perfil.getId()));
            }

            return (PerfilVO) criteria.uniqueResult();

        } catch (Exception ej) {
            @SuppressWarnings("unused")
            DataBaseException edb = new DataBaseException("Falha ao acessar os dados. Contate o suporte.");
        }

        return null;
    }

    /**
     * Lista todos os perfis cadastrados
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<PerfilVO> findAll() {

        Criteria criteria = getSession().createCriteria(PerfilVO.class).createAlias("perfilFuncionalidade", "funcionalidade", JoinType.LEFT_OUTER_JOIN);

        criteria.addOrder(Order.asc("descricao"));
        List<PerfilVO> perfis = mountPerfil(((List<PerfilVO>) criteria.list()));

        Collections.sort(perfis);

        return perfis;
    }

    public List<PerfilVO> mountPerfil(List<PerfilVO> perfis) {
        Map<Long, PerfilVO> mapPerfil = new HashMap<Long, PerfilVO>();
        for (PerfilVO perfilVO : perfis) {
            if (!mapPerfil.containsKey(perfilVO.getId())) {
                mapPerfil.put((Long) perfilVO.getId(), perfilVO);
            }
        }

        return (new ArrayList<PerfilVO>(mapPerfil.values()));
    }

    public void salvarListaFuncionalidade(PerfilVO instance) throws AppException {
        super.update(instance);
    }

    public PerfilVO getPerfilPorSigla(String perfil) {
        try {
            Criteria criteria = getSession().createCriteria(PerfilVO.class);

            if (!Util.isNullOuVazio(perfil)) {
                criteria.add(Restrictions.eq("this.sigla", perfil));

                PerfilVO perfilVO = (PerfilVO) criteria.uniqueResult();

                return perfilVO;
            }
        } catch (Exception ej) {
            @SuppressWarnings("unused")
            DataBaseException edb = new DataBaseException("Falha ao acessar os dados. Contate o suporte.");
        }

        return null;
    }

    public Boolean isGerente(Integer nu_funcao) {
        if (nu_funcao != null) {
            Criteria criteria = entityManager.unwrap(Session.class).createCriteria(ParametroSistemaVO.class);

            criteria.add(Restrictions.eq("noParametroSistema", "FUNCOES_GERENCIAIS"));

            ParametroSistemaVO parametroSistema = (ParametroSistemaVO) criteria.uniqueResult();

            if (parametroSistema != null) {
                String[] lista = parametroSistema.getVlParametroSistema().split(",");

                for (String funcao : lista) {
                    if (funcao.equals(nu_funcao.toString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public List<PerfilVO> obterPerfil(String pGrupoUsuario) {
        Criteria criteria = getSession().createCriteria(PerfilVO.class);

        criteria.add(Restrictions.in("descricao", pGrupoUsuario.toUpperCase().split(",")));

        return ((List<PerfilVO>) criteria.list());
    }

}
