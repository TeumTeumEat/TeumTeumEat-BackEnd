package im.swyp.teumteumeat.global.config.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${infra.fcm.key-path}")
    private Resource fcmKeyResource;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (!fcmKeyResource.exists()) {
                    throw new RuntimeException("FCM Key file not found. keyPath: " + fcmKeyResource.getURI());
                }

                try (InputStream serviceAccount = fcmKeyResource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                }
            }
            return FirebaseMessaging.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
