package views;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import javax.swing.JOptionPane;
import smtp.SmtpProtocol;
import smtp.client.SmtpClient;
import smtp.exceptions.SmtpClientInitializationException;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpClientView extends javax.swing.JFrame
{

    protected static final Pattern PATTERN_EMAIL = Pattern.compile(
        "((?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\]))",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * @param args Command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try
        {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(SmtpClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new SmtpClientView().setVisible(true);
        });
    }

    /**
     * Creates new form SmtpClientView
     */
    public SmtpClientView()
    {
        // Initialize view
        this.initComponents();

        // Initialize properties
        this.executor = Executors.newFixedThreadPool(1);
        this.dns = new HashMap<>();

        // Initialize fake DNS service
        try
        {
            this.dns.put("localhost.fr", new Pair<>(InetAddress.getByName("127.0.0.1"), 10000));
            this.dns.put("univ-lyon1.fr", new Pair<>(InetAddress.getByName("134.214.118.237"), 9999));
        }
        catch(UnknownHostException ex)
        {
            Logger.getLogger(SmtpClientView.class.getName()).log(
                Level.SEVERE,
                "An address couldn't be parsed.",
                ex
            );
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        senderLabel = new javax.swing.JLabel();
        senderField = new javax.swing.JTextField();
        recipientsLabel = new javax.swing.JLabel();
        recipientsField = new javax.swing.JTextField();
        subjectLabel = new javax.swing.JLabel();
        subjectField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        bodyField = new javax.swing.JTextPane();
        buttonsPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        sendButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Client SMTP");
        setMinimumSize(new java.awt.Dimension(600, 400));
        setPreferredSize(new java.awt.Dimension(600, 400));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0};
        getContentPane().setLayout(layout);

        senderLabel.setText("Expéditeur");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        getContentPane().add(senderLabel, gridBagConstraints);

        senderField.setText("alexis.rabilloud@univ-lyon1.fr");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        getContentPane().add(senderField, gridBagConstraints);

        recipientsLabel.setText("Destinataire(s)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(recipientsLabel, gridBagConstraints);

        recipientsField.setText("bruno.buiret@localhost.fr");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        getContentPane().add(recipientsField, gridBagConstraints);

        subjectLabel.setText("Objet");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(subjectLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        getContentPane().add(subjectField, gridBagConstraints);

        jScrollPane1.setViewportView(bodyField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

        resetButton.setText("Tout effacer");
        resetButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                resetButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(resetButton);

        sendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/mail-forward.png"))); // NOI18N
        sendButton.setText("Envoyer");
        sendButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(sendButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     *
     * @param evt
     */
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        this.executor.execute(() ->
        {
            this.setFieldsAndButtonsEnabled(false);

            // Fetch fields' value
            String senderValue = this.senderField.getText();
            String recipientsValue = this.recipientsField.getText();
            String[] recipientsList = new String[]{};
            String subjectValue = this.subjectField.getText();
            String bodyValue = this.bodyField.getText();

            // Perform checks and tests
            List<String> errorsList = new ArrayList<>();

            if(null != senderValue)
            {
                senderValue = senderValue.trim();

                if(!senderValue.isEmpty())
                {
                    Matcher matcher = SmtpClientView.PATTERN_EMAIL.matcher(senderValue);

                    if(!matcher.matches())
                    {
                        errorsList.add("L'adressse email de l'expéditeur est invalide.");
                    }
                }
                else
                {
                    errorsList.add("Vous devez spécifier l'adresse email de l'expéditeur.");
                }
            }
            else
            {
                errorsList.add("Vous devez spécifier l'adresse email de l'expéditeur.");
            }

            if(null != recipientsValue)
            {
                recipientsValue = recipientsValue.trim();

                if(!recipientsValue.isEmpty())
                {
                    Matcher matcher;
                    String domain;
                    recipientsList = recipientsValue.split(",\\s*");

                    for(int i = 0; i < recipientsList.length; i++)
                    {
                        recipientsList[i] = recipientsList[i].trim();

                        if(!recipientsList[i].isEmpty())
                        {
                            matcher = SmtpClientView.PATTERN_EMAIL.matcher(recipientsList[i]);

                            if(matcher.matches())
                            {
                                domain = recipientsList[i].substring(recipientsList[i].indexOf("@") + 1);

                                // @todo InetAddress.getByName(domain)?
                                if(!this.dns.containsKey(domain))
                                {
                                    errorsList.add(String.format("Le domaine \"%s\" n'est pas connu du DNS.", domain));
                                }
                            }
                            else
                            {
                                errorsList.add(String.format(
                                    "L'adresse email \"%s\" est invalide.",
                                    recipientsList[i]
                                ));
                            }
                        }
                        else
                        {
                            recipientsList[i] = null;
                        }
                    }
                }
                else
                {
                    errorsList.add("Vous devez spécifier au-moins un destinataire.");
                }
            }
            else
            {
                errorsList.add("Vous devez spécifier au-moins un destinataire.");
            }

            if(null != subjectValue)
            {
                subjectValue = subjectValue.trim();
            }

            if(null != bodyValue)
            {
                bodyValue = bodyValue.trim();
            }

            if((null == subjectValue || subjectValue.isEmpty()) && (null == bodyValue || bodyValue.isEmpty()))
            {
                errorsList.add("Aucun contenu à envoyer.");
            }

            // There are no errors as of yet, try sending the mail
            if(errorsList.isEmpty())
            {
                // Initialize some more vars
                List<String> successList = new ArrayList<>();
                
                // Build a map connecting each recipient to their SMTP server's domain
                Map<String, Set<String>> domains = new HashMap<>();
                String domain;

                for(String recipient : recipientsList)
                {
                    if(null != recipient)
                    {
                        // Extract the domain from the mail address
                        domain = recipient.substring(recipient.indexOf("@") + 1);

                        // Initialize the domain's list if it hasn't been already
                        if(!domains.containsKey(domain))
                        {
                            domains.put(domain, new HashSet<>());
                        }

                        domains.get(domain).add(recipient);
                    }
                }

                // Build body
                StringBuilder bodyBuilder = new StringBuilder();

                if(null != subjectValue && !subjectValue.isEmpty())
                {
                    bodyBuilder.append("Subject: ");
                    bodyBuilder.append(subjectValue);
                    bodyBuilder.append(SmtpProtocol.END_OF_LINE);
                    bodyBuilder.append(SmtpProtocol.END_OF_LINE);
                }

                if(null != bodyValue)
                {
                    bodyBuilder.append(bodyValue);
                }

                bodyBuilder.append(SmtpProtocol.END_OF_DATA);
                bodyValue = bodyBuilder.toString();

                // Connect to each needed SMTP server
                SmtpClient client;
                Pair<InetAddress, Integer> serverData;
                String senderDomain = senderValue.substring(senderValue.indexOf("@") + 1);
                List<String> serverErrors;

                for(Map.Entry<String, Set<String>> entry : domains.entrySet())
                {
                    // Fetch the server's connection data and establish connection
                    serverData = this.dns.get(entry.getKey());

                    try
                    {
                        client = new SmtpClient(serverData.getKey(), serverData.getValue());

                        // First, send greetings
                        if(1 == client.ehlo(senderDomain))
                        {
                            // Then, initiate transaction
                            if(1 == client.mailFrom(senderValue))
                            {
                                // Add every recipient
                                for(String recipient : entry.getValue())
                                {
                                    if(1 == client.rcptTo(recipient))
                                    {
                                        successList.add(recipient);
                                    }
                                    else
                                    {
                                        errorsList.add(String.format(
                                            "Impossible d'ajouter \"%s\" à la liste des destinataires.",
                                            recipient
                                        ));
                                    }
                                }

                                // Try sending the body
                                if(2 == client.data())
                                {
                                    if(1 != client.sendMailBody(bodyValue))
                                    {
                                        successList.removeAll(entry.getValue());
                                        errorsList.add(String.format(
                                            "Impossible de terminer la transaction avec le serveur SMTP \"%s\".",
                                            entry.getKey()
                                        ));
                                    }
                                }
                                else
                                {
                                    errorsList.add(String.format(
                                        "Impossible d'initier la lecture du corps de l'email avec le serveur SMTP \"%s\".",
                                        entry.getKey()
                                    ));
                                }
                            }
                            else
                            {
                                errorsList.add(String.format(
                                    "Impossible d'initier une transaction avec le serveur SMTP \"%s\".",
                                    entry.getKey()
                                ));
                            }
                        }
                        else
                        {
                            errorsList.add(String.format(
                                "Impossible de saluer le serveur SMTP \"%s\".",
                                entry.getKey()
                            ));
                        }

                        if(3 != client.quit())
                        {
                            errorsList.add(String.format(
                                "Impossible de clore la connexion au serveur SMTP \"%s\".",
                                entry.getKey()
                            ));
                        }
                    }
                    catch(SmtpClientInitializationException ex)
                    {
                        errorsList.add(String.format(
                            "Impossible d'établir la connexion au serveur SMTP \"%s\".",
                            entry.getKey()
                        ));
                    }
                }

                if(errorsList.isEmpty())
                {
                    JOptionPane.showMessageDialog(
                        this,
                        "Votre email a bien été envoyé.",
                        "Envoi réussi",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    this.clearFields();
                }
                else if(!errorsList.isEmpty() && !successList.isEmpty())
                {
                    // Build message
                    StringBuilder messageBuilder = new StringBuilder();
                    messageBuilder.append("Votre email a bien été envoyé à :\n");
                    
                    for(String recipient : successList)
                    {
                        messageBuilder.append("- ");
                        messageBuilder.append(recipient);
                        messageBuilder.append("\n");
                    }
                    
                    messageBuilder.append("Mais :\n");
                    
                    for(String error : errorsList)
                    {
                        messageBuilder.append("- ");
                        messageBuilder.append(error);
                        messageBuilder.append("\n");
                    }
                    
                    // Then, display warning dialog
                    JOptionPane.showMessageDialog(
                        this,
                        messageBuilder.toString(),
                        "Envoi partiellement réussi",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
                else
                {
                    this.showErrorDialog(errorsList);
                }
            }
            else
            {
                this.showErrorDialog(errorsList);
            }

            this.setFieldsAndButtonsEnabled(true);
        });
    }//GEN-LAST:event_sendButtonActionPerformed

    /**
     *
     * @param evt
     */
    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        this.executor.execute(() ->
        {
            this.clearFields();
        });
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     *
     */
    private void clearFields()
    {
        this.senderField.setText(null);
        this.recipientsField.setText(null);
        this.subjectField.setText(null);
        this.bodyField.setText(null);
    }

    /**
     *
     * @param enabled
     */
    private void setFieldsAndButtonsEnabled(boolean enabled)
    {
        this.senderField.setEnabled(enabled);
        this.recipientsField.setEnabled(enabled);
        this.subjectField.setEnabled(enabled);
        this.bodyField.setEnabled(enabled);
        this.resetButton.setEnabled(enabled);
        this.sendButton.setEnabled(enabled);
    }

    /**
     *
     * @param errorsList
     */
    private void showErrorDialog(List<String> errorsList)
    {
        StringBuilder errorsListBuilder = new StringBuilder();

        for(String error : errorsList)
        {
            errorsListBuilder.append("- ");
            errorsListBuilder.append(error);
            errorsListBuilder.append("\n");
        }

        JOptionPane.showMessageDialog(
            this,
            errorsList.size() > 1 ? errorsListBuilder.toString() : errorsListBuilder.toString().substring(2),
            errorsList.size() > 1 ? "Plusieurs erreurs ont eu lieu" : "Une erreur a eu lieu",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane bodyField;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField recipientsField;
    private javax.swing.JLabel recipientsLabel;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField senderField;
    private javax.swing.JLabel senderLabel;
    private javax.swing.JTextField subjectField;
    private javax.swing.JLabel subjectLabel;
    // End of variables declaration//GEN-END:variables
    private final ExecutorService executor;
    private final Map<String, Pair<InetAddress, Integer>> dns;
}
