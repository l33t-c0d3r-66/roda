/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

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
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.JobsHelper;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(JobsResource.ENDPOINT)
@Api(value = JobsResource.SWAGGER_ENDPOINT)
public class JobsResource {
  public static final String ENDPOINT = "/v1/jobs";
  public static final String SWAGGER_ENDPOINT = "v1 jobs";

  private static Logger LOGGER = LoggerFactory.getLogger(JobsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Jobs", notes = "Gets a list of Jobs.", response = Job.class, responseContainer = "List")
  public Response listJobs(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    boolean showInactive = true;
    IndexResult<Job> listJobsIndexResult = org.roda.wui.api.controllers.Browser.find(Job.class, Filter.NONE,
      Sorter.NONE, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), Facets.NONE, user,
      showInactive);

    // transform controller method output
    Jobs jobs = JobsHelper.getJobsFromIndexResult(listJobsIndexResult);

    return Response.ok(jobs, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Job", notes = "Creates a new Job.", response = Job.class)
  public Response createJob(Job job, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Job updatedJob = org.roda.wui.api.controllers.Jobs.createJob(user, job);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(updatedJob).type(mediaType).build();
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Starts an already created Job", notes = "Starts Job.", response = Job.class)
  public Response startJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Job job = org.roda.wui.api.controllers.Jobs.startJob(user, jobId);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(job).type(mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Job", notes = "Gets a particular Job.", response = Job.class)
  public Response getJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Job job = org.roda.wui.api.controllers.Browser.retrieve(user, Job.class, jobId);

    return Response.ok(job, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Job", notes = "Delete a particular Job, stoping it if still running.", response = ApiResponseMessage.class)
  public Response deleteJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Jobs.deleteJob(user, jobId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Job deleted"), mediaType).build();
  }

  // FIXME WIP - not working yet
  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/reports")
  public Response getReport(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) {

    try {
      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, jobId));
      Sorter sorter = null;
      Sublist sublist = new Sublist();
      IndexResult<Report> find = RodaCoreFactory.getIndexService().find(Report.class, filter, sorter, sublist);

      StringBuilder sb = new StringBuilder();

      for (Report Report : find.getResults()) {
        sb.append(Report.toString());
        sb.append("<br/>");
      }

      return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
    } catch (RODAException e) {
      LOGGER.error("Error getting jobs", e);
    }
    return Response.ok("PUM", MediaType.TEXT_HTML).build();

  }

}
