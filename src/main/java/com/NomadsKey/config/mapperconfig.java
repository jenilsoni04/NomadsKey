package com.NomadsKey.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class mapperconfig {
    @Bean
    public ModelMapper mapper()
    {
        return new ModelMapper();
    }
}
