package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.language.Metaphone;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.OcorrenciaAtendimentoDAO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;

@Stateless
public class OcorrenciaAtendimentoService extends AbstractService<OcorrenciaAtendimentoVO> implements Serializable {

    private static final long serialVersionUID = 4730493080918884205L;

    @Inject
    private OcorrenciaAtendimentoDAO dao;

    @Override
    protected void validaCamposObrigatorios(OcorrenciaAtendimentoVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegras(OcorrenciaAtendimentoVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegrasExcluir(OcorrenciaAtendimentoVO entity) throws BusinessException {

    }

    @Override
    protected GenericDAO<OcorrenciaAtendimentoVO> getDAO() {
        return this.dao;
    }

    public OcorrenciaAtendimentoVO findByFonetica(String nome) {
        Metaphone metaphone = new Metaphone();

        for (OcorrenciaAtendimentoEnum item : OcorrenciaAtendimentoEnum.values()) {
            if (metaphone.isMetaphoneEqual(item.getLabel(), nome)) {
                return this.findById(item.getValor());
            }
        }

        return null;
    }

}
