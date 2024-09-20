package com.example.securedoc.enumeration.converter;

import com.example.securedoc.enumeration.Authority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Authority, String> {

    @Override
    public String convertToDatabaseColumn(Authority authority) {
        if (authority == null) {
            return null;
        }

        return authority.getValue();
    }

    @Override
    public Authority convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        return Stream.of(Authority.values())
                .filter(authority -> authority.getValue().equals(value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
