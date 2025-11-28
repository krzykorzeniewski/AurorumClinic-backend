package pl.edu.pja.aurorumclinic.test_config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import software.amazon.awssdk.services.s3.S3Client;

public abstract class IntegrationTest {

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean
    S3Client s3Client;

    @MockitoBean
    SmsService smsService;

    @MockitoBean
    OpenAiChatModel openAiChatModel;

    @MockitoBean
    ObjectStorageService objectStorageService;
}
