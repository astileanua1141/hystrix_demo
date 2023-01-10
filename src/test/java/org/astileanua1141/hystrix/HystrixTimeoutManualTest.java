package org.astileanua1141.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class HystrixTimeoutManualTest {

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
                equalTo("Success"));
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
}