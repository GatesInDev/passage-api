package br.com.passage.api.iam.internal.config;

import br.com.passage.api.iam.internal.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro responsável por autenticar as requisições a partir do token JWT.
 *
 * <p>Lê o token do cabeçalho {@code Authorization: Bearer}, valida a assinatura e a
 * expiração e registra o usuário no contexto de segurança. Requisições sem token seguem
 * sem autenticação e são barradas pelas regras do {@link SecurityConfig}.</p>
 *
 * <p>O usuário é recarregado do banco a cada requisição. Usuários inativos ou excluídos
 * não são autenticados, mesmo com um token ainda válido.</p>
 *
 * @see JwtService
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Autentica a requisição quando ela possui um token válido.
     *
     * @param request     requisição HTTP
     * @param response    resposta HTTP
     * @param filterChain cadeia de filtros
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException
    {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String userEmail = jwtService.validateToken(token);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (userDetails.isEnabled())
            {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}