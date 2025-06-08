package org.example.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpTransport;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

// //https://modelcontextprotocol.io/llms-full.txt
//https://github.com/modelcontextprotocol/java-sdk
public class Main {
    public static void main(String[] args) {


          ServerParameters params = ServerParameters
                .builder("python")
                    .args("-m","mcp_server_time"
                            , "--local-timezone=Asia/Jerusalem")
                .build();
        params = ServerParameters
                .builder("python")
                .args("-m","mcp_server_git"
                        )
                .build();

        final McpClientTransport transport = new StdioClientTransport(params);
        final McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .roots(true)      // Enable roots capability
         //               .sampling()       // Enable sampling capability
                        .build())
//                .sampling(new Function<McpSchema.CreateMessageRequest, McpSchema.CreateMessageResult>() {
//                    @Override
//                    public McpSchema.CreateMessageResult apply(McpSchema.CreateMessageRequest createMessageRequest) {
//                        return McpSchema.CreateMessageResult.builder().build();
//                    }
//                })
                .build();
        client.initialize();
        final McpSchema.ListToolsResult listToolsResult = client.listTools();
        final List<McpSchema.Tool> tools = listToolsResult.tools();
        for (McpSchema.Tool tool : tools) {
            System.out.println(tool);

        }

        System.out.println("Hello world!");
    }
}