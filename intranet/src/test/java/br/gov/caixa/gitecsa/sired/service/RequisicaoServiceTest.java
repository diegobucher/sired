package br.gov.caixa.gitecsa.sired.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Ignore
public class RequisicaoServiceTest {

  @InjectMocks
  private RequisicaoService requisicaoService;
  
  @Mock
  private TramiteRequisicaoService tramiteService;
  
  private TramiteRequisicaoVO tramite;
  
  private RequisicaoVO requisicaoVO;
  
  @Mock
  private RequisicaoDAO dao;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    this.requisicaoVO = new RequisicaoVO();
    this.requisicaoVO.setId(1L);
    this.requisicaoVO.setArquivoJustificativa("");
    this.requisicaoVO.setBase(new BaseVO());
    this.requisicaoVO.setCodigoRequisicao(1L);
    this.requisicaoVO.setUnidadeSolicitante(new UnidadeVO());
    this.requisicaoVO.setDocumento(new DocumentoVO());
    this.requisicaoVO.setCodigoUsuarioAbertura("cod");
    this.requisicaoVO.setFormato(FormatoDocumentoEnum.COPIA_SIMPLES);
    this.requisicaoVO.setPrazoAtendimento(new Date());
    this.tramite = new TramiteRequisicaoVO();
    this.tramite.setArquivoDisponibilizado("Arquivo");
    this.tramite.setDataHoraAtendimento(new Date());
    this.tramite.setOcorrencia(new OcorrenciaAtendimentoVO());
    this.tramite.setObservacao("Obs");
    this.tramite.setQtdDisponibilizadaDocumento(10);
    this.requisicaoVO.setTramiteRequisicaoAtual(this.tramite);
  }

  @Test
  public void testRealizaAtualizacaoRequisicaoTramite() throws RequiredException, BusinessException, Exception {
    Mockito.when(this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.FECHADA, "SIRED")).thenReturn(tramite);
    Boolean result = this.requisicaoService.realizaAtualizacaoRequisicaoTramite(requisicaoVO);  
    assertTrue(result);
  }
  
  @Test
  public void pesquisaAbertasHojeTest() {
    Date hojeMeiaNoite = new Date();
    List<RequisicaoVO> requisicoesList = new ArrayList<>();
    UnidadeVO unidadeVO = new UnidadeVO();
    unidadeVO.setId(7470L);
    unidadeVO.setSiglaUnidade("GITECSA");
    this.requisicaoVO.setUnidadeSolicitante(unidadeVO);
    requisicoesList.add(this.requisicaoVO);
    Mockito.when(this.dao.pesquisaAbertasHoje(hojeMeiaNoite, hojeMeiaNoite)).thenReturn(requisicoesList);
    Map<UnidadeVO, RequisicaoDTO> requisicaoMap = this.requisicaoService.pesquisaAbertasHoje(hojeMeiaNoite, hojeMeiaNoite);
    assertTrue(!requisicaoMap.isEmpty());
  }

}
