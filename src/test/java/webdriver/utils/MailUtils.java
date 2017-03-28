package webdriver.utils;

import com.google.common.base.Function;
import webdriver.BaseEntity;
import webdriver.Logger;
import webdriver.PropertiesResourceManager;
import webdriver.waitings.SmartWait;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

/** 
 * Work with mail.
 */
public class MailUtils extends BaseEntity {

	private String host, username, password;
	private Properties properties = new Properties();
	private MAIL_PROTOCOLS protocol;
	private static final long TIME_TO_WAIT_MAIL = 120000;
	private Store store;

	/** construct mail connector
	 * @param host host
	 * @param username username
	 * @param password password
	 * @throws IOException
	 */
	public MailUtils(String host, String username, String password) {
		this.host = host;
	    this.username = username;
	    this.password = password;
	    readConfig(host);
	    store = connect();
	}

	/**
	 * account in format ivashko@mail.ru
	 * @param account
	 * @param password
	 * @throws IOException
	 */
	public MailUtils(String account,String password){
		this(account.split("@")[1], account, password);
	}


	/** Construct mail connector.
	 * @param host host
	 * @param username username
	 * @param password password
	 * @throws IOException
	 */
	public MailUtils(String host, String username, String password, String fileName){
		this.host = host;
	    this.username = username;
	    this.password = password;
	    readConfig(host,fileName);
	    store = connect();
	}

	/** available protocols
	 */
	public enum MAIL_PROTOCOLS{
		POP3("pop3"), SMTP("smtp"), IMAP("imap"), IMAPS("imaps"), POP3S("pop3s");

		private String protocol;

		/** constructor
		 * @param name mail protocol name
		 */
		MAIL_PROTOCOLS(String name){
			protocol = name;
		}

		@Override
		public String toString() {
			return protocol;
		}

	}

	/**
	 * Return connected store
	 * @return Store
	 */
	public Store getStoreConnected() {
		if (store.isConnected()) {
			return store;
		}
		store = connect();
		return store;
	}

	/**
	 * @param folder folder
     * @param permission permissions for access to folder(user Folder.READ_ONLY and e.i.)
     * @return messages
	 */
	public Message[] getMessages(Folder folder,int permission){
		// Get folder
		Message[] messages = null;
    	try {
    		folder.open(permission);
			// Get directory
			messages = folder.getMessages();
		} catch (MessagingException e) {
			formatLogMsg("Impossible to get messages: " + e.getMessage());
			logger.info(this, e);
		}
	    return messages;
	}

	/**
	 * @param folder folder
	 * @return messages
	 */
	public Message[] getMessages(Folder folder){
		return getMessages(folder,Folder.READ_WRITE);
	}
	
	
	/**
	 * @param folderName name of folder in mailbox
	 * @return messages
	 */
	public Message[] getMessages(String folderName){
		try {
			return getMessages(getStoreConnected().getFolder(folderName),Folder.READ_WRITE);
		} catch (MessagingException e) {
			logger.info(this, e);
		}
		return new Message[0];
	}
	
	/** Get link from the letter
	 * @param subject subject
	 * @return link
	 */
	public String getMessageContent(String folderName, String subject){
		try {
			Multipart part = (Multipart) waitForLetter(folderName,subject).getContent();
			return (String) part.getBodyPart(0).getContent();
		} catch (IOException | MessagingException e) {
            logger.debug(this, e);
			formatLogMsg("It is impossible to get content of message: " + e.getMessage());
		} catch (NullPointerException e) {
            logger.debug(this, e);
			return "There were no mails for account activation or reset password in 2 minutes!";
		}
		return null;
	}

	/** Get link from the letter
	 * @param subject subject
	 * @param text text
	 * @return link
	 */
	public String getMessageContent(String folderName, String subject, String text){
		try {
			Multipart part = (Multipart) waitForLetter(folderName,subject, text).getContent();
			return (String) part.getBodyPart(0).getContent();
		} catch (IOException | MessagingException e) {
            logger.debug(this, e);
			formatLogMsg("It is impossible to get content of message: " + e.getMessage());
		}
        return null;
	}

	/** wait for letter with necessary subject is present in mailbox
	 * @param subject subject of letter
	 * @return message
	 * @throws MessagingException MessagingException
	 */
	public Message waitForLetter(final String folderName, final String subject) throws MessagingException{
        return waitForLetter(folderName, subject, "");
	}

	/** wait for letter with necessary subject and address is present in mailbox
	 * @param subject subject of letter
	 * @param text text that message contains
	 * @return message
	 * @throws MessagingException MessagingException
	 */
	public Message waitForLetter(final String folderName, final String subject, final String text) throws MessagingException{
        WaitForLetterCondition condition = new WaitForLetterCondition(folderName, subject, text);
        try {
            return SmartWait.waitFor(condition, this);
        } catch (Exception e) {
            logger.debug(this, e);
            formatLogMsg(String.format("Mailbox not contains letter with subject '%1$s'. There was waiting: %2$s mills", subject, TIME_TO_WAIT_MAIL));
            return null;
        }
	}

	/** by default folder "INBOX" and permissions Folder.READ_ONLY
	 * @return messages
	 */
	public Message[] getMessages(){
		return getMessages("INBOX");
	}

	/** connect to mailbox
	 * @return Store
	 */
	private Store connect(){
		for(int i = 0; i <= 10; i++){
			// Get session
			properties.setProperty("mail.store.protocol", protocol.toString());
			Session session = Session.getDefaultInstance(properties, null);
		    // Get the store
		    try {
		    	store = session.getStore(protocol.toString());
		    	store.connect(host, username, password);
		    	break;
		    } catch (NoSuchProviderException e) {
                logger.debug(this, e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
                    logger.debug(this, e1);
				}
		    	e.printStackTrace();
			} catch (MessagingException e) {
                logger.debug(this, e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
                    logger.debug(this, e1);
				}
			}
		}
		return store;
	}
	
	/** Remove all the message from box
	 * @param folderName name of folder(for example "INBOX")
	 */
	public String deleteAllMessages(String folderName){
		//check if connection is established
		try{
			Folder inbox = getStoreConnected().getFolder(folderName);
			inbox.open(Folder.READ_WRITE);
			//geting all the messages from a folder
			Message[] messages = inbox.getMessages();
			for(Message message:messages) {
				message.setFlag(Flags.Flag.DELETED, true);
			}
			inbox.close(true);
			return "All messages were deleted successfull from " + username;
		}catch(MessagingException e){
            logger.debug(this, e);
			return "Messaging exception: " + e.getMessage();
		}
	}

	/**
	 * Is Client connected
	 * @return
	 */
	public Boolean isConnected(){
		return store.isConnected();
	}
	
	/** close store
	 */
	public void closeStore(){
		try {
			store.close();
		} catch (Exception e) {
            logger.debug(this, e);
			formatLogMsg(e.getMessage());
		}
	}
	
	/**
	 * Read config
	 * @param host
	 */
	private void readConfig(String host,String fileName){
		PropertiesResourceManager pm = new PropertiesResourceManager(fileName);
		String prop = pm.getProperty(host);
		this.host = prop.split(";")[0];
		this.protocol = MAIL_PROTOCOLS.valueOf(prop.split(";")[1].toUpperCase());
	}

	/**
	 * Read config
	 * @param host
	 */
	public void readConfig(String host){
		readConfig(host,"mail.properties");
	}
	
	@Override
	protected String formatLogMsg(String message) {
		return String.format("%1$s '%2$s' %3$s %4$s", "Mail Utils", this.host, Logger.LOG_DELIMITER, message);
	}

	/**
	 * Delete message that match the sunject and body
	 * @param folderName
	 * @param subject
	 * @param body
	 */
	public void deleteMessageThatContainsInfo(String folderName, String subject, String body) {
		try {
			waitForLetter(folderName,subject,body).setFlag(Flags.Flag.DELETED, true);
		} catch (MessagingException e) {
            logger.debug(this, e);
			formatLogMsg(e.getMessage());
		}
	}
	
	/**
	 * Delete message that match the sunject and body
	 * @param folderName
	 * @param subject
	 */
	public void deleteMessageThatContainsInfo(String folderName, String subject) {
		try {
			waitForLetter(folderName,subject).setFlag(Flags.Flag.DELETED, true);
		} catch (MessagingException e) {
            logger.debug(this, e);
			formatLogMsg(e.getMessage());
		}
	}

    @Override
    public String toString() {
        return "MailUtils{" +
                "host='" + host + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", properties=" + properties +
                ", protocol=" + protocol +
                ", store=" + store +
                '}';
    }

    private boolean isAppropriateMessage(Message message, String subject, String text) {
        try {
            String content = (String) ((Multipart) message.getContent()).getBodyPart(0).getContent();
            String actualSubject = message.getSubject();
            return actualSubject.contains(subject) && !(!text.isEmpty() && !content.contains(text));
        } catch (IOException | MessagingException e) {
            logger.debug(this, e);
            return false;
        }
    }

    private class WaitForLetterCondition implements Function<MailUtils, Message> {

        private final String folderName;
        private final String subject;
        private final String text;

        public WaitForLetterCondition(String folderName, String subject, String text) {
            this.folderName = folderName;
            this.subject = subject;
            this.text = text;
        }

        @Override
        public Message apply(MailUtils mailUtils) {
            Message[] messages;
            Folder folder = null;
            try {
                folder = getStoreConnected().getFolder(folderName);
                messages = getMessages(folder);
                for (Message m : messages) {
                    if (isAppropriateMessage(m, subject, text)) {
                        return m;
                    }
                }
            } catch (MessagingException e) {
                logger.debug(this, e);
                formatLogMsg("It is impossible to get subject of message: " + e.getMessage());
            }
            try {
                folder.close(false);
            } catch (Exception e) {
                logger.debug(this, e);
                formatLogMsg(e.getMessage());
            }
            return null;
        }
    }
}
