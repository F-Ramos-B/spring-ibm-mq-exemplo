package br.com.fran.ibmmq.config;

import javax.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.retry.annotation.EnableRetry;

@EnableJms
@EnableRetry
@Configuration
@Slf4j
public class MQConfig {

    private static final String QUEUE = "SPRING_TESTE";

    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all auto-configured defaults to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some settings if necessary.
        factory.setMessageConverter(jacksonJmsMessageConverter());
        return factory;
    }

    @Bean
    @Primary
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setDefaultDestinationName(QUEUE);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTemplate;
    }

//    @Bean
//    public JmsTemplate jmsTemplateFirstRetry(ConnectionFactory connectionFactory) {
//        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
//        jmsTemplate.setDefaultDestinationName(QUEUE);
//        jmsTemplate.setDeliveryDelay(Duration.ofSeconds(10).toMillis());
//        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
//        return jmsTemplate;
//    }
//
//    @Bean
//    public JmsTemplate jmsTemplateSecondRetry(ConnectionFactory connectionFactory) {
//        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
//        jmsTemplate.setDefaultDestinationName(QUEUE);
//        jmsTemplate.setDeliveryDelay(Duration.ofSeconds(25).toMillis());
//        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
//        return jmsTemplate;
//    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

//    @Bean
//    public DefaultMessageListenerContainer container(ConnectionFactory connectionFactory, JmsTemplate template, MessageListener myListener) {
//        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setDestinationName(QUEUE);
//        container.setSessionTransacted(true);
//        container.setMessageListener(new RetryingListener(myListener, template));
//        container.setMessageConverter(jacksonJmsMessageConverter());
//
//        return container;
//    }

//    @Bean
//    public MessageListener myListener() {
//        return m -> {
//            log.info("received: " + m);
//            try {
//                final MensagemDTO body = m.getBody(MensagemDTO.class);
//                
//                
//                
//                log.info("body in retry {}");
//            } catch (RuntimeException | JMSException e) {
//                log.error("erro", e);
//            }
//        };
//    }
//    public static class RetryingListener implements MessageListener {
//
//        private final RetryTemplate retryTemplate = new RetryTemplate();
//
//        private final MessageListener delegate;
//
//        private final JmsTemplate jmsTemplate;
//
//        public RetryingListener(MessageListener delegate, JmsTemplate template) {
//            
//            this.delegate = delegate;
//            SimpleRetryPolicy policy = new SimpleRetryPolicy();
//            policy.setMaxAttempts(2);
//            this.retryTemplate.setRetryPolicy(policy);
//
//            ExponentialBackOffPolicy ebop = new ExponentialBackOffPolicy();
//
//            ebop.setMultiplier(6);
//            ebop.setInitialInterval(10);
//            ebop.setMaxInterval(60);
//
//            this.retryTemplate.setBackOffPolicy(ebop);
//            this.retryTemplate.setRetryContextCache(new SoftReferenceMapRetryContextCache());
//            this.jmsTemplate = template;
//        }
//
//        @Override
//        public void onMessage(Message message) {
//            String jmsMessageID = null;
//            try {
//                jmsMessageID = message.getJMSMessageID();
//            } catch (JMSException e) {
//                log.error("error", e);
//            }
//            this.retryTemplate.execute((RetryContext context) -> {
//                context.setAttribute("message", message);
//                RetryingListener.this.delegate.onMessage(message);
//                log.info("fez RETRIER");
//                return null;
//            }, (RetryContext context) -> {
//                final TextMessage message1 = (TextMessage) context.getAttribute("message");
//                RetryingListener.this.jmsTemplate.send("SPRING_TESTE_DLQ", s -> {
//                    Message m = s.createTextMessage(message1.getText());
//                    // copy other headers from original as needed
//                    m.setStringProperty("exception", context.getLastThrowable().getMessage());
//                    log.info("sent to dlq:" + m);
//                    return m;
//                });
//                return null;
//            }, new DefaultRetryState(jmsMessageID, false, null));
//        }
//
//    }

}
