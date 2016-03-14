package net.kemitix.node;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
     * Test that node data is recoverable.
     */
    @Test
    public void shouldReturnNodeData() {
        //given
        val data = "this node data";
        //when
        node = new NodeItem<>(data);
        //then
        Assert.assertThat("can get the data from a node", node.getData(),
                is(data));
    }

    /**
     * Test that passing null as node data throws exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenDataIsNull() {
        //when
        node = new NodeItem<>(null);
    }

    /**
     * Test that default node parent is null.
     */
    @Test
    public void shouldHaveNullForDefaultParent() {
        //given
        node = new NodeItem<>("data");
        //then
        Assert.assertThat("node created without a parent has null as parent",
                node.getParent(), nullValue());
    }

    /**
     * Test that provided node parent is returned.
     */
    @Test
    public void shouldReturnNodeParent() {
        //given
        val parent = new NodeItem<String>("parent");
        //when
        node = new NodeItem<>("subject", parent);
        //then
        Assert.assertThat("node created with a parent can return the parent",
                node.getParent(), is(parent));
    }

    /**
     * Test that setting the parent on a node where the proposed parent is a
     * child of the node throws an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenSettingParentToAChild() {
        //given
        node = new NodeItem<>("subject");
        val child = new NodeItem<String>("child", node);
        //when
        node.setParent(child);
    }

    /**
     * Test that when parent is added to created node, the created node is now a
     * child of the parent.
     */
    @Test
    public void shouldAddNewNodeAsChildToParent() {
        //given
        val parent = new NodeItem<String>("parent");
        //when
        node = new NodeItem<>("subject", parent);
        //then
        Assert.assertThat(
                "when a node is created with a parent, the parent has the new"
                        + " node among it's children", parent.getChildren(),
                hasItem(node));
    }

    /**
     * Test that we return the same parent when set.
     */
    @Test
    public void shouldReturnSetParent() {
        //given
        node = new NodeItem<>("subject");
        val parent = new NodeItem<String>("parent");
        //when
        node.setParent(parent);
        //then
        Assert.assertThat(
                "when a node is assigned a new parent that parent can be "
                        + "returned", node.getParent(), is(parent));
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenSetParentNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.setParent(null);
    }

    /**
     * Test that we throw an exceptions when attempting to node as its own
     * parent.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenSetParentSelf() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.setParent(node);
    }

    /**
     * Test that when a node with an existing parent is assigned a new parent,
     * that the old parent no longer sees it as one of its children.
     */
    @Test
    public void shouldUpdateOldParentWhenNodeSetToNewParent() {
        //given
        node = new NodeItem<>("subject");
        val child = node.createChild("child");
        val newParent = new NodeItem<String>("newParent");
        //when
        child.setParent(newParent);
        //then
        Assert.assertThat(
                "when a node is assigned a new parent, the old parent is "
                        + "replaced", child.getParent(), is(newParent));
        Assert.assertThat(
                "when a node is assigned a new parent, the old parent no "
                        + "longer has the node among it's children",
                node.getChild("child").isPresent(), is(false));
    }

    /**
     * Test that when a node is added as a child to another node, that it's
     * previous parent no longer has it as a child.
     */
    @Test
    public void shouldRemoveNodeFromOldParentWhenAddedAsChildToNewParent() {
        //given
        node = new NodeItem<>("subject");
        val child = node.createChild("child");
        val newParent = new NodeItem<String>("newParent");
        //when
        newParent.addChild(child);
        //then
        Assert.assertThat(
                "when a node with an existing parent is added as a child "
                        + "to another node, then the old parent is replaced",
                child.getParent(), is(newParent));
        Assert.assertThat(
                "when a node with an existing parent is added as a child to "
                        + "another node, then the old parent no longer has "
                        + "the node among it's children",
                node.getChild("child").isPresent(), is(false));
    }

    /**
     * Test that adding null as a child throws an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenAddingNullAsChild() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.addChild(null);
    }

    /**
     * Test that adding a child is returned.
     */
    @Test
    public void shouldReturnAddedChild() {
        //given
        node = new NodeItem<>("subject");
        val child = new NodeItem<String>("child");
        //when
        node.addChild(child);
        //then
        Assert.assertThat(
                "when a node is added as a child, the node is among the "
                        + "children", node.getChildren(), hasItem(child));
    }

    /**
     * Test that adding a node as it's own child throws an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowNEWhenAddingANodeAsOwnChild() {
        //given
        node = new NodeItem<>("subject");
        //then
        node.addChild(node);
    }

    /**
     * Test that adding a node to itself as a child causes an exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingSelfAsChild() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.addChild(node);
    }

    /**
     * Test that adding the parent of a node to the node as a child causes an
     * exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingParentAsChild() {
        //given
        val parent = new NodeItem<String>("parent");
        node = new NodeItem<>("subject", parent);
        //when
        node.addChild(parent);
    }

    /**
     * Test that adding the grandparent to a node as a child causes an
     * exception.
     */
    @Test(expected = NodeException.class)
    public void shouldThrowWhenAddingGrandParentAsChild() {
        //given
        val grandParent = new NodeItem<String>("grandparent");
        val parent = new NodeItem<String>("parent", grandParent);
        node = new NodeItem<>("subject", parent);
        //when
        node.addChild(grandParent);
    }

    /**
     * Test that adding a child to a node, sets the child's parent node.
     */
    @Test
    public void shouldSetParentOnChildWhenAddedAsChild() {
        //given
        val child = new NodeItem<String>("child");
        node = new NodeItem<>("subject");
        //when
        node.addChild(child);
        //then
        Assert.assertThat(
                "when a node is added as a child, the child has the node as "
                        + "its parent", child.getParent(), is(node));
    }

    /**
     * Test that we can walk a tree to the target node.
     */
    @Test
    public void shouldWalkTreeToNode() {
        //given
        val grandparent = "grandparent";
        val grandParentNode = new NodeItem<String>(grandparent);
        val parent = "parent";
        val parentNode = new NodeItem<String>(parent, grandParentNode);
        val subject = "subject";
        node = new NodeItem<>(subject, parentNode);
        //when
        val result = grandParentNode.walkTree(Arrays.asList(parent, subject));
        //then
        Assert.assertThat("when we walk the tree to a node it is found",
                result.isPresent(), is(true));
        if (result.isPresent()) {
            Assert.assertThat(
                    "when we walk the tree to a node the correct node is found",
                    result.get(), is(node));
        }
    }

    /**
     * Test that we get an empty {@link Optional} when walking a path that
     * doesn't exist.
     */
    @Test
    public void shouldNotFindNonExistentChildNode() {
        //given
        val parent = "parent";
        val parentNode = new NodeItem<String>(parent);
        val subject = "subject";
        node = new NodeItem<>(subject, parentNode);
        //when
        val result = parentNode.walkTree(Arrays.asList(subject, "no child"));
        //then
        Assert.assertThat(
                "when we walk the tree to a node that doesn't exists, nothing"
                        + " is found", result.isPresent(), is(false));
    }

    /**
     * Test that when we pass null we get an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNEWhenWalkTreeNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.walkTree(null);
    }

    /**
     * Test that when we pass an empty path we get and empty {@link Optional} as
     * a result.
     */
    @Test
    public void shouldReturnEmptyForEmptyWalkTreePath() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.walkTree(Collections.emptyList());
    }

    /**
     * Test that we can create a chain of descendant nodes.
     */
    @Test
    public void shouldCreateDescendantNodes() {
        //given
        node = new NodeItem<>("subject");
        val alphaData = "alpha";
        val betaData = "beta";
        val gammaData = "gamma";
        //when
        node.createDescendantLine(
                Arrays.asList(alphaData, betaData, gammaData));
        //then
        val alphaOptional = node.getChild(alphaData);
        Assert.assertThat(
                "when creating a descendant line, the first element is found",
                alphaOptional.isPresent(), is(true));
        if (alphaOptional.isPresent()) {
            val alpha = alphaOptional.get();
            Assert.assertThat(
                    "when creating a descendant line, the first element has "
                            + "the current node as its parent",
                    alpha.getParent(), is(node));
            val betaOptional = alpha.getChild(betaData);
            Assert.assertThat(
                    "when creating a descendant line, the second element is "
                            + "found", betaOptional.isPresent(), is(true));
            if (betaOptional.isPresent()) {
                val beta = betaOptional.get();
                Assert.assertThat(
                        "when creating a descendant line, the second element "
                                + "has the first as its parent",
                        beta.getParent(), is(alpha));
                val gammaOptional = beta.getChild(gammaData);
                Assert.assertThat(
                        "when creating a descendant line, the third element "
                                + "is found", gammaOptional.isPresent(),
                        is(true));
                if (gammaOptional.isPresent()) {
                    val gamma = gammaOptional.get();
                    Assert.assertThat(
                            "when creating a descendant line, the third "
                                    + "element has the second as its parent",
                            gamma.getParent(), is(beta));
                }
            }
        }
    }

    /**
     * Test that if we pass null to create a chain of descendant nodes we get an
     * exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenCreateDescendantNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.createDescendantLine(null);
    }

    /**
     * Test that if we pass an empty list nothing is changed.
     */
    @Test
    public void shouldChangeNothingWhenCreateDescendantEmpty() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.createDescendantLine(Collections.emptyList());
        //then
        Assert.assertThat(
                "when creating a descendant line from an empty list, nothing "
                        + "is created", node.getChildren().size(), is(0));
    }

    /**
     * Test that we can find a child of a node.
     */
    @Test
    public void shouldFindExistingChildNode() {
        //given
        node = new NodeItem<>("subject");
        val childData = "child";
        val child = new NodeItem<String>(childData, node);
        //when
        val found = node.findOrCreateChild(childData);
        //then
        Assert.assertThat(
                "when searching for a child by data, the matching child is "
                        + "found", found, is(child));
    }

    /**
     * Test that we create a missing child of a node.
     */
    @Test
    public void shouldFindCreateNewChildNode() {
        //given
        node = new NodeItem<>("subject");
        val childData = "child";
        //when
        val found = node.findOrCreateChild(childData);
        //then
        Assert.assertThat(
                "when searching for a child by data, a new node is created",
                found.getData(), is(childData));
    }

    /**
     * Test that if we pass null we get an exception.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEFWhenFindOrCreateChildNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.findOrCreateChild(null);
    }

    /**
     * Test that we can get the node for a child.
     */
    @Test
    public void shouldGetChild() {
        //given
        node = new NodeItem<>("subject");
        val childData = "child";
        val child = new NodeItem<String>(childData);
        node.addChild(child);
        //when
        val found = node.getChild(childData);
        //then
        Assert.assertThat("when retrieving a child by its data, it is found",
                found.isPresent(), is(true));
        if (found.isPresent()) {
            Assert.assertThat(
                    "when retrieving a child by its data, it is the expected "
                            + "node", found.get(), is(child));
        }
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenGetChildNull() {
        //given
        node = new NodeItem<>("subject");
        //when
        node.getChild(null);
    }

    /**
     * Test that we create a child as a child of the current node and with the
     * current node as its parent.
     */
    @Test
    public void shouldCreateChild() {
        //given
        node = new NodeItem<>("subject");
        val childData = "child";
        //when
        val child = node.createChild(childData);
        //then
        Assert.assertThat(
                "when creating a child node, the child has the current node "
                        + "as its parent", child.getParent(), is(node));
        val foundChild = node.getChild(childData);
        Assert.assertThat(
                "when creating a child node, the child can be found by its "
                        + "data", foundChild.isPresent(), is(true));
        if (foundChild.isPresent()) {
            Assert.assertThat(
                    "when creating a child node, the correct child can be "
                            + "found by its data", foundChild.get(), is(child));
        }
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
