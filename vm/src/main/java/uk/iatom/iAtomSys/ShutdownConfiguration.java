package uk.iatom.iAtomSys;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ShutdownConfiguration {

    @Bean
    public TerminateBean getTerminateBean() {
        return new TerminateBean();
    }


    public static class TerminateBean {

        @PreDestroy
        public void onDestroy() {
            System.out.println("Spring Container is destroyed!");
        }
    }
}