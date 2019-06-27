package br.gov.caixa.gitecsa.sired.extra.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

public interface EmpresaDAO extends GenericDAO<EmpresaVO> {

    EmpresaVO obterEmpresaCNPJ(Long cnpj);

}
