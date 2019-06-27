package br.gov.caixa.gitecsa.sired.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class MinhasRequisicoesService extends PaginatorService<RequisicaoVO> {

    private static final long serialVersionUID = 1674767751955948635L;

    @Inject
    private RequisicaoDAO dao;

    @Inject
    private ParametroSistemaService parametroSistemaService;

    private Integer count;

    @Override
    protected void validaCamposObrigatorios(RequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(RequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(RequisicaoVO entity) {

    }

    @Override
    protected GenericDAO<RequisicaoVO> getDAO() {
        return this.dao;
    }

    @Override
    public List<RequisicaoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException {
        List<RequisicaoVO> listRequisicoes = this.pesquisarUltimasRequisicoesUsuario(JSFUtil.getUsuario());
        this.count = listRequisicoes.size();
        return listRequisicoes;
    }

    @Override
    public Integer count(Map<String, Object> filters) {
        return this.count;
    }

    private List<RequisicaoVO> pesquisarUltimasRequisicoesUsuario(UsuarioLdap usuario) throws DataBaseException {
        ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_QTD_MINHAS_REQUISICOES);
        return this.dao.findUltimasRequisicoesUsuario(usuario, Integer.valueOf(parametro.getVlParametroSistema()));
    }

}
