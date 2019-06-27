package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.ParametroSistemaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

@Stateless
public class ParametroSistemaService extends AbstractService<ParametroSistemaVO> {

    private static final long serialVersionUID = -5756580844519809453L;

    @Inject
    private ParametroSistemaDAO dao;

    @Override
    protected void validaCamposObrigatorios(ParametroSistemaVO entity) {

    }

    @Override
    protected void validaRegras(ParametroSistemaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(ParametroSistemaVO entity) {

    }

    @Override
    protected GenericDAO<ParametroSistemaVO> getDAO() {
        return this.dao;
    }

    public ParametroSistemaVO findByNome(String nome) throws DataBaseException {
		try {
			return this.dao.findByNome(nome);
		} catch (Exception e) {
			System.err
					.println("**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
			e.printStackTrace();
			System.err
					.println("**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
			throw e;
		}
    }

	public boolean ehAmbienteDesenvolvimento() {
		try {
			ParametroSistemaVO parametroSistema = this.dao.findByNome(Constantes.PARAMETRO_DESENVOLVIMENTO);
			if (!ObjectUtils.isNullOrEmpty(parametroSistema)) {
				if (Long.valueOf(parametroSistema.getVlParametroSistema()).equals(AtivoInativoEnum.ATIVO.getValue())) {
					return true;
				}
			}
		} catch (Exception e) {
			// caso ocorra algum erro, como a variavel nao existir na base, nao
			// eh DEV.
		}
		return false;
	}
	
}
