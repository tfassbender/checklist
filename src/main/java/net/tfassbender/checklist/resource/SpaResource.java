package net.tfassbender.checklist.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Catch-all resource that serves index.html for any client-side route.
 * This enables React Router to handle navigation on direct URL access and page refresh.
 *
 * Excluded paths:
 * - /api/* — handled by actual REST resources
 * - paths ending in a file extension (e.g. .js, .css) — served as static files
 */
@Path("/")
public class SpaResource {

    @GET
    @Path("{path:(?!api/)(?!.*\\.\\w{1,5}$).*}")
    @Produces(MediaType.TEXT_HTML)
    @PermitAll
    public Response spa(@PathParam("path") String path) {
        InputStream stream = getClass().getResourceAsStream("/META-INF/resources/index.html");
        if (stream == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(stream).type(MediaType.TEXT_HTML).build();
    }
}
