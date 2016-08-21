package net.kemitix.node;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @param name   the name of the node
     * @param parent the parent of the node
     * @param <T>    the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> namedChild(
            final T data, final String name, final Node<T> parent) {
        return new NodeItem<>(data, name, parent);
    }

    /**
     * Creates an immutable copy of an existing node tree.
     *
     * @param root the root node of the source tree
     * @param <T>  the type of the data
     *
     * @return the immutable copy of the tree
     */
    public static <T> Node<T> asImmutable(final Node<T> root) {
        if (root.getParent().isPresent()) {
            throw new IllegalArgumentException("source must be the root node");
        }
        final Set<Node<T>> children = getImmutableChildren(root);
        return ImmutableNodeItem.newRoot(root.getData().orElse(null),
                root.getName(), children);
    }

    private static <T> Set<Node<T>> getImmutableChildren(final Node<T> source) {
        return source.getChildren()
                     .stream()
                     .map(Nodes::asImmutableChild)
                     .collect(Collectors.toSet());
    }

    private static <T> Node<T> asImmutableChild(
            final Node<T> source) {
        final Optional<Node<T>> sourceParent = source.getParent();
        if (sourceParent.isPresent()) {
            return ImmutableNodeItem.newChild(source.getData().orElse(null),
                    source.getName(), sourceParent.get(),
                    getImmutableChildren(source));
        } else {
            throw new IllegalArgumentException(
                    "source must not be the root node");
        }
    }

}
