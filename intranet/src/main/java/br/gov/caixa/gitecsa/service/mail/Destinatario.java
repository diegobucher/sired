package br.gov.caixa.gitecsa.service.mail;

import java.util.HashSet;
import java.util.Set;

import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

public class Destinatario {
    private String enderecoEmail;
    private String nome;
    private Set<String> destinatariosCampoComCopia;

    Destinatario(String email) {
        this.enderecoEmail = email;
    }

    Destinatario(String email, String nome) {
        this.enderecoEmail = email;
        setNomeMaiusculas(nome);
    }

    public String getEnderecoEmail() {
        return enderecoEmail;
    }

    public String getNome() {
        return nome;
    }

    public void setNomeMaiusculas(String nome) {
        if (!ObjectUtils.isNullOrEmpty(nome)) {
            this.nome = nome.toUpperCase();
        }
    }

    public Set<String> getDestinatariosCampoComCopia() {
        return destinatariosCampoComCopia;
    }

    public void addDestinatarioCampoComCopia(String destinatarioCampoComCopia) {
        if (ObjectUtils.isNullOrEmpty(this.destinatariosCampoComCopia)) {
            this.destinatariosCampoComCopia = new HashSet<String>();
        }
        this.destinatariosCampoComCopia.add(destinatarioCampoComCopia);
    }

    // /**
    // * Monta os destinatários de email para o campo Com Cópia.
    // *
    // * @return String com o conteúdo a ser adicionado no campo Com Cópia do
    // email.
    // */
    // public String getEnderecosEmailCopia() {
    // String enderecoEmailCopia = null;
    // if (!Util.isNullOuVazio(this.destinatariosCampoComCopia)) {
    // enderecoEmailCopia =
    // StringUtils.join(this.getDestinatariosCampoComCopia(),
    // EnviaEmailService.SEPARADOR_EMAILS);
    // // for (String dest : destinatariosCampoComCopia) {
    // // enderecoEmailCopia += dest + EnviaEmailService.SEPARADOR_EMAILS;
    // // }
    // // enderecoEmailCopia.substring(0, enderecoEmailCopia.length() -
    // EnviaEmailService.SEPARADOR_EMAILS.length());
    // }
    // return enderecoEmailCopia;
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((enderecoEmail == null) ? 0 : enderecoEmail.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Destinatario other = (Destinatario) obj;
        if (enderecoEmail == null) {
            if (other.enderecoEmail != null)
                return false;
        } else if (!enderecoEmail.equalsIgnoreCase(other.enderecoEmail))
            return false;
        return true;
    }
}
