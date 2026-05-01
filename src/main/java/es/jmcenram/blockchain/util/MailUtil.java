package es.jmcenram.blockchain.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;

/**
 * Utilidad encargada del envio de correos desde la aplicacion.
 *
 * Permite:
 * - Crear la sesion SMTP
 * - Construir mensajes HTML
 * - Enviar credenciales o notificaciones al usuario
 *
 * Centraliza la configuracion del remitente y evita duplicar la integracion con Jakarta Mail.
 *
 * @author Jcena
 * @version 1.0
 */
public class MailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private static final String REMITENTE = "registroblockchain@gmail.com";
    private static final String PASSWORD = "ayez ddsw ypln kciv";

    /**
     * Construye la sesion SMTP autenticada usada por los envios de correo.
     *
     * Centralizar host, puerto, TLS y credenciales evita repetir configuracion en cada mensaje.
     *
     * @return sesion SMTP autenticada y preparada para enviar correos
     */
    private static Session getSession() {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            /**
             * Entrega a Jakarta Mail las credenciales del remitente configurado.
             *
             * El Authenticator las solicita durante la negociacion SMTP, no al crear cada correo.
             *
             * @return credenciales SMTP usadas por Jakarta Mail durante la autenticacion
             */
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });
    }

    /**
     * Envía un email HTML genérico
     */
    public static void enviarEmail(String destinatario, String asunto, String html) {
        try {
            Message message = new MimeMessage(getSession());

            message.setFrom(new InternetAddress(REMITENTE, Messages.getString("email_platform_name")));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario)
            );

            message.setSubject(asunto);
            message.setContent(html, "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (Exception e) {
            Platform.runLater(() ->
                    mostrarError(Messages.getString("error_sending_email"))
            );
        }
    }

    /**
     * Email específico para creación de usuario
     */
    public static void enviarEmailRegistro(String email, String nombre, String password)
            throws MessagingException {

        String html = """
                <html>
                <body style="font-family: Arial; background:#f4f6f8; padding:20px;">
                    <div style="max-width:600px;margin:auto;background:white;border-radius:10px;overflow:hidden;">
                        
                        <div style="background:#0f172a;color:white;padding:20px;text-align:center;">
                            <h2>%s</h2>
                        </div>

                        <div style="padding:30px;">
                            <h3>%s</h3>
                            <p>%s</p>

                            <div style="background:#e0f2fe;padding:15px;border-radius:8px;">
                                <strong>%s</strong>
                                <p style="font-size:18px;">%s</p>
                            </div>

                            <p>%s</p>
                        </div>

                        <div style="background:#0f172a;color:#94a3b8;text-align:center;padding:10px;font-size:12px;">
                            %s
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                Messages.getString("email_platform_name"),
                Messages.getString("email_hello").formatted(nombre),
                Messages.getString("email_account_created"),
                Messages.getString("email_temp_password"),
                password,
                Messages.getString("email_change_password"),
                Messages.getString("email_footer")
        );

        enviarEmail(email, Messages.getString("email_subject"), html);
    }
}