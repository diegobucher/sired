package br.gov.caixa.gitecsa.sired.dto;

import br.gov.caixa.gitecsa.sired.enumerator.RemoteCommandMessageTypeEnum;

public class RemoteCommandMessage {
    
    private String message;
    
    private RemoteCommandMessageTypeEnum type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RemoteCommandMessageTypeEnum getType() {
        return type;
    }
    
    public void setType(RemoteCommandMessageTypeEnum type) {
        this.type = type;
    }
}
