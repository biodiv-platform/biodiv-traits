/**
 * 
 */
package com.strandls.traits.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.taxonomy.pojo.FileMetadata;
import com.strandls.traits.ApiConstants;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.FactsCreateData;
import com.strandls.traits.pojo.FactsUpdateData;
import com.strandls.traits.pojo.TraitsCreateData;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.pojo.TraitsValuePair;
import com.strandls.traits.services.TraitsServices;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */
@Api("Traits Service")
@Path(ApiConstants.V1 + ApiConstants.FACTSERVICE)
public class TraitsController {

	@Inject
	private TraitsServices services;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)

	@ApiOperation(value = "Dummy API Ping", notes = "Checks validity of war file at deployment", response = String.class)
	public Response ping() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path(ApiConstants.TRAIT + ApiConstants.ALL)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch all the Traits", notes = "Returns all the IBP traits", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to fetch all the traits", response = String.class) })

	public Response getAllTraits() {
		try {
			List<TraitsValuePair> result = services.getAllObservationTraits();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.TRAIT + ApiConstants.CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Create Trait", notes = "Creates trait with trait values and maps with taxon", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to create trait", response = String.class) })

	public Response createTrait(@ApiParam(name = "traitsCreateData") List<TraitsCreateData> traitsCreateData) {
		try {
			String result = services.createTraits(traitsCreateData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.TRAIT + ApiConstants.UPDATE + "/{traitId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Update Trait", notes = "Updates the trait", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to update trait", response = String.class) })

	public Response updateTrait(@PathParam("traitId") String traitId,
			@ApiParam(name = "traitsCreateData") List<TraitsValuePair> traitsUpdateData) {
		try {
			String result = services.updateTraits(Long.parseLong(traitId), traitsUpdateData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/{objectType}/{objectId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Traits by objectType and Object ID", notes = "Returns the key value pair of Tarits for a particular Object", response = FactValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Traits not found", response = String.class) })

	public Response getFacts(@PathParam("objectType") String objectType,
			@ApiParam(value = "ID of Show that needs to be fetched", required = true) @PathParam("objectId") String objectId) {

		try {
			Long objId = Long.parseLong(objectId);
			List<FactValuePair> facts = services.getFacts(objectType, objId, null);
			return Response.status(Status.OK).entity(facts).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@POST
	@Path(ApiConstants.CREATE + "/{objectType}/{objectId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Create facts for a Object", notes = "Returns the Success and failure", response = FactValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Traits not found", response = String.class),
			@ApiResponse(code = 206, message = "Patially created", response = String.class) })

	public Response createFacts(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId,
			@ApiParam(name = "factsCreateData") FactsCreateData factsCreateData) {
		try {
			Long objId = Long.parseLong(objectId);
			List<FactValuePair> result = services.createFacts(request, objectType, objId, factsCreateData);
			if (result.isEmpty())
				return Response.status(Status.CREATED).entity(null).build();
			return Response.status(206).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.IBP + "/{traitId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Traits by Facts ID for ibp", notes = "Returns the key value pair of Tarits", response = FactValuePair.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Traits not found", response = String.class) })

	public Response getFactIbp(@PathParam("traitId") String traitId) {
		try {
			Long id = Long.parseLong(traitId);
			FactValuePair fact = services.getFactIbp(id);
			return Response.status(Status.OK).entity(fact).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIESGROUPID + "/{speciesGroupId}/{languageId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find all Trait Values pair for Specific SpeciesGroupId", notes = "Return the Key value pairs of Traits", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Species Not Found", response = String.class) })

	public Response getTraitList(@PathParam("speciesGroupId") String speciesGroupId, @PathParam("languageId") String languageId) {

		try {
			Long sGroup = Long.parseLong(speciesGroupId);
			Long language = Long.parseLong(languageId);
			List<TraitsValuePair> result = services.getObservationTraitList(sGroup, language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}

	}

	@GET
	@Path(ApiConstants.TRAIT + "/{traitId}/{languageId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find by traitId and languageId", notes = "Returns trait details", response = Facts.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Couldn't get trait details", response = String.class) })

	public Response getTraitByTraitIdByLangId(@PathParam("traitId") String trtId,
			@PathParam("languageId") String langId) {
		try {
			Long traitId = Long.parseLong(trtId);
			Long languageId = Long.parseLong(langId);
			Map<String, Object> result = services.fetchByTraitIdByLanguageId(traitId, languageId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.TRAIT + "/{traitId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find by traitId", notes = "Returns trait details for all translation", response = Facts.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Couldn't find trait details", response = String.class) })

	public Response getTraitByTraitId(@PathParam("traitId") String trtId) {
		try {
			Long traitId = Long.parseLong(trtId);
			List<Map<String, Object>> result = services.fetchByTraitId(traitId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.TAXON + "/{taxonId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find facts by taxonId", notes = "Returns list of facts for a particular TaxonId", response = Facts.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "traits not found for TaxonId", response = String.class) })

	public Response getFactsBytaxonId(@PathParam("taxonId") String taxnId) {
		try {
			Long taxonId = Long.parseLong(taxnId);
			List<Facts> result = services.fetchByTaxonId(taxonId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.TAXON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find all facts based of comma separated value ids", notes = "Returns a List of Taxon Id", response = Long.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Unable to retrive the data", response = String.class) })
	public Response getTaxonListByValueId(@QueryParam("valueList") String values) {
		try {
			List<Long> result = services.fetchTaxonIdByValueId(values);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.VALUE + "/{traitId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find the value of Traits", notes = "Returns the values of traits based on trait's ID", response = TraitsValue.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to get the values", response = String.class) })

	public Response getTraitsValue(@PathParam("traitId") String traitId) {
		try {
			Long trait = Long.parseLong(traitId);
			List<TraitsValue> result = services.fetchTraitsValue(trait);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.UPDATE + "/{objectType}/{objectId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Adds new Traits", notes = "Returns the list of allTraitValue Pair", response = FactValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to edit the Traits", response = String.class) })

	public Response addNewTraits(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId, @ApiParam(name = "factsAddData") Map<String, List> factsAddData,
			@QueryParam("userId") String userId, @QueryParam("taxonId") String taxonId) {
		try {
			Long objId = Long.parseLong(objectId);
			String result = services.addNewTraits(request, objectType, objId, factsAddData, userId, taxonId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.UPDATE + "/{objectType}/{objectId}/{traitId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "Updates the Traits with Values", notes = "Returns the list of allTraitValue Pair", response = FactValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Unable to edit the Traits", response = String.class) })

	public Response updateTraits(@Context HttpServletRequest request, @PathParam("objectType") String objectType,
			@PathParam("objectId") String objectId, @PathParam("traitId") String traitId,
			@ApiParam(name = "factsUpdateData") FactsUpdateData factsUpdateData) {
		try {
			Long objId = Long.parseLong(objectId);
			Long trait = Long.parseLong(traitId);

			List<FactValuePair> result = services.updateTraits(request, objectType, objId, trait, factsUpdateData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIES + ApiConstants.TRAIT + "/{taxonId}/{languageId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Species traits and value", notes = "Return all the species traits for that taxon", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to fetch the data", response = String.class) })

	public Response getSpeciesTraits(@PathParam("taxonId") String taxonId, @PathParam("languageId") String languageId) {
		try {
			Long taxonConceptId = Long.parseLong(taxonId);
			Long language = Long.parseLong(languageId);
			List<TraitsValuePair> result = services.getSpeciesTraits(taxonConceptId, language);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIES + "/{languageId}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "All Species traits and value", notes = "Return all the species traits", response = TraitsValuePair.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to fetch the data", response = String.class) })

	public Response getAllSpeciesTraits(@PathParam("languageId") String languageId) {
		try {
			Long language = Long.parseLong(languageId);
			List<TraitsValuePair> result = services.getAllSpeciesTraits(language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("upload/batch-upload")
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload the file for taxon definition batch upload", notes = "Returns succuess failure", response = FileMetadata.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	public Response uploadSearch(final FormDataMultiPart multiPart) {
		FormDataBodyPart filePart = multiPart.getField("file");
		List<String> traits = Arrays.asList(multiPart.getField("traits").getValue().split("\\|"));
		String scientificNameColumn = multiPart.getField("scientificName").getValue();
		String taxonColumn = multiPart.getField("TaxonConceptId").getValue();
		String speciesIdColumn = multiPart.getField("SpeciesId").getValue();
		String contributorColumn = multiPart.getField("Contributor").getValue();
		FormDataBodyPart attributionColumn = multiPart.getField("Attribution");
		FormDataBodyPart licenseColumn = multiPart.getField("License");
		if (filePart == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("File not present").build();
		} else {
			List<Map<String, String>> result;
			result = services.importSpeciesTraits(filePart, traits, scientificNameColumn, taxonColumn, speciesIdColumn,
					contributorColumn, (attributionColumn != null) ? attributionColumn.getValue() : null,
					(licenseColumn != null) ? licenseColumn.getValue() : null);
			return Response.ok().entity(result).build();
		}
	}

}
