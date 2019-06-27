package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.GrupoDAO;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

@Stateless
public class GrupoService extends AbstractService<GrupoVO> {

    private static final String EVENTO_OBTER_DEMANDAS = "obterDemandas";

    private static final String FUNCIONALIDADE_TIPO_DEMANDA = "TipoDemanda";

    private static final String EVENTO_FIND_ALL = "findAll";

    private static final String EVENTO_GET_BY_ID = "getById";

    private static final String FUNCIONALIDADE_GRUPO = "Grupo";

    private static final long serialVersionUID = -3284205211460584589L;

    @Inject
    private GrupoDAO grupoDAO;

    @Override
    protected void validaRegras(GrupoVO entity) {
        duplicidade(entity);
    }

    private void duplicidade(GrupoVO entity) {
        List<GrupoVO> lista = grupoDAO.findAll();

        for (GrupoVO obj : lista) {
            if (entity.getId() == null) {
                if (obj.getNome().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.INFORMACAO_CRUD_DUPLICIDADE));
                    break;
                }
            } else {
                if ((!obj.getId().equals(entity.getId())) && obj.getNome().trim().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.INFORMACAO_CRUD_DUPLICIDADE));
                    break;
                }

            }
        }
    }

    public void salvarListaCampos(GrupoVO instance) throws Exception {
        super.updateImpl(instance);
    }

    public GrupoVO getById(Object id) {
        try {
            return grupoDAO.getById(id);
        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_GRUPO, EVENTO_GET_BY_ID));
        }

        return null;
    }

    public GrupoVO obterGrupo(DocumentoVO documento) {
        try {
            return grupoDAO.obterGrupo(documento);
        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_GRUPO, EVENTO_FIND_ALL));
        }

        return null;
    }

    public List<TipoDemandaVO> obterDemandas() {
        try {
            return grupoDAO.obterDemandas();
        } catch (Exception e) {
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_TIPO_DEMANDA, EVENTO_OBTER_DEMANDAS));
        }

        return null;
    }

    @Override
    protected void validaCamposObrigatorios(GrupoVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(GrupoVO entity) {
        // do nothing
    }

    @Override
    protected GenericDAO<GrupoVO> getDAO() {
        return grupoDAO;
    }

}
