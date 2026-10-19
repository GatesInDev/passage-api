package br.com.passage.api.iam.internal.service.auth;

import br.com.passage.api.iam.dto.auth.LoginRequest;
import br.com.passage.api.iam.dto.auth.TokenResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request)
    {
        try
        {
            var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication authentication = authenticationManager.authenticate(authToken);

            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);

            return new TokenResponse(token, "Bearer", jwtService.getExpirationInSeconds());
        } catch (BadCredentialsException ex)
        {
            throw new BusinessException("Credenciais inválidas: e-mail ou senha incorretos.");
        }
    }
}