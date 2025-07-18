package greencity.exception.exceptions;

/**
 * Exception thrown when a user tries to access or modify a resource
 * that they do not own.
 * @author Mykyta Sirobaba
 */
public class UserNotOwnerException extends RuntimeException {
    /**
     * Constructor for UserNotOwnerException.
     *
     * @param message - giving message.
     */
    public UserNotOwnerException(String message) {
        super(message);
    }
}
