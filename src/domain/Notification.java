package domain;

import java.util.Date;

/**
 * Notification represents a message sent to a library user
 * about an event such as a reservation becoming available
 * or an overdue fine being issued.
 *
 * sendEmail() simulates dispatching the notification.
 * In production this would connect to a mail service
 * such as JavaMail or SendGrid.
 *
 * Used by:
 *   - ReservationDAO.java  when a reservation is fulfilled
 *   - FineDAO.java         when a new fine is issued
 */
public class Notification {

    // --------------------------------------------------------
    // Attributes - mirrors the notifications table columns
    // --------------------------------------------------------

    // Auto-generated primary key from the database
    private Integer notificationId;

    // Body text of the notification message
    private String message;

    // Category e.g. "RESERVATION", "FINE", "OVERDUE"
    private String type;

    // Date and time the notification was dispatched
    private Date sentDate;

    // --------------------------------------------------------
    // Constructor
    // --------------------------------------------------------

    /**
     * Creates a fully populated Notification object.
     * notificationId of 0 signals the DB will assign the ID.
     *
     * @param notificationId - database primary key (0 for new)
     * @param message        - body text of the notification
     * @param type           - category of the notification
     * @param sentDate       - date the notification was sent
     */
    public Notification(Integer notificationId, String message,
                        String type, Date sentDate) {
        this.notificationId = notificationId;
        this.message        = message;
        this.type           = type;
        this.sentDate       = sentDate;
    }

    // --------------------------------------------------------
    // Getters - retrieve private attribute values
    // --------------------------------------------------------

    public Integer getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Date getSentDate() {
        return sentDate;
    }

    // --------------------------------------------------------
    // Setters - update private attribute values
    // --------------------------------------------------------

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    // --------------------------------------------------------
    // sendEmail - dispatches notification to a user
    // --------------------------------------------------------

    /**
     * Simulates sending an email notification to a user.
     * Prints the notification details to the console.
     * In a production system this method would call an
     * external email API to deliver the message.
     *
     * @param user - User to send the notification to
     */
    public void sendEmail(User user) {
        System.out.println("\n--- Sending Notification ---");
        System.out.println("To      : " + user.getEmail());
        System.out.println("Name    : " + user.getName());
        System.out.println("Type    : " + type);
        System.out.println("Message : " + message);
        System.out.println("Sent    : " + sentDate);
        System.out.println("Notification dispatched successfully.");
    }

    // --------------------------------------------------------
    // toString - display notification details in readable format
    // --------------------------------------------------------

    /**
     * Returns formatted notification details.
     *
     * @return formatted string of all notification fields
     */
    @Override
    public String toString() {
        return  "\n=============================="    +
                "\nNotification ID : " + notificationId +
                "\nType            : " + type           +
                "\nMessage         : " + message        +
                "\nSent Date       : " + sentDate       +
                "\n==============================";
    }
}
