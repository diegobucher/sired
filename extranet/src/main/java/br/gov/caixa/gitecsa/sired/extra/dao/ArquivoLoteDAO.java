package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.extra.dto.FiltroArquivoLoteDTO;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

public interface ArquivoLoteDAO extends GenericDAO<ArquivoLoteVO> {
    List<ArquivoLoteVO> consultar(FiltroArquivoLoteDTO filtro, Integer offset, Integer limit);

    Integer count(FiltroArquivoLoteDTO filtro);
}
