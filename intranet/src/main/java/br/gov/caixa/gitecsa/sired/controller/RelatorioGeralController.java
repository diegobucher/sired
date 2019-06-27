package br.gov.caixa.gitecsa.sired.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.converter.DataConverterSIRED;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;

@Named("relatorioGeralController")
@ViewScoped
public class RelatorioGeralController implements Serializable {

    private static final long serialVersionUID = -6358867135591574653L;

    private Date dataInicio = Util.getDataMesPassado();

    private Date dataFim;

    private List<ResumoAtendimentoRequisicaoDTO> listRequisicao;

    private List<ResumoAtendimentoRemessaDTO> listRemessa;

    private ResumoAtendimentoRequisicaoDTO totalRequisicao = new ResumoAtendimentoRequisicaoDTO();

    private ResumoAtendimentoRemessaDTO totalRemessa = new ResumoAtendimentoRemessaDTO();

    @Inject
    private RequisicaoService requisicaoService;

    @Inject
    private RemessaService remessaService;

    @Inject
    protected FacesMensager facesMessager;

    public void localizar() {
        preencheDataFimDefault();

        if (this.validarCampos()) {
            this.gerarResumoRequisicao();
            this.gerarResumoRemessa();

            if (this.listRequisicao.size() == 0 && this.listRemessa.size() == 0) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MI017"));
            }
        }
    }

    /**
     * Preenche a data fim com a data atual quando o usuário não preenche a mesma.
     */
    public void preencheDataFimDefault() {
        if ((!Util.isNullOuVazio(this.getDataInicio())) && Util.isNullOuVazio(this.getDataFim())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") == null || !DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim"))) {
            this.setDataFim(new Date());
            updateComponentes("formConsulta:idDataFim");
        }
    }

    /**
     * Faz update nos componentes dos respectivos ids passados como parâmetro
     * 
     * @param idComponente
     */
    protected void updateComponentes(String... idComponente) {
        for (String id : idComponente) {
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(id);
        }
    }

    private void gerarResumoRequisicao() {

        this.totalRequisicao = new ResumoAtendimentoRequisicaoDTO();
        this.listRequisicao = this.requisicaoService.getResumoAtendimentos(this.dataInicio, this.dataFim);

        int totalAbertas = 0;
        int totalFechadas = 0;
        int totalRecuperadas = 0;
        int totalNaoLocalizadas = 0;
        int totalOutras = 0;

        for (ResumoAtendimentoRequisicaoDTO resumo : this.listRequisicao) {
            totalAbertas += resumo.getNumAbertas();
            totalFechadas += resumo.getNumFechadas();
            totalRecuperadas += resumo.getNumRecuperadas();
            totalNaoLocalizadas += resumo.getNumNaoLocalizadas();
            totalOutras += resumo.getNumOutras();
        }

        this.totalRequisicao.setNumAbertas(totalAbertas);
        this.totalRequisicao.setNumFechadas(totalFechadas);
        this.totalRequisicao.setNumRecuperadas(totalRecuperadas);
        this.totalRequisicao.setNumNaoLocalizadas(totalNaoLocalizadas);
        this.totalRequisicao.setNumOutras(totalOutras);
    }

    private void gerarResumoRemessa() {

        this.totalRemessa = new ResumoAtendimentoRemessaDTO();
        this.listRemessa = this.remessaService.getResumoAtendimentos(this.dataInicio, this.dataFim);

        int totalAbertas = 0;
        int totalFechadas = 0;
        int totalAgendadas = 0;
        int totalRecebidas = 0;
        int totalConferidas = 0;

        int totalQtdRemessas = 0;
        int totalQtdItensRemessas = 0;
        int totalRemessasDentroPrazo = 0;

        for (ResumoAtendimentoRemessaDTO resumo : this.listRemessa) {

            BaseVO base = new BaseVO();
            base.setNome(resumo.getBase());

            resumo.setNumItens(this.remessaService.getQtdItensPorBase(base, this.dataInicio, this.dataFim));
            resumo.setNumRemessas(this.remessaService.getQtdRemessasPorBase(base, this.dataInicio, this.dataFim));
            resumo.setNumRemessasDentroPrazo(this.remessaService.getQtdRemessasDentroPrazoPorBase(base, this.dataInicio, this.dataFim));

            totalAbertas += resumo.getNumAbertas();
            totalFechadas += resumo.getNumFechadas();
            totalAgendadas += resumo.getNumAgendadas();
            totalRecebidas += resumo.getNumRecebidas();
            totalConferidas += resumo.getNumConferidas();

            totalQtdRemessas += resumo.getNumRemessas();
            totalQtdItensRemessas += resumo.getNumItens();
            totalRemessasDentroPrazo += resumo.getNumRemessasDentroPrazo();
        }

        this.totalRemessa.setNumAbertas(totalAbertas);
        this.totalRemessa.setNumFechadas(totalFechadas);
        this.totalRemessa.setNumAgendadas(totalAgendadas);
        this.totalRemessa.setNumRecebidas(totalRecebidas);
        this.totalRemessa.setNumConferidas(totalConferidas);

        this.totalRemessa.setNumRemessas(totalQtdRemessas);
        this.totalRemessa.setNumItens(totalQtdItensRemessas);
        this.totalRemessa.setNumRemessasDentroPrazo(totalRemessasDentroPrazo);
    }

    private boolean validarCampos() {
        if (ObjectUtils.isNullOrEmpty(this.dataInicio)) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
            return false;
        }

        if (ObjectUtils.isNullOrEmpty(this.dataFim)) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataFim"));
            return false;
        }

        Calendar dtInicio = Calendar.getInstance();
        dtInicio.setLenient(false);
        dtInicio.setTime(this.dataInicio);

        Calendar dtFim = Calendar.getInstance();
        dtFim.setLenient(false);
        dtFim.setTime(this.dataFim);

        if (dtInicio.after(Calendar.getInstance())) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataInicio"));
            return false;
        }

        if (dtFim.after(Calendar.getInstance())) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataFim"));
            return false;
        }

        if (dtFim.before(dtInicio)) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
            return false;
        }

        return true;
    }

    public int sort(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            return ((String) o1).compareToIgnoreCase(((String) o2));
        }

        return ((Integer) o1).compareTo((Integer) o2);
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public List<ResumoAtendimentoRequisicaoDTO> getListRequisicao() {
        return listRequisicao;
    }

    public void setListRequisicao(List<ResumoAtendimentoRequisicaoDTO> listRequisicao) {
        this.listRequisicao = listRequisicao;
    }

    public List<ResumoAtendimentoRemessaDTO> getListRemessa() {
        return listRemessa;
    }

    public void setListRemessa(List<ResumoAtendimentoRemessaDTO> listRemessa) {
        this.listRemessa = listRemessa;
    }

    public ResumoAtendimentoRequisicaoDTO getTotalRequisicao() {
        return totalRequisicao;
    }

    public void setTotalRequisicao(ResumoAtendimentoRequisicaoDTO totalRequisicao) {
        this.totalRequisicao = totalRequisicao;
    }

    public ResumoAtendimentoRemessaDTO getTotalRemessa() {
        return totalRemessa;
    }

    public void setTotalRemessa(ResumoAtendimentoRemessaDTO totalRemessa) {
        this.totalRemessa = totalRemessa;
    }

}
