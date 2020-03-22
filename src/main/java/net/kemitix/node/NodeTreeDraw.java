package net.kemitix.node;

/**
 * Draw a representation of the tree.
 */
public class NodeTreeDraw {

    /**
     * Draw a representation of the tree.
     *
     * @param depth current depth for recursion
     *
     * @return a representation of the tree
     */
    @SuppressWarnings("movevariableinsideif")
    public <T> String drawTree(
            final Node<T> node,
            final int depth
    ) {
        final StringBuilder sb = new StringBuilder();
        final String unnamed = "(unnamed)";
        if (node.isNamed()) {
            sb.append(formatByDepth(node.getName(), depth));
        } else if (!node.getChildren().isEmpty()) {
            sb.append(formatByDepth(unnamed, depth));
        }
        node.getChildren().forEach(c -> sb.append(drawTree(c, depth + 1)));
        return sb.toString();
    }

    private String formatByDepth(final String value, final int depth) {
        return String.format("[%1$" + (depth + value.length()) + "s]\n", value);
    }

}
