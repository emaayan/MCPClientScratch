package org.example.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.example.app.Service;
import org.example.app.model.Asset;
import org.example.app.model.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class Main {


    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Starting server");

        final ObjectMapper objectMapper = new ObjectMapper();
        final Service service = new Service();


//        McpServerFeatures.SyncResourceSpecification syncResourceSpecification = new McpServerFeatures.SyncResourceSpecification(
//                new McpSchema.Resource("custom://resource", "name", "description", "mime-type", null),
//                (exchange, request) -> {
//                    // Resource read implementation
//                    final List<McpSchema.ResourceContents> contents=new ArrayList<>();
//                    return new McpSchema.ReadResourceResult(contents);
//                }
//        );
//
//        McpServerFeatures.SyncPromptSpecification syncPromptSpecification = new McpServerFeatures.SyncPromptSpecification(
//                new McpSchema.Prompt("greeting", "description", List.of(
//                        new McpSchema.PromptArgument("name", "description", true)
//                )),
//                (exchange, request) -> {
//                    // Prompt implementation
//                    String description="";
//                    final List<McpSchema.PromptMessage> messages=List.of();
//                    return new McpSchema.GetPromptResult(description, messages);
//                }
//        );


        final McpServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        final McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("AssetSerice", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .logging()           // Enable logging support
                        .tools(true)         // Enable tool support

                        ///.resources(false,true)     // Enable resource support
                        //.prompts(true)       // Enable prompt support
                        .build())
                .tools(
                        new McpServerFeatures.SyncToolSpecification(
                                new McpSchema.Tool("get_cities", "get the list of all available cities, each city contains a name and it's id", """
                                        {
                                          "type" : "object",
                                          "id" : "urn:jsonschema:Operation",
                                          "properties" : {                           
                                          }
                                        }
                                        """),
                                (exchange, arguments) -> {
                                    final Collection<City> cities = service.getCities();

                                    final List<McpSchema.Content> results = cities.stream().map(v -> {
                                        try {
                                            final String content = objectMapper.writeValueAsString(v);
                                            return new McpSchema.TextContent(content);
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).collect(Collectors.toList());
                                    return new McpSchema.CallToolResult(results, false);
                                }
                        ),
                        new McpServerFeatures.SyncToolSpecification(
                                new McpSchema.Tool("get_city_by_name", "get a city by the name", """
                                        {
                                          "type" : "object",
                                          "id" : "urn:jsonschema:Operation",
                                          "properties" : {
                                            "cityName" : {
                                              "type" : "string"
                                            }
                                          }
                                        }
                                        """),
                                (exchange, arguments) -> {
                                    final Object o = arguments.get("cityName");
                                    final Optional<City> cityByName = service.getCityByName(o.toString());
                                    final McpSchema.CallToolResult callToolResult = cityByName.map(city -> {
                                        try {
                                            final String s = objectMapper.writeValueAsString(city);
                                            return new McpSchema.CallToolResult(s, false);
                                        } catch (JsonProcessingException e) {
                                            LOGGER.error("Failed to turn to json", e);
                                            return new McpSchema.CallToolResult(e.getMessage(), true);
                                        }
                                    }).orElseGet(() -> new McpSchema.CallToolResult("Failed to find City " + o, true));
                                    return callToolResult;
                                }
                        )
                        , new McpServerFeatures.SyncToolSpecification(
                                new McpSchema.Tool("get_assets", "get all asserts, each asset contains it's id , name, and the id of the city it's located", """
                                        {
                                          "type" : "object",
                                          "id" : "urn:jsonschema:Operation",
                                          "properties" : {                                           
                                          }
                                        }
                                        """),
                                (exchange, arguments) -> {
                                    final Collection<Asset> assets = service.getAssets();

                                    final List<McpSchema.Content> results = assets.stream().map(v -> {
                                        try {
                                            final String content = objectMapper.writeValueAsString(v);
                                            return new McpSchema.TextContent(content);
                                        } catch (JsonProcessingException e) {
                                            LOGGER.error("Failed to turn to json",e);
                                            throw new RuntimeException(e);
                                        }
                                    }).collect(Collectors.toList());
                                    return new McpSchema.CallToolResult(results, false);
                                }
                        )
                )
                .build();
// Register tools, resources, and prompts
//        syncServer.addTool(new McpServerFeatures.SyncToolSpecification(
//                new McpSchema.Tool("get_cities", "get the list of all available cities", """
//                        {
//                          "type" : "object",
//                          "id" : "urn:jsonschema:Operation",
//                          "properties" : {
//                            "operation" : {
//                              "type" : "string"
//                            }
//                          }
//                        }
//                        """),
//                (exchange, arguments) -> {
//                    final Collection<City> cities = service.getCities();
//
//                    final List<McpSchema.Content> results = cities.stream().map(v -> {
//                        try {
//                            final String content = objectMapper.writeValueAsString(v);
//                            LOGGER.info(content);
//                            return new McpSchema.TextContent(content);
//                        } catch (JsonProcessingException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }).collect(Collectors.toList());
//                    return new McpSchema.CallToolResult(results, false);
//                }
//        ));
        //   syncServer.addResource(syncResourceSpecification);
        //    syncServer.addPrompt(syncPromptSpecification);

// Close the server when done
        //  syncServer.close();
    }
}
