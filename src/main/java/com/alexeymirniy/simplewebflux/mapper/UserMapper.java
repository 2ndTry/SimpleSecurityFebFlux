package com.alexeymirniy.simplewebflux.mapper;

import com.alexeymirniy.simplewebflux.dto.UserDto;
import com.alexeymirniy.simplewebflux.entity.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto map(UserEntity userEntity);

    @InheritInverseConfiguration
    UserEntity map(UserDto userDto);
}