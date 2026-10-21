package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
