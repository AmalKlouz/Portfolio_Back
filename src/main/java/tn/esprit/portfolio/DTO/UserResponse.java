package tn.esprit.portfolio.DTO;

// UserResponse.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.portfolio.Entity.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
}