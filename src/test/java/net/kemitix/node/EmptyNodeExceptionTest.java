package net.kemitix.node;

import lombok.val;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EmptyNodeException}.
 *
 * @author pcampbell
 */
public class EmptyNodeExceptionTest {

    /**
     * Test that message provided to constructor is returned.
     */
    @Test
    public void shouldReturnConstructorMessage() {
        //given
        val message = "this is the message";
        //when
        val nodeException = new EmptyNodeException(message);
        //then
        assertThat(nodeException.getMessage(), is(message));
    }

}
