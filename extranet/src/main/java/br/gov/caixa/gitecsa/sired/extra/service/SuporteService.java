package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.language.Metaphone;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.SuporteDAO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

@Stateless(name = "tipoSuporteService")
public class SuporteService extends AbstractService<SuporteVO> implements Serializable {

    private static final long serialVersionUID = 777550929150587026L;

    @Inject
    private SuporteDAO dao;

    @Override
    protected void validaCamposObrigatorios(SuporteVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegras(SuporteVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegrasExcluir(SuporteVO entity) throws BusinessException {

    }

    @Override
    protected GenericDAO<SuporteVO> getDAO() {
        return this.dao;
    }

    public SuporteVO findByFonetica(String nome) {

        Metaphone metaphone = new Metaphone();

        for (TipoSuporteEnum item : TipoSuporteEnum.values()) {
            if (metaphone.isMetaphoneEqual(item.name(), nome)) {
                return this.findById(item.getValor());
            }
        }

        return null;
    }

}
