/**
 *
 */
package com.strandls.traits.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.taxonomy.pojo.FileMetadata;
import com.strandls.traits.ApiConstants;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.FactsCreateData;
import com.strandls.traits.pojo.FactsUpdateData;
import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsCreateData;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.pojo.TraitsValuePair;
import com.strandls.traits.services.TraitsServices;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * @author Abhishek Rudra
 *
 */
@Tag(name = "Traits Service", description = "Services for managing traits and facts")
@Path(ApiConstants.V1 + ApiConstants.FACTSERVICE)
public class TraitsController {

	private final TraitsServices services;

	@Inject
	public TraitsController(TraitsServices services) {
		this.services = services;
	}

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Dummy API Ping", description = "Checks validity of war file at deployment")
	public Response ping() {
		return Response.status(Status.OK).entity("PONG").build();
	}

	@GET
	@Path(ApiConstants.TRAIT + ApiConstants.ALL)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetch all the Traits", description = "Returns all the IBP traits")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch all the traits", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllTraits() {
		try {
			List<TraitsValuePair> result = services.getAllObservationTraits();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.TRAIT + ApiConstants.LIST)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Fetch all the Traits Names", description = "Returns all the traits")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched trait names", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Traits.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch all the traits", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllTraitsNames() {
		try {
			List<Traits> result = services.getAllTraitsNames();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(ApiConstants.TRAIT + ApiConstants.CREATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create Trait", description = "Creates trait with trait values and maps with taxon")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Trait created successfully", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "unable to create trait", content = @Content(schema = @Schema(type = "string"))) })
	public Response createTrait(
			@RequestBody(description = "List of traits to create", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsCreateData.class)))) List<TraitsCreateData> traitsCreateData) {
		try {
			String result = services.createTraits(traitsCreateData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@PUT
	@Path(ApiConstants.TRAIT + ApiConstants.UPDATE + "/{traitId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update Trait", description = "Updates the trait")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Trait updated successfully", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "unable to update trait", content = @Content(schema = @Schema(type = "string"))) })
	public Response updateTrait(
			@Parameter(description = "ID of the trait to update") @PathParam("traitId") String traitId,
			@RequestBody(description = "Trait data for update", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsCreateData.class)))) List<TraitsCreateData> traitsUpdateData) {
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
	@Operation(summary = "Find Traits by objectType and Object ID", description = "Returns the key value pair of Tarits for a particular Object")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FactValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "Traits not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getFacts(
			@Parameter(description = "Type of the object (e.g., 'observation')") @PathParam("objectType") String objectType,
			@Parameter(description = "ID of the object that needs to be fetched", required = true) @PathParam("objectId") String objectId) {
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
	@Operation(summary = "Create facts for a Object", description = "Returns the Success and failure")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Facts created successfully"),
			@ApiResponse(responseCode = "206", description = "Partially created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FactValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "Traits not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response createFacts(@Context HttpServletRequest request,
			@Parameter(description = "Type of the object") @PathParam("objectType") String objectType,
			@Parameter(description = "ID of the object") @PathParam("objectId") String objectId,
			@RequestBody(description = "Facts creation data", required = true, content = @Content(schema = @Schema(implementation = FactsCreateData.class))) FactsCreateData factsCreateData) {
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
	@Operation(summary = "Find Traits by Facts ID for ibp", description = "Returns the key value pair of Tarits")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found trait", content = @Content(schema = @Schema(implementation = FactValuePair.class))),
			@ApiResponse(responseCode = "400", description = "Traits not found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getFactIbp(@Parameter(description = "ID of the trait fact") @PathParam("traitId") String traitId) {
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
	@Operation(summary = "Find all Trait Values pair for Specific SpeciesGroupId", description = "Return the Key value pairs of Traits")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "Species Not Found", content = @Content(schema = @Schema(type = "string"))) })
	public Response getTraitList(
			@Parameter(description = "ID of the species group") @PathParam("speciesGroupId") String speciesGroupId,
			@Parameter(description = "ID of the language") @PathParam("languageId") String languageId) {
		try {
			if (speciesGroupId == null || speciesGroupId.equals("undefined") || languageId == null
					|| languageId.equals("undefined")) {
				return Response.status(Status.BAD_REQUEST).entity("speciesGroupId or languageId is missing").build();
			}
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
	@Operation(summary = "Find by traitId and languageId", description = "Returns trait details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found trait details", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "Couldn't get trait details", content = @Content(schema = @Schema(type = "string"))) })
	public Response getTraitByTraitIdByLangId(
			@Parameter(description = "ID of the trait") @PathParam("traitId") String trtId,
			@Parameter(description = "ID of the language") @PathParam("languageId") String langId) {
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
	@Operation(summary = "Find by traitId", description = "Returns trait details for all translation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found trait details", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
			@ApiResponse(responseCode = "400", description = "Couldn't find trait details", content = @Content(schema = @Schema(type = "string"))) })
	public Response getTraitByTraitId(@Parameter(description = "ID of the trait") @PathParam("traitId") String trtId) {
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
	@Operation(summary = "Find facts by taxonId", description = "Returns list of facts for a particular TaxonId")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found facts", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Facts.class)))),
			@ApiResponse(responseCode = "400", description = "traits not found for TaxonId", content = @Content(schema = @Schema(type = "string"))) })
	public Response getFactsBytaxonId(@Parameter(description = "ID of the taxon") @PathParam("taxonId") String taxnId) {
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
	@Operation(summary = "Find all facts based of comma separated value ids", description = "Returns a List of Taxon Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found taxon IDs", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
			@ApiResponse(responseCode = "404", description = "Unable to retrive the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response getTaxonListByValueId(
			@Parameter(description = "Comma-separated list of trait value IDs") @QueryParam("valueList") String values) {
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
	@Operation(summary = "Find the value of Traits", description = "Returns the values of traits based on trait's ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found trait values", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValue.class)))),
			@ApiResponse(responseCode = "400", description = "unable to get the values", content = @Content(schema = @Schema(type = "string"))) })
	public Response getTraitsValue(@Parameter(description = "ID of the trait") @PathParam("traitId") String traitId) {
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
	@Operation(summary = "Adds new Traits", description = "Returns the list of allTraitValue Pair")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Traits updated successfully", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Unable to edit the Traits", content = @Content(schema = @Schema(type = "string"))) })
	public Response bulkTraitsUpdate(@Context HttpServletRequest request,
			@Parameter(description = "Type of the object") @PathParam("objectType") String objectType,
			@Parameter(description = "ID of the object") @PathParam("objectId") String objectId,
			@RequestBody(description = "Map of facts to add", required = true, content = @Content(schema = @Schema(type = "object", implementation = Map.class))) Map<String, List> factsAddData,
			@Parameter(description = "ID of the user") @QueryParam("userId") String userId,
			@Parameter(description = "ID of the taxon") @QueryParam("taxonId") String taxonId) {
		try {
			Long objId = Long.parseLong(objectId);
			String result = services.bulkTraitsUpdate(request, objectType, objId, factsAddData, userId, taxonId);
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
	@Operation(summary = "Updates the Traits with Values", description = "Returns the list of allTraitValue Pair")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Traits updated successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FactValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "Unable to edit the Traits", content = @Content(schema = @Schema(type = "string"))) })
	public Response updateTraits(@Context HttpServletRequest request,
			@Parameter(description = "Type of the object") @PathParam("objectType") String objectType,
			@Parameter(description = "ID of the object") @PathParam("objectId") String objectId,
			@Parameter(description = "ID of the trait") @PathParam("traitId") String traitId,
			@RequestBody(description = "Facts update data", required = true, content = @Content(schema = @Schema(implementation = FactsUpdateData.class))) FactsUpdateData factsUpdateData) {
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
	@Operation(summary = "Species traits and value", description = "Return all the species traits for that taxon")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found species traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response getSpeciesTraits(
			@Parameter(description = "ID of the taxon concept") @PathParam("taxonId") String taxonId,
			@Parameter(description = "ID of the language") @PathParam("languageId") String languageId) {
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
	@Operation(summary = "All Species traits and value", description = "Return all the species traits")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found all species traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllSpeciesTraits(
			@Parameter(description = "ID of the language") @PathParam("languageId") String languageId) {
		try {
			Long language = Long.parseLong(languageId);
			List<TraitsValuePair> result = services.getAllSpeciesTraits(language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.LIST + "/{languageId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "All traits list", description = "Return all the traits list for a language")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found all traits", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraitsValuePair.class)))),
			@ApiResponse(responseCode = "400", description = "unable to fetch the data", content = @Content(schema = @Schema(type = "string"))) })
	public Response getAllTraitsList(
			@Parameter(description = "ID of the language") @PathParam("languageId") String languageId) {
		try {
			Long language = Long.parseLong(languageId);
			List<TraitsValuePair> result = services.getAllTraits(language);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path(ApiConstants.UPLOAD + ApiConstants.BATCH)
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Upload the file for taxon definition batch upload", description = "Returns success or failure")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "File processed", content = @Content(schema = @Schema(implementation = FileMetadata.class))),
			@ApiResponse(responseCode = "400", description = "file not present", content = @Content(schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "500", description = "ERROR", content = @Content(schema = @Schema(type = "string"))) })
	public Response uploadSearch(
			@RequestBody(description = "Multipart-form data for batch upload.", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = FileMetadata.class))) final FormDataMultiPart multiPart) {
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
