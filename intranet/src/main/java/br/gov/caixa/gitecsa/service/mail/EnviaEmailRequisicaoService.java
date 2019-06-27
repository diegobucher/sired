package br.gov.caixa.gitecsa.service.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.mail.MessagingException;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.ldap.AutenticadorLdap;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoDTO;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRequisicaoXLS;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.TramiteRequisicaoService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class EnviaEmailRequisicaoService extends EnviaEmailService {

  @Inject
  private TramiteRequisicaoService tramiteRequisicaoService;

  @Inject
  private Logger logger;
  
  private ParametroSistemaService parametroSistemaService;

  private static final String PZ_MANUTENCAO_ARQUIVOS = "PZ_MANUTENCAO_ARQUIVOS";
  private static final String PZ_REABERTURA = "PZ_REABERTURA";

  @PostConstruct
  public void init() {
  }

  public void enviaEmailSolicitante(ParametroSistemaService parametroSistemaService) throws EmailException, Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRequisicaoVO> listaTramites = tramiteRequisicaoService.consultaTramitesEmailSolicitante();
    if (Util.isNullOuVazio(listaTramites)) { // registra que nao existem
                                             // tramites pendentes para
                                             // envio.
      logger.info(getLogRequisicaoSolicitanteSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRequisicaoVO>> mapTramites = new HashMap<String, List<TramiteRequisicaoVO>>();
      // percorre os tramites gerando um Map, sendo a chave a matricula do
      // Usuário da Abertura e o conteudo os trâmites.
      for (TramiteRequisicaoVO tramite : listaTramites) {
        String usuarioAbertura = tramite.getRequisicao().getCodigoUsuarioAbertura().toUpperCase();
        if (!mapTramites.containsKey(usuarioAbertura)) {
          mapTramites.put(usuarioAbertura, new ArrayList<TramiteRequisicaoVO>());
        }
        mapTramites.get(usuarioAbertura).add(tramite);
      }

      for (String usuarioAbertura : mapTramites.keySet()) {
        List<TramiteRequisicaoVO> listaTramitesUsuario = mapTramites.get(usuarioAbertura);

        UsuarioLdap usuarioLdap =
            new AutenticadorLdap().pesquisarForaDoGrupoDoLdap(usuarioAbertura, System.getProperty(Constantes.INTRANET_URL_LDAP));
        Destinatario destinatario;
        if (!ObjectUtils.isNullOrEmpty(usuarioLdap)) {
          destinatario = new Destinatario(usuarioAbertura + SiredUtils.DOMINIO_EMAIL);
          destinatario.setNomeMaiusculas(usuarioLdap.getNomeUsuario());
          // Adiciona na cópia do email a unidade do primeiro trâmite a ser enviado ao usuário.
          destinatario.addDestinatarioCampoComCopia(
              listaTramitesUsuario.get(0).getRequisicao().getUnidadeSolicitante().getEnderecoEmail());
        } else { // Usuário saiu da Caixa, então envia email somente para a Unidade.
          destinatario = new Destinatario(listaTramitesUsuario.get(0).getRequisicao().getUnidadeSolicitante().getEnderecoEmail());
          destinatario.setNomeMaiusculas(listaTramitesUsuario.get(0).getRequisicao().getUnidadeSolicitante().getNome());
          System.err
              .println("*************************" + usuarioAbertura + " não possui usuário no LDAP! **************************");
        }

        enviaTramites(destinatario, listaTramitesUsuario, false);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRequisicaoSolicitante(mensagemEnviadas));
    }
  }

  public void enviaEmailBase(ParametroSistemaService parametroSistemaService) throws EmailException, Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRequisicaoVO> listaTramites = tramiteRequisicaoService.consultaTramitesEmailBase();
    if (Util.isNullOuVazio(listaTramites)) { // registra que nao existem
                                             // tramites pendentes para
                                             // envio.
      logger.info(getLogRequisicaoBaseSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRequisicaoVO>> mapTramites = new HashMap<String, List<TramiteRequisicaoVO>>();
      // percorre os tramites gerando um Map, sendo a chave o email da
      // base e o conteudo os trâmites.
      for (TramiteRequisicaoVO tramite : listaTramites) {
        BaseVO baseVO = tramite.getRequisicao().getBase();
        if (ObjectUtils.isNullOrEmpty(baseVO)) {
          System.err.println("*************************" + tramite.getRequisicao().getCodigoRequisicao()
              + " não possui Base de Arquivo definida! **************************");
        } else {
          String emailBase = baseVO.getCaixaPostal().toLowerCase();
          if (!mapTramites.containsKey(emailBase)) {
            mapTramites.put(emailBase, new ArrayList<TramiteRequisicaoVO>());
          }
          mapTramites.get(emailBase).add(tramite);
        }
      }

      for (String emailBase : mapTramites.keySet()) {
        List<TramiteRequisicaoVO> listaTramitesUsuario = mapTramites.get(emailBase);
        Destinatario destinatario = new Destinatario(emailBase);
        destinatario.setNomeMaiusculas(listaTramitesUsuario.get(0).getRequisicao().getBase().getNome());

        enviaTramites(destinatario, listaTramitesUsuario, false);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRequisicaoBase(mensagemEnviadas));
    }
  }

  public void enviaEmailTerceirizada(ParametroSistemaService parametroSistemaService) throws EmailException, Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRequisicaoVO> listaTramites = tramiteRequisicaoService.consultaTramitesEmailTerceirizada();
    if (Util.isNullOuVazio(listaTramites)) { // registra que nao existem
                                             // tramites pendentes para
                                             // envio.
      logger.info(getLogRequisicaoTerceirizadaSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRequisicaoVO>> mapTramites = new HashMap<String, List<TramiteRequisicaoVO>>();
      // percorre os tramites gerando um Map, sendo a chave o email da
      // Terceirizada e o conteudo os trâmites.
      for (TramiteRequisicaoVO tramite : listaTramites) {
        if (!ObjectUtils.isNullOrEmpty(tramite.getRequisicao().getEmpresaContrato())) {
          String emailTerceirizada = tramite.getRequisicao().getEmpresaContrato().getEmpresa().getCaixaPostal();
          if (!mapTramites.containsKey(emailTerceirizada)) {
            mapTramites.put(emailTerceirizada, new ArrayList<TramiteRequisicaoVO>());
          }
          mapTramites.get(emailTerceirizada).add(tramite);
        }
      }

      for (String emailTerceirizada : mapTramites.keySet()) {
        List<TramiteRequisicaoVO> listaTramitesUsuario = mapTramites.get(emailTerceirizada);
        Destinatario destinatario = new Destinatario(emailTerceirizada);
        destinatario.setNomeMaiusculas(listaTramitesUsuario.get(0).getRequisicao().getEmpresaContrato().getEmpresa().getNome());

        // adiciona todos os usuários da Terceirizada que fizeram algum
        // atendimento na Solicitacao na Cópia do Email.
        for (TramiteRequisicaoVO tramite : listaTramitesUsuario) {
          for (TramiteRequisicaoVO tramiteSolicitacao : tramite.getRequisicao().getTramiteRequisicoes()) {
            if (EnviaEmailService.EhUsuarioTerceirizada(tramiteSolicitacao.getCodigoUsuario())) {
              destinatario.addDestinatarioCampoComCopia(tramiteSolicitacao.getCodigoUsuario());
            }
          }
        }

        enviaTramites(destinatario, listaTramitesUsuario, true);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRequisicaoTerceirizada(mensagemEnviadas));
    }
  }

  private void enviaTramites(Destinatario destinatario, List<TramiteRequisicaoVO> tramites, boolean extranet)
      throws EmailException, DataBaseException {
    StringBuilder mensagem = new StringBuilder();
    mensagem.append(getHtmlCabecalho(extranet));
    mensagem.append("Prezado(a) " + destinatario.getNome() + "," + HTML_QUEBRA_LINHA);
    mensagem.append(HTML_QUEBRA_LINHA);
    mensagem.append("Informamos a alteração na situação da(s) Requisição(ões) de documentos abaixo:" + HTML_QUEBRA_LINHA);
    mensagem.append(HTML_TABELA_INICIO);
    mensagem.append(getHtmlLinha(new String[] { "CÓD DA REQUISIÇÃO", "DATA HORA", "DOCUMENTO", "SITUAÇÃO", "OBSERVAÇÃO" }));

    for (TramiteRequisicaoVO tramite : tramites) {
      String codigoRequisicao = tramite.getRequisicao().getCodigoRequisicao().toString().trim();
      String linkRequisicao = montaHtmlLink(extranet, "/paginas/requisicao/consulta?requisicoes=", codigoRequisicao);
      mensagem.append(getHtmlLinha(new String[] { linkRequisicao, Util.formatDataHora(tramite.getDataHora()),
          tramite.getRequisicao().getDocumento().getNome(), tramite.getSituacaoRequisicao().getNome(),
          tramite.getObservacao() }));
    }
    mensagem.append(HTML_TABELA_FIM);
    mensagem.append(HTML_QUEBRA_LINHA);
    mensagem.append(HTML_QUEBRA_LINHA);
    mensagem.append("ATENÇÃO:" + HTML_QUEBRA_LINHA);
    mensagem.append("Documentos digitalizados estarão disponíveis no sistema durante "
        + this.parametroSistemaService.findByNome(PZ_MANUTENCAO_ARQUIVOS).getVlParametroSistema() + " dia(s)."
        + HTML_QUEBRA_LINHA);
    if (!extranet) {
      mensagem.append("A avaliação deverá ser feita em até "
          + this.parametroSistemaService.findByNome(PZ_REABERTURA).getVlParametroSistema() + " dia(s)." + HTML_QUEBRA_LINHA);
    }
    mensagem.append(
        "Caso a Requisição seja de Documento Original, favor atuar na recepção e devolução do mesmo, através do Menu Original no SIRED.");
    mensagem.append(getHtmlRodape(extranet));

    enviaEmail(this.parametroSistemaService, "SIRED - Notificações sobre Requisições", destinatario, mensagem, null, null);
  }

  /**
   * Metodo utilizado para atualizar o trâmite da requisição com a data hora do envio do e-mail para o usuário e a
   * unidade de lotação do mesmo.
   * @throws Exception
   * @throws BusinessException
   * @throws RequiredException
   */
  @TransactionAttribute(TransactionAttributeType.NEVER)
  private void updateDataHoraEmailNosTramites(List<TramiteRequisicaoVO> listaTramitesUsuario)
      throws RequiredException, BusinessException, Exception {
    try {
      Date dataHoraTramiteEmail = new Date();
      for (TramiteRequisicaoVO tramite : listaTramitesUsuario) {
        tramite.setDataHoraTramiteEmail(dataHoraTramiteEmail);
        tramiteRequisicaoService.update(tramite);
      }
    } catch (Exception e) {
      System.err.println("********************************************* updateDataHoraEmailNosTramites:");
      System.err.println(e);
      e.printStackTrace();
      System.err.println("********************************************* updateDataHoraEmailNosTramites:");
      throw e;
    }
  }

  public void enviaEmailRequisicoesAbertas(Map<UnidadeVO, RequisicaoDTO> requisicoesList,
      ParametroSistemaService parametroService) throws EmailException, AppException, MessagingException, FileNotFoundException, IOException {
    for (Map.Entry<UnidadeVO, RequisicaoDTO> entry : requisicoesList.entrySet()) {
      UnidadeVO unidade = entry.getKey();
      RequisicaoDTO requisicaoDTO = entry.getValue();
      List<RequisicaoVO> requisicoesAbertasList = requisicaoDTO.getRequisicaoList();
      StringBuilder mensagem = montaCorpoEmail(unidade, requisicoesAbertasList);
      Destinatario destinatario = new Destinatario(unidade.getEnderecoEmail());
      destinatario.setNomeMaiusculas(unidade.getNome());
      
      ExportarRequisicaoXLS exportador = new ExportarRequisicaoXLS();
      exportador.setData(requisicoesAbertasList);
      
      String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
      String filename = FileUtils.appendDateTimeToFileName(MensagemUtils.obterMensagem("requisicao.consulta.label.arquivoExportacao"), new Date());
      File relatorio = exportador.export(caminho + filename);
     
      enviaEmail(parametroService, "SIRED - Requisições Abertas", destinatario, mensagem, relatorio.getAbsolutePath(), filename);
      
      org.apache.commons.io.FileUtils.forceDeleteOnExit(relatorio);
    }

  }

  private StringBuilder montaCorpoEmail(UnidadeVO unidade, List<RequisicaoVO> requisicoesAbertasList) throws AppException {
    StringBuilder sb = new StringBuilder();
    sb.append(getHtmlCabecalho(false));
    sb.append("Prezado(a) " + unidade.getNome() + "," + HTML_QUEBRA_LINHA);
    sb.append(HTML_QUEBRA_LINHA);
    sb.append("Informamos a abertura da(s) Requisição(ões) de documentos abaixo:" + HTML_QUEBRA_LINHA);
    sb.append(HTML_QUEBRA_LINHA);
    sb.append(HTML_TABELA_INICIO);
    sb.append(getHtmlLinha(
        new String[] { "CÓD DA REQUISIÇÃO", "UNID. SOLICITANTE", "DT. ABERTURA", "PRAZO", "DOCUMENTO", "SITUAÇÃO", "FORMATO", "USUÁRIO ABERT." }));

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    for (RequisicaoVO requisicaoVO : requisicoesAbertasList) {
      String codigoRequisicao = requisicaoVO.getCodigoRequisicao().toString().trim();
      String linkRequisicao = montaHtmlLink(false, "/paginas/requisicao/consulta?requisicoes=", codigoRequisicao);
      sb.append(getHtmlLinha(new String[] { linkRequisicao, requisicaoVO.getUnidadeSolicitante().getNome(),
          sdf.format(requisicaoVO.getDataHoraAbertura()), sdf.format(requisicaoVO.getPrazoAtendimento()),
          requisicaoVO.getDocumento().getNome(), "ABERTA", requisicaoVO.getFormato().toString(), requisicaoVO.getCodigoUsuarioAbertura() }));
    }

    sb.append(HTML_TABELA_FIM);
    sb.append(HTML_QUEBRA_LINHA);
    sb.append(HTML_QUEBRA_LINHA);
    return sb;
  }

}
