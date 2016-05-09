package common.mails;

import common.mails.exceptions.AlreadyMarkedForDeletionException;
import common.mails.exceptions.FailedMailBoxUpdateException;
import common.mails.exceptions.MarkedForDeletionException;
import common.mails.exceptions.NonExistentMailException;
import common.mails.exceptions.UnknownMailBoxException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class MailBox
{
    /**
     * The associated file's path.
     */
    protected File path;

    /**
     *
     */
    protected List<Mail> mailsList;

    /**
     *
     */
    protected List<Mail> mailsToDeleteList;

    /**
     * Creates a new mailbox associated with a given file.
     *
     * @param path The associated file's path.
     */
    public MailBox(File path)
    {
        // Initialize parameters
        this.path = path;
        this.mailsList = new ArrayList<>();
        this.mailsToDeleteList = new ArrayList<>();
    }

    /**
     * Gets the mailbox's path.
     *
     * @return The mailbox's path.
     */
    public File getPath()
    {
        return this.path;
    }

    /**
     * Gets this mailbox' username.
     *
     * @return The mailbox' username.
     */
    public String getUserName()
    {
        String name = this.path.getName();

        return name.substring(0, name.indexOf(".mbox"));
    }

    /**
     * Saves the contents of the mailbox in its associated file, overwriting
     * everything.
     *
     * @throws common.mails.exceptions.FailedMailBoxUpdateException If the
     * mailbox couldn't be saved.
     * @throws java.io.FileNotFoundException If the mailbox doesn't exist.
     * @throws java.lang.IllegalArgumentException If the mailbox isn't a file or
     * can't be written.
     */
    public void save()
    throws FailedMailBoxUpdateException, FileNotFoundException, IllegalArgumentException
    {
        // If the mailbox exists, is it a file?
        if(this.path.exists() && !this.path.isFile())
        {
            throw new IllegalArgumentException(String.format(
                "Mailbox \"%s\" isn't a file.",
                this.path.getAbsolutePath()
            ));
        }
        // Can it be written?
        else if(this.path.exists() && this.path.isFile() && !this.path.canWrite())
        {
            throw new IllegalArgumentException(String.format(
                "Mailbox \"%s\" can't be written.",
                this.path.getAbsolutePath()
            ));
        }

        // Remove the emails marked for deletion but clone it first in case of a bug
        List<Mail> clonedMailsList = new ArrayList<>(this.mailsList);
        this.mailsList.removeAll(this.mailsToDeleteList);

        // Initialize vars
        BufferedOutputStream mailBoxStream = null;
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());
        Map<String, String> headers;

        try
        {
            // Try opening the mailbox
            mailBoxStream = new BufferedOutputStream(new FileOutputStream(this.path));

            for(Mail mail : this.mailsList)
            {
                // Write headers
                headers = mail.getHeaders();
                
                if(!headers.isEmpty())
                {
                    for(Map.Entry<String, String> entry : headers.entrySet())
                    {
                        dataWriter.writeBytes(entry.getKey());
                        dataWriter.writeBytes(": ");
                        dataWriter.writeBytes(entry.getValue());
                        dataWriter.writeBytes("\r\n");
                    }

                    // Write separator
                    dataWriter.writeBytes("\r\n");
                }

                // Write body, first split it every 76 characters
                /*
                List<String> bodyFragments = new ArrayList<>();
                int currentIndex = 0, bodyLength = mail.getBody().length();

                while(currentIndex < bodyLength)
                {
                    if(bodyLength - currentIndex >= 76)
                    {
                        bodyFragments.add(
                            mail.getBody().substring(
                                currentIndex, currentIndex + 76
                            )
                        );

                        currentIndex += 76;
                    }
                    else
                    {
                        bodyFragments.add(
                            mail.getBody().substring(
                                currentIndex
                            )
                        );
                        currentIndex = mail.getBody().length();
                    }
                }

                dataWriter.writeBytes(
                    String.join(
                        "\r\n",
                        bodyFragments
                    )
                );

                // End the body
                dataWriter.writeBytes("\r\n.\r\n");
                */
                dataWriter.writeBytes(mail.getBody());
            }

            // Write the emails into the file
            mailBoxStream.write(dataStream.toByteArray());
        }
        catch(FileNotFoundException ex)
        {
            // This error shouldn't happen because if the file doesn't exist, it'll be created
            throw ex;
        }
        catch(IOException ex)
        {
            // Reset the mails list
            this.mailsList = clonedMailsList;

            // Throw another exception
            FailedMailBoxUpdateException exception = new FailedMailBoxUpdateException(
                String.format(
                    "Mailbox \"%s\" couldn't be saved.",
                    this.path.getAbsolutePath()
                ),
                ex
            );

            throw exception;
        }
        finally
        {
            if(mailBoxStream != null)
            {
                try
                {
                    mailBoxStream.close();
                }
                catch(IOException ex)
                {
                    // Reset the mails list
                    this.mailsList = clonedMailsList;

                    Logger.getLogger(MailBox.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Loads the content of the mailbox from its associated file using UTF-8 by
     * default.
     *
     * @throws common.mails.exceptions.UnknownMailBoxException If the mailbox
     * doesn't exist.
     * @throws java.io.FileNotFoundException If the mailbox doesn't exist.
     * @throws java.lang.IllegalArgumentException If the mailbox isn't a file or
     * can't be read.
     */
    public void load()
    throws UnknownMailBoxException, FileNotFoundException, IOException
    {
        this.load(StandardCharsets.UTF_8);
    }

    /**
     * Loads the content of the mailbox from its associated file.
     *
     * @param charset The charset to build the strings with.
     * @throws common.mails.exceptions.UnknownMailBoxException If the mailbox
     * doesn't exist.
     * @throws java.io.FileNotFoundException If the mailbox doesn't exist.
     * @throws java.lang.IllegalArgumentException If the mailbox isn't a file or
     * can't be read.
     */
    public void load(Charset charset)
    throws UnknownMailBoxException, FileNotFoundException, IOException
    {
        // Does the mailbox exist?
        if(!this.path.exists())
        {
            throw new UnknownMailBoxException(String.format(
                "Mailbox \"%s\" doesn't exist.",
                this.path.getAbsolutePath()
            ));
        }
        // Is it an actual file?
        else if(!this.path.isFile())
        {
            throw new IllegalArgumentException(String.format(
                "Mailbox \"%s\" isn't an actual file.",
                this.path.getAbsolutePath()
            ));
        }
        // Can it be read?
        else if(!this.path.canRead())
        {
            throw new IllegalArgumentException(String.format(
                "Mailbox \"%s\" can't be read.",
                this.path.getAbsolutePath()
            ));
        }

        // Initialize vars
        BufferedInputStream mailBoxStream = null;
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());
        int currentCharacter = -1, previousCharacter = -1;
        Mail mail;
        boolean endOfHeaders, endOfMail;
        final int ASCII_LF = (int) '\n';
        final int ASCII_CR = (int) '\r';
        final int ASCII_DOT = (int) '.';
        boolean fileEmpty = false;

        try
        {
            // Try opening the mailbox
            mailBoxStream = new BufferedInputStream(new FileInputStream(this.path));
            
            // Is the file empty?
            if(mailBoxStream.markSupported())
            {
                mailBoxStream.mark(10);
                
                if(-1 == mailBoxStream.read())
                {
                    fileEmpty = true;
                }
                else
                {
                    mailBoxStream.reset();
                }
            }

            // Read only if the file isn't empty
            if(!fileEmpty)
            {
                while(mailBoxStream.available() > 0)
                {
                    // Create a new mail
                    mail = new Mail();
                    endOfHeaders = endOfMail = false;

                    // Read the headers
                    do
                    {
                        // Read the next character
                        previousCharacter = currentCharacter;
                        currentCharacter = mailBoxStream.read();

                        // Have we reached the end of a line?
                        if(currentCharacter == ASCII_LF && previousCharacter == ASCII_CR)
                        {
                            // Write the header
                            mail.addHeader(new String(dataStream.toByteArray(), charset).trim());

                            // And clear the output stream to start a new header
                            dataStream.reset();
                        }
                        // Ignore this character, we are going to reach the end of a line
                        else if(currentCharacter == ASCII_CR && previousCharacter != ASCII_LF)
                        {
                        }
                        // Have we reached the headers limit?
                        else if(currentCharacter == ASCII_CR && previousCharacter == ASCII_LF)
                        {
                            // Get rid of the next character, it must be an LF
                            mailBoxStream.read();

                            // Stop this loop
                            endOfHeaders = true;
                        }
                        // Otherwise, simply add the current character to the output stream
                        else
                        {
                            dataWriter.writeByte(currentCharacter);
                        }
                    }
                    while(!endOfHeaders);

                    // Read the contents
                    do
                    {
                        // Read the next character
                        previousCharacter = currentCharacter;
                        currentCharacter = mailBoxStream.read();

                        // Have we reached the end of the mail?
                        if(currentCharacter == ASCII_DOT && previousCharacter == ASCII_LF)
                        {
                            // Write the contents
                            mail.setBody(new String(dataStream.toByteArray(), charset).trim());

                            // Get rid of the next two characters
                            mailBoxStream.read();
                            mailBoxStream.read();

                            // And clear the output stream to start a new mail
                            dataStream.reset();

                            // Then, stop this loop
                            endOfMail = true;
                        }
                        // Otherwise, simply add the current character to the output stream
                        else
                        {
                            dataWriter.writeByte(currentCharacter);
                        }
                    }
                    while(!endOfMail);

                    // Save the email
                    this.mailsList.add(mail);
                }
            }
        }
        catch(FileNotFoundException ex)
        {
            // This error shouldn't happen because the mailbox's existence is tested
            throw ex;
        }
        catch(IOException ex)
        {
            Logger.getLogger(MailBox.class.getName()).log(
                Level.SEVERE,
                String.format(
                    "Couldn't read mailbox \"%s\" correctly.",
                    this.path.getAbsolutePath()
                ),
                ex
            );

            throw ex;
        }
        finally
        {
            if(mailBoxStream != null)
            {
                try
                {
                    mailBoxStream.close();
                }
                catch(IOException ex)
                {
                    Logger.getLogger(MailBox.class.getName()).log(
                        Level.SEVERE,
                        String.format(
                            "Couldn't close mailbox \"%s\" correctly.",
                            this.path.getAbsolutePath()
                        ),
                        ex
                    );
                }
            }
        }
    }

    /**
     * Gets the number of mails, including those marked for deletion, in this mailbox.
     *
     * @return The number of mails.
     */
    public int getSize()
    {
        return this.mailsList.size();
    }

    /**
     * Adds a mail to the mailbox, which will need to be saved later.
     *
     * @param mail The mail to add.
     */
    public void add(Mail mail)
    {
        this.mailsList.add(mail);
    }

    /**
     * Get a mail by its index.
     *
     * @param index The mail's index.
     * @return The mail if it exists.
     * @throws common.mails.exceptions.MarkedForDeletionException If the mail has been
     * marked for deletion.
     * @throws common.mails.exceptions.NonExistentMailException If the mail doesn't
     * exist.
     */
    public Mail get(int index)
    {
        if(index < this.mailsList.size())
        {
            Mail mail = this.mailsList.get(index);

            if(!this.mailsToDeleteList.contains(mail))
            {
                return mail;
            }
            else
            {
                throw new MarkedForDeletionException(String.format(
                    "Mail #%d is marked for deletion.",
                    index
                ));
            }
        }
        else
        {
            throw new NonExistentMailException(String.format(
                "Mail #%d doesn't exist.",
                index
            ));
        }
    }

    /**
     * Gets the list of every mail, including those marked for deletion.
     *
     * @return The list of mails.
     */
    public List<Mail> getAll()
    {
        return this.mailsList;
    }

    /**
     * Marks a mail for deletion if it hasn't been already.
     *
     * @param index The mail's index.
     */
    public void delete(int index)
    {
        if(index < this.mailsList.size())
        {
            this.delete(this.mailsList.get(index));
        }
        else
        {
            throw new NonExistentMailException(String.format(
                "Mail #%d doesn't exist.",
                index
            ));
        }
    }

    /**
     * Marks a mail for deletion if it hasn't been already.
     *
     * @param mail The mail to mark.
     */
    public void delete(Mail mail)
    {
        if(!this.mailsToDeleteList.contains(mail))
        {
            this.mailsToDeleteList.add(mail);
        }
        else
        {
            throw new AlreadyMarkedForDeletionException(String.format(
                "Mail #%d is already marked for deletion.",
                this.mailsList.indexOf(mail)
            ));
        }
    }

    /**
     * Tests if a mail is marked for deletion.
     *
     * @param index The mail's index.
     * @return <code>true</code> if it supposed to be deleted,
     * <code>false</code> otherwise.
     */
    public boolean isDeleted(int index)
    {
        if(index < this.mailsList.size())
        {
            return this.isDeleted(this.mailsList.get(index));
        }
        else
        {
            throw new NonExistentMailException(String.format(
                "Mail #%d doesn't exist.",
                index
            ));
        }
    }

    /**
     * Tests if a mail is marked for deletion.
     *
     * @param mail The mail to test.
     * @return <code>true</code> if it supposed to be deleted,
     * <code>false</code> otherwise.
     */
    public boolean isDeleted(Mail mail)
    {
        return this.mailsToDeleteList.contains(mail);
    }

    /**
     * Resets the mailbox by unmarking the mails marked for deletion.
     */
    public void reset()
    {
        this.mailsToDeleteList.clear();
    }
}
