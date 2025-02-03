package ir.piana.dev.inquiry.publisher.security.authentication.manager;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

//@RequiredArgsConstructor
public class PublisherAuthenticationManager implements AuthenticationManager {
//    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public PublisherAuthenticationManager(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    //    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        /*Optional<User> user = userRepo.findByUserName(authentication.getName());
        if (user.isPresent()) {
            if (passwordEncoder.matches(authentication.getCredentials().toString(), user.get().getPassword())) {
                List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
                for (Role role : user.get().getRoleSet()) {
                    grantedAuthorityList.add(new SimpleGrantedAuthority(role.getName()));
                }
                return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), grantedAuthorityList);
            } else {
                throw new BadCredentialsException("Wrong Password");
            }
        } else {
            throw new BadCredentialsException("Wrong UserName");
        }*/
        return null;
    }
}
