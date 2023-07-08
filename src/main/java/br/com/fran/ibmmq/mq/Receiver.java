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

//    @Autowired
//    @Qualifier("jmsTemplateFirstRetry")
//    private JmsTemplate jmsTemplateFirstRetry;
//
//    @Autowired
//    @Qualifier("jmsTemplateSecondRetry")
//    private JmsTemplate jmsTemplateSecondRetry;
//    @JmsListener(destination = "SPRING_TESTE", containerFactory = "myFactory")
//    public void receiveMessage(MensagemDTO mensagem) {
//        final int randomInterval = RandomUtil.randomInterval(0, 100);
//
//        log.info("Seed: {}", randomInterval);
//
//        if (randomInterval > 75) {
//            final int attempts = mensagem.incrementAttempt();
//            log.info("ERROR em mensagem, attempts: {}", attempts);
//
//            switch (attempts) {
//                case 1:
//                    log.info("Retry 1 em mensagem");
//                    jmsTemplateFirstRetry.convertAndSend(mensagem);
//                    return;
//                case 2:
//                    log.info("Retry 2 em mensagem");
//                    jmsTemplateSecondRetry.convertAndSend(mensagem);
//                    return;
//                default:
//                    log.error("Desistindo de mensagem {} depois de {} attempts", mensagem.getUuid(), attempts);
//                    break;
//            }
//
//        } else {
//            log.info("Received {} apos {} attempts", mensagem, mensagem.getAttempts());
//        }
//    }
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

//    @Bean
//    public MessageListener myListener() {
//        return m -> {
//            log.info("received: " + m);
//
//            try {
//                final MensagemDTO body = m.getBody(MensagemDTO.class);
//
//                final int randomInterval = RandomUtil.randomInterval(0, 100);
//                log.info("Seed: {}", randomInterval);
//
//                if (randomInterval >= 50) {
//                    throw new RuntimeException("Simulando erro em msg");
//                }
//
//                log.info("Mensagem {} consumida", body);
//            } catch (RuntimeException | JMSException e) {
//                log.error("erro", e);
//            }
//        };
//    }
}
