package eventmanagement.security;

import eventmanagement.entity.Admin;
import eventmanagement.entity.Customer;
import eventmanagement.entity.Organizer;
import eventmanagement.entity.People;
import eventmanagement.repository.PeopleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Nạp tài khoản từ people + admin/organizer/customer. Role lấy từ bảng vai trò thật trong database. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PeopleRepository peopleRepository;

    public CustomUserDetailsService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email == null ? "" : email.trim();
        People person = peopleRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));

        String role = roleOf(person);
        if (role == null) {
            // Có trong people nhưng không thuộc admin/organizer/customer: không cho đăng nhập
            throw new UsernameNotFoundException("Tài khoản chưa được gán vai trò.");
        }
        return new CustomUserDetails(person.getId(), person.getName(), person.getEmail(),
                person.getPassword(), role);
    }

    public static String roleOf(People person) {
        if (person instanceof Admin) {
            return CustomUserDetails.ROLE_ADMIN;
        }
        if (person instanceof Organizer) {
            return CustomUserDetails.ROLE_ORGANIZER;
        }
        if (person instanceof Customer) {
            return CustomUserDetails.ROLE_CUSTOMER;
        }
        return null;
    }
}
