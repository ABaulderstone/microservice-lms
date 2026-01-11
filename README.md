# Micro Service LMS

This is a self learning exercise for me to increase my understanding of

- Microservices
- Kafka
- gRPC / Protocol Buffers

## Purpose

I have worked with several Learning Management Systems over my career as an Educator, and I've always - perhaps naively - thought "I could do this better". I know the domain very well and I know the kind of features I wish I had available to me. Because I've spent a lot of time thinking about the domain I can focus in particular learning the tech as I build out something that ideally I could use in my current work.

## Tech Stack

I'm working with Spring Boot primarily because it's a framework I'm very comfortable working with; I'd rather spend the cognitive energy on figuring out the above. Furthermore, working on 'crunchier' problems exposes me to more Java/Spring Boot features. It's a win-win in the sense that I can expand my Spring Boot skill set and pick some new stuff up along the way.

### Services

Currently the service boundaries are:

- API Gateway - to provide a traditional REST API for my frontend
- Auth/User service - for Users, Roles, JWT generation
- Profile Service - for holding User details (name, location etc), as well as demographic information
- Cohort Service - for cohorts, enrollments, class schedules etc
  I also plan to implement
- Email service
- Notification service
- Integration service (github, zoom, microsoft forms etc)

### Authentication

I'm using a combination of Spring Security and JWT at the `API Gateway` level. I've wrestled with Spring Security in the past so I'm somewhat aware of it's idiosyncrasies. In case you're not or I'm reading this as a reference document at some point in the future there are a few aspects worth noting.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                        RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                        RestAccessDeniedHandler restAccessDeniedHandler)
                        throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/login",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(restAuthenticationEntryPoint)
                                                .accessDeniedHandler(restAccessDeniedHandler))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
```

- Spring Security was built with an MVC/ server html rendered pattern in mind and by default uses a session. You need to explicitly turn that off
</hr>
- If (like me) you are a control freak you might want to build your own `entry points` or handlers for the 401 and 403 errors that can be thrown by unauthenticated requests or requests without the right permissions to perform an action. It's a bit of a drag but you can create them and inject them into the Security Filter chain with a bit of Spring dependency injection magic. Trying to throw `ResponseStatusException` (which feels like the correct thing to do particular if you're coming from another framework) results in those exceptions getting swallowed and getting a 500 internal server error back, it's pretty opaque.
- These entrypoints look like this

```java
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiErrorWriter apiErrorWriter;

    public RestAccessDeniedHandler(ApiErrorWriter apiErrorWriter) {
        this.apiErrorWriter = apiErrorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        apiErrorWriter.write(request, response, HttpStatus.FORBIDDEN,
                "Forbidden: " + accessDeniedException.getMessage());
    }

}
```

- It's probably worth exploring `ApiErrorWriter` the `ApiErrorResponse` DTO and the `GlobalExceptionHandler` to see how I've made my error responses uniform and informative
</hr>
- There's a couple of gotchas in the `JWTAuthenticationFilter` worth elaborating on

```java

```
