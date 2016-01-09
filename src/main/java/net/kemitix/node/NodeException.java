package net.kemitix.node;

/**
 * Represents an error within the tree node.
 *
 * @author pcampbell
 */
@SuppressWarnings("serial")
public class NodeException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param message the message
     */
    public NodeException(final String message) {
        super(message);
    }

}
