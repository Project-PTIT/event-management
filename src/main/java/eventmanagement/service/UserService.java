package eventmanagement.service;

import eventmanagement.dto.ChangePasswordRequest;
import eventmanagement.dto.UpdateProfileRequest;
import eventmanagement.dto.UserProfileResponse;
import eventmanagement.entity.Customer;
import eventmanagement.entity.Organizer;
import eventmanagement.entity.People;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.PeopleRepository;
import eventmanagement.security.CustomUserDetails;
import eventmanagement.security.CustomUserDetailsService;
import eventmanagement.util.PasswordRules;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final PeopleRepository peopleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(PeopleRepository peopleRepository, PasswordEncoder passwordEncoder) {
        this.peopleRepository = peopleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        return toResponse(findPerson(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        People person = findPerson(userId);

        person.setName(request.getName().trim());
        person.setPhoneNumber(request.getPhoneNumber().trim());

        if (person instanceof Customer customer) {
            if (request.getGender() == null || request.getDateOfBirth() == null) {
                throw new BadRequestException("Vui lòng nhập giới tính và ngày sinh.");
            }
            customer.setGender(request.getGender());
            customer.setDateOfBirth(request.getDateOfBirth());
        } else if (person instanceof Organizer organizer) {
            if (request.getAddress() == null || request.getAddress().isBlank()) {
                throw new BadRequestException("Vui lòng nhập địa chỉ.");
            }
            organizer.setAddress(request.getAddress().trim());
        }

        return toResponse(peopleRepository.save(person));
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        People person = findPerson(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), person.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng.");
        }
        PasswordRules.validate(request.getNewPassword());
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        person.setPassword(passwordEncoder.encode(request.getNewPassword()));
        peopleRepository.save(person);
    }

    /** Admin: danh sách người dùng, lọc theo role (ADMIN / ORGANIZER / CUSTOMER, có thể kèm tiền tố ROLE_). */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listUsers(String roleFilter) {
        String wanted = normalizeRole(roleFilter);
        return peopleRepository.findAll(Sort.by("id")).stream()
                .filter(p -> wanted == null || wanted.equals(CustomUserDetailsService.roleOf(p)))
                .map(this::toResponse)
                .toList();
    }

    public UserProfileResponse toResponse(People person) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmail(person.getEmail());
        response.setPhoneNumber(person.getPhoneNumber());
        response.setRole(CustomUserDetailsService.roleOf(person));

        if (person instanceof Customer customer) {
            response.setGender(customer.getGender());
            response.setDateOfBirth(customer.getDateOfBirth());
        } else if (person instanceof Organizer organizer) {
            response.setTaxCode(organizer.getTaxCode());
            response.setAddress(organizer.getAddress());
        }
        return response;
    }

    private People findPerson(String userId) {
        return peopleRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String value = role.trim().toUpperCase();
        if (!value.startsWith("ROLE_")) {
            value = "ROLE_" + value;
        }
        if (!value.equals(CustomUserDetails.ROLE_ADMIN)
                && !value.equals(CustomUserDetails.ROLE_ORGANIZER)
                && !value.equals(CustomUserDetails.ROLE_CUSTOMER)) {
            throw new BadRequestException("Role không hợp lệ. Chỉ nhận ADMIN, ORGANIZER hoặc CUSTOMER.");
        }
        return value;
    }
}
