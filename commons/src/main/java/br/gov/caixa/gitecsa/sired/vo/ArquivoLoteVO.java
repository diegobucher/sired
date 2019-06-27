package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.ConclusaoAtendimentoLoteEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs04_arquivo_lote", schema = Constantes.SCHEMADB_NAME)
public class ArquivoLoteVO extends BaseEntity {
    private static final long serialVersionUID = 49786883127123901L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_arquivo_lote", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_empresa_c12", columnDefinition = "int2")
    private EmpresaVO empresa;

    @Column(name = "no_arquivo_lote")
    private String nome;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_envio_arquivo")
    private Date dataEnvioArquivo;

    @Column(name = "co_usuario", columnDefinition = "bpchar")
    private String codigoUsuario;

    @Column(name = "no_relatorio_lote", columnDefinition = "bpchar")
    private String relatorioLote;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_relatorio_concluido", columnDefinition = "int2")
    private ConclusaoAtendimentoLoteEnum concluido;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public EmpresaVO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaVO empresa) {
        this.empresa = empresa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getDataEnvioArquivo() {
        return dataEnvioArquivo;
    }

    public void setDataEnvioArquivo(Date dataEnvioArquivo) {
        this.dataEnvioArquivo = dataEnvioArquivo;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public String getRelatorioLote() {
        return relatorioLote;
    }

    public void setRelatorioLote(String relatorioLote) {
        this.relatorioLote = relatorioLote;
    }

    public ConclusaoAtendimentoLoteEnum getConcluido() {
        return concluido;
    }

    public void setConcluido(ConclusaoAtendimentoLoteEnum concluido) {
        this.concluido = concluido;
    }
}
