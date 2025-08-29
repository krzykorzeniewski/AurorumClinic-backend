package pl.edu.pja.aurorumclinic.features.users.services;

import pl.edu.pja.aurorumclinic.features.users.dtos.GetBasicUserInfoResponse;

public interface UserService {
    GetBasicUserInfoResponse getBasicUserInfo(Long id);
}
