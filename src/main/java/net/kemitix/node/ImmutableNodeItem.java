package net.kemitix.node;

import java.util.List;
import java.util.Set;

/**
 * Represents an immutable tree of nodes.
 *
 * <p>Due to the use of generics the data within a node may not be immutable.
 * (We can't create a defensive copy.) So if a user were to use {@code
 * getData()} they could then modify the original data within the node. This
 * wouldn't affect the integrity of the node tree structure, however.</p>
 *
 * @param <T> the type of data stored in each node
 *
 * @author pcampbell
 */
final class ImmutableNodeItem<T> extends AbstractNodeItem<T> {

    private static final String IMMUTABLE_OBJECT = "Immutable object";

    private ImmutableNodeItem(
            final T data, final String name, final Node<T> parent,
            final Set<Node<T>> children) {
        super(data, name, parent, children);
    }

    static <T> ImmutableNodeItem<T> newRoot(
            final T data, final String name, final Set<Node<T>> children) {
        return new ImmutableNodeItem<>(data, name, null, children);
    }

    static <T> ImmutableNodeItem<T> newChild(
            final T data, final String name, final Node<T> parent,
            final Set<Node<T>> children) {
        return new ImmutableNodeItem<>(data, name, parent, children);
    }

    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void setData(final T data) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void setParent(final Node<T> parent) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void addChild(final Node<T> child) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public Node<T> createChild(final T child) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public Node<T> createChild(final T child, final String name) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void createDescendantLine(final List<T> descendants) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public Node<T> findOrCreateChild(final T child) {
        return findChild(child).orElseThrow(
                () -> new UnsupportedOperationException(IMMUTABLE_OBJECT));
    }

    @Override
    public void insertInPath(final Node<T> node, final String... path) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void removeChild(final Node<T> node) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void removeParent() {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

}
