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
import br.gov.caixa.gitecsa.sired.dao.FeriadoDAO;
import br.gov.caixa.gitecsa.sired.service.FeriadoService;
import br.gov.caixa.gitecsa.sired.siico.dao.FeriadoSiicoDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewFeriadoVO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;

@Stateless(name = "feriadoSiicoService")
public class FeriadoSiicoService extends AbstractService<ViewFeriadoVO> {

    private static final String ORIGEM_AUTOMATICA = "A";

    private static final long serialVersionUID = -8167158972133375005L;
    
    @Inject 
    private FeriadoSiicoDAO dao;
    
    @Inject
    private FeriadoDAO feriadoDAO;
    
    @Inject
    private FeriadoService feriadoService; 

    @Override
    protected void validaCamposObrigatorios(ViewFeriadoVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(ViewFeriadoVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(ViewFeriadoVO entity) throws BusinessException {
        
    }
    
    @Override
    protected GenericDAO<ViewFeriadoVO> getDAO() {
        return this.dao;
    }
    
    @Transactional(value=TxType.REQUIRES_NEW)
    public String sincronizar() throws AppException {
        try {
            
            List<FeriadoVO> listUpdated = new ArrayList<FeriadoVO>();
            List<FeriadoVO> listInserted = new ArrayList<FeriadoVO>();
            
            List<ViewFeriadoVO> listFeriado = this.findAll();
            Map<Object, FeriadoVO> mapFeriado = this.feriadoService.findAllAsMap();
            
            for (ViewFeriadoVO obj : listFeriado) {
                FeriadoVO entity = mapFeriado.get(obj.getNuFeriado());
                
                if (ObjectUtils.isNullOrEmpty(entity)) {
                    entity = new FeriadoVO();
                    listInserted.add(entity);
                } else {
                    listUpdated.add(entity);
                }
                
                entity.setId(obj.getNuFeriado());
                entity.setData(obj.getDtFeriado());
                entity.setModalidade(obj.getIcModalidade());
                entity.setTsInclusao(obj.getTsInclusao());
                entity.setOrigem(ORIGEM_AUTOMATICA);
                
                if (!ObjectUtils.isNullOrEmpty(obj.getSgUf())) {
                    entity.setUf(new UFVO());
                    entity.getUf().setId(obj.getSgUf());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getNuLocalidade())) {
                    entity.setMunicipio(new MunicipioVO());
                    entity.getMunicipio().setId(obj.getNuLocalidade());
                }
            }
            
            this.feriadoDAO.update(listUpdated);
            this.feriadoDAO.persist(listInserted);
            
            return String.format("INSERIDO(S): %s / ATUALIZADO(S): %s", listInserted.size(), listUpdated.size());
            
        } catch (Exception e) {
            throw new AppException(e);
        }
    }
}
