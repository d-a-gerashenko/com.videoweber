package com.videoweber.server.service.communicator_service;

import com.videoweber.server.service.communicator_service.request.check_server_state.CheckServerStateProcessor;
import com.videoweber.server.service.communicator_service.request.check_server_state.CheckServerStateFactory;
import com.videoweber.internet.client.Client;
import com.videoweber.internet.client.RequestManager;
import com.videoweber.internet.client.channel_request.ChRequestManager;
import com.videoweber.internet.client.channel_request.FileEncoder;
import com.videoweber.server.service.communicator_service.request.add_channel_response.AddChannelResponseFactory;
import com.videoweber.server.service.communicator_service.request.add_channel_response.AddChannelResponseProcessor;
import com.videoweber.server.service.AppStateHolderService;
import com.videoweber.lib.app.service.ParametersService;
import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.app.service.TempDirService;
import com.videoweber.lib.engines.javacv.FfmpegFramer;
import com.videoweber.lib.framer.Framer;
import com.videoweber.server.entity.ChannelEntity;
import com.videoweber.server.entity.SampleEntity;
import com.videoweber.server.repository.ChannelRepository;
import com.videoweber.server.repository.SampleRepository;
import com.videoweber.server.service.HibernateService;
import com.videoweber.server.service.communicator_service.channel_request_processor.CheckUnrecordedSamplesRequestProcessor;
import com.videoweber.server.service.communicator_service.channel_request_processor.GetFirstSampleRequestProcessor;
import com.videoweber.server.service.communicator_service.channel_request_processor.GetFrameRequestProcessor;
import com.videoweber.server.service.communicator_service.channel_request_processor.GetLastFrameRequestProcessor;
import com.videoweber.server.service.communicator_service.channel_request_processor.GetSampleFileRequestProcessor;
import com.videoweber.server.service.communicator_service.channel_request_processor.GetSamplesRequestProcessor;
import com.videoweber.server.service.communicator_service.request.get_channel_requests.GetChannelRequestsFactory;
import com.videoweber.server.service.communicator_service.request.get_channel_requests.GetChannelRequestsProcessor;
import com.videoweber.server.service.communicator_service.request.update_server_state.UpdateServerStateFactory;
import com.videoweber.server.service.communicator_service.request.update_server_state.UpdateServerStateProcessor;
import com.videoweber.server.service.storage_service.StorageService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class CommunicatorService extends Service {

    private static final Logger LOG = Logger.getLogger(CommunicatorService.class.getName());

    private final RequestManager requestManager;

    public CommunicatorService(ServiceContainer serviceContainer) {
        super(serviceContainer);

        final ServiceContainer sc = getServiceContainer();

        FileEncoder fileEncoder = new FileEncoder(
                sc
                .getService(ParametersService.class)
                .getProperty("ENCRYPTION_KEY")
        );

        Client client = new Client(
                sc.getService(ParametersService.class).getProperty("INTERNET_SERVER_URL"),
                sc.getService(ParametersService.class).getProperty("INTERNET_SERVER_KEY")
        );

        Framer framer = new FfmpegFramer(
                sc.getService(TempDirService.class)
                .createTempDir()
        );

        // ChannelRequestManager initialization...
        ChRequestManager chRequestManager = new ChRequestManager();
        chRequestManager.registerProcessor(new GetFrameRequestProcessor(
                (ChannelRepository) sc.getService(HibernateService.class)
                .getRepository(ChannelEntity.class),
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class),
                sc.getService(StorageService.class),
                framer,
                fileEncoder
        ));
        chRequestManager.registerProcessor(new GetLastFrameRequestProcessor(
                (ChannelRepository) sc.getService(HibernateService.class)
                .getRepository(ChannelEntity.class),
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class),
                sc.getService(StorageService.class),
                framer,
                fileEncoder
        ));
        chRequestManager.registerProcessor(new GetSampleFileRequestProcessor(
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class),
                sc.getService(StorageService.class),
                fileEncoder
        ));
        chRequestManager.registerProcessor(new GetSamplesRequestProcessor(
                (ChannelRepository) sc.getService(HibernateService.class)
                .getRepository(ChannelEntity.class),
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class)
        ));
        chRequestManager.registerProcessor(new CheckUnrecordedSamplesRequestProcessor(
                (ChannelRepository) sc.getService(HibernateService.class)
                .getRepository(ChannelEntity.class),
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class)
        ));
        chRequestManager.registerProcessor(new GetFirstSampleRequestProcessor(
                (ChannelRepository) sc.getService(HibernateService.class)
                .getRepository(ChannelEntity.class),
                (SampleRepository) sc.getService(HibernateService.class)
                .getRepository(SampleEntity.class)
        ));

        // RequestManager initialization...
        requestManager = new RequestManager(client);

        requestManager.registerProcessor(new AddChannelResponseProcessor());
        requestManager.registerFactory(new AddChannelResponseFactory());

        requestManager.registerProcessor(new CheckServerStateProcessor());
        requestManager.registerFactory(new CheckServerStateFactory(
                sc.getService(AppStateHolderService.class)
        ));

        requestManager.registerProcessor(new GetChannelRequestsProcessor(
                chRequestManager
        ));
        requestManager.registerFactory(new GetChannelRequestsFactory());

        requestManager.registerProcessor(new UpdateServerStateProcessor());
        requestManager.registerFactory(new UpdateServerStateFactory(
                sc.getService(HibernateService.class),
                sc.getService(AppStateHolderService.class)
        ));
    }

    public void executeConnection() {
        try {
            requestManager.processRequest(requestManager.getFactory(CheckServerStateFactory.class).createRequest());
            requestManager.processRequest(requestManager.getFactory(GetChannelRequestsFactory.class).createRequest());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error during connection to internet server.", ex);
        }
    }

}
