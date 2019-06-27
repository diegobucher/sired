package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.ArrayList;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.sired.extra.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

public interface TramiteRequisicaoDAO extends GenericDAO<TramiteRequisicaoVO> {

    void excluirByRequisicao(RequisicaoVO requisicaoVO) throws AppException;

    List<TramiteRequisicaoSuporteDTO> getGroupByTipoSuporte(RelatorioSuporteDTO relatorio) throws AppException;

    ArrayList<TramiteRequisicaoSuporteDTO> mounRetornoConsultaByTipoSuporte(List<TramiteRequisicaoSuporteDTO> lista);

    List<TramiteRequisicaoVO> obterTramitesPorRequisicao(Long idRequisicao);
    
    List<TramiteRequisicaoVO> findAtendimentosRequisicao(RequisicaoVO requisicao);

}
