package br.gov.caixa.gitecsa.sired.siico.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.UnidadeDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.siico.dao.UnidadeSiicoDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewUnidadeVO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "unidadeSiicoService")
public class UnidadeSiicoService extends AbstractService<ViewUnidadeVO> {
 
    private static final long serialVersionUID = 9113982017947330374L;

    @Inject 
    private UnidadeSiicoDAO dao;
    
    @Inject
    private UnidadeDAO unidadeDAO;
    
    @Inject
    private UnidadeService unidadeService;
    
    @Override
    protected void validaCamposObrigatorios(ViewUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(ViewUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(ViewUnidadeVO entity) throws BusinessException {
        
    }

    @Override
    protected GenericDAO<ViewUnidadeVO> getDAO() {
        return this.dao;
    }
    
    public String sincronizar() throws AppException {
        try {
            
            List<UnidadeVO> listUpdated = new ArrayList<UnidadeVO>();
            List<UnidadeVO> listInserted = new ArrayList<UnidadeVO>();
            
            List<ViewUnidadeVO> listUnidadeVOs = this.findAll();
            Map<Object, UnidadeVO> mapUnidade = this.unidadeService.findAllAsMap();
            
            for (ViewUnidadeVO obj : listUnidadeVOs) {
                
                UnidadeVO entity = mapUnidade.get(obj.getNuUnidade());
                
                if (ObjectUtils.isNullOrEmpty(entity)) {
                    entity = new UnidadeVO();
                    entity.setUnidadeGrupos(new HashSet<UnidadeGrupoVO>());
                    listInserted.add(entity);
                }
                
                Integer originalHash = entity.hashCodeSync();
                
                entity.setId(obj.getNuUnidade());
                entity.setIcAtivo(SimNaoEnum.SIM.getId().intValue());
                entity.setNome(obj.getNoUnidade().trim());
                
                if (!ObjectUtils.isNullOrEmpty(obj.getSgUnidade())) {
                    entity.setSiglaUnidade(obj.getSgUnidade());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getSgLocalizacao())) {
                    entity.setSiglaLocalizacao(obj.getSgLocalizacao());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getNuTpUnidade())) {
                    entity.setTipoUnidade(new TipoUnidadeVO());
                    entity.getTipoUnidade().setId(obj.getNuTpUnidade());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getUf())) {
                    entity.setUf(new UFVO());
                    entity.getUf().setId(obj.getUf());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getNuLocalidade())) {
                    entity.setMunicipio(new MunicipioVO());
                    entity.getMunicipio().setId(obj.getNuLocalidade());
                }
                
                if (!ObjectUtils.isNullOrEmpty(obj.getNuUnidadeSub())) {
                    UnidadeVO unidadePai = mapUnidade.get(obj.getNuUnidadeSub());
                    if (!ObjectUtils.isNullOrEmpty(unidadePai)) {
                        entity.setUnidadeVinculadora(unidadePai);
                    } else {
                        entity.setUnidadeVinculadora(this.unidadeService.findById(obj.getNuUnidadeSub()));
                    }
                }
                
                if (!ObjectUtils.isNullOrEmpty(entity.getId()) && !originalHash.equals(entity.hashCodeSync())) {
                    listUpdated.add(entity);
                }
            }
            
            this.unidadeDAO.persist(listInserted);
            this.unidadeDAO.update(listUpdated);
            
            return String.format("INSERIDO(S): %s / ATUALIZADO(S): %s", listInserted.size(), listUpdated.size());
            
        } catch (Exception e) {
            throw new AppException(e);
        }
    }
}
