package icu.jiapeng.qk.resource;

import icu.jiapeng.qk.service.AccessKeyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
public class AuthResource {

    @Inject
    AccessKeyService accessKeyService;

    @POST
    @Path("/refresh")
    public Response refresh() {
        accessKeyService.refresh();
        return Response.ok().build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok().build();
    }
}
