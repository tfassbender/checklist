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
import net.tfassbender.checklist.model.CheckListItem;
import net.tfassbender.checklist.service.ListService;

@Path("/api/lists/{listId}/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ItemResource {

    @Inject
    ListService listService;

    @Context
    SecurityContext securityContext;

    private String getUsername() {
        return securityContext.getUserPrincipal().getName();
    }

    @POST
    public Response addItem(@PathParam("listId") String listId, CreateItemRequest request) {
        try {
            CheckListItem item = listService.addItem(getUsername(), listId, request.getDescription());
            return Response.status(Response.Status.CREATED).entity(item).build();
        } catch (ListService.ListException e) {
            int status = e.getMessage().contains("not found") ? 404 : 400;
            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage(), status))
                    .build();
        }
    }

    @PUT
    @Path("/{itemId}")
    public Response updateItem(@PathParam("listId") String listId,
                               @PathParam("itemId") String itemId,
                               UpdateItemRequest request) {
        try {
            CheckListItem item = listService.updateItem(
                    getUsername(), listId, itemId,
                    request.getDescription(), request.getChecked());
            return Response.ok(item).build();
        } catch (ListService.ListException e) {
            int status = e.getMessage().contains("not found") ? 404 : 400;
            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage(), status))
                    .build();
        }
    }

    @DELETE
    @Path("/{itemId}")
    public Response deleteItem(@PathParam("listId") String listId,
                               @PathParam("itemId") String itemId) {
        try {
            listService.deleteItem(getUsername(), listId, itemId);
            return Response.noContent().build();
        } catch (ListService.ListException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        }
    }

    @PUT
    @Path("/reorder")
    public Response reorderItems(@PathParam("listId") String listId, ReorderItemsRequest request) {
        try {
            CheckList list = listService.reorderItems(getUsername(), listId, request.getItemIds());
            return Response.ok(list).build();
        } catch (ListService.ListException e) {
            int status = e.getMessage().contains("not found") ? 404 : 400;
            return Response.status(status)
                    .entity(new ErrorResponse(e.getMessage(), status))
                    .build();
        }
    }
}
