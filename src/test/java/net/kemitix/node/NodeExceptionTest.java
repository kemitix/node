package net.kemitix.node;

import net.kemitix.node.NodeException;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link NodeException}.
 *
 * @author pcampbell
 */
public class NodeExceptionTest {

    /**
     * Class under test.
     */
    private NodeException nodeException;

    /**
     * Test that message provided to constructor is returned.
     */
    @Test
    public void shouldReturnConstructorMessage() {
        //given
        final String message = "this is the message";
        //when
        nodeException = new NodeException(message);
        //then
        assertThat(nodeException.getMessage(), is(message));
    }

}
