package org.astileanua1141.hystrix;

import com.netflix.hystrix.HystrixCommand;

public class RemoteServiceTestCommand extends HystrixCommand<String> {

    private RemoteServiceTestSimulator remoteService;

    protected RemoteServiceTestCommand(Setter setter, RemoteServiceTestSimulator remoteService) {
        super(setter);
        this.remoteService = remoteService;
    }


    @Override
    protected String run() throws Exception {
        return remoteService.execute();
    }
}
