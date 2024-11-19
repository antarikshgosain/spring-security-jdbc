package dev.anta.secure_jdbc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /*@Bean
    InMemoryUserDetailsManager user() {
        return new InMemoryUserDetailsManager(
                User.withUsername("user")
                        .password("{noop}user01")
                        .roles("USER")
                        .build() ,
                User.withUsername("admin")
                        .password("{noop}admin01")
                        .roles("ADMIN")
                        .build()
        );
    }*/

    /* Important Information
        // DDL Path Variable: JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION
        // DDL File Location: org/springframework/security/core/userdetails/jdbc/users.ddl
        // Modified Script:
            create table users(username varchar(50) not null primary key,password varchar(500) not null,enabled boolean not null);
            create table authorities (username varchar(50) not null,authority varchar(50) not null,constraint fk_authorities_users foreign key(username) references users(username));
            create unique index ix_auth_username on authorities (username,authority);
    */

    /* //Works well for H2 In-Memory Database
    @Bean
    EmbeddedDatabase dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("dashingboard")
                .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
                .build();
    }*/

    @Bean
    public DataSource datasource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://URL:PORT/DB_NAME");
        dataSource.setUsername("USERNAME");
        dataSource.setPassword("PASSWORD");
        return dataSource;
    }

    @Bean
    JdbcUserDetailsManager user(DataSource datasource, PasswordEncoder passwordEncoder) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(datasource);
        //if user is to be created, whole create table script is also called
        if(!jdbcUserDetailsManager.userExists("superadmin")){
            UserDetails superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("superadmin01"))
                    .roles("ADMIN")
                    .build();
            jdbcUserDetailsManager.createUser(superAdmin);
        }
        return jdbcUserDetailsManager;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .authorizeRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(header -> header.frameOptions().sameOrigin())
                .formLogin(withDefaults())
                .build();
    }
}
