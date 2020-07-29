package org.acme.kafka.streams.aggregator.rest;

import org.acme.kafka.streams.aggregator.model.WeatherStationData;
import org.acme.kafka.streams.aggregator.streams.InteractiveQueries;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@ApplicationScoped
@Path("/weather-stations")
public class WeatherStationEndpoint {

    @Inject
    InteractiveQueries interactiveQueries;

    @GET
    @Path("/data/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeatherStationData(@PathParam("id") int id) {
        List<WeatherStationData> result = interactiveQueries.getWeatherStationData(id);
        if (result.size() > 0) {
            return Response.ok(result).build();
        } else {
            return Response.status(Status.NOT_FOUND.getStatusCode(),
                    "No data found for weather station " + id).build();
        }
    }
}
