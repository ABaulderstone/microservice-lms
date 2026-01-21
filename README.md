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
  ***
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

---

- There's a couple of gotchas in the `JWTAuthenticationFilter` worth elaborating on

```java
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                Claims claims = jwtService.parseToken(token);

                @SuppressWarnings("unchecked")
                List<String> roleNames = (List<String>) claims.get("roles", List.class);

                var roles = roleNames.stream()
                        .map(roleName -> (GrantedAuthority) () -> "ROLE_" + roleName)
                        .toList();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(),
                        null, roles);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);
            return;
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            throw new InsufficientAuthenticationException("Authentication Required");
        } catch (Exception e) {
            throw new ServletException("Failed to process JWT authentication", e);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
```

- Make sure to clear security context on JWT error

```java
auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
```

does some really heavy lifting. Without this you'll get weird 401 errors with malformed requests (missing body on a POST is a very common one)

---

- Spring Security comes with a built in @PreAuthorize annotation as well as an `@AllowedRoles` annotation that you can enable with additional configuration. But they both have a somewhat awkward syntax so I created my own `@RequireRoles` annotation. It is a bit of a pain because to set up the interceptor:

```java
@Component
public class RequireRolesInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        RequireRoles requireRoles = null;
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        } else {
            requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);

        }
        if (requireRoles != null) {
            checkRoles(requireRoles.value());
        }
        return true;
    }

    private void checkRoles(String[] requiredRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Authentication Required");
        }
        Set<String> authRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean allowed = Arrays.stream(requiredRoles).anyMatch(r -> authRoles.contains("ROLE_" + r));
        if (!allowed) {
            throw new AccessDeniedException("User does not have required role");
        }
    }

}
```

You need to register it in WebConfig

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String API_PREFIX = "/api/v1";
    private final RequireRolesInterceptor requireRolesInterceptor;

    public WebConfig(RequireRolesInterceptor requireRolesInterceptor) {
        this.requireRolesInterceptor = requireRolesInterceptor;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_PREFIX, c -> !c.getPackageName().startsWith("org.springdoc"));

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requireRolesInterceptor)
                .addPathPatterns(API_PREFIX + "/**");
    }
}
```

#### Service Level authentication/authorization

Because the microservices will not be exposed externally - and due to not wanting to drown in auth complexity - I have chosen not to use Service tokens and instead extract the userId and roles from the JWT at the api gateway layer and pass them along as metadata headers to the individual services. This allows me to easily manually test GRPC requests and responses with `Postman` while in development.

### Testing

#### E2E

End to end testing of the API will use `PlayWright` over `RestAssured` because of the nature of the one db per microservice pattern we don't get as much benefit from running against an in memory database and I would prefer an as realistic as possible test of the whole system.

#### Unit

Business logic within the API gateway and microservices will use `JUnit` and `Mockito` to ensure all edge cases are thoroughly tested

### Shared code

#### Protobufs

All `.proto` files live in `/proto` - while in a real project it would make more sense for this to be a version controlled repository/package this works reasonably well for this project. The `pom.xml` in each service reads from the shared path in the IDE and will compile the stubs on any file change. The dockerfiles copy the proto directory in the build stage, before compiling to a single executable jar. This is faciliated by the excellent `org.xolstice` `protobuf-maven-plugin`. The following code takes care of compilation in both environments

```xml
			<plugin>
				<groupId>org.xolstice.maven.plugins</groupId>
				<artifactId>protobuf-maven-plugin</artifactId>
				<version>0.6.1</version>
				<configuration>
					<protocArtifact>
						com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}
					</protocArtifact>
					<pluginId>grpc-java</pluginId>
					<pluginArtifact>
						io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}
					</pluginArtifact>
					<pluginParameter>useJakartaAnnotations=true</pluginParameter>
					<protoSourceRoot>${proto.dir}</protoSourceRoot>
				</configuration>
				<executions>
					<execution>
						<id>compile</id>
						<goals>
							<goal>compile</goal>
							<goal>compile-custom</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

	<profiles>
		<!-- IDE profile -->
		<profile>
			<id>ide</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<proto.dir>${project.basedir}/../proto</proto.dir>
			</properties>
		</profile>

		<!-- Docker profile -->
		<profile>
			<id>docker</id>
			<properties>
				<proto.dir>${project.basedir}/proto</proto.dir>
			</properties>
		</profile>
	</profiles>
```

## Debugging

### Kcat

```bash
docker run --rm -it --network microservice-lms_default edenhill/kcat:1.7.1 kcat -b kafka:9092 -t user.created.v1 -C -f 'key=%k | value=%s\n'
```

then

```bash
kcat -b kafka:9092 -t user.created.v1 -C -f 'key=%k | value=%s\n'

```

### DB access
