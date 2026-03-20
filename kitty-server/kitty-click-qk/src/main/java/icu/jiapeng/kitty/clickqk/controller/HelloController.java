package icu.jiapeng.kitty.clickqk.controller;


import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@Path("/runtime")
public class HelloController {
    @GET
    @Path("/hello")
    public Uni<String> hello() {
        return Uni.createFrom().item("/report/hello");
    }
}