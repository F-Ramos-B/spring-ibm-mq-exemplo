package br.com.fran.ibmmq.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensagemDTO {

    private String uuid = UUID.randomUUID().toString();
    private String nome;
    private int idade;
    private String cidade;
    private int attempts = 0;
    
    public int incrementAttempt() {
        this.attempts++;
        return attempts;
    }

    public MensagemDTO(MensagemDTO mensagemDTO) {
        this.nome = mensagemDTO.getNome();
        this.idade = mensagemDTO.getIdade();
        this.cidade = mensagemDTO.getCidade();
    }
    
}
