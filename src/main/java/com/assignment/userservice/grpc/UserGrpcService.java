
package com.assignment.userservice.grpc;

import com.assignment.protos.user.v1.GetUserRequest;
import com.assignment.protos.user.v1.ListUsersRequest;
import com.assignment.protos.user.v1.ListUsersResponse;
import com.assignment.protos.user.v1.UserResponse;
import com.assignment.protos.user.v1.UserRole;
import com.assignment.protos.user.v1.UserServiceGrpc;
import com.assignment.userservice.entity.UserEntity;
import com.assignment.userservice.exception.DuplicateEmailException;
import com.assignment.userservice.exception.UserNotFoundException;
import com.assignment.userservice.service.UserService;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void createUser(
            com.assignment.protos.user.v1.CreateUserRequest request,
            StreamObserver<UserResponse> responseObserver) {
        try {
            com.assignment.userservice.dto.CreateUserRequest createRequest =
                    new com.assignment.userservice.dto.CreateUserRequest();

            createRequest.setFullName(request.getFullName());
            createRequest.setEmail(request.getEmail());

            UserEntity user = userService.createUser(createRequest);

            responseObserver.onNext(toResponse(user));
            responseObserver.onCompleted();
        } catch (DuplicateEmailException exception) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS
                            .withDescription("A user with this email already exists")
                            .asRuntimeException());
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(exception.getMessage())
                            .asRuntimeException());
        } catch (Exception exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unable to create user")
                            .asRuntimeException());
        }
    }

    @Override
    public void getUser(
            GetUserRequest request,
            StreamObserver<UserResponse> responseObserver) {
        try {
            if (request.getUserId() <= 0) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("User ID must be positive")
                                .asRuntimeException());
                return;
            }

            UserEntity user = userService.getUserById(request.getUserId());

            responseObserver.onNext(toResponse(user));
            responseObserver.onCompleted();
        } catch (UserNotFoundException exception) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found")
                            .asRuntimeException());
        } catch (Exception exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unable to retrieve user")
                            .asRuntimeException());
        }
    }

    @Override
    public void listUsers(
            ListUsersRequest request,
            StreamObserver<ListUsersResponse> responseObserver) {
        try {
            List<UserEntity> users = userService.getAllUsers();

            ListUsersResponse.Builder response = ListUsersResponse.newBuilder();
            for (UserEntity user : users) {
                response.addUsers(toResponse(user));
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unable to retrieve users")
                            .asRuntimeException());
        }
    }

    private UserResponse toResponse(UserEntity user) {
        UserRole role = user.getRole()
                == com.assignment.userservice.enums.UserRole.ADMIN
                ? UserRole.USER_ROLE_ADMIN
                : UserRole.USER_ROLE_GUEST;

        return UserResponse.newBuilder()
                .setId(user.getId())
                .setFullName(user.getFullName())
                .setEmail(user.getEmail())
                .setRole(role)
                .setCreatedAt(toTimestamp(user.getCreatedAt()))
                .setUpdatedAt(toTimestamp(user.getUpdatedAt()))
                .build();
    }

    private Timestamp toTimestamp(java.time.LocalDateTime dateTime) {
        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }
}
