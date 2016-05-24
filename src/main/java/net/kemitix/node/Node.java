package net.kemitix.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An interface for tree node items.
 *
 * @param <T> the type of data held in each node
 *
 * @author pcampbell
 */
public interface Node<T> {

    /**
     * Fetch the data held within the node.
     *
     * @return the node's data
     */
    T getData();

    /**
     * Returns true if the node is empty (has no data).
     *
     * @return true is data is null
     */
    boolean isEmpty();

    /**
     * Fetch the parent node.
     * <p>
     * If the node is a root node, i.e. has no parent, then this will return
     * null.
     *
     * @return the parent node
     */
    Node<T> getParent();

    /**
     * Fetches the child nodes.
     *
     * @return the set of child nodes
     */
    Set<Node<T>> getChildren();

    /**
     * Adds the child to the node.
     *
     * @param child the node to add
     */
    void addChild(final Node<T> child);

    /**
     * Creates a new node and adds it as a child of the current node.
     *
     * @param child the child node's data
     *
     * @return the new child node
     */
    Node<T> createChild(final T child);

    /**
     * Populates the tree with the path of nodes, each being a child of the
     * previous node in the path.
     *
     * @param descendants the line of descendants from the current node
     */
    void createDescendantLine(final List<T> descendants);

    /**
     * Looks for a child node and returns it, creating a new child node if one
     * isn't found.
     *
     * @param child the child's data to search or create with
     *
     * @return the found or created child node
     */
    Node<T> findOrCreateChild(final T child);

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    Optional<Node<T>> getChild(final T child);

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    boolean isChildOf(final Node<T> node);

    /**
     * Make the current node a direct child of the parent.
     *
     * @param parent the new parent node
     */
    void setParent(final Node<T> parent);

    /**
     * Walks the node tree using the path to select each child.
     *
     * @param path the path to the desired child
     *
     * @return the child or null
     */
    Optional<Node<T>> walkTree(final List<T> path);

}
