package net.tfassbender.checklist.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import net.tfassbender.checklist.dto.*;
import net.tfassbender.checklist.model.CheckList;
import net.tfassbender.checklist.service.ListService;

import java.util.List;

@Path("/api/lists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ListResource {

    @Inject
    ListService listService;

    @Context
    SecurityContext securityContext;

    private String getUsername() {
        return securityContext.getUserPrincipal().getName();
    }

    @GET
    public Response getAllLists() {
        List<CheckListSummary> lists = listService.getAllLists(getUsername());
        return Response.ok(lists).build();
    }

    @POST
    public Response createList(CreateListRequest request) {
        try {
            CheckList list = listService.createList(getUsername(), request.name());
            return Response.status(Response.Status.CREATED).entity(list).build();
        } catch (ListService.ListException e) {
            int status = e.getMessage().contains("already exists") ? 409 : 400;
            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage(), status))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getList(@PathParam("id") String id) {
        return listService.getList(getUsername(), id)
                .map(list -> Response.ok(list).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("List not found", 404))
                        .build());
    }

    @PUT
    @Path("/{id}")
    public Response updateList(@PathParam("id") String id, UpdateListRequest request) {
        try {
            CheckList list = listService.updateList(getUsername(), id, request.name(), request.notes());
            return Response.ok(list).build();
        } catch (ListService.ListException e) {
            int status = e.getMessage().contains("not found") ? 404 :
                         e.getMessage().contains("already exists") ? 409 : 400;
            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage(), status))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteList(@PathParam("id") String id) {
        try {
            listService.deleteList(getUsername(), id);
            return Response.noContent().build();
        } catch (ListService.ListException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        }
    }

    @POST
    @Path("/{id}/reset")
    public Response resetList(@PathParam("id") String id) {
        try {
            CheckList list = listService.resetList(getUsername(), id);
            return Response.ok(list).build();
        } catch (ListService.ListException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        }
    }
}
