package io.github.utk003.spigot_scripting.util.exception;

/**
 * Thrown whenever the script linking process encounters an issue of any kind.
 * <p>
 * {@code LinkingException} is a subclass of {@link RuntimeException}.
 *
 * @author Utkarsh Priyam
 * @version 12/17/20
 */
public class LinkingException extends RuntimeException {
    /**
     * Constructs a new {@code LinkingException} with {@code null} as its
     * detail message. The cause is not initialized.
     */
    public LinkingException() {
        super();
    }

    /**
     * Constructs a new {@code LinkingException} with the specified detail message.
     * The cause is not initialized.
     *
     * @param message The detail message. The detail message is saved for
     *                later retrieval by the getMessage() method.
     */
    public LinkingException(String message) {
        super(message);
    }
}
