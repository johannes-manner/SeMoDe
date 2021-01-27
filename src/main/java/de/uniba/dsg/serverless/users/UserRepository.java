package de.uniba.dsg.serverless.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findUserByUsername(String username) throws UsernameNotFoundException;
}
