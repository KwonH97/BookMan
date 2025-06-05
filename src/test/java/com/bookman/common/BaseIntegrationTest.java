package com.bookman.common;

import com.bookman.auth.util.JwtUtil;
import com.bookman.user.entity.User;
import com.bookman.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestRestTemplate
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtil jwtUtil;

    protected MockMvc mockMvc;

    protected User testUser;
    protected User testAdmin;
    protected String userToken;
    protected String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        createTestUsers();
        generateTokens();
    }

    private void createTestUsers() {
        // 테스트 일반 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .password(passwordEncoder.encode("password"))
                .fullName("테스트 사용자")
                .role(User.Role.USER)
                .isActive(true)
                .build();
        // createdAt은 @PrePersist에서 자동 설정됨
        testUser = userRepository.save(testUser);

        // 테스트 관리자 생성
        testAdmin = User.builder()
                .username("testadmin")
                .email("testadmin@test.com")
                .password(passwordEncoder.encode("password"))
                .fullName("테스트 관리자")
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();
        // createdAt은 @PrePersist에서 자동 설정됨
        testAdmin = userRepository.save(testAdmin);
    }

    private void generateTokens() {
        userToken = jwtUtil.generateToken(testUser);
        adminToken = jwtUtil.generateToken(testAdmin);
    }

    protected String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
