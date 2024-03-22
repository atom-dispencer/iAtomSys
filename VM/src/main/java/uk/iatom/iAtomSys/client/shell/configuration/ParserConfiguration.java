package uk.iatom.iAtomSys.client.shell.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNullApi;

@Configuration
public class ParserConfiguration {


    public static class Custom implements Converter {

        @Override
        public Object convert(Object source) {
            if (!(source instanceof Object)) {
                return source;
            }

            return null;
        }
    }
}
