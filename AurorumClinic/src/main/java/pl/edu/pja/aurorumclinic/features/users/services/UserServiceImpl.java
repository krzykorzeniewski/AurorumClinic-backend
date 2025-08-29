package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetBasicUserInfoResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public GetBasicUserInfoResponse getBasicUserInfo(Long id) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        return GetBasicUserInfoResponse.builder()
                .userId(userFromDb.getId())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();
    }
}
