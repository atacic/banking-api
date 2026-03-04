package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.event.NotificationEvent;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.RoleRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.TransferServiceImpl;
import com.aleksa.banking_api.service.impl.UserServiceImpl;
import com.aleksa.banking_api.service.impl.notification.NotificationConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class NotificationIntegrationIT extends IntegrationTestBase {

    @Autowired
    private TransferServiceImpl transferService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoSpyBean
    private NotificationConsumer notificationConsumer;

    @Test
    void shouldSendAndConsumeNotificationAfterUserRegistration() {

        // Given
        roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(RoleName.ROLE_USER);
                    return roleRepository.save(newRole);
                });
        RegisterRequest request = new RegisterRequest("newuser@test.com", "pass123", "New User", RoleName.ROLE_USER);

        // When
        userService.registerUser(request);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
            verify(notificationConsumer, atLeastOnce()).consumeEmailNotification(captor.capture());

            boolean notified = captor.getAllValues().stream()
                    .anyMatch(event -> event.recipientEmail().equals("newuser@test.com") && event.type() == NotificationEvent.EventType.USER_REGISTRATION);

            assertThat(notified).isTrue();
        });
    }

    @Test
    @WithMockUser(username = "sender@test.com", roles = "USER")
    void shouldSendAndConsumeNotificationAfterTransfer() {

        // Given
        User senderUser = userRepository.save(new User(LocalDateTime.now(), null, "sender@test.com", "pass", "Sender", LocalDateTime.now(), UserStatus.ACTIVE));
        User receiverUser = userRepository.save(new User(LocalDateTime.now(), null, "receiver@test.com", "pass", "Receiver", LocalDateTime.now(), UserStatus.ACTIVE));

        Account senderAccount = accountRepository.save(new Account(BigDecimal.valueOf(1000), senderUser, "ACC-SENDER", "EUR", AccountStatus.ACTIVE, null));
        Account receiverAccount = accountRepository.save(new Account(BigDecimal.valueOf(500), receiverUser, "ACC-RECEIVER", "EUR", AccountStatus.ACTIVE, null));

        TransferCreateRequest request = new TransferCreateRequest(
                senderAccount.getAccountNumber(),
                receiverAccount.getAccountNumber(),
                BigDecimal.valueOf(100),
                "Gift"
        );

        // When
        transferService.createTransfer(request);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
            verify(notificationConsumer, atLeastOnce()).consumeEmailNotification(captor.capture());
            
            boolean senderNotified = captor.getAllValues().stream()
                    .anyMatch(event -> event.recipientEmail().equals("sender@test.com") && event.type() == NotificationEvent.EventType.TRANSFER_SENT);
            boolean receiverNotified = captor.getAllValues().stream()
                    .anyMatch(event -> event.recipientEmail().equals("receiver@test.com") && event.type() == NotificationEvent.EventType.TRANSFER_RECEIVED);
            
            assertThat(senderNotified).isTrue();
            assertThat(receiverNotified).isTrue();
        });
    }
}
