package br.gov.caixa.gitecsa.service.mail;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Ignore
public class EnviaEmailRequisicaoServiceTest {

  @InjectMocks
  private EnviaEmailRequisicaoService enviaEmailRequisicaoService;
  
  private Map<UnidadeVO, RequisicaoDTO> requisicoesMap;
  private RequisicaoVO requisicaoVO;
  private RequisicaoDTO requisicaoDTO;
  private UnidadeVO unidadeVO;
  private BaseVO base;
  private TramiteRequisicaoVO tramiteRequisicaoVO;
  private UFVO ufVO;
  private GrupoVO grupoVO;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.requisicoesMap = new HashMap<>();
    this.requisicaoVO = new RequisicaoVO();
    this.requisicaoDTO = new RequisicaoDTO();
    this.unidadeVO = new UnidadeVO();
    this.ufVO = new UFVO();
    this.ufVO.setId("BA");
    this.ufVO.setNome("Bahia");
    this.unidadeVO.setId(7470L);
    this.unidadeVO.setNome("GITECSA");
    this.grupoVO = new GrupoVO();
    TipoUnidadeVO tipoUnidadeVO = new TipoUnidadeVO();
    tipoUnidadeVO.setId(1L);
    this.unidadeVO.setTipoUnidade(tipoUnidadeVO);
    UFVO ufVO = new UFVO();
    ufVO.setId("BA");
    this.unidadeVO.setUf(ufVO);
    this.requisicaoVO.setUnidadeSolicitante(this.unidadeVO);
    this.requisicaoVO.setDataHoraAbertura(new Date());
    this.requisicaoVO.setPrazoAtendimento(new Date());
    DocumentoVO documento = new DocumentoVO();
    documento.setNome("Documento");
    documento.setGrupo(this.grupoVO);
    Set<GrupoCampoVO> grupoCampos = new HashSet<>();
    GrupoCampoVO grupoCampoVO = new GrupoCampoVO();
    grupoCampoVO.setLegenda("Legenda");
    CampoVO campo = new CampoVO();
    campo.setDescricao("Desc");
    campo.setTipo(TipoCampoEnum.TEXTO);
    campo.setNome("Teste");
    grupoCampoVO.setCampo(campo);
    grupoCampos.add(grupoCampoVO);
    grupoVO.setGrupoCampos(grupoCampos);
    this.requisicaoVO.setDocumento(documento);
    this.requisicaoVO.setFormato(FormatoDocumentoEnum.COPIA_SIMPLES);
    this.requisicaoVO.setCodigoUsuarioAbertura("c000000");
    this.requisicaoVO.setCodigoRequisicao(123456L);
    this.requisicaoVO.setQtSolicitadaDocumento(1);
    RequisicaoDocumentoVO reqDoc = new RequisicaoDocumentoVO();
    TipoDemandaVO tipoDemanda = new TipoDemandaVO();
    tipoDemanda.setNome("Nome");
    reqDoc.setTipoDemanda(tipoDemanda);
    reqDoc.setNuDocumentoExigido("1");
    reqDoc.setUnidadeGeradora(this.unidadeVO);
    this.requisicaoVO.setRequisicaoDocumento(reqDoc);
    this.base = new BaseVO();
    this.base.setNome("Base");
    this.requisicaoVO.setBase(this.base);
    this.tramiteRequisicaoVO = new TramiteRequisicaoVO();
    SituacaoRequisicaoVO situacaoRequisicao = new SituacaoRequisicaoVO();
    situacaoRequisicao.setNome("Situação");
    situacaoRequisicao.setId(1L);
    this.tramiteRequisicaoVO.setSituacaoRequisicao(situacaoRequisicao);
    this.requisicaoVO.setTramiteRequisicaoAtual(this.tramiteRequisicaoVO);
    List<RequisicaoVO> requisicoesList = new ArrayList<>();
    requisicoesList.add(this.requisicaoVO);
    this.requisicaoDTO.setRequisicao(requisicoesList);
    this.requisicoesMap.put(this.unidadeVO, this.requisicaoDTO);
  }

  @Test
  public void enviaEmailRequisicoesAbertasTest() throws FileNotFoundException, EmailException, AppException, MessagingException, IOException {
    ParametroSistemaService parametroSistemaService = new ParametroSistemaService();
    this.enviaEmailRequisicaoService.enviaEmailRequisicoesAbertas(this.requisicoesMap, parametroSistemaService);
    assertTrue(true);
  }

}
