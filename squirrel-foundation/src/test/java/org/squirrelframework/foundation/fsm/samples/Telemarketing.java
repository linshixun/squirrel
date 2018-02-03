package org.squirrelframework.foundation.fsm.samples;

import org.apache.commons.lang3.RandomStringUtils;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Random;

public class Telemarketing {

    // 1. Define State Machine Event
    enum FSMEvent {
        无声, 拒绝, 应答, 没听清, 疑问, 同意
    }

    // 2. Define State Machine Class
    @StateMachineParameters(stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {


        protected void 介绍话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }

        protected void 邀约话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }

        protected void 结束话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }
        protected void 挽回1话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }

        protected void QA1话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }

        protected void 挽回2话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }

        protected void QA2话术(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }
    }

    public static void main(String[] args) {
        // 3. Build State Transitions
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        builder.externalTransition().from("打招呼").to("介绍").on(FSMEvent.应答);


        builder.externalTransition().from("打招呼").to("打招呼").on(FSMEvent.无声);


        builder.externalTransition().from("介绍").to("邀约").on(FSMEvent.同意);
        builder.externalTransition().from("介绍").to("介绍").on(FSMEvent.没听清);
        builder.externalTransition().from("介绍").to("挽回1").on(FSMEvent.拒绝);


        builder.externalTransition().from("挽回1").to("挽回1").on(FSMEvent.拒绝);

        builder.externalTransition().from("挽回1").to("介绍").on(FSMEvent.同意);

        builder.externalTransition().from("介绍").to("QA库1").on(FSMEvent.疑问);
        builder.externalTransition().from("QA库1").to("QA库1").on(FSMEvent.疑问);

        builder.externalTransition().from("QA库1").to("介绍").on(FSMEvent.无声);


        builder.externalTransition().from("邀约").to("结束").on(FSMEvent.同意);
        builder.externalTransition().from("邀约").to("邀约").on(FSMEvent.没听清);
        builder.externalTransition().from("邀约").to("QA库2").on(FSMEvent.疑问);
        builder.externalTransition().from("QA库2").to("QA库2").on(FSMEvent.疑问);

        builder.externalTransition().from("QA库2").to("邀约").on(FSMEvent.无声);
        builder.externalTransition().from("邀约").to("挽回2").on(FSMEvent.拒绝);

        builder.externalTransition().from("挽回2").to("挽回2").on(FSMEvent.拒绝);

        builder.externalTransition().from("挽回2").to("邀约").on(FSMEvent.同意);

        builder.onEntry("介绍").callMethod("介绍话术");
        builder.onEntry("邀约").callMethod("邀约话术");
        builder.onEntry("结束").callMethod("结束话术");
        builder.onEntry("挽回1").callMethod("挽回1话术");
        builder.onEntry("QA库1").callMethod("QA1话术");
        builder.onEntry("挽回2").callMethod("挽回2话术");
        builder.onEntry("QA库2").callMethod("QA2话术");
        // 4. Use State Machine
        UntypedStateMachine fsm = builder.newStateMachine("打招呼");


        int length = FSMEvent.values().length;
        fsm.start();
        while (!fsm.getCurrentState().equals("结束")){

            FSMEvent x = FSMEvent.values()[new Random().nextInt(length)];
            System.out.println(x);

            fsm.fire(x);
            System.out.println("Current state is " + fsm.getCurrentState());
        }

    }
}
