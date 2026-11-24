package eventmanagement.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** Thông tin người dùng lưu trong session (Serializable). Mật khẩu bị xóa sau khi xác thực. */
public class CustomUserDetails implements UserDetails, CredentialsContainer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_ORGANIZER = "ROLE_ORGANIZER";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    private final String id;
    private final String name;
    private final String email;
    private final String role;
    private String password;

    public CustomUserDetails(String id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
