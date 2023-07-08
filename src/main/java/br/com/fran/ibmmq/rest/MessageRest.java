package br.com.fran.ibmmq.rest;

import br.com.fran.ibmmq.dto.MensagemDTO;
import br.com.fran.ibmmq.mq.Receiver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/mensagem")
@Slf4j
public class MessageRest {

    @Autowired
    private JmsTemplate jmsTemplate;

//    @Autowired
//    @Qualifier("jmsTemplateFirstRetry")
//    private JmsTemplate jmsTemplateFirstRetry;
//
//    @Autowired
//    @Qualifier("jmsTemplateSecondRetry")
//    private JmsTemplate jmsTemplateSecondRetry;
    @GetMapping("/cache")
    public ResponseEntity<List<MensagemDTO>> getCache() {
        // Send a message with a POJO - the template reuse the message converter
        return ResponseEntity.ok(Receiver.CACHE_OK);
    }

    @GetMapping("/dlq-cache")
    public ResponseEntity<List<MensagemDTO>> getDLQCache() {
        // Send a message with a POJO - the template reuse the message converter
        return ResponseEntity.ok(Receiver.CACHE_DLQ);
    }

    @PostMapping
    public void enviarMensagem(@RequestBody MensagemDTO mensagem) {
        // Send a message with a POJO - the template reuse the message converter
        log.info("Enviando mensagem: {}", mensagem);
        jmsTemplate.convertAndSend(mensagem);
    }

//    @PostMapping("/delayed")
//    public void enviarMensagemDelay(@RequestBody MensagemDTO mensagem) {
//        // Send a message with a POJO - the template reuse the message converter
//        log.info("Enviando mensagem: {}", mensagem);
//        jmsTemplateFirstRetry.convertAndSend(mensagem);
//    }
    @PostMapping("/batch/{amount}")
    public void enviarBatch(@RequestBody MensagemDTO mensagem, @PathVariable("amount") Integer amount) {
        // Send a message with a POJO - the template reuse the message converter
        log.info("Enviando mensagem batch: {}", mensagem);

        for (int i = 0; i < (amount == null ? 200 : amount); i++) {
            jmsTemplate.convertAndSend(new MensagemDTO(mensagem));
        }

        log.info("Batch concluido");

    }

}
