package br.gov.caixa.gitecsa.service.mail;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.dto.RemessasAbertasDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Ignore
public class EnviaEmailRemessaServiceTest {

  @InjectMocks
  private EnviaEmailRemessaService enviaEmailRemessaService;
  private ParametroSistemaService parametroService;
  private Map<UnidadeVO, RemessasAbertasDTO> remessasMap;
  private List<RemessaVO> remessasList;
  private RemessasAbertasDTO remessasAbertasDTO; 
  private RemessaVO remessaVO;
  private UnidadeVO unidadeVO;
  private BaseVO base;
  private TramiteRemessaVO tramiteRemessaVO;
  private SituacaoRemessaVO situacaoVO;
  private RemessaDocumentoVO remessaDocumentoVO;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.remessasAbertasDTO = new RemessasAbertasDTO();
    this.remessasList = new ArrayList<>();
    this.situacaoVO = new SituacaoRemessaVO();
    this.situacaoVO.setNome("ABERTA");
    this.tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO.setSituacao(this.situacaoVO);
    this.remessaVO = new RemessaVO();
    this.remessaVO.setId(1L);
    this.unidadeVO = new UnidadeVO();
    this.unidadeVO.setId(7470L);
    this.unidadeVO.setNome("GITECSA");
    TipoUnidadeVO tipoUnidadeVO = new TipoUnidadeVO();
    tipoUnidadeVO.setId(1L);
    this.unidadeVO.setTipoUnidade(tipoUnidadeVO);
    UFVO ufVO = new UFVO();
    ufVO.setId("BA");
    this.unidadeVO.setUf(ufVO);
    this.remessaVO.setUnidadeSolicitante(this.unidadeVO);
    this.remessaVO.setDataHoraAbertura(new Date());
    this.remessaVO.setTramiteRemessaAtual(this.tramiteRemessaVO);
    this.base = new BaseVO();
    this.base.setNome("Base");
    this.remessaVO.setBase(this.base);
    this.remessaVO.setCodigoUsuarioAbertura("c000000");
    this.remessaDocumentoVO = new RemessaDocumentoVO();
    this.remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
    this.remessaDocumentoVO.setCodigoRemessa(123456L);
    this.remessaDocumentoVO.setUnidadeGeradora(this.unidadeVO);
    DocumentoVO documento = new DocumentoVO();
    documento.setNome("Documento");
    this.remessaDocumentoVO.setDocumento(documento);
    this.remessaDocumentoVO.setDataGeracao(new Date());
    List<RemessaDocumentoVO> listaDocumentos = new ArrayList<>();
    listaDocumentos.add(remessaDocumentoVO);
    this.remessaVO.setRemessaDocumentos(listaDocumentos);
    this.remessasList.add(this.remessaVO);
    this.remessasAbertasDTO.setRemessasList(this.remessasList);
    this.remessasAbertasDTO.setUnidade(this.unidadeVO);
    this.remessasMap = new HashMap<>();
    this.remessasMap.put(this.unidadeVO, this.remessasAbertasDTO);
    this.parametroService = new ParametroSistemaService();
  }

  @Test
  public void enviaEmailRemessasAbertasTest() throws EmailException, IOException, AppException {
    this.enviaEmailRemessaService.enviaEmailRemessasAbertas(this.remessasMap, this.parametroService);
    assertTrue(true);
  }

}
