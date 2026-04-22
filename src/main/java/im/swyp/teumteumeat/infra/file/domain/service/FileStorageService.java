package im.swyp.teumteumeat.infra.file.domain.service;

import java.net.URL;

public interface FileStorageService {

    String generateFileKey(String fileName);

    URL getUploadUrl(String key);

    String getPublicUrl(String key);
}
