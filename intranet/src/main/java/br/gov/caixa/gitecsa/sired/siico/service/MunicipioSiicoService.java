package br.gov.caixa.gitecsa.sired.siico.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.MunicipioDAO;
import br.gov.caixa.gitecsa.sired.service.MunicipioService;
import br.gov.caixa.gitecsa.sired.siico.dao.MunicipioSiicoDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewMunicipioVO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;

@Stateless(name = "municipioSiicoService")
public class MunicipioSiicoService extends AbstractService<ViewMunicipioVO> {

    private static final long serialVersionUID = -8167158972133375005L;
    
    @Inject 
    private MunicipioSiicoDAO dao;
    
    @Inject
    private MunicipioDAO municipioDAO;
    
    @Inject 
    private MunicipioService municipioService;

    @Override
    protected void validaCamposObrigatorios(ViewMunicipioVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(ViewMunicipioVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(ViewMunicipioVO entity) throws BusinessException {
        
    }
    
    @Override
    protected GenericDAO<ViewMunicipioVO> getDAO() {
        return this.dao;
    }
    
    @Transactional(value=TxType.REQUIRES_NEW)
    public String sincronizar() throws AppException {
        try {
            
            List<MunicipioVO> listUpdated = new ArrayList<MunicipioVO>();
            List<MunicipioVO> listInserted = new ArrayList<MunicipioVO>();
            
            List<ViewMunicipioVO> listMunicipio = this.findAll();
            Map<Object, MunicipioVO> mapMunicipio = this.municipioService.findAllAsMap();
            
            for (ViewMunicipioVO obj : listMunicipio) {
                MunicipioVO entity = mapMunicipio.get(obj.getNuLocalidade());
                
                if (ObjectUtils.isNullOrEmpty(entity)) {
                    entity = new MunicipioVO();
                    listInserted.add(entity);
                }
                
                Integer originalHash = entity.hashCodeSync();
                
                entity.setNome(obj.getNoLocalidade().trim());
                
                if (!ObjectUtils.isNullOrEmpty(obj.getSgUf())) {
                    entity.setUf(new UFVO());
                    entity.getUf().setId(obj.getSgUf());
                }
                
                if (!ObjectUtils.isNullOrEmpty(entity.getId()) && !originalHash.equals(entity.hashCodeSync())) {
                    listUpdated.add(entity);
                } else {
                    entity.setId(obj.getNuLocalidade());
                }
            }
            
            this.municipioDAO.update(listUpdated);
            this.municipioDAO.persist(listInserted);
            
            return String.format("INSERIDO(S): %s / ATUALIZADO(S): %s", listInserted.size(), listUpdated.size());
            
        } catch (Exception e) {
            throw new AppException(e);
        }
    }
}
