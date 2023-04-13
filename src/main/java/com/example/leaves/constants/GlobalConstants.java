package com.example.leaves.constants;

public class GlobalConstants {
    public static final String INITIAL_DEPARTMENTS = "ADMIN, IT, ACCOUNTING";
    public static final String EASTER_HOLIDAYS = "Good Friday, Holy Saturday, Easter Sunday, Easter Monday";
    public static final String CHRISTMAS_HOLIDAYS_PREFIX = "Christmas";

    //TODO RENAME H2 IS IN HERE
    public static final String[] SWAGGER_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            // other public endpoints of your API may be appended to this array
            "/h2-console/**"
    };
}
