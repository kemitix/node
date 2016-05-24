package net.kemitix.node;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a tree of nodes.
 *
 * @param <T> the type of data stored in each node
 *
 * @author pcampbell
 */
public class NodeItem<T> implements Node<T> {

    private final T data;

    private final Set<Node<T>> children = new HashSet<>();

    private Function<Node<T>, String> nameSupplier;

    private Node<T> parent;

    private String name;

    /**
     * Create unnamed root node.
     *
     * @param data the data or null
     */
    public NodeItem(final T data) {
        this.data = data;
        this.nameSupplier = (n) -> null;
    }

    /**
     * Create named root node.
     *
     * @param data the data or null
     * @param name the name
     */
    public NodeItem(final T data, final String name) {
        this(data);
        this.name = name;
    }

    /**
     * Creates root node with a name supplier.
     *
     * @param data         the data or null
     * @param nameSupplier the name supplier function
     */
    public NodeItem(
            final T data, final Function<Node<T>, String> nameSupplier) {
        this(data);
        this.nameSupplier = nameSupplier;
        name = generateName();
    }

    /**
     * Creates a node with a parent.
     *
     * @param data   the data or null
     * @param parent the parent node
     */
    public NodeItem(final T data, final Node<T> parent) {
        this.data = data;
        setParent(parent);
        this.name = generateName();
    }

    /**
     * Creates a named node with a parent.
     *
     * @param data   the data or null
     * @param name   the name
     * @param parent the parent node
     */
    public NodeItem(final T data, final String name, final Node<T> parent) {
        this.data = data;
        this.name = name;
        setParent(parent);
    }

    /**
     * Creates a node with a name supplier and a parent.
     *
     * @param data         the data or null
     * @param nameSupplier the name supplier function
     * @param parent       the parent node
     */
    public NodeItem(
            final T data, final Function<Node<T>, String> nameSupplier,
            final Node<T> parent) {
        this(data, nameSupplier);
        setParent(parent);
    }

    private String generateName() {
        return getNameSupplier().apply(this);
    }

    private Function<Node<T>, String> getNameSupplier() {
        if (nameSupplier != null) {
            return nameSupplier;
        }
        // no test for parent as root nodes will always have a default name
        // supplier
        return ((NodeItem<T>) parent).getNameSupplier();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    @Override
    public Node<T> getParent() {
        return parent;
    }

    @Override
    public Set<Node<T>> getChildren() {
        return children;
    }

    /**
     * Make the current node a direct child of the parent.
     *
     * @param parent the new parent node
     */
    @Override
    public final void setParent(final Node<T> parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        if (this.equals(parent) || parent.isChildOf(this)) {
            throw new NodeException("Parent is a descendant");
        }
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = parent;
        parent.addChild(this);
    }

    /**
     * Adds the child to the node.
     *
     * @param child the node to add
     */
    @Override
    public void addChild(final Node<T> child) {
        if (child == null) {
            throw new NullPointerException("child");
        }
        if (this.equals(child) || isChildOf(child)) {
            throw new NodeException("Child is an ancestor");
        }
        if (child.isNamed()) {
            final Optional<Node<T>> existingChild = findChildNamed(
                    child.getName());
            if (existingChild.isPresent() && existingChild.get() != child) {
                throw new NodeException(
                        "Node with that name already exists here");
            }
        }
        children.add(child);
        if (child.getParent() == null || !child.getParent().equals(this)) {
            child.setParent(this);
        }
    }

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    @Override
    public boolean isChildOf(final Node<T> node) {
        return parent != null && (node.equals(parent) || parent.isChildOf(
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
    public Optional<Node<T>> walkTree(final List<T> path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (path.size() > 0) {
            Optional<Node<T>> found = getChild(path.get(0));
            if (found.isPresent()) {
                if (path.size() > 1) {
                    return found.get().walkTree(path.subList(1, path.size()));
                }
                return found;
            }
        }
        return Optional.empty();
    }

    /**
     * Populates the tree with the path of nodes, each being a child of the
     * previous node in the path.
     *
     * @param descendants the line of descendants from the current node
     */
    @Override
    public void createDescendantLine(final List<T> descendants) {
        if (descendants == null) {
            throw new NullPointerException("descendants");
        }
        if (!descendants.isEmpty()) {
            findOrCreateChild(descendants.get(0)).createDescendantLine(
                    descendants.subList(1, descendants.size()));
        }
    }

    /**
     * Looks for a child node and returns it, creating a new child node if one
     * isn't found.
     *
     * @param child the child's data to search or create with
     *
     * @return the found or created child node
     */
    @Override
    public Node<T> findOrCreateChild(final T child) {
        if (child == null) {
            throw new NullPointerException("child");
        }
        return getChild(child).orElseGet(() -> createChild(child));
    }

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    @Override
    public Optional<Node<T>> getChild(final T child) {
        if (child == null) {
            throw new NullPointerException("child");
        }
        return children.stream()
                       .filter((Node<T> t) -> t.getData().equals(child))
                       .findAny();
    }

    /**
     * Creates a new node and adds it as a child of the current node.
     *
     * @param child the child node's data
     *
     * @return the new child node
     */
    @Override
    public Node<T> createChild(final T child) {
        if (child == null) {
            throw new NullPointerException("child");
    @Override
    public Optional<Node<T>> findChildNamed(final String named) {
        if (named == null) {
            throw new NullPointerException("name");
        }
        return children.stream()
                       .filter((Node<T> t) -> t.getName().equals(named))
                       .findAny();
    }

    @Override
    public Node<T> getChildNamed(final String named) {
        final Optional<Node<T>> optional = findChildNamed(named);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NodeException("Named child not found");
    }

    @Override
    public boolean isNamed() {
        return name != null && name.length() > 0;
    }

        }
        return new NodeItem<>(child, this);
    }

}
