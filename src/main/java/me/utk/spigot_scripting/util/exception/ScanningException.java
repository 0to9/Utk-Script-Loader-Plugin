package me.utk.spigot_scripting.util.exception;

/**
 * Thrown whenever the scanning process encounters an issue of any kind.
 * <p>
 * {@code ScanningException} is a subclass of {@link RuntimeException}.
 *
 * @author Utkarsh Priyam
 * @version 1/21/20
 */
public class ScanningException extends RuntimeException {
    /**
     * Constructs a new {@code ScanningException} with {@code null} as its
     * detail message. The cause is not initialized.
     */
    public ScanningException() {
        super();
    }

    /**
     * Constructs a new {@code ScanningException} with the specified detail message.
     * The cause is not initialized.
     *
     * @param message The detail message. The detail message is saved for
     *                later retrieval by the getMessage() method.
     */
    public ScanningException(String message) {
        super(message);
    }
}
