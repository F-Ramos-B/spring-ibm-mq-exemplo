package br.com.fran.ibmmq.mq;

import br.com.fran.ibmmq.dto.MensagemDTO;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Receiver {

    @Autowired
    private JmsTemplate jmsTemplate;

    public static final List<MensagemDTO> CACHE_OK = new LinkedList<>();
    public static final List<MensagemDTO> CACHE_DLQ = new LinkedList<>();

    @JmsListener(destination = "SPRING_TESTE", containerFactory = "myFactory", concurrency = "7-10")
    @Retryable(backoff = @Backoff(delay = 2000))
    public void receiveMessage(MensagemDTO mensagem) {
        final int randomInterval = RandomUtil.randomInterval(0, 100);

        log.info("Seed: {}", randomInterval);

        if (randomInterval >= 30) {
            final RuntimeException runtimeException = new RuntimeException("Simulando erro em msg");
            log.error("Simulando erro em msg", runtimeException);
            throw runtimeException;
        }

        log.info("Mensagem {} consumida", mensagem);
        CACHE_OK.add(mensagem);
    }

    @Recover
    public void recover(Exception ex, MensagemDTO mensagem) {
        log.info("RECOVER INICIADO para mensagem: {}", mensagem);
        log.info("Erro na mensagem", ex);
        jmsTemplate.convertAndSend("SPRING_TESTE_DLQ", mensagem);
    }

    @JmsListener(destination = "SPRING_TESTE_DLQ", containerFactory = "myFactory")
    public void receiveMessageDLQ(MensagemDTO mensagem) {
        CACHE_DLQ.add(mensagem);
    }

}
