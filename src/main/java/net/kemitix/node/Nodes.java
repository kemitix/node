package net.kemitix.node;

/**
 * Utility class for {@link Node} items.
 *
 * @author pcampbell
 */
public final class Nodes {

    private Nodes() {
    }

    /**
     * Creates a new unnamed root node.
     *
     * @param data the data the node will contain
     * @param <T>  the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> unnamedRoot(final T data) {
        return new NodeItem<>(data);
    }

    /**
     * Creates a new named root node.
     *
     * @param data the data the node will contain
     * @param name the name of the node
     * @param <T>  the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> namedRoot(final T data, final String name) {
        return new NodeItem<>(data, name);
    }

    /**
     * Creates a new unnamed child node.
     *
     * @param data   the data the node will contain
     * @param parent the parent of the node
     * @param <T>    the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> unnamedChild(final T data, final Node<T> parent) {
        return new NodeItem<>(data, parent);
    }

    /**
     * Creates a new named child node.
     *
     * @param data   the data the node will contain
     *               @param name the name of the node
     * @param parent the parent of the node
     * @param <T>    the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> namedChild(
            final T data, final String name, final Node<T> parent) {
        return new NodeItem<>(data, name, parent);
    }

}
