package net.kemitix.node;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Test for {@link ImmutableNodeItem}.
 *
 * @author pcampbell
 */
public class ImmutableNodeItemTest {

    private Node<String> immutableNode;

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
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.setName("named"))
                .withMessage("Immutable object");
    }

    @Test
    public void rootNodeShouldHaveNoParent() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("data"));
        //then
        assertThat(immutableNode.findParent()).as("immutableNode created without a parent has no parent")
                                              .isEmpty();
    }

    @Test
    public void shouldContainImmutableCopyOfChild() {
        //given
        Node<String> parent = Nodes.unnamedRoot("root");
        Node<String> child = Nodes.namedChild("child", "child", parent);
        //when
        immutableNode = Nodes.asImmutable(parent);
        //then
        Optional<Node<String>> immutableChild =
                immutableNode.findChildByName("child");
        assertThat(immutableChild).isNotEqualTo(Optional.of(child));
        assertThat(immutableChild.map(Node::getName)).contains("child");
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
        Optional<Node<String>> foundParent =
                immutableNode.findChildByName("child")
                        .flatMap(Node::findParent);
        assertThat(foundParent).isNotEmpty();
        foundParent.ifPresent(p ->
                assertThat(p)
                        .hasFieldOrPropertyWithValue("name", "root")
                        .hasFieldOrPropertyWithValue("data", "parent"));
    }

    @Test
    public void shouldNotBeAbleToAddChildToImmutableTree() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("root"));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        Nodes.unnamedChild("child", immutableNode))
                .withMessage("Immutable object");
    }

    @Test
    public void shouldThrowExceptionWhenSetParent() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.setParent(Nodes.unnamedRoot("child")))
                .withMessage("Immutable object");
    }

    @Test
    public void shouldThrowExceptionWhenAddingChild() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.addChild(Nodes.unnamedRoot("child")))
                .withMessage("Immutable object");
    }

    @Nested
    @DisplayName("findInPath")
    public class FindInPathTests {

        /**
         * Test that we can walk a tree to the target node.
         */
        @Test
        public void shouldWalkTreeToNode() {
            //given
            val root = Nodes.unnamedRoot("root");
            Nodes.namedChild("child", "child", Nodes.unnamedChild("parent", root));
            immutableNode = Nodes.asImmutable(root);
            //when
            val result = immutableNode.findInPath(Arrays.asList("parent", "child"));
            //then
            assertThat(result.isPresent()).isTrue();
            result.map(value ->
                    assertThat(value.getName()).isEqualTo("child"));
        }
    }

    /**
     * Test that we get an empty {@link Optional} when walking a path that
     * doesn't exist.
     */
    @Test
    public void shouldNotFindNonExistentChildNode() {
        //given
        val root = Nodes.unnamedRoot("root");
        Nodes.unnamedChild("child", Nodes.unnamedChild("parent", root));
        immutableNode = Nodes.asImmutable(root);
        //when
        val result = immutableNode.findInPath(Arrays.asList("parent", "no child"));
        //then
        assertThat(result.isPresent()).isFalse();
    }

    /**
     * Test that when we pass null we get an exception.
     */
    @Test
    public void shouldThrowNEWhenWalkTreeNull() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("subject"));
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        immutableNode.findInPath(null))
                .withMessageContaining("path");
    }

    /**
     * Test that when we pass an empty path we get and empty {@link Optional} as
     * a result.
     */
    @Test
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
        result.map(resultNode ->
                assertThat(resultNode.findData())
                        .contains("child"));
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void getChildShouldThrowNPEWhenThereIsNoChild() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("data"));
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        immutableNode.findChild(null))
                .withMessageContaining("child");
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
        Optional<Node<String>> result = immutableNode.findChildByName("alpha");
        //then
        assertThat(result.map(Node::getName))
                .contains(alpha.getName());
    }

    @Test
    public void getChildNamedFindsNothing() {
        //given
        val root = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        val beta = Nodes.namedRoot("beta data", "beta");
        root.addChild(alpha);
        root.addChild(beta);
        immutableNode = Nodes.asImmutable(root);
        //then
        assertThat(immutableNode.findChildByName("gamma"))
                .isEmpty();
    }

    @Test
    public void removingParentThrowsException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.removeParent())
                .withMessage("Immutable object");
    }

    @Test
    public void findChildNamedShouldThrowNPEWhenNameIsNull() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        immutableNode.findChildByName(null))
                .withMessageContaining("name");
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
        //then
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.removeChild(null))
                .withMessage("Immutable object");
    }

    @Test
    public void setDataShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot("initial"));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.setData("updated"))
                .withMessage("Immutable object");
    }

    @Test
    public void createChildThrowsException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(null));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.createChild("child data", "child name"))
                .withMessage("Immutable object");
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
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.createChild("child"))
                .withMessage("Immutable object");
    }

    @Test
    public void createDescendantLineShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.createDescendantLine(Arrays.asList("child", "grandchild")))
                .withMessage("Immutable object");
    }

    @Test
    public void insertInPathShouldThrowException() {
        //given
        immutableNode = Nodes.asImmutable(Nodes.unnamedRoot(""));
        //when
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        immutableNode.insertInPath(null, ""))
                .withMessage("Immutable object");
    }

    @Test
    public void AsImmutableShouldThrowIAEWhenNotRoot() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() ->
                        Nodes.asImmutable(
                                Nodes.unnamedChild("child",
                                        Nodes.unnamedRoot("root"))))
                .withMessage("source must be the root node");
    }

    @Test
    public void canStreamAll() throws Exception {
        //given
        val node = Nodes.namedRoot("root", "root");
        val n1 = Nodes.namedChild("one", "one", node);
        val n2 = Nodes.namedChild("two", "two", node);
        Nodes.namedChild("three", "three", n1);
        Nodes.namedChild("four", "four", n2);
        val n5 = Nodes.namedChild("five", "five", n1);
        val n6 = Nodes.namedChild("six", "six", n2);
        Nodes.namedChild("seven", "seven", n5);
        Nodes.namedChild("eight", "eight", n6);
        val immutableRoot = Nodes.asImmutable(node);
        //when
        val result = immutableRoot.stream().collect(Collectors.toList());
        //then
        assertThat(result).as("full tree").hasSize(9);
        // and
        assertThat(immutableRoot
                .findChild("one")
                .map(Node::stream)
                .map(Stream::count)
        )
                .as("sub-tree")
                .contains(4L);
    }
}
