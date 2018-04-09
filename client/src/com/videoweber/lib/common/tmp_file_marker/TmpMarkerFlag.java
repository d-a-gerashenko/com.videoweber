package com.videoweber.lib.common.tmp_file_marker;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TmpMarkerFlag implements Serializable {

    private final UUID sessionUuid = TmpMarkers.SESSION_UUID;

    public UUID getSessionUuid() {
        return sessionUuid;
    }
}
