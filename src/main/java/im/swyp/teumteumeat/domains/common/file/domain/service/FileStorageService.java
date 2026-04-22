package im.swyp.teumteumeat.domains.common.file.domain.service;

import java.net.URL;

public interface FileStorageService {

    String generateFileKey(String fileName);

    URL getUploadUrl(String key);

    String getPublicUrl(String key);
}
