package uk.iatom.iAtomSys;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ShutdownConfiguration {

    @Bean
    public TerminationBean getTerminateBean() {
        return new TerminationBean();
    }


    public static class TerminationBean {

        @PreDestroy
        public void onDestroy() {
            System.out.println("TerminationBean destruction triggered...");
        }
    }
}