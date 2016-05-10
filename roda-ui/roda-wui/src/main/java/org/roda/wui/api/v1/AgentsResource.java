/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(AgentsResource.ENDPOINT)
@Api(value = AgentsResource.SWAGGER_ENDPOINT)
public class AgentsResource {
  public static final String ENDPOINT = "/v1/agents";
  public static final String SWAGGER_ENDPOINT = "v1 agents";

  private static Logger LOGGER = LoggerFactory.getLogger(AgentsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Agents", notes = "Gets a list of Agents.", response = Agent.class, responseContainer = "List")
  public Response listAgents(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    // TODO add show inactive to API
    boolean showInactive = false;
    IndexResult<Agent> listAgentsIndexResult = org.roda.wui.api.controllers.Browser.find(Agent.class, Filter.NONE,
      Sorter.NONE, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), Facets.NONE, user,
      showInactive);

    // transform controller method output
    List<Agent> agents = org.roda.wui.api.controllers.Agents.retrieveAgents(listAgentsIndexResult);

    return Response.ok(agents, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Agent", notes = "Creates a new Agent.", response = Agent.class)
  public Response createAgent(Agent agent, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Agent newAgent = org.roda.wui.api.controllers.Agents.createAgent(user, agent);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(newAgent).type(mediaType).build();
  }

  @GET
  @Path("/{agentId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Agent", notes = "Gets a particular Agent.", response = Agent.class)
  public Response getAgent(@PathParam("agentId") String agentId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Agent agent = org.roda.wui.api.controllers.Browser.retrieve(user, Agent.class, agentId);

    return Response.ok(agent, mediaType).build();
  }

  @DELETE
  @Path("/{agentId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Agent", notes = "Delete a particular Agent.", response = ApiResponseMessage.class)
  public Response deleteAgent(@PathParam("agentId") String agentId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Agents.deleteAgent(user, agentId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Agent deleted"), mediaType).build();
  }
}
