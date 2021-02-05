package io.github.utk003.spigot_scripting.util.exception;

/**
 * Thrown whenever the script compiling process encounters an issue of any kind.
 * <p>
 * {@code CompilingException} is a subclass of {@link RuntimeException}.
 *
 * @author Utkarsh Priyam
 * @version 12/17/20
 */
public class CompilingException extends RuntimeException {
    /**
     * Constructs a new {@code CompilingException} with {@code null} as its
     * detail message. The cause is not initialized.
     */
    public CompilingException() {
        super();
    }

    /**
     * Constructs a new {@code CompilingException} with the specified detail message.
     * The cause is not initialized.
     *
     * @param message The detail message. The detail message is saved for
     *                later retrieval by the getMessage() method.
     */
    public CompilingException(String message) {
        super(message);
    }
}