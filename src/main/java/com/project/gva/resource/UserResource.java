package com.project.gva.resource;

import com.project.gva.entity.AuthToken;
import com.project.gva.entity.LoginFail;
import com.project.gva.entity.UserEntity;
import com.project.gva.exception.NotFoundException;
import com.project.gva.model.Response;
import com.project.gva.model.Types;
import com.project.gva.repository.AuthRepository;
import com.project.gva.repository.LoginFailRepository;
import com.project.gva.repository.UserRepository;
import com.project.gva.service.message.MessageService;
import lombok.Data;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "user")
public class UserResource {

    private final
    UserRepository userRepository;

    private
    Response response;

    private final
    AuthRepository authRepository;

    private final
    MessageService messageService;

    private final
    LoginFailRepository loginFailRepository;

    @Value(value = "${app.sms.receiver}")
    private String SMS_RECEIVER;

    @Autowired
    public UserResource(UserRepository userRepository, AuthRepository authRepository, MessageService messageService, LoginFailRepository loginFailRepository) {
        response = new Response();
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.messageService = messageService;
        this.loginFailRepository = loginFailRepository;
    }

    @GetMapping
    public ResponseEntity getUser(String email) {
        UserEntity user = this.userRepository.findByEmailEqualsAndBlockedUntilLessThanEqual(email, new Date());
        if (Objects.isNull(user))
            response = Response.builder().message("NOT USER FOUND WITH GIVEN EMAIL: " + email).content(null).status(Response.HttpStatus.NOT_FOUND).build();
        else response = response.of(user);
        return ResponseEntity.status(response.getStatus().code()).body(response);
    }

    @PostMapping(value = "auth")
    public ResponseEntity auth(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        UserEntity user = this.userRepository.findByEmailEqualsAndBlockedUntilLessThanEqual(loginRequest.getUsername(), new Date());
        String host = request.getRemoteHost();
        if (Objects.nonNull(user) && BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            AuthToken token = this.authRepository.findByUserEqualsAndValidEquals(user, true);
            user.setConnectionDate(new Date());
            this.userRepository.save(user);
            if (Objects.isNull(token)) {
                token = AuthToken.builder().token(UUID.randomUUID().toString()).created(new Date()).user(user).valid(true).build();
                this.authRepository.save(token);
            }
            token.setCode((new Random()).nextInt(999999));
            String message = String.format("SU CÓDIGO DE ACCESO ES: %s, FECHA: %s", token.getCode(), (new Date()).toString());
            this.messageService.sendSms(user.getPhone(), message, host, Types.MessageTopic.PROCESO_LOGIN, Types.MessageStatus.SUCCESS, user.getId().toString());
            return ResponseEntity.ok(token);
        } else if (Objects.nonNull(user)) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.MINUTE, -3);
            List<LoginFail> fails = this.loginFailRepository.findAllByIpEqualsAndDateGreaterThanEqual(host, date.getTime());
            if (fails.size() >= 3) {
                date = Calendar.getInstance();
                date.add(Calendar.MINUTE, 30);
                user.setBlockedUntil(date.getTime());
                this.userRepository.saveAndFlush(user);
                String message = String.format("SE HA BLOQUEADO EL ACCESO A LA CUENTA: %s, HASTA: %s", loginRequest.getUsername(), date.getTime().toString());
                this.messageService.sendSms(user.getPhone(), message, host, Types.MessageTopic.PROCESO_LOGIN, Types.MessageStatus.WARNING, loginRequest.username);
            }
            LoginFail loginFail = LoginFail
                    .builder()
                    .date(new Date())
                    .ip(host)
                    .user(user)
                    .build();
            this.loginFailRepository.saveAndFlush(loginFail);
        }
        response = Response.builder().message("USUARIO O CONTRASEÑA INCORRECTOS: " + loginRequest.username).status(Response.HttpStatus.UNAUTHORIZED).build();
        String message = String.format("LOGIN FALLIDO, PARA EL USUARIO: %s, FECHA: %s", loginRequest.getUsername(), (new Date()).toString());
        this.messageService.sendSms(SMS_RECEIVER, message, host, Types.MessageTopic.PROCESO_LOGIN, Types.MessageStatus.ERROR, loginRequest.username);
        return ResponseEntity.status(response.getStatus().code()).body(response);
    }

    @GetMapping(value = "valid")
    public ResponseEntity valid(@RequestHeader(name = "X-AUTH-TOKEN") String accessToken) {
        AuthToken token = this.authRepository.findByTokenEqualsAndValidEquals(accessToken, true);
        if (Objects.nonNull(token)) {
            return ResponseEntity.ok(token);
        }
        response = Response.builder().message("TOKEN INVALIDO: " + accessToken).status(Response.HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(response.getStatus().code()).body(response);
    }

    @PostMapping(value = "logout")
    public ResponseEntity logout(@RequestHeader(name = "X-AUTH-TOKEN") String accessToken) {
        AuthToken token = this.authRepository.findById(accessToken).orElseThrow(NotFoundException::new);
        token.setValid(false);
        this.authRepository.save(token);
        return ResponseEntity.ok(token);
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;
    }
}
