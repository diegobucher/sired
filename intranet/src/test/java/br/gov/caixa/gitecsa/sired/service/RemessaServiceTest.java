package br.gov.caixa.gitecsa.sired.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.dto.RemessasAbertasDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.ReportUtils;

@Ignore
//@RunWith(MockitoJUnitRunner.class)
public class RemessaServiceTest {
  
  @Spy
  @InjectMocks
  private RemessaService remessaService;
  
  @Mock
  private RemessaDAO remessaDAO;
  
  @Mock
  private TramiteRemessaService tramiteRemessaService;
  
  @Mock
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;
  
  private TramiteRemessaVO tramiteRemessaVO;
  
  private List<RemessaVO> remessaList;
  
  private StreamedContent etiquetaMovDiario;
  
  @Mock
  private ReportUtils reportUtils;
  
  @Mock
  private ParametroSistemaService parametroSistemaService;
  
  @Before
  public void setUp() throws FileNotFoundException {
    MockitoAnnotations.initMocks(this);
    this.remessaList = new ArrayList<>();
    RemessaVO remessaVO = new RemessaVO();
    tramiteRemessaVO = new TramiteRemessaVO();
    remessaVO.setId(1L);
    this.remessaList.add(remessaVO);
    tramiteRemessaVO.setCodigoUsuario("SIRED");
    tramiteRemessaVO.setId(1L);
  }

  @Test
  public void fecharRemessasConferidasConfirmadasTest() {
    Mockito.when(this.remessaDAO.findAllConferidasConfirmadas()).thenReturn(this.remessaList);
    Mockito.when(this.tramiteRemessaService.getBySituacaoRemessa(SituacaoRemessaEnum.FECHADA, "SIRED")).thenReturn(tramiteRemessaVO);
    int valor = remessaService.fecharRemessasConferidasConfirmadas();
    
    assertTrue(valor > 0);
  }
  
  
  @Test
  public void testImprimirEtiquetaMovDiario() throws Exception {
    Calendar data = Calendar.getInstance();
    data.set(2018, 01, 01);
    RemessaVO remessa = new RemessaVO();
    UnidadeVO unidade = new UnidadeVO();
    BaseVO baseVO = new BaseVO();
    remessa.setId(1L);
    remessa.setCodigoRemessaTipoC(1L);
    remessa.setUnidadeSolicitante(unidade);
    unidade.setNome("nome unidade");
    unidade.setSiglaUnidade("sigla unidade");
    unidade.setSiglaLocalizacao("sigla localizacao");
    unidade.setId(1L);
    remessa.setDataHoraAbertura(data.getTime());
    remessa.setLacre(1L);
    List<MovimentoDiarioRemessaCDTO> remessaMovimentoDiario = new ArrayList<>();
    MovimentoDiarioRemessaCDTO movDiarioDTO = new MovimentoDiarioRemessaCDTO();
    movDiarioDTO.setChave("1");
    movDiarioDTO.setDataMovimento(data.getTime());
    movDiarioDTO.setRemessa(remessa);
    List<RemessaMovimentoDiarioVO> movDiarioList = new ArrayList<>();
    RemessaMovimentoDiarioVO remessaMovDiario = new RemessaMovimentoDiarioVO();
    remessaMovDiario.setId(1L);
    remessaMovDiario.setDataMovimento(data.getTime());
    remessaMovDiario.setUnidadeGeradora(unidade);
    remessaMovDiario.setRemessa(remessa);
    movDiarioList.add(remessaMovDiario);
    movDiarioDTO.setRemessaMovDiarioList(movDiarioList);
    remessa.setMovimentosDiarioList(movDiarioList);
    remessa.setDataMovimentosList(remessaMovimentoDiario);
    movDiarioDTO.setRemessaMovDiarioList(movDiarioList);
    remessa.setDataMovimentosList(remessaMovimentoDiario);
    baseVO.setUnidade(unidade);
    remessa.setBase(baseVO);
    remessa.setCodigoRemessaTipoC(1L);
    remessaMovimentoDiario.add(movDiarioDTO);
    
    Mockito.when(remessaMovimentoDiarioService.findByUnidadeDtMovimento(remessaMovDiario)).thenReturn(remessaMovDiario);
    Mockito.when(remessaDAO.findByIdFetchAll(remessa)).thenReturn(remessa);

    Mockito.doReturn(new byte[0]).when(reportUtils).obterRelatorio(Matchers.any(InputStream.class), 
        Matchers.any(HashMap.class), Matchers.any(List.class), Matchers.anyString());
   
    try {
      StreamedContent etiqueta = this.remessaService.imprimirEtiquetaMovDiario(remessa);
      assertTrue(!etiqueta.equals(null));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testImprimirEtiquetaMovDiarioException() throws Exception {
    RemessaVO remessa = new RemessaVO();
    try {
      StreamedContent etiqueta = this.remessaService.imprimirEtiquetaMovDiario(remessa);
    }catch (Exception e) {
      assertTrue(e != null);
    }
  }
  
  @Test
  public void testEtiquetaMovDiario() {
    this.remessaService.setEtiquetaMovDiario(this.etiquetaMovDiario);
    StreamedContent etiqueta = this.remessaService.getEtiquetaMovDiario();
    assertTrue(etiqueta == this.etiquetaMovDiario);
  }
  
  @Test
  public void testIsMaiorNoventaDias() throws NumberFormatException, DataBaseException {
    ParametroSistemaVO parametro = new ParametroSistemaVO();
    parametro.setVlParametroSistema("10");
    Mockito.when(parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_MOVIMENTO_DIARIO)).thenReturn(parametro);
    Calendar data = Calendar.getInstance();
    data.set(2018, 01, 01);
    RemessaMovimentoDiarioVO remessaMovDiario = new RemessaMovimentoDiarioVO();
    remessaMovDiario.setDataMovimento(data.getTime());
    RemessaVO remessaVO = new RemessaVO();
    remessaVO.setDataHoraAbertura(data.getTime());
    List<RemessaMovimentoDiarioVO> remessaDocumentos = new ArrayList<>();
    remessaDocumentos.add(remessaMovDiario);
    remessaVO.setMovimentosDiarioList(remessaDocumentos);
    remessaMovDiario.setRemessa(remessaVO);
    Boolean valor = this.remessaService.isMaiorNoventaDias(remessaMovDiario);
    assertFalse(valor);
  }
  
  @Test
  public void pesquisaAbertasHojeABTest() {
    Date hojeMeiaNoite = new Date();
    RemessaVO remessaAB = new RemessaVO();
    UnidadeVO unidadeSolicitante = new UnidadeVO();
    unidadeSolicitante.setId(7470L);
    remessaAB.setUnidadeSolicitante(unidadeSolicitante);
    this.remessaList.add(remessaAB);
    List<RemessaVO> remessaCList = new ArrayList<>();
    Mockito.when(this.remessaDAO.pesquisaAbertasHojeAB(hojeMeiaNoite, hojeMeiaNoite)).thenReturn(this.remessaList);
    Mockito.when(this.remessaDAO.pesquisaAbertasHojeC(hojeMeiaNoite, hojeMeiaNoite)).thenReturn(remessaCList);
    Map<UnidadeVO, RemessasAbertasDTO> remessasMap = this.remessaService.pesquisaAbertasHoje(hojeMeiaNoite, hojeMeiaNoite);
    assertTrue(!remessasMap.isEmpty());
  }
  
  @Test
  public void pesquisaAbertasHojeTipoCTest() {
    List<RemessaVO> remessaList = new ArrayList<>();
    Date hojeMeiaNoite = new Date();
    UnidadeVO unidadeGeradora = new UnidadeVO();
    unidadeGeradora.setId(7470L);
    UnidadeVO unidadeSolicitante = new UnidadeVO();
    unidadeSolicitante.setId(7470L);
    RemessaVO remessaC = new RemessaVO();
    remessaC.setId(1234L);
    remessaC.setUnidadeSolicitante(unidadeSolicitante);
    RemessaMovimentoDiarioVO remessaMovDiario = new RemessaMovimentoDiarioVO();
    remessaMovDiario.setId(1L);
    remessaMovDiario.setDataMovimento(hojeMeiaNoite);
    remessaMovDiario.setUnidadeGeradora(unidadeGeradora);
    List<RemessaMovimentoDiarioVO> movimentosDiarioList = new ArrayList<>();
    movimentosDiarioList.add(remessaMovDiario);
    remessaC.setMovimentosDiarioList(movimentosDiarioList);
    List<RemessaVO> remessaCList = new ArrayList<>();
    remessaCList.add(remessaC);
    Mockito.when(this.remessaDAO.pesquisaAbertasHojeAB(hojeMeiaNoite, hojeMeiaNoite)).thenReturn(remessaList);
    Mockito.when(this.remessaDAO.pesquisaAbertasHojeC(hojeMeiaNoite, hojeMeiaNoite)).thenReturn(remessaCList);
    Mockito.when(this.remessaDAO.obterRemessaComMovimentosDiarios((Long) remessaC.getId())).thenReturn(remessaC);
    Map<UnidadeVO, RemessasAbertasDTO> remessasMap = this.remessaService.pesquisaAbertasHoje(hojeMeiaNoite, hojeMeiaNoite);
    assertTrue(!remessasMap.isEmpty());
  }
 
}
