package com.colutti;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

public class FluxTest {

    @Test
    public void fluxSubscriber(){
        Flux<String> fluxString = Flux.just("Vinicius","Colutti","Developer","Test")
                .log();

        StepVerifier.create(fluxString)
                .expectNext("Vinicius","Colutti","Developer","Test")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        Flux<Integer> fluxNumber= Flux.range(1,5)
                .log();

        fluxNumber.subscribe(i -> System.out.println("Number: "+i));

        StepVerifier.create(fluxNumber)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList(){
        Flux<Integer> fluxList = Flux.fromIterable(List.of(1,2,3,4,5))
                .log();

        fluxList.subscribe(i -> System.out.println("Number: "+i));

        StepVerifier.create(fluxList)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError(){
        Flux<Integer> fluxNumber= Flux.range(1,5)
                .log()
                .map( i ->{
                    if(i == 4){
                        throw new IndexOutOfBoundsException("index error");
                    }
                    return i;
                });

        fluxNumber.subscribe(i -> System.out.println("Number: "+i), Throwable::printStackTrace,
                () -> System.out.println("done"), subscription -> subscription.request(3));

        StepVerifier.create(fluxNumber)
                .expectNext(1,2,3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure(){
        Flux<Integer> fluxNumber= Flux.range(1,10)
                .log();

        fluxNumber.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;
            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure(){
        Flux<Integer> fluxNumber= Flux.range(1,10)
                .log();

        fluxNumber.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;
            @Override
            protected void hookOnSubscribe(Subscription subscription){
                request(requestCount);
            }
            @Override
            protected void hookOnNext(Integer value){
                count++;
                if(count >= requestCount){
                    count = 0;
                    request(requestCount);
                }
            }
        });
    }

    @Test
    public void fluxSubscriberPrettyBackpressure(){
        Flux<Integer> fluxNumber= Flux.range(1,10)
                .log()
                .limitRate(3);

        fluxNumber.subscribe(i -> System.out.println("Number: "+i));

        StepVerifier.create(fluxNumber)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();
        interval.subscribe(i -> System.out.println(i));
        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() throws InterruptedException {
        Flux<Long> interval = createInterval();

        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .thenAwait(Duration.ofDays(2))
                .expectNext(0L)
                .expectNext(1L)
                .thenCancel()
                .verify();

    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .take(10)
                .log();
    }


}
