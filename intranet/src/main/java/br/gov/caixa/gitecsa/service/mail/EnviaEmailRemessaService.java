package br.gov.caixa.gitecsa.service.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.ldap.AutenticadorLdap;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.dto.RemessasAbertasDTO;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRemessaXLS;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.TramiteRemessaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class EnviaEmailRemessaService extends EnviaEmailService {

  @Inject
  private TramiteRemessaService tramiteRemessaService;

  @Inject
  private Logger logger;

  private ParametroSistemaService parametroSistemaService;

  @PostConstruct
  public void init() {
  }

  public void enviaEmailSolicitante(ParametroSistemaService parametroSistemaService) throws EmailException, Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRemessaVO> listaTramites = tramiteRemessaService.consultaTramitesEmailSolicitante();
    if (Util.isNullOuVazio(listaTramites)) {
      logger.info(getLogRemessaSolicitanteSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRemessaVO>> mapTramites = new HashMap<String, List<TramiteRemessaVO>>();
      // percorre os tramites gerando um Map com a matricula do Usuário da
      // Abertura e os trâmites.
      for (TramiteRemessaVO tramite : listaTramites) {
        String usuarioAbertura = tramite.getRemessa().getCodigoUsuarioAbertura().toUpperCase();
        if (!mapTramites.containsKey(usuarioAbertura)) {
          mapTramites.put(usuarioAbertura, new ArrayList<TramiteRemessaVO>());
        }
        mapTramites.get(usuarioAbertura).add(tramite);
      }

      for (String usuarioAbertura : mapTramites.keySet()) {
        List<TramiteRemessaVO> listaTramitesUsuario = mapTramites.get(usuarioAbertura);

        UsuarioLdap usuarioLdap =
            new AutenticadorLdap().pesquisarForaDoGrupoDoLdap(usuarioAbertura, System.getProperty(Constantes.INTRANET_URL_LDAP));
        Destinatario destinatario;
        if (!ObjectUtils.isNullOrEmpty(usuarioLdap)) {
          destinatario = new Destinatario(usuarioAbertura + SiredUtils.DOMINIO_EMAIL);
          destinatario.setNomeMaiusculas(usuarioLdap.getNomeUsuario());
          // Adiciona na cópia do email a unidade do primeiro trâmite a ser enviado ao usuário.
          destinatario
              .addDestinatarioCampoComCopia(listaTramitesUsuario.get(0).getRemessa().getUnidadeSolicitante().getEnderecoEmail());
        } else { // Usuário saiu da Caixa, então envia email somente para a Unidade.
          destinatario = new Destinatario(listaTramitesUsuario.get(0).getRemessa().getUnidadeSolicitante().getEnderecoEmail());
          destinatario.setNomeMaiusculas(listaTramitesUsuario.get(0).getRemessa().getUnidadeSolicitante().getNome());
          System.err
              .println("*************************" + usuarioAbertura + " não possui usuário no LDAP! **************************");
        }

        enviaTramites(destinatario, listaTramitesUsuario, false);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRemessaSolicitante(mensagemEnviadas));
    }
  }

  public void enviaEmailBase(ParametroSistemaService parametroSistemaService) throws EmailException, Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRemessaVO> listaTramites = tramiteRemessaService.consultaTramitesEmailBase();
    if (Util.isNullOuVazio(listaTramites)) { // registra que nao existem
                                             // tramites pendentes para
                                             // envio.
      logger.info(getLogRequisicaoBaseSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRemessaVO>> mapTramites = new HashMap<String, List<TramiteRemessaVO>>();
      // percorre os tramites gerando um Map, sendo a chave o email da
      // base e o conteudo os trâmites.
      for (TramiteRemessaVO tramite : listaTramites) {
        BaseVO baseVO = tramite.getRemessa().getBase();
        if (ObjectUtils.isNullOrEmpty(baseVO)) {
          System.err.println("*************************" + tramite.getRemessa().getId()
              + " não possui Base de Arquivo definida! **************************");
        } else {
          String emailBase = baseVO.getCaixaPostal().toLowerCase();
          if (!mapTramites.containsKey(emailBase)) {
            mapTramites.put(emailBase, new ArrayList<TramiteRemessaVO>());
          }
          mapTramites.get(emailBase).add(tramite);
        }
      }

      for (String emailBase : mapTramites.keySet()) {
        List<TramiteRemessaVO> listaTramitesUsuario = mapTramites.get(emailBase);
        Destinatario destinatario = new Destinatario(emailBase);
        destinatario.setNomeMaiusculas(listaTramitesUsuario.get(0).getRemessa().getBase().getNome());

        enviaTramites(destinatario, listaTramitesUsuario, false);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRequisicaoBase(mensagemEnviadas));
    }
  }

  public void enviaEmailTerceirizada(ParametroSistemaService parametroSistemaService) throws Exception {
    this.parametroSistemaService = parametroSistemaService;
    int mensagemEnviadas = 0;
    List<TramiteRemessaVO> listaTramites = tramiteRemessaService.consultaTramitesEmailTerceirizada();
    if (Util.isNullOuVazio(listaTramites)) {
      logger.info(getLogRemessaTerceirizadaSemPendencia());
    } else { // envia os tramites pendentes
      Map<String, List<TramiteRemessaVO>> mapTramites = new HashMap<String, List<TramiteRemessaVO>>();
      // percorre os tramites gerando um Map com o email da Terceirizada e
      // os trâmites.
      for (TramiteRemessaVO tramite : listaTramites) {
        String emailTerceirizada = tramite.getRemessa().getEmpresaContrato().getEmpresa().getCaixaPostal();
        if (!mapTramites.containsKey(emailTerceirizada)) {
          mapTramites.put(emailTerceirizada, new ArrayList<TramiteRemessaVO>());
        }
        mapTramites.get(emailTerceirizada).add(tramite);
      }

      for (String emailTerceirizada : mapTramites.keySet()) {
        List<TramiteRemessaVO> listaTramitesUsuario = mapTramites.get(emailTerceirizada);
        Destinatario destinatario = new Destinatario(emailTerceirizada);
        destinatario.setNomeMaiusculas(
            listaTramitesUsuario.get(0).getRemessa().getEmpresaContrato().getEmpresa().getNome().toUpperCase());

        // adiciona todos os usuários da Terceirizada que fizeram algum
        // atendimento na Solicitacao na Cópia do Email.
        for (TramiteRemessaVO tramite : listaTramitesUsuario) {
          for (TramiteRemessaVO tramiteSolicitacao : tramite.getRemessa().getTramiteRemessas()) {
            if (EnviaEmailService.EhUsuarioTerceirizada(tramiteSolicitacao.getCodigoUsuario())) {
              destinatario.addDestinatarioCampoComCopia(tramiteSolicitacao.getCodigoUsuario());
            }
          }
        }

        enviaTramites(destinatario, listaTramitesUsuario, true);
        mensagemEnviadas++;
        updateDataHoraEmailNosTramites(listaTramitesUsuario);
      }
      logger.info(montarEmailLogRemessaTerceirizada(mensagemEnviadas));
    }
  }

  private void enviaTramites(Destinatario destinatario, List<TramiteRemessaVO> tramites, boolean extranet)
      throws EmailException, DataBaseException {
    StringBuilder mensagem = new StringBuilder();
    mensagem.append(getHtmlCabecalho(extranet));
    mensagem.append("Prezado(a) " + destinatario.getNome().toUpperCase() + "," + HTML_QUEBRA_LINHA);
    mensagem.append(HTML_QUEBRA_LINHA);
    mensagem.append("Informamos a alteração na situação da(s) Remessa(s) de documentos abaixo:" + HTML_QUEBRA_LINHA);
    mensagem.append(HTML_QUEBRA_LINHA);
    mensagem.append(HTML_TABELA_INICIO);
    mensagem.append(getHtmlLinha(new String[] { "Nº DA REMESSA", "DATA HORA", "DOCUMENTO", "SITUAÇÃO", "OBSERVAÇÃO" }));

    for (TramiteRemessaVO tramite : tramites) {
      RemessaVO remessaVO = tramite.getRemessa();
      List<RemessaDocumentoVO> listaDocumentoRemessa = remessaVO.getRemessaDocumentos();
      String listaDocumentosDiarios = "";
      if(!ObjectUtils.isNullOrEmpty(listaDocumentoRemessa)) {
        for (RemessaDocumentoVO remessaMovimentoDiarioVO : listaDocumentoRemessa) {
          listaDocumentosDiarios.concat(remessaMovimentoDiarioVO.getDocumento().getNome() + ",");
        }
      }
      String numeroRemessa = tramite.getRemessa().getId().toString().trim();
      String linkRemessa = montaHtmlLink(extranet, "/paginas/remessa/consulta?remessas=", numeroRemessa);
      mensagem.append(getHtmlLinha(new String[] { linkRemessa, Util.formatDataHora(tramite.getDataTramiteRemessa()),
          listaDocumentosDiarios, tramite.getSituacao().getNome(), tramite.getObservacao() }));
    }
    mensagem.append(HTML_TABELA_FIM);

    mensagem.append(getHtmlRodape(extranet));

    enviaEmail(this.parametroSistemaService, "SIRED - Notificações sobre Remessas", destinatario, mensagem, null, null);
  }

  /**
   * Metodo utilizado para atualizar o trâmite da requisição com a data hora do envio do e-mail para o usuário e a
   * unidade de lotação do mesmo.
   */
  @TransactionAttribute(TransactionAttributeType.NEVER)
  private void updateDataHoraEmailNosTramites(List<TramiteRemessaVO> listaTramitesUsuario)
      throws RequiredException, BusinessException, Exception {
    Date dataHoraTramiteEmail = new Date();
    for (TramiteRemessaVO tramite : listaTramitesUsuario) {
      tramite.setDataTramiteEmail(dataHoraTramiteEmail);
      tramiteRemessaService.update(tramite);
    }
  }
  
  public void enviaEmailRemessasAbertas(Map<UnidadeVO, RemessasAbertasDTO> remessasList, ParametroSistemaService parametroService) throws EmailException, IOException, AppException {
    for(Map.Entry<UnidadeVO, RemessasAbertasDTO> entry : remessasList.entrySet()) {
      UnidadeVO unidade = entry.getKey();
      RemessasAbertasDTO remessasDTO = entry.getValue();
      List<RemessaVO> remessasAbertasList = remessasDTO.getRemessasList();
      StringBuilder mensagem = montaCorpoEmail(unidade, remessasAbertasList);
      Destinatario destinatario = new Destinatario(unidade.getEnderecoEmail());
      destinatario.setNomeMaiusculas(unidade.getNome());
      
      String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
      String filename = FileUtils.appendDateTimeToFileName(MensagemUtils.obterMensagem("remessa.consulta.label.arquivoExportacao"), new Date());
      
      File file = new File(caminho + filename);
      OutputStream outputStream = new FileOutputStream(file);
      
      ExportarRemessaXLS exportador = new ExportarRemessaXLS();
      exportador.exportarTabelaXLS(outputStream, remessasAbertasList);
      outputStream.flush();
      outputStream.close();
      
      enviaEmail(parametroService, "SIRED - Remessas Abertas", destinatario, mensagem, file.getAbsolutePath(), filename);
      
      org.apache.commons.io.FileUtils.deleteQuietly(file);
    }
  }
  
  private StringBuilder montaCorpoEmail(UnidadeVO unidade, List<RemessaVO> remessasList) {
    StringBuilder sb = new StringBuilder();
    sb.append(getHtmlCabecalho(false));
    sb.append("Prezado(a) " + unidade.getNome() + "," + HTML_QUEBRA_LINHA);
    sb.append(HTML_QUEBRA_LINHA);
    sb.append("Informamos a abertura da(s) Remessa(s) de documentos abaixo:" + HTML_QUEBRA_LINHA);
    sb.append(HTML_TABELA_INICIO);
    sb.append(getHtmlLinha(new String[] { "Nº / CÓD. TIPO C", "UNIDADE SOLICITANTE", "DT. ABERTURA", "BASE", "USUÁRIO ABERT." }));
    
      for (RemessaVO remessaVO : remessasList) {
        String codigoRemessa = "";
        if(remessaVO.getCodigoRemessaTipoC() != null) {
          codigoRemessa = remessaVO.getCodigoRemessaTipoC().toString().trim();
        }else {
          codigoRemessa = remessaVO.getId().toString();
        }
        String linkRequisicao = montaHtmlLink(false, "/paginas/remessa/consulta?remessas=", codigoRemessa);
        sb.append(getHtmlLinha(new String[] { linkRequisicao, remessaVO.getUnidadeSolicitante().getNome(), Util.formatDataHora(remessaVO.getDataHoraAbertura()), remessaVO.getBase().getNome(), remessaVO.getCodigoUsuarioAbertura() }));
      }
      
    sb.append(HTML_TABELA_FIM);
    sb.append(HTML_QUEBRA_LINHA);
    sb.append(HTML_QUEBRA_LINHA);
    return sb;
  }
  
}
