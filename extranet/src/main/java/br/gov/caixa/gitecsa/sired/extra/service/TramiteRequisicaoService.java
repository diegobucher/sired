package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.extra.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.sired.extra.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class TramiteRequisicaoService extends AbstractService<TramiteRequisicaoVO> {
    private static final String ERRO_GRAVAR_ARQUIVO = "Erro tentando gravar o arquivo: ";

    private static final long serialVersionUID = 3686251199326853006L;

    @Inject
    private transient Logger LOG;

    @Inject
    private TramiteRequisicaoDAO tramiteRequisicaoDAO;

    @Inject
    private RequisicaoDAO requisicaoDAO;
    
    @Inject
    private DocumentoOriginalService docOriginalService;

    public void excluirByRequisicao(RequisicaoVO requisicaoVO) throws AppException {
        tramiteRequisicaoDAO.excluirByRequisicao(requisicaoVO);
    }

    public SituacaoRequisicaoEnum salvarTramiteRequisicao(List<TramiteRequisicaoVO> tramitesRequisicao) throws BusinessException {
        SituacaoRequisicaoEnum situacaoPendente = null;

        for (TramiteRequisicaoVO tramite : tramitesRequisicao) {
            Long situacao = (Long) tramite.getSituacaoRequisicao().getId();

            tramite = salvarTramiteRequisicao(tramite);

            if (situacao.equals(SituacaoRequisicaoEnum.ABERTA.getId()) || situacao.equals(SituacaoRequisicaoEnum.REABERTA.getId())) {
                situacaoPendente = SituacaoRequisicaoEnum.PENDENTE_UPLOAD;
            }

        }

        return situacaoPendente;
    }

    public TramiteRequisicaoVO salvarTramiteRequisicao(TramiteRequisicaoVO tramite) throws BusinessException {
        try {
            Long situacao = (Long) tramite.getSituacaoRequisicao().getId();
            Long ocorrencia = (Long) tramite.getOcorrencia().getId();

            if (situacao.equals(SituacaoRequisicaoEnum.ABERTA.getId())) {
                if (ocorrencia.equals(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor())) {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId()));
                } else if (ocorrencia.equals(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getValor())) {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId()));
                } else {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.ATENDIDA.getId()));
                }
            } else if (situacao.equals(SituacaoRequisicaoEnum.REABERTA.getId())) {
                if (ocorrencia.equals(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor())) {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId()));
                } else if (ocorrencia.equals(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getValor())) {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId()));
                } else {
                    tramite.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.REATENDIDA.getId()));
                }
            }

            RequisicaoVO requisicao = requisicaoDAO.findById((Long) tramite.getRequisicao().getId());

            requisicao.setTramiteRequisicaoAtual(tramite);

            requisicaoDAO.update(requisicao);

            tramite = tramiteRequisicaoDAO.save(tramite);

            return tramite;
        } catch (Exception e) {
            LOG.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "TramiteRequisicaoService", "obterTramitesPorRequisicao()"));
            throw new BusinessException(e.getMessage());
        }
    }

    public TramiteRequisicaoVO salvarTramiteRequisicao(RequisicaoVO requisicao, TramiteRequisicaoVO tramiteAtual, UploadedFile arquivoDisponibilizado,
            String nomeArquivo) throws BusinessException {
        try {
            Long situacao = (Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId();
            Long ocorrencia = (Long) tramiteAtual.getOcorrencia().getId();

            TramiteRequisicaoVO tramiteNovo = new TramiteRequisicaoVO();

            Util.copiarInformacoesTramite(tramiteAtual, tramiteNovo);

            tramiteNovo.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
            tramiteNovo.setDataHora(Calendar.getInstance().getTime());
            tramiteNovo.setArquivoDisponibilizado(nomeArquivo);
            
            if (!ObjectUtils.isNullOrEmpty(tramiteNovo.getObservacao())) {
                String obs = tramiteNovo.getObservacao();
                tramiteNovo.setObservacao(obs.substring(0, Math.min(Constantes.NUM_CHARS_OBSERVACAO_TRAMITE, obs.length())));
            }

            tramiteNovo.setRequisicao(requisicao);

            if (situacao.equals(SituacaoRequisicaoEnum.ABERTA.getId()) || situacao.equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId())) {
                if (ocorrencia.equals(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getValor())) {
                    tramiteNovo.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId()));
                } else {
                    tramiteNovo.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.ATENDIDA.getId()));
                }
            } else if (situacao.equals(SituacaoRequisicaoEnum.REABERTA.getId()) || situacao.equals(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId())) {
                if (ocorrencia.equals(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getValor())) {
                    tramiteNovo.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId()));
                } else {
                    tramiteNovo.setSituacaoRequisicao(new SituacaoRequisicaoVO(SituacaoRequisicaoEnum.REATENDIDA.getId()));
                }
            }
            tramiteNovo.setDataHoraAtendimento(Calendar.getInstance().getTime());

            requisicao = requisicaoDAO.findById((Long) requisicao.getId());

            requisicao.setTramiteRequisicaoAtual(tramiteNovo);

            requisicaoDAO.update(requisicao);

            tramiteNovo = tramiteRequisicaoDAO.save(tramiteNovo);
            
            // #OS310
            if ((SituacaoRequisicaoEnum.ATENDIDA.getId().equals(tramiteNovo.getSituacaoRequisicao().getId())
                    || SituacaoRequisicaoEnum.REATENDIDA.getId().equals(tramiteNovo.getSituacaoRequisicao().getId())) 
                    && OcorrenciaAtendimentoEnum.ORIGINAL_UNIDADE.getValor().equals(ocorrencia)) {
                UsuarioLdap usuario = (UsuarioLdap)RequestUtils.getSessionValue("usuario");
                this.docOriginalService.registrarAndamento(SituacaoDocumentoOriginalEnum.ENVIADO, requisicao, usuario.getEmail());
            }

            if (arquivoDisponibilizado != null) {
                FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_DOCUMENTOS));
                FileUtils.saveFile(new File(System.getProperty(Constantes.EXTRANET_DIRETORIO_DOCUMENTOS) + nomeArquivo), arquivoDisponibilizado.getContents());
            }

            return tramiteNovo;
        } catch (IOException e) {
            LOG.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "TramiteRequisicaoService", "obterTramitesPorRequisicao()"));
            throw new BusinessException(ERRO_GRAVAR_ARQUIVO + e.getMessage());
        } catch (Exception e) {
            LOG.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "TramiteRequisicaoService", "obterTramitesPorRequisicao()"));
            throw new BusinessException(e.getMessage());
        }
    }

    public List<TramiteRequisicaoSuporteDTO> getGroupByTipoSuporte(RelatorioSuporteDTO relatorio) throws AppException {
        return tramiteRequisicaoDAO.getGroupByTipoSuporte(relatorio);
    }

    public ArrayList<TramiteRequisicaoSuporteDTO> mounRetornoConsultaByTipoSuporte(List<TramiteRequisicaoSuporteDTO> lista) {
        return tramiteRequisicaoDAO.mounRetornoConsultaByTipoSuporte(lista);
    }

    public List<TramiteRequisicaoVO> obterTramitesPorRequisicao(Long idRequisicao) {
        return tramiteRequisicaoDAO.obterTramitesPorRequisicao(idRequisicao);
    }

    @Override
    protected void validaCamposObrigatorios(TramiteRequisicaoVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(TramiteRequisicaoVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegras(TramiteRequisicaoVO entity) {
        // do nothing
    }

    @Override
    protected GenericDAO<TramiteRequisicaoVO> getDAO() {
        return tramiteRequisicaoDAO;
    }
    
    public List<TramiteRequisicaoVO> findAtendimentosRequisicao(RequisicaoVO requisicao) {
        return this.tramiteRequisicaoDAO.findAtendimentosRequisicao(requisicao);
    }

}
