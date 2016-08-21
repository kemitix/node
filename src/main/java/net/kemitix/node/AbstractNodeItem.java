package net.kemitix.node;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract node item, providing default implementations for most read-only
 * operations.
 *
 * @param <T> the type of data stored in each node
 *
 * @author pcampbell
 */
abstract class AbstractNodeItem<T> implements Node<T> {

    private T data;

    private String name;

    private Node<T> parent;

    private final Set<Node<T>> children;

    protected AbstractNodeItem(
            final T data, final String name, final Node<T> parent,
            final Set<Node<T>> children) {
        this.data = data;
        this.name = name;
        this.parent = parent;
        this.children = children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    @Override
    public Optional<Node<T>> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Set<Node<T>> getChildren() {
        return new HashSet<>(children);
    }

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    @Override
    public Optional<Node<T>> findChild(final T child) {
        if (child == null) {
            throw new NullPointerException("child");
        }
        return children.stream().filter(node -> {
            final Optional<T> d = node.getData();
            return d.isPresent() && d.get().equals(child);
        }).findAny();
    }

    @Override
    public Node<T> getChild(final T child) {
        return findChild(child).orElseThrow(
                () -> new NodeException("Child not found"));
    }

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    @Override
    public boolean isDescendantOf(final Node<T> node) {
        return parent != null && (node.equals(parent) || parent.isDescendantOf(
                node));
    }

    /**
     * Walks the node tree using the path to select each child.
     *
     * @param path the path to the desired child
     *
     * @return the child or null
     */
    @Override
    public Optional<Node<T>> findInPath(final List<T> path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (path.size() > 0) {
            Optional<Node<T>> found = findChild(path.get(0));
            if (found.isPresent()) {
                if (path.size() > 1) {
                    return found.get().findInPath(path.subList(1, path.size()));
                }
                return found;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Node<T>> findChildByName(final String named) {
        if (named == null) {
            throw new NullPointerException("name");
        }
        return children.stream()
                       .filter(n -> n.getName().equals(named))
                       .findAny();
    }

    @Override
    public Node<T> getChildByName(final String named) {
        return findChildByName(named).orElseThrow(
                () -> new NodeException("Named child not found"));
    }

    @Override
    public String drawTree(final int depth) {
        final StringBuilder sb = new StringBuilder();
        final String unnamed = "(unnamed)";
        if (isNamed()) {
            sb.append(String.format("[%1$" + (depth + name.length()) + "s]\n",
                    name));
        } else if (!children.isEmpty()) {
            sb.append(
                    String.format("[%1$" + (depth + unnamed.length()) + "s]\n",
                            unnamed));
        }
        getChildren().forEach(c -> sb.append(c.drawTree(depth + 1)));
        return sb.toString();
    }

    @Override
    public boolean isNamed() {
        return name != null && name.length() > 0;
    }
}
