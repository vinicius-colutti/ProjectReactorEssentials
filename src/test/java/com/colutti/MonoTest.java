package com.colutti;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

public class MonoTest {

    @Test
    public void monoSubscriber(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe();

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> System.out.println("Value: "+ s));

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});
        mono.subscribe(s -> System.out.println("Value: "+ s), s -> System.out.println("Error: "+ s));

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> System.out.println("Value: "+ s), Throwable::printStackTrace,
                () -> System.out.println("FINISHED"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> System.out.println("Value: "+ s), Throwable::printStackTrace,
                () -> System.out.println("FINISHED"),
                subscription -> subscription.request(5));
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        String name = "Vinicius Colutti";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> System.out.println("Subscribed "+ subscription))
                .doOnRequest(longNumber -> System.out.println("Request Received, starting actions..."))
                .doOnNext(s -> System.out.println("Value is here. Executing doOnNext "+ s))
                .doOnSuccess(s -> System.out.println("doOnSuccess executed"));

        mono.subscribe(s -> System.out.println("Value: "+ s), Throwable::printStackTrace,
                () -> System.out.println("FINISHED"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethodsWithFlatMap(){
        String name = "Vinicius Colutti";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> System.out.println("Subscribed "+ subscription))
                .doOnRequest(longNumber -> System.out.println("Request Received, starting actions..."))
                .doOnNext(s -> System.out.println("Value is here. Executing doOnNext "+ s))
                .flatMap(s -> Mono.empty())
                .doOnSuccess(s -> System.out.println("doOnSuccess executed"));

        mono.subscribe(s -> System.out.println("Value: "+ s), Throwable::printStackTrace,
                () -> System.out.println("FINISHED"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> System.out.println("Error message "+ e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        String name = "Vinicius Colutti";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> System.out.println("Error message "+ e.getMessage()))
                .onErrorResume(s -> {
                    System.out.println("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> System.out.println("Error message "+ e.getMessage()))
                .onErrorReturn("EMPTY")
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }

}
