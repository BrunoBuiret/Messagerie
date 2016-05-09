package smtp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import smtp.SmtpProtocol;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpClient {

    /**
     *
     */
    protected enum SmtpState {
        Initialisation,
        Connected,
        MailTransaction,
        WaitForData,
        DataTransaction,
        EndDataTransaction,
        WaitForExitConfirm;
    };

    /**
     *
     */
    protected SSLSocket socket;

    /**
     *
     */
    protected BufferedOutputStream socketWriter;

    /**
     *
     */
    protected BufferedInputStream socketReader;

    /**
     *
     */
    protected SmtpState currentState = SmtpState.Initialisation;

    /**
     * Creates a new SMTP client.
     *
     * @param host
     * @param port
     */
    public SmtpClient(InetAddress host, int port, String domain) {
        try {
            // Initialize vars
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            // Create socket
            this.socket = (SSLSocket) factory.createSocket(host, port);

            // Determine which cipher suites can be used
            this.socket.setEnabledCipherSuites(this.socket.getSupportedCipherSuites());

            // Start handshake
            this.socket.startHandshake();

            // Get streams
            this.socketWriter = new BufferedOutputStream(this.socket.getOutputStream());
            this.socketReader = new BufferedInputStream(this.socket.getInputStream());

        } catch (IOException ex) {
            // @todo Throw exception to avoid methods being executed.
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(this.readResponse());
    }

    /**
     *
     * @return
     */
    protected String readResponse() {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());
        int readByte;

        try {
            // Try reading everything
            do {
                readByte = this.socketReader.read();

                if (-1 != readByte) {
                    dataWriter.writeByte(readByte);
                }
            } while (this.socketReader.available() > 0 && -1 != readByte);

            // Get the byte array
            byte[] byteArray = dataStream.toByteArray();

            return byteArray.length > 0 ? new String(byteArray).trim() : null;
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(
                    Level.SEVERE,
                    "Couldn't read response from server.",
                    ex
            );
        }

        return null;
    }

    /**
     *
     * @param request
     * @throws java.io.IOException
     */
    protected void sendRequest(String request)
            throws IOException {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());

        try {
            // Transform the response into a byte array
            dataWriter.writeBytes(request);

            // Then, send the response to the client
            this.socketWriter.write(dataStream.toByteArray());
            this.socketWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(
                    Level.SEVERE,
                    "Couldn't send request to the server.",
                    ex
            );

            throw ex;
        }
    }

    /**
     *
     * @param futureState
     * @param serverResponse
     * @return
     */
    protected int stateValidation(SmtpState futureState, String serverResponse) {
        System.out.println(serverResponse);
                
        if (serverResponse.startsWith("250")) {
            this.currentState = futureState;
            return 1;
        }

        if (serverResponse.startsWith("354")) {
            this.currentState = futureState;
            return 2;
        }
        
        if (serverResponse.startsWith("221")) {
            this.currentState = futureState;
            return 3;
        }
        
        System.out.println("Server error");
        return 0;
    }

    /**
     * Sends an extended hello greetings.
     *
     * @param domain
     * @return
     */
    public int ehlo(String domain) {
        try {
            this.sendRequest("EHLO " + domain + SmtpProtocol.END_OF_LINE);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.MailTransaction, this.readResponse());
    }

    /**
     * Starts a transaction.
     *
     * @param mailAddress
     * @return
     * @todo Check greetings have already been sent.
     */
    public int mailFrom(String mailAddress) {
        try {
            this.sendRequest("MAIL FROM:<" + mailAddress + ">" + SmtpProtocol.END_OF_LINE);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.WaitForData, this.readResponse());
    }

    /**
     * Adds a recipient to the current transaction.
     *
     * @param recipient
     * @return
     * @todo Check there is a transaction.
     */
    public int rcptTo(String recipient) {
        try {
            this.sendRequest("RCPT TO:<" + recipient + ">" + SmtpProtocol.END_OF_LINE);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.DataTransaction, this.readResponse());
    }

    /**
     *
     * @return
     */
    public int data() {
        try {
            this.sendRequest("DATA" + SmtpProtocol.END_OF_LINE);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.EndDataTransaction, this.readResponse());
    }

    /**
     * Sends the mail's body.
     *
     * @param body
     * @return
     * @todo Check body ends with "CRLF.CRLF"
     */
    public int sendMailBody(String body) {
        try {
            this.sendRequest(body);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.WaitForExitConfirm, this.readResponse());
    }

    /**
     * Closes the connection.
     *
     * @return
     */
    public int quit() {
        try {
            this.sendRequest("QUIT" + SmtpProtocol.END_OF_LINE);
        } catch (IOException ex) {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.Initialisation, this.readResponse());
    }
}
