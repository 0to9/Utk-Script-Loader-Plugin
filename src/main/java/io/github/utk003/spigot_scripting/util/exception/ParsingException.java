package io.github.utk003.spigot_scripting.util.exception;

/**
 * Thrown whenever the parsing process encounters an issue of any kind.
 * <p>
 * {@code ParsingException} is a subclass of {@link RuntimeException}.
 *
 * @author Utkarsh Priyam
 * @version 4/7/20
 */
public class ParsingException extends RuntimeException {
    /**
     * Constructs a new {@code ParsingException} with {@code null} as its
     * detail message. The cause is not initialized.
     */
    public ParsingException() {
        super();
    }

    /**
     * Constructs a new {@code ParsingException} with the specified detail message.
     * The cause is not initialized.
     *
     * @param message The detail message. The detail message is saved for
     *                later retrieval by the getMessage() method.
     */
    public ParsingException(String message) {
        super(message);
    }
}