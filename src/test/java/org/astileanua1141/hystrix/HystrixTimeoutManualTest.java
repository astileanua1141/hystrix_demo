package org.astileanua1141.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class HystrixTimeoutManualTest {

    public String invokeRemoteService(HystrixCommand.Setter config, int timeout) throws InterruptedException {
        String response = null;

        try {
            response = new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(timeout)).execute();
        } catch (HystrixRuntimeException ex) {
            System.out.println("ex = " + ex);
        }

        return response;
    }

    @Test
    public void givenInputBobAndDefaultSettings_whenCommandExecuted_thenReturnHelloBob() {
        assertThat(new CommandHelloWorld("Bobby").execute(), equalTo("Hello Bobby"));
    }

    @Test
    public void givenSvcTimeoutOf100AndDefaultSettings_whenRemoteSvcExecuted_thenReturnSuccess()
            throws InterruptedException {

        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup"));

        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(100)).execute(),
                equalTo("Success!"));
    }

    @Test
    public void
    givenSvcTimeoutOf5000AndExecTimeoutOf10000_whenRemoteSvcExecuted_thenReturnSuccess()
            throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"));

        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(10_000);
        config.andCommandPropertiesDefaults(commandProperties);

        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                equalTo("Success!"));
    }

    @Test(expected = HystrixRuntimeException.class)
    public void givenSvcTimeoutOf15000AndExecTimeoutOf5000_whenRemoteEvcExecuted_thenExpectHre() throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup3"));

        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(5_000);
        config.andCommandPropertiesDefaults(commandProperties);

        new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(15_000)).execute();
    }

    @Test
    public void
    givenSvcTimeoutOf5000AndExecTimeoutOf10000AndThreadPool_whenRemoteSvcExecuted_thenReturnSuccess()
            throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"));

        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(10_000);
        config.andCommandPropertiesDefaults(commandProperties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(10)
                .withCoreSize(3)
                .withQueueSizeRejectionThreshold(10));

        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
                equalTo("Success!"));
    }

    @Test
    public void givenCircuitBreakerSetup_whenRemoteSvcCmdExecuted_thenReturnSuccess() throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"));

        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        properties.withExecutionTimeoutInMilliseconds(1000);
        // Circuit Breaker Sleep Window - the time interval after which the request to the remote service will be resumed
        properties.withCircuitBreakerSleepWindowInMilliseconds(4000);
        properties.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withCircuitBreakerEnabled(true);
        //Circuit Breaker Volume Threshold - minimum number of requests needed before the failure rate will be considered
        properties.withCircuitBreakerRequestVolumeThreshold(1);

        config.andCommandPropertiesDefaults(properties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(1)
                .withCoreSize(1)
                .withQueueSizeRejectionThreshold(1));

        assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));
        assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));
        assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));

        Thread.sleep(5000);

        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(), equalTo("Success!"));
        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(), equalTo("Success!"));
        assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(), equalTo("Success!"));

    }


}