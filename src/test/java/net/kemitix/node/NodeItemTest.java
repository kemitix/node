package net.kemitix.node;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link NodeItem}.
 *
 * @author pcampbell
 */
public class NodeItemTest {

    /**
     * Class under test.
     */
    private Node<String> node;

    /**
     * Test {@link NodeItem#Node(java.lang.Object) } that node data is
     * recoverable.
     */
    @Test
    public void shouldReturnNodeData() {
        //given
        final String data = "this node data";
        //when
        node = new NodeItem<>(data);
        //then
        assertThat(node.getData(), is(data));
    }

    /**
     * Test {@link NodeItem#Node(java.lang.Object) } that passing null as node
     * data throws exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenDataIsNull() {
        //when
        node = new NodeItem<>(null);
    }

    /**
     * Test {@link NodeItem#Node(java.lang.Object) } that default node parent is
     * null.
     */
    @Test
    public void shouldHaveNullForDefaulParent() {
        //given
        node = new NodeItem<>("data");
        //then
        assertNull(node.getParent());
    }

    /**
     * Test {@link NodeItem#Node(java.lang.Object, net.kemitix.node.Node) } that
     * provided node parent is returned.
     */
    @Test
    public void shouldReturnNodeParent() {
        //given
        Node<String> parent = new NodeItem<>("parent");
        //when
        node = new NodeItem<>("subject", parent);
        //then
        assertThat(node.getParent(), is(parent));
    }

    /**
     * Test {@link NodeItem#Node(java.lang.Object, net.kemitix.node.Node) } that
     * setting the parent on a node where the proposed parent is a child of the
     * node throws an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenSettingParentToAChild() {
        //given
        node = new NodeItem<>("subject");
        Node<String> child = new NodeItem<>("child", node);
        //when
        node.setParent(child);
    }

    /**
     * Test {@link NodeItem#Node(java.lang.Object, net.kemitix.node.Node) } that
     * when parent is added to created node, the created node is now a child of
     * the parent.
     */
    @Test
    public void shouldAddNewNodeAsChildToParent() {
        //given
        Node<String> parent = new NodeItem<>("parent");
        //when
        node = new NodeItem<>("subject", parent);
        //then
        assertThat(parent.getChildren(), hasItem(node));
    }

    /**
     * Test {@link NodeItem#setParent(net.kemitix.node.Node) } that we return
     * the same parent when set.
     */
    @Test
    public void shouldReturnSetParent() {
        //given
        node = new NodeItem<>("subject");
        Node<String> parent = new NodeItem<>("parent");
        //when
        node.setParent(parent);
        //then
        assertThat(node.getParent(), is(parent));
    }

    /**
     * Test {@link NodeItem#setParent(net.kemitix.node.Node) } that we throw an
     * exception when passed null.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenSetParentNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.setParent(null);
    }

    /**
     * Test {@link NodeItem#setParent(net.kemitix.node.Node) } that we throw an
     * exceptions when attempting to node as its own parent.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenSetParentSelf() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.setParent(node);
    }

    /**
     * Test {@link NodeItem#setParent(net.kemitix.node.Node) } that when a node
     * with an existing parent is assigned a new parent, that the old parent no
     * longer sees it as one of its children.
     */
    @Test
    public void shouldUpdateOldParentWhenNodeSetToNewParent() {
        //given
        node = new NodeItem<>("subject");
        Node<String> child = node.createChild("child");
        Node<String> newParent = new NodeItem<>("newParent");
        //when
        child.setParent(newParent);
        //then
        assertThat(child.getParent(), is(newParent));
        assertFalse(node.getChild("child").isPresent());
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that when a node
     * is added as a child to another node, that it's previous parent no longer
     * has it as a child.
     */
    @Test
    public void shouldRemoveNodeFromOldParentWhenAddedAsChildToNewParent() {
        //given
        node = new NodeItem<>("subject");
        Node<String> child = node.createChild("child");
        Node<String> newParent = new NodeItem<>("newParent");
        //when
        newParent.addChild(child);
        //then
        assertThat(child.getParent(), is(newParent));
        assertFalse(node.getChild("child").isPresent());
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding null
     * as a child throws an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenAddingNullAsChild() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.addChild(null);
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding a
     * child is returned.
     */
    @Test
    public void shouldReturnAddedChild() {
        //given
        Node<String> child = new NodeItem<>("child");
        node = new NodeItem<>("subject");
        //when
        node.addChild(child);
        //then
        assertThat(node.getChildren(), hasItem(child));
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding a node
     * as it's own child throws an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenAddingANodeAsOwnChild() {
        //given
        node = new NodeItem<>("subject");
        //then
        node.addChild(node);
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding a node
     * to itself as a child causes an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingSelfAsChild() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.addChild(node);
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding the
     * parent to node causes an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingParentAsChild() {
        //given
        Node<String> parent = new NodeItem<>("parent");
        node = new NodeItem<>("subject", parent);
        //when
        node.addChild(parent);
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding the
     * grandparent to node causes an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingGrandParentAsChild() {
        //given
        Node<String> grandParent = new NodeItem<>("grandparent");
        Node<String> parent = new NodeItem<>("parent", grandParent);
        node = new NodeItem<>("subject", parent);
        //when
        node.addChild(grandParent);
    }

    /**
     * Test {@link NodeItem#addChild(net.kemitix.node.Node) } that adding a
     * child to a node, sets the child's parent node.
     */
    @Test
    public void shouldSetParentOnChildWhenAddedAsChild() {
        //given
        Node<String> child = new NodeItem<>("child");
        node = new NodeItem<>("subject");
        //when
        node.addChild(child);
        //then
        assertThat(child.getParent(), is(node));
    }

    /**
     * Test {@link NodeItem#walkTree(java.util.List) } that we can walk a tree
     * to the target node.
     */
    @Test
    public void shouldWalkTreeToNode() {
        //given
        final String grandparent = "grandparent";
        Node<String> grandParentNode = new NodeItem<>(grandparent);
        final String parent = "parent";
        Node<String> parentNode = new NodeItem<>(parent, grandParentNode);
        final String subject = "subject";
        node = new NodeItem<>(subject, parentNode);
        //when
        Optional<Node<String>> result = grandParentNode.walkTree(Arrays.asList(
                parent, subject));
        //then
        assertTrue(result.isPresent());
        assertThat(result.get(), is(node));
    }

    /**
     * Test {@link NodeItem#walkTree(java.util.List) } that we get an empty
     * {@link Optional} when walking a path that doesn't exist.
     */
    @Test
    public void shouldNotFindNonExistantChildNode() {
        //given
        final String parent = "parent";
        Node<String> parentNode = new NodeItem<>(parent);
        final String subject = "subject";
        node = new NodeItem<>(subject, parentNode);
        //when
        Optional<Node<String>> result = parentNode.walkTree(Arrays.asList(
                subject, "no child"));
        //then
        assertFalse(result.isPresent());
    }

    /**
     * Test {@link NodeItem#walkTree(java.util.List) } that when we pass null we
     * get an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNEWhenWalkTreeNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.walkTree(null);
    }

    /**
     * Test {@link NodeItem#walkTree(java.util.List) } that when we pass an
     * empty path we get and empty {@link Optional} as a result.
     */
    @Test
    public void shouldReturnEmptyForEmptyWalkTreePath() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.walkTree(Collections.emptyList());
    }

    /**
     * Test {@link NodeItem#createDescendantLine(java.util.List) } that we can
     * create a chain of descendant nodes.
     */
    @Test
    public void shouldCreateDescendantNodes() {
        //given
        node = new NodeItem<>("subject");
        final String alphaData = "alpha";
        final String betaData = "beta";
        final String gammaData = "gamma";
        //when
        node.createDescendantLine(
                Arrays.asList(alphaData, betaData, gammaData));
        //then
        final Optional<Node<String>> alphaOptional = node.getChild(alphaData);
        assertTrue(alphaOptional.isPresent());
        Node<String> alpha = alphaOptional.get();
        assertThat(alpha.getParent(), is(node));
        final Optional<Node<String>> betaOptional = alpha.getChild(betaData);
        assertTrue(betaOptional.isPresent());
        Node<String> beta = betaOptional.get();
        assertThat(beta.getParent(), is(alpha));
        final Optional<Node<String>> gammaOptional = beta.getChild(gammaData);
        assertTrue(gammaOptional.isPresent());
        Node<String> gamma = gammaOptional.get();
        assertThat(gamma.getParent(), is(beta));
    }

    /**
     * Test {@link NodeItem#createDescendantLine(java.util.List) } that if we
     * pass null to create a chain of descendant nodes we get an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenCreateDescendantNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.createDescendantLine(null);
    }

    /**
     * Test {@link NodeItem#createDescendantLine(java.util.List) } that if we
     * pass an empty list nothing is changed.
     */
    @Test
    public void shouldChangeNothingWhenCreateDescendantEmpty() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.createDescendantLine(Collections.emptyList());
        //then
        assertThat(node.getChildren().size(), is(0));
    }

    /**
     * Test {@link NodeItem#findOrCreateChild(java.lang.Object) } that we can
     * find a child of a node.
     */
    @Test
    public void shouldFindExistingChildNode() {
        //given
        node = new NodeItem<>("subject");
        final String childData = "child";
        Node<String> child = new NodeItem<>(childData, node);
        //when
        Node<String> found = node.findOrCreateChild(childData);
        //then
        assertThat(found, is(child));
    }

    /**
     * Test {@link NodeItem#findOrCreateChild(java.lang.Object) } that we create
     * a missing child of a node.
     */
    @Test
    public void shouldFindCreateNewChildNode() {
        //given
        node = new NodeItem<>("subject");
        final String childData = "child";
        //when
        Node<String> found = node.findOrCreateChild(childData);
        //then
        assertThat(found.getData(), is(childData));
    }

    /**
     * Test {@link NodeItem#findOrCreateChild(java.lang.Object) } that if we
     * pass null we get an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEFWhenFindOrCreateChildNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.findOrCreateChild(null);
    }

    /**
     * Test {@link NodeItem#getChild(java.lang.Object) } that we can get the
     * node for a child.
     */
    @Test
    public void shouldGetChild() {
        //given
        node = new NodeItem<>("subject");
        final String childData = "child";
        Node<String> child = new NodeItem<>(childData);
        node.addChild(child);
        //when
        Optional<Node<String>> found = node.getChild(childData);
        //then
        assertTrue(found.isPresent());
        assertThat(found.get(), is(child));
    }

    /**
     * Test {@link NodeItem#getChild(java.lang.Object) } that we throw an
     * exception when passed null.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenGetChildNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.getChild(null);
    }

    /**
     * Test {@link NodeItem#createChild(java.lang.Object) } that we create a
     * child as a child of the current node and with the current node as its
     * parent.
     */
    @Test
    public void shoudCreateChild() {
        //given
        node = new NodeItem<>("subject");
        final String childData = "child";
        //when
        Node<String> child = node.createChild(childData);
        //then
        assertThat(child.getParent(), is(node));
        final Optional<Node<String>> foundChild = node.getChild(childData);
        assertTrue(foundChild.isPresent());
        assertThat(foundChild.get(), is(child));
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenCreateChildNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.createChild(null);
    }

}
