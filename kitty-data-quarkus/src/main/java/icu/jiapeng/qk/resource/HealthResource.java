package icu.jiapeng.qk.resource;

import icu.jiapeng.qk.model.HealthStatus;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatus health() {
        return new HealthStatus("ok", "1.0.0");
    }

    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }
}
