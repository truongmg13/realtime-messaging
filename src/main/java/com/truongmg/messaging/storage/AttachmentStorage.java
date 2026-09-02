package com.truongmg.messaging.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface AttachmentStorage {
    String store(InputStream in, UUID id, String filename) throws IOException;
}
