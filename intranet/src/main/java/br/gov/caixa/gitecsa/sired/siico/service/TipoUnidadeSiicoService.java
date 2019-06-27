package br.gov.caixa.gitecsa.sired.siico.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.TipoUnidadeDAO;
import br.gov.caixa.gitecsa.sired.siico.dao.TipoUnidadeSiicoDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewTipoUnidadeVO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;

@Stateless(name = "tipoUnidadeSiicoService")
public class TipoUnidadeSiicoService extends AbstractService<ViewTipoUnidadeVO> {
 
    private static final long serialVersionUID = 6967753197206156789L;
    
    @Inject 
    private TipoUnidadeSiicoDAO dao;
    
    @Inject
    private TipoUnidadeDAO tipoUnidadeDAO;
    
    @Override
    protected void validaCamposObrigatorios(ViewTipoUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(ViewTipoUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(ViewTipoUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected GenericDAO<ViewTipoUnidadeVO> getDAO() {
        return this.dao;
    }
    
    @Transactional(value=TxType.REQUIRES_NEW)
    public String sincronizar() throws AppException {
        try {
            
            List<TipoUnidadeVO> listUpdated = new ArrayList<TipoUnidadeVO>();
            List<TipoUnidadeVO> listInserted = new ArrayList<TipoUnidadeVO>();
            
            List<ViewTipoUnidadeVO> listTipoUnidade = this.findAll();
            
            for (ViewTipoUnidadeVO obj : listTipoUnidade) {
                TipoUnidadeVO entity = this.tipoUnidadeDAO.findById(obj.getNuTipoUnidade());
                
                if (ObjectUtils.isNullOrEmpty(entity)) {
                    entity = new TipoUnidadeVO();
                    entity.setId(obj.getNuTipoUnidade());
                    listInserted.add(entity);
                } else {
                    listUpdated.add(entity);
                }
                
                entity.setDescricao(obj.getDeTipoUnidade());
                entity.setIndicadorUnidade(obj.getIcUnidade());
                entity.setSiglaTipoUnidade(obj.getSgTipoUnidade());
            }
            
            this.tipoUnidadeDAO.update(listUpdated);
            this.tipoUnidadeDAO.persist(listInserted);
            
            return String.format("INSERIDO(S): %s / ATUALIZADO(S): %s", listInserted.size(), listUpdated.size());
            
        } catch (Exception e) {
            throw new AppException(e);
        }
    }
}
