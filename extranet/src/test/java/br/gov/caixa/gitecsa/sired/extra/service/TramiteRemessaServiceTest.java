package br.gov.caixa.gitecsa.sired.extra.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRemessaDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

@Ignore
public class TramiteRemessaServiceTest {
  
  @InjectMocks
  private TramiteRemessaService tramiteRemessaService;
  
  private TramiteRemessaVO tramiteRemessaVO;
  
  private RemessaVO remessaVO;
  
  @Mock
  private TramiteRemessaDAO tramiteRemessaDAO;
  
  private SituacaoRemessaVO situacaoRemessaVO;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.tramiteRemessaVO = new TramiteRemessaVO();
    this.remessaVO = new RemessaVO();
    this.situacaoRemessaVO = new SituacaoRemessaVO();
    this.situacaoRemessaVO.setId(1L);
  }

  @Test
  public void testValidaCamposObrigatorios() throws BusinessException {
    this.tramiteRemessaService.validaCamposObrigatorios(this.tramiteRemessaVO);
    assertTrue(true);
  }

  @Test
  public void testValidaRegras() throws BusinessException {
    this.tramiteRemessaService.validaRegras(this.tramiteRemessaVO);
    assertTrue(true);
  }
  
  @Test
  public void testValidaRegrasExcluir() throws BusinessException {
    this.tramiteRemessaService.validaRegrasExcluir(this.tramiteRemessaVO);
    assertTrue(true);
  }
  
  @Test
  public void testSalvarTramiteRemessaAlterada() throws AppException {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessaAlterada(this.remessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testSalvarTramiteRemessaEmAlteracao() throws AppException {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessaEmAlteracao(this.remessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testSalvarTramiteRemessaRecebida() throws AppException {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessaRecebida(this.remessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testSalvarTramiteRemessaConferida() throws AppException {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessaConferida(this.remessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testBuscarSituacaoRemessa() {
    Mockito.doReturn(this.situacaoRemessaVO).when(this.tramiteRemessaDAO).buscarSituacaoRemessa(1L);
    this.situacaoRemessaVO = this.tramiteRemessaService.buscarSituacaoRemessa(1L);
    assertTrue(1L == (Long) this.situacaoRemessaVO.getId());
  }
  
  @Test
  public void testSalvarTramiteRemessaRascunho() throws AppException {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessaRascunho(this.remessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testSalvarTramiteRemessa() {
    Mockito.doReturn(this.tramiteRemessaVO).when(this.tramiteRemessaDAO).save(this.tramiteRemessaVO);
    TramiteRemessaVO tramite = this.tramiteRemessaService.salvarTramiteRemessa(this.tramiteRemessaVO);
    assertTrue(tramite != null);
  }
  
  @Test
  public void testFindByRemessa() throws AppException {
    List<TramiteRemessaVO> tramiteRemessaVOList = new ArrayList<>();
    TramiteRemessaVO tramiteRemessa = new TramiteRemessaVO();
    tramiteRemessaVOList.add(tramiteRemessa);
    Mockito.doReturn(tramiteRemessaVOList).when(this.tramiteRemessaDAO).findByRemessa(this.remessaVO);
    List<TramiteRemessaVO> tramiteRemessaList = this.tramiteRemessaService.findByRemessa(this.remessaVO);
    assertTrue(tramiteRemessaList.size() > 0);
  }
}
