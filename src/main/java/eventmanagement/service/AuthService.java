package eventmanagement.service;

import eventmanagement.dto.LoginRequest;
import eventmanagement.dto.RegisterCustomerRequest;
import eventmanagement.dto.RegisterOrganizerRequest;
import eventmanagement.dto.UserProfileResponse;
import eventmanagement.entity.Customer;
import eventmanagement.entity.Organizer;
import eventmanagement.exception.BadRequestException;
import eventmanagement.repository.CustomerRepository;
import eventmanagement.repository.OrganizerRepository;
import eventmanagement.repository.PeopleRepository;
import eventmanagement.security.CustomUserDetails;
import eventmanagement.util.IdGenerator;
import eventmanagement.util.PasswordRules;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final PeopleRepository peopleRepository;
    private final CustomerRepository customerRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthService(PeopleRepository peopleRepository, CustomerRepository customerRepository, OrganizerRepository organizerRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, UserService userService) {
        this.peopleRepository = peopleRepository;
        this.customerRepository = customerRepository;
        this.organizerRepository = organizerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    /**
     * Tạo people + customer trong cùng một transaction; lỗi ở bước nào cũng rollback cả hai.
     */
    @Transactional
    public UserProfileResponse registerCustomer(RegisterCustomerRequest request) {
        String email = normalizeEmail(request.getEmail());
        ensureEmailAvailable(email);
        PasswordRules.validate(request.getPassword());

        Customer customer = new Customer();
        customer.setId(IdGenerator.nextId("KH", 3));
        customer.setName(request.getName().trim());
        customer.setEmail(email);
        customer.setPhoneNumber(request.getPhoneNumber().trim());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setGender(request.getGender());
        customer.setDateOfBirth(request.getDateOfBirth());

        return userService.toResponse(customerRepository.save(customer));
    }

    /**
     * Tạo people + organizer trong cùng một transaction.
     */
    @Transactional
    public UserProfileResponse registerOrganizer(RegisterOrganizerRequest request) {
        String email = normalizeEmail(request.getEmail());
        String taxCode = request.getTaxCode().trim();
        ensureEmailAvailable(email);
        if (organizerRepository.existsByTaxCode(taxCode)) {
            throw new BadRequestException("Mã số thuế đã được đăng ký.");
        }
        PasswordRules.validate(request.getPassword());

        Organizer organizer = new Organizer();
        organizer.setId(IdGenerator.nextId("TC", 3));
        organizer.setName(request.getName().trim());
        organizer.setEmail(email);
        organizer.setPhoneNumber(request.getPhoneNumber().trim());
        organizer.setPassword(passwordEncoder.encode(request.getPassword()));
        organizer.setTaxCode(taxCode);
        organizer.setAddress(request.getAddress().trim());

        return userService.toResponse(organizerRepository.save(organizer));
    }

    /**
     * Xác thực bằng Spring Security rồi lưu SecurityContext vào session.
     * Sai email hoặc mật khẩu -> BadCredentialsException -> GlobalExceptionHandler trả 401.
     */
    public UserProfileResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail().trim(), request.getPassword()));

        // Chống session fixation: đổi session id nếu đã có session trước khi đăng nhập
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return userService.getProfile(principal.getId());
    }

    private void ensureEmailAvailable(String email) {
        if (peopleRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email đã được sử dụng.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
