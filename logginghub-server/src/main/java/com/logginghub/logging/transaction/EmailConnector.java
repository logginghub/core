package com.logginghub.logging.transaction;

import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.logginghub.logging.transaction.configuration.EmailConnectorConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.HTMLBuilder2.Element;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;
import com.sun.mail.smtp.SMTPTransport;

@Provides(EmailContent.class) public class EmailConnector implements Module<EmailConnectorConfiguration>, Destination<EmailContent> {

    private static final Logger logger = Logger.getLoggerFor(EmailConnector.class);
    private EmailConnectorConfiguration configuration;
    private Throttler throttler;

    @Override public void configure(EmailConnectorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        if (configuration.getSendingThottle() != null && configuration.getSendingThottle().length() > 0) {
            throttler = new Throttler(TimeUtils.parseInterval(configuration.getSendingThottle()), TimeUnit.MILLISECONDS);
        }

    }

    @Override public void start() {}

    @Override public void stop() {}

    @Override public void send(final EmailContent t) {
        
        if (throttler != null) {
            if (!throttler.isOkToFire()) {
                logger.warn("We've tried to send too many emails within the throttle period, dropping this message : {}", t);
                return;
            }
        }
        
        WorkerThread.execute("Email Sender", new Runnable() {
            @Override public void run() {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

                // Get a Properties object
                Properties props = System.getProperties();

                List<String> properties = configuration.getProperties();
                for (String string : properties) {
                    String[] split = string.split("=");
                    props.setProperty(split[0], split[1]);
                }

                Session session = Session.getInstance(props, null);

                // -- Create a new message --
                final MimeMessage msg = new MimeMessage(session);

                try {
                    // -- Set the FROM and TO fields --
                    msg.setFrom(new InternetAddress(t.getFromAddress()));
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(t.getToAddress(), false));

                    if (t.getCcAddress().length() > 0) {
                        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(t.getCcAddress(), false));
                    }

                    if (t.getBccAddress().length() > 0) {
                        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(t.getBccAddress(), false));
                    }

                    msg.setSubject(t.getSubject());
                    if (t.isHTML()) {
                        msg.setText(t.getMessage(), "utf-8", "html");
                    }
                    else {
                        msg.setText(t.getMessage(), "utf-8");
                    }
                    msg.setSentDate(new Date());

                    SMTPTransport transport = (SMTPTransport) session.getTransport("smtps");

                    transport.connect(configuration.getSMTPHost(), configuration.getUsername(), configuration.getPassword());
                    transport.sendMessage(msg, msg.getAllRecipients());
                    transport.close();
                }
                catch (Exception e) {
                    logger.warn(e, "Failed to send email");
                }
            }
        });

       
    }

    /**
     * Send email using GMail SMTP server.
     * 
     * @param username
     *            GMail username
     * @param password
     *            GMail password
     * @param recipientEmail
     *            TO recipient
     * @param title
     *            title of the message
     * @param message
     *            message to be sent
     * @throws AddressException
     *             if the email address parse failed
     * @throws MessagingException
     *             if the connection is dead or not in the connected state or if the message is not
     *             a MimeMessage
     */
    public static void Send(final String username, final String password, String recipientEmail, String title, String message)
                    throws AddressException, MessagingException {
        EmailConnector.Send(username, password, recipientEmail, "", title, message);
    }

    /**
     * Send email using GMail SMTP server.
     * 
     * @param username
     *            GMail username
     * @param password
     *            GMail password
     * @param recipientEmail
     *            TO recipient
     * @param ccEmail
     *            CC recipient. Can be empty if there is no CC recipient
     * @param title
     *            title of the message
     * @param message
     *            message to be sent
     * @throws AddressException
     *             if the email address parse failed
     * @throws MessagingException
     *             if the connection is dead or not in the connected state or if the message is not
     *             a MimeMessage
     */
    public static void Send(final String username, final String password, String recipientEmail, String ccEmail, String title, String message)
                    throws AddressException, MessagingException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtps.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtps.auth", "true");

        /*
         * If set to false, the QUIT command is sent and the connection is immediately closed. If
         * set to true (the default), causes the transport to wait for the response to the QUIT
         * command.
         * 
         * ref :
         * http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
         * http://forum.java.sun.com/thread.jspa?threadID=5205249 smtpsend.java - demo program from
         * javamail
         */
        props.put("mail.smtps.quitwait", "false");

        Session session = Session.getInstance(props, null);

        // -- Create a new message --
        final MimeMessage msg = new MimeMessage(session);

        // -- Set the FROM and TO fields --
        msg.setFrom(new InternetAddress(username + "@gmail.com"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

        if (ccEmail.length() > 0) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
        }

        msg.setSubject(title);
        msg.setText(message, "utf-8", "html");
        msg.setSentDate(new Date());

        SMTPTransport t = (SMTPTransport) session.getTransport("smtps");

        t.connect("smtp.gmail.com", username, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
    }

    public static void main(String[] args) {
        HTMLBuilder2 builder = new HTMLBuilder2();
        Element div = builder.getBody().createChild("div");
        div.createImage("http://www.logginghub.com/LoggingHubLogo.png");
        div.setText("<h1>This is an html test email</h1>");

        try {
            EmailConnector.Send("vertexlabstest", "c9107400573be66b5bb60a6933144b34", "jamesshaw5@gmail.com", "Test email", builder.toString());
        }
        catch (AddressException e) {
            e.printStackTrace();
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}