package com.rinconcitomarino.service;

import com.rinconcitomarino.model.Opinion;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class OpinionNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OpinionNotificationService.class);
    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String notificationTo;
    private final String from;

    public OpinionNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notifications.opinions.enabled:false}") boolean enabled,
            @Value("${app.notifications.opinions.to:}") String notificationTo,
            @Value("${app.notifications.from:no-reply@rinconcitomarino.local}") String from
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.notificationTo = notificationTo;
        this.from = from;
    }

    public void enviarOpinionRecibida(Opinion opinion) {
        if (!enabled || notificationTo == null || notificationTo.isBlank()) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Opinion email skipped: JavaMailSender is not configured.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(notificationTo);
            helper.setReplyTo(opinion.getCorreo());
            helper.setSubject("Nueva opinion recibida - Rinconcito Marino");
            helper.setText(crearPlantillaHtml(opinion), true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Opinion email could not be sent: {}", ex.getMessage());
        }
    }

    private String crearPlantillaHtml(Opinion opinion) {
        String nombres = escapar(opinion.getNombres());
        String correo = escapar(opinion.getCorreo());
        String comentario = escapar(opinion.getComentario()).replace("\n", "<br>");
        String fecha = opinion.getFechaRegistro() == null ? "Recien recibida" : opinion.getFechaRegistro().format(FECHA_FORMATO);

        return """
                <!DOCTYPE html>
                <html lang="es">
                <body style="margin:0;padding:0;background:#eef3f6;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef3f6;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:620px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 18px 45px rgba(15,31,45,.14);">
                          <tr>
                            <td style="background:#212529;padding:28px 32px;color:#ffffff;">
                              <div style="font-size:13px;letter-spacing:.12em;text-transform:uppercase;color:#f2c57c;">Rinconcito Marino</div>
                              <h1 style="margin:10px 0 0;font-size:28px;line-height:1.2;">Nueva opinion recibida</h1>
                              <p style="margin:10px 0 0;color:#d8dee3;font-size:15px;">Un cliente dejo un comentario desde la pagina web.</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:30px 32px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="padding:0 0 16px;">
                                    <div style="font-size:13px;color:#6b7280;">Cliente</div>
                                    <div style="font-size:20px;font-weight:700;color:#123d5c;">%s</div>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:0 0 16px;">
                                    <div style="font-size:13px;color:#6b7280;">Correo</div>
                                    <a href="mailto:%s" style="font-size:16px;color:#0f4c75;text-decoration:none;">%s</a>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:0 0 22px;">
                                    <div style="font-size:13px;color:#6b7280;">Fecha</div>
                                    <div style="font-size:16px;color:#1f2937;">%s</div>
                                  </td>
                                </tr>
                              </table>
                              <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;padding:22px;">
                                <div style="font-size:13px;color:#6b7280;margin-bottom:8px;">Comentario</div>
                                <div style="font-size:17px;line-height:1.65;color:#111827;">%s</div>
                              </div>
                              <div style="padding-top:24px;">
                                <a href="mailto:%s" style="display:inline-block;background:#0f4c75;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:10px;font-weight:700;">Responder al cliente</a>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f3f4f6;padding:18px 32px;color:#6b7280;font-size:13px;">
                              Este mensaje fue generado automaticamente desde el formulario de opiniones.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(nombres, correo, correo, fecha, comentario, correo);
    }

    private String escapar(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
