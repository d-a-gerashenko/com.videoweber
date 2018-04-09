package com.videoweber.internet.client.channel_request;

import com.videoweber.lib.app.App;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class ChRequestManager {

    private final Map<String, ChRequestProcessor> processors = new HashMap<>();

    public void registerProcessor(ChRequestProcessor processor) {
            if (processors.containsKey(processor.getCommand())) {
                throw new RuntimeException(
                        String.format(
                                "Processor for \"%s\" already registered.",
                                processor.getCommand()
                        )
                );
            }
            processors.put(processor.getCommand(), processor);
    }

    public ChResponse process(ChRequest chRequest) {
        ChRequestProcessor processor = processors.get(chRequest.getCommand());
        if (processor == null) {
            throw new RuntimeException(
                    String.format(
                            "Processor for \"%s\" isn't registered.",
                            chRequest.getCommand()
                    )
            );
        }

        try {
            return processor.process(chRequest);
        } catch (Exception ex) {
            return new ChResponse(
                    ChResponse.Status.ERROR,
                    App.versionInfo() + " can't process request: " + ex
            );
        }
    }
}
