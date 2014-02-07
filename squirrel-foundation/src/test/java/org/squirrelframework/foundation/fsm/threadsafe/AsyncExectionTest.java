package org.squirrelframework.foundation.fsm.threadsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.AnonymousUntypedAction;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.OnTransitionDecline;

public class AsyncExectionTest {
    
    private UntypedStateMachineBuilder builder = null;
    
    @Before
    public void setUp() throws Exception {
        builder = StateMachineBuilderFactory.create(ConcurrentSimpleStateMachine.class);
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testTimedState() {
        final StringBuilder logger = new StringBuilder();
        // timed state must be defined before transition
        builder.defineTimedState("A", 4, 10, "FIRST", null);
        builder.internalTransition().within("A").on("FIRST").perform(new AnonymousUntypedAction() {
            @Override
            public void execute(Object from, Object to, Object event,
                    Object context, UntypedStateMachine stateMachine) {
                if (logger.length() > 0) {
                    logger.append('.');
                }
                logger.append("AToBOnFIRST");
            }
        });
        builder.transition().from("A").to("B").on("SECOND");
        final UntypedStateMachine fsm = builder.newStateMachine("A");
        fsm.addDeclarativeListener(new Object() {
            @OnTransitionDecline
            public void onTransitionDeclined() {
                fail();
            }
        });
        fsm.start();
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
        }
        fsm.fire("SECOND");
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
        }
        fsm.terminate();
        assertEquals("AToBOnFIRST.AToBOnFIRST.AToBOnFIRST.AToBOnFIRST.AToBOnFIRST", logger.toString());
    }
    
    @Test
    public void testAsyncMethodCall() {
        builder.transition().from("A").to("B").on("FIRST").callMethod("fromAToB");
        builder.transition().from("B").to("C").on("SECOND").callMethod("fromBToC");
        final ConcurrentSimpleStateMachine fsm = builder.newUntypedStateMachine("A");
        fsm.fire("FIRST");
        assertEquals("C", fsm.getCurrentState());
        assertEquals("fromAToB.fromBToC", fsm.logger.toString());
        assertTrue(Thread.currentThread()!=fsm.fromAToBCallThread);
        assertTrue(Thread.currentThread()==fsm.fromBToCCallThread);
    }
    
    @Test(timeout=1000)
    public void testTimedoutActionCall() {
        builder.transition().from("A").to("B").on("FIRST").perform(new AnonymousUntypedAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context,
                    UntypedStateMachine stateMachine) {
                try {
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                }
            }
            
            @Override
            public long timeout() {
                return 10;
            }
            
            @Override
            public boolean isAsync() {
                return true;
            }
        });
        
        final ConcurrentSimpleStateMachine fsm = builder.newUntypedStateMachine("A");
        try {
            fsm.fire("FIRST");
        } catch(TransitionException e) {
            assertTrue(e.getTargetException().getClass()==TimeoutException.class);
            return;
        }
        fail();
    }
    
    @Test
    public void testAsyncActionException() {
        final String errMsg = "This exception is thrown on purpse.";
        builder.transition().from("A").to("B").on("FIRST").perform(new AnonymousUntypedAction() {
            @Override
            public void execute(Object from, Object to, Object event, Object context,
                    UntypedStateMachine stateMachine) {
                throw new IllegalArgumentException(errMsg);
            }
            
            @Override
            public boolean isAsync() {
                return true;
            }
        });
        
        final ConcurrentSimpleStateMachine fsm = builder.newUntypedStateMachine("A");
        try {
            fsm.fire("FIRST");
        } catch(TransitionException e) {
            assertTrue(e.getTargetException().getClass()==IllegalArgumentException.class);
            assertTrue(e.getTargetException().getMessage()==errMsg);
            return;
        }
        fail();
    }
}