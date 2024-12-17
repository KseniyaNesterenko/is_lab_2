package cs.ifmo.is.lab1;

import cs.ifmo.is.lab1.service.BookCreatureService;
import jakarta.enterprise.context.ApplicationScoped;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationScoped
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        packages("cs.ifmo.is.lab1.controller");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(BookCreatureService.class);
            }
        });
    }
}
