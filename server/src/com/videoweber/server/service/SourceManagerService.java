package com.videoweber.server.service;

import com.videoweber.lib.app.service.Service;
import com.videoweber.lib.app.service.ServiceContainer;
import com.videoweber.lib.channel.Source;
import com.videoweber.lib.channel.sources.Rtsp;
import com.videoweber.server.entity.SourceEntity;
import com.videoweber.server.entity.SourceRtspEntity;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SourceManagerService extends Service{

    public SourceManagerService(ServiceContainer serviceContainer) {
        super(serviceContainer);
    }

    public Source createSource(SourceEntity sourceEntity) {
        if (sourceEntity == null) {
            throw new NullPointerException();
        }
        if (sourceEntity instanceof SourceRtspEntity) {
            SourceRtspEntity sourceRtspEntity = (SourceRtspEntity)sourceEntity;
            return new Rtsp(sourceRtspEntity.getMediaType(), sourceRtspEntity.getUri());
        }
        throw new RuntimeException("Unsupported SourceEntity class: " + sourceEntity.getClass().getName());
    }
}
