package br.com.qrserve.infrastructure.notification;

import br.com.qrserve.application.sessao.FeedbackSender;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotification implements FeedbackSender {

    @Override
    public void notificar() {
        // TODO notificação em web socket será implementada na task QR-004
    }
}
