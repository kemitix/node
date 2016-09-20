package net.kemitix.node;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link ImmutableNodeItem}.
 *
 * @author pcampbell
 */
public class ImmutableNodeItemTest {

    private static final String IMMUTABLE_OBJECT = "Immutable object";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Node<String> immutableNode;

    private void expectImmutableException() {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(IMMUTABLE_OBJECT);
    }

    @Test
    public void getDataReturnsData() {
        //given
        val data = "this immutableNode data";
        //when
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(data));
        //then
        assertThat(immutableNode.getData()).as(
                "can get the data from a immutableNode").
                                                   contains(data);
    }

    @Test
    public void canCreateAnEmptyAndUnnamedNode() {
        //when
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(immutableNode.isEmpty())
              .as("immutableNode is empty")
              .isTrue();
        softly.assertThat(immutableNode.isNamed())
              .as("immutableNode is unnamed")
              .isFalse();
        softly.assertAll();
    }

    @Test
    public void shouldThrowExceptionOnSetName() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        expectImmutableException();
        //when
        immutableNode.setName("named");
    }

    @Test
    public void rootNodeShouldHaveNoParent() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("data"));
        //then
        assertThat(immutableNode.getParent()).as(
                "immutableNode created without a parent has no parent")
                                             .isEmpty();
    }

    @Test
    public void shouldContainImmutableCopyOfChild() {
        //given
        val parent = Nodes.unnamedRoot("root");
        val child = Nodes.namedChild("child", "child", parent);
        //when
        immutableNode = Nodes.asImmutable(parent);
        //then
        val immutableChild = immutableNode.getChildByName("child");
        assertThat(immutableChild).isNotSameAs(child);
        assertThat(immutableChild.getName()).isEqualTo("child");
    }

    @Test
    public void childShouldHaveImmutableParent() {
        //given
        val parent = Nodes.namedRoot("parent", "root");
        Nodes.namedChild("subject", "child", parent);
        //when
        immutableNode = Nodes.asImmutable(parent);
        //then
        // get the immutable node's child's parent
        val immutableChild = immutableNode.getChildByName("child");
        final Optional<Node<String>> optionalParent
                = immutableChild.getParent();
        if (optionalParent.isPresent()) {
            val p = optionalParent.get();
            assertThat(p).hasFieldOrPropertyWithValue("name", "root")
                         .hasFieldOrPropertyWithValue("data",
                                 Optional.of("parent"));
        }
    }

    @Test
    public void shouldNotBeAbleToAddChildToImmutableTree() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("root"));
        expectImmutableException();
        //when
        Nodes.unnamedChild("child", immutableNode);
    }

    @Test
    public void shouldThrowExceptionWhenSetParent() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        expectImmutableException();
        //when
        immutableNode.setParent(Nodes.unnamedRoot("child"));
    }

    @Test
    public void shouldThrowExceptionWhenAddingChild() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        expectImmutableException();
        //when
        immutableNode.addChild(Nodes.unnamedRoot("child"));
    }

    /**
     * Test that we can walk a tree to the target node.
     */
    @Test
    @Category(NodeFindInPathTestsCategory.class)
    public void shouldWalkTreeToNode() {
        //given
        val root = Nodes.unnamedRoot("root");
        Nodes.namedChild("child", "child", Nodes.unnamedChild("parent", root));
        immutableNode = Nodes.asImmutable(root);
        //when
        val result = immutableNode.findInPath(Arrays.asList("parent", "child"));
        //then
        assertThat(result.isPresent()).isTrue();
        if (result.isPresent()) {
            assertThat(result.get().getName()).isEqualTo("child");
        }
    }

    /**
     * Test that we get an empty {@link Optional} when walking a path that
     * doesn't exist.
     */
    @Test
    @Category(NodeFindInPathTestsCategory.class)
    public void shouldNotFindNonExistentChildNode() {
        //given
        val root = Nodes.unnamedRoot("root");
        Nodes.unnamedChild("child", Nodes.unnamedChild("parent", root));
        immutableNode = Nodes.asImmutable(root);
        //when
        val result = immutableNode.findInPath(
                Arrays.asList("parent", "no child"));
        //then
        assertThat(result.isPresent()).isFalse();
    }

    /**
     * Test that when we pass null we get an exception.
     */
    @Test
    @Category(NodeFindInPathTestsCategory.class)
    public void shouldThrowNEWhenWalkTreeNull() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        exception.expect(NullPointerException.class);
        exception.expectMessage("path");
        //when
        immutableNode.findInPath(null);
    }

    /**
     * Test that when we pass an empty path we get and empty {@link Optional} as
     * a result.
     */
    @Test
    @Category(NodeFindInPathTestsCategory.class)
    public void shouldReturnEmptyForEmptyWalkTreePath() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        //when
        val result = immutableNode.findInPath(Collections.emptyList());
        //then
        assertThat(result).isEmpty();
    }

    /**
     * Test that we can find a child of a immutableNode.
     */
    @Test
    public void shouldFindExistingChildNode() {
        //given
        val root = Nodes.unnamedRoot("root");
        Nodes.unnamedChild("child", root);
        immutableNode = Nodes.asImmutable(root);
        //when
        val result = immutableNode.findChild("child");
        //then
        assertThat(result.isPresent()).isTrue();
        if (result.isPresent()) {
            assertThat(result.get().getData()).contains("child");
        }
    }

    /**
     * Test that if we pass null we get an exception.
     */
    @Test
    public void findOrCreateChildShouldThrowNPEFWhenChildIsNull() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        immutableNode.findOrCreateChild(null);
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void getChildShouldThrowNPEWhenThereIsNoChild() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("data"));
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        immutableNode.findChild(null);
    }

    @Test
    public void getChildNamedFindsChild() {
        //given
        val root = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        val beta = Nodes.namedRoot("beta data", "beta");
        root.addChild(alpha);
        root.addChild(beta);
        immutableNode = Nodes.asImmutable(root);
        //when
        val result = immutableNode.getChildByName("alpha");
        //then
        assertThat(result.getName()).isEqualTo(alpha.getName());
    }

    @Test
    public void getChildNamedFindsNothing() {
        //given
        val root = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        val beta = Nodes.namedRoot("beta data", "beta");
        root.addChild(alpha);
        root.addChild(beta);
        exception.expect(NodeException.class);
        exception.expectMessage("Named child not found");
        immutableNode = Nodes.asImmutable(root);
        //when
        immutableNode.getChildByName("gamma");
    }

    @Test
    public void removingParentThrowsException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        expectImmutableException();
        //when
        immutableNode.removeParent();
    }

    @Test
    public void findChildNamedShouldThrowNPEWhenNameIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("name");
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //when
        immutableNode.findChildByName(null);
    }

    @Test
    public void isNamedNull() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //then
        assertThat(immutableNode.isNamed()).isFalse();
    }

    @Test
    public void isNamedEmpty() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.namedRoot(null, ""));
        //then
        assertThat(immutableNode.isNamed()).isFalse();
    }

    @Test
    public void isNamedNamed() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.namedRoot(null, "named"));
        //then
        assertThat(immutableNode.isNamed()).isTrue();
    }

    @Test
    public void removeChildThrowsExceptions() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        expectImmutableException();
        //then
        immutableNode.removeChild(null);
    }

    @Test
    public void drawTreeIsCorrect() {
        //given
        val root = Nodes.namedRoot("root data", "root");
        val bob = Nodes.namedChild("bob data", "bob", root);
        val alice = Nodes.namedChild("alice data", "alice", root);
        Nodes.namedChild("dave data", "dave", alice);
        Nodes.unnamedChild("bob's child's data",
                bob); // has no name and no children so no included
        val kim = Nodes.unnamedChild("kim data", root); // nameless mother
        Nodes.namedChild("lucy data", "lucy", kim);
        immutableNode = Nodes.asImmutable(root);
        //when
        val tree = immutableNode.drawTree(0);
        //then
        String[] lines = tree.split("\n");
        assertThat(lines).contains("[root]", "[ alice]", "[  dave]",
                "[ (unnamed)]", "[  lucy]", "[ bob]");
        assertThat(lines).containsSubsequence("[root]", "[ alice]", "[  dave]");
        assertThat(lines).containsSubsequence("[root]", "[ (unnamed)]",
                "[  lucy]");
        assertThat(lines).containsSubsequence("[root]", "[ bob]");
    }

    @Test
    public void setDataShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("initial"));
        expectImmutableException();
        //when
        immutableNode.setData("updated");
    }

    @Test
    public void createChildThrowsException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        expectImmutableException();
        //when
        immutableNode.createChild("child data", "child name");
    }

    @Test
    public void canGetChildWhenFound() {
        //given
        val root = Nodes.unnamedRoot("data");
        val child = Nodes.namedChild("child data", "child name", root);
        immutableNode = Nodes.asImmutable(root);
        //when
        val found = immutableNode.getChild("child data");
        //then
        assertThat(found.getName()).isEqualTo(child.getName());
    }

    @Test
    public void canGetChildWhenNotFound() {
        //given
        exception.expect(NodeException.class);
        exception.expectMessage("Child not found");
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("data"));
        //when
        immutableNode.getChild("child data");
    }

    @Test
    public void canSafelyHandleFindChildWhenAChildHasNoData() {
        //given
        val root = Nodes.unnamedRoot("");
        Nodes.unnamedChild(null, root);
        immutableNode = Nodes.asImmutable(root);
        //when
        immutableNode.findChild("data");
    }

    @Test
    public void createChildShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        expectImmutableException();
        //when
        immutableNode.createChild("child");
    }

    @Test
    public void createDescendantLineShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        expectImmutableException();
        //when
        immutableNode.createDescendantLine(
                Arrays.asList("child", "grandchild"));
    }

    @Test
    public void insertInPathShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        expectImmutableException();
        //when
        immutableNode.insertInPath(null, "");
    }

    @Test
    public void findOrCreateChildShouldReturnChildWhenChildIsFound() {
        //given
        val root = Nodes.unnamedRoot("");
        Nodes.namedChild("child", "child", root);
        immutableNode = Nodes.asImmutable(root);
        //when
        val found = immutableNode.findOrCreateChild("child");
        assertThat(found).extracting(Node::getName).contains("child");
    }

    @Test
    public void findOrCreateChildShouldThrowExceptionWhenChildNotFound() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        expectImmutableException();
        //when
        immutableNode.findOrCreateChild("child");
    }
}
