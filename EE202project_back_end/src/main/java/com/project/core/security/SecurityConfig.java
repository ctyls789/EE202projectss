package com.project.core.security;

import com.project.core.security.jwt.AuthEntryPointJwt;
import com.project.core.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 啟用方法級別的安全性註解
public class SecurityConfig {

    @Autowired
    private EmployeeUserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    // 【已修正】@Bean 方法本身不應該有 @Autowired 註解。
    // Spring 會自動將這個 Bean 注入到需要它的地方。
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 白名單 (一定要先列)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/order/addForm").permitAll()
                        // 2. 需要權限的 API
                        .requestMatchers(HttpMethod.GET, "/api/employee-users", "/api/employee-users/**")
                        .hasAnyAuthority("EMPLOYEE_VIEW", "EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/employee-users").hasAuthority("EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/employee-users/**").hasAuthority("EMPLOYEE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/employee-users/**").hasAuthority("EMPLOYEE_MANAGE")

                        .requestMatchers(HttpMethod.GET, "/api/system-logs", "/api/system-logs/**")
                        .hasAnyAuthority("SYSTEM_LOG_VIEW", "SYSTEM_LOG_MANAGE")
                        .requestMatchers("/api/system-logs/**").hasAuthority("SYSTEM_LOG_MANAGE")

                        // 物料管理權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/materials", "/api/depot/materials/**")
                        .hasAuthority("INVENTORY_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/depot/materials").hasAuthority("INVENTORY_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/depot/materials/**").hasAuthority("INVENTORY_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/depot/materials/**").hasAuthority("INVENTORY_MANAGE")

                        // 庫存異動紀錄權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/transactions")
                        .hasAuthority("INVENTORY_HISTORY_VIEW")

                        // 入庫單權限
                        .requestMatchers(HttpMethod.GET, "/api/depot/inbound-receipts",
                                "/api/depot/inbound-receipts/**")
                        .hasAnyAuthority("INBOUND_VIEW", "INBOUND_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/depot/inbound-receipts").hasAuthority("INBOUND_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/depot/inbound-receipts/**")
                        .hasAuthority("INBOUND_MANAGE")

                        // 供應商管理 API
                        .requestMatchers(HttpMethod.GET, "/api/supplier/list", "/api/supplier/{id}")
                        .hasAnyAuthority("SUPPLIER_VIEW", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/supplier/add").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/supplier/update").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/supplier/{id}").hasAuthority("SUPPLIER_MANAGE")

                        // 採購訂單 API
                        .requestMatchers(HttpMethod.GET, "/api/order/list", "/api/order/edit/**",
                                "/api/order/supplier-ratio", "/api/order/amount-per-month")
                        .hasAnyAuthority("SUPPLIER_VIEW", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/order/insert").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/order/update").hasAuthority("SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/order/delete/**").hasAuthority("SUPPLIER_MANAGE")

                        // 工單管理 API
                        .requestMatchers(HttpMethod.GET, "/api/workorder", "/api/workorder/**",
                                "/api/workorder/{woId}/materials")
                        .hasAnyAuthority("WORKORDER_VIEW", "WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/workorder", "/api/workorder/picking")
                        .hasAuthority("WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/workorder/**").hasAuthority("WORKORDER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/workorder/**").hasAuthority("WORKORDER_MANAGE")

                        // BOM 管理 API
                        .requestMatchers(HttpMethod.GET, "/api/boms/**").hasAnyAuthority("BOM_VIEW", "BOM_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/boms/add").hasAuthority("BOM_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/boms/update").hasAuthority("BOM_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/boms/**").hasAuthority("BOM_MANAGE")

                        // 機台管理 API
                        .requestMatchers(HttpMethod.GET, "/api/machine/**")
                        .hasAnyAuthority("MACHINE_VIEW", "MACHINE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/machine").hasAuthority("MACHINE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/machine/**").hasAuthority("MACHINE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/machine/**").hasAuthority("MACHINE_MANAGE")

                        // 機台檔案 API
                        .requestMatchers(HttpMethod.GET, "/api/files/**")
                        .hasAnyAuthority("MACHINE_FILE_VIEW", "MACHINE_FILE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/files").hasAuthority("MACHINE_FILE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/files/**").hasAuthority("MACHINE_FILE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/files/**").hasAuthority("MACHINE_FILE_MANAGE")

                        // 機台維修 API
                        .requestMatchers(HttpMethod.GET, "/api/repair/**")
                        .hasAnyAuthority("MACHINE_REPAIR_VIEW", "MACHINE_REPAIR_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/repair").hasAuthority("MACHINE_REPAIR_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/repair/**").hasAuthority("MACHINE_REPAIR_MANAGE")

                        // 機台保養 API
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/**")
                        .hasAnyAuthority("MACHINE_MAINTENANCE_VIEW", "MACHINE_MAINTENANCE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/maintenance").hasAuthority("MACHINE_MAINTENANCE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/maintenance/**")
                        .hasAuthority("MACHINE_MAINTENANCE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/**")
                        .hasAuthority("MACHINE_MAINTENANCE_MANAGE")

                        // 物料管理 API
                        .requestMatchers(HttpMethod.GET, "/api/material/**")
                        .hasAnyAuthority("MATERIAL_VIEW", "MATERIAL_MANAGE")

                        // 領料單管理 API
                        .requestMatchers(HttpMethod.GET, "/api/picking-orders/**")
                        .hasAnyAuthority("PICKING_ORDER_VIEW", "PICKING_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/picking-orders").hasAuthority("PICKING_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/picking-orders/**")
                        .hasAnyAuthority("PICKING_ORDER_MANAGE", "PICKING_ORDER_APPROVE")
                        .requestMatchers(HttpMethod.DELETE, "/api/picking-orders/**")
                        .hasAuthority("PICKING_ORDER_MANAGE")

                        // 請假管理 API
                        .requestMatchers(HttpMethod.GET, "/api/leave/**", "/leave/**")
                        .hasAnyAuthority("LEAVE_VIEW", "LEAVE_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/leave/**", "/leave/**").hasAuthority("LEAVE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/leave/**", "/leave/**").hasAuthority("LEAVE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/leave/**", "/leave/**").hasAuthority("LEAVE_MANAGE")

                        // 3. 其他所有 /api/** 都要驗證 (暫時允許所有 /api/** 訪問，用於調試)
                        // .requestMatchers("/api/**").permitAll() // 暫時註解掉這行，測試權限控制(暫時先減因為要測試)
                        // .requestMatchers("/api/**").denyAll()// (暫時先加測試用)

                        // 4. 非 API 路徑 (前端路徑) 全部放行
                        .anyRequest().permitAll());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173", "http://172.22.34.82:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
