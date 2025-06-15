package com.defi.common.vertx;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Function;

/**
 * {@code HttpApi} provides utility methods for handling HTTP requests asynchronously and synchronously
 * using Vert.x framework. It wraps the request handling into async and sync handlers, which are used to
 * process requests and respond with appropriate results.
 *
 * <p>This class provides two main methods for handling requests: {@code handleAsync} and {@code handleSync}.
 * Both methods wrap a function that performs the actual business logic, but {@code handleAsync} runs
 * the logic asynchronously, while {@code handleSync} runs it synchronously.</p>
 */
public class HttpApi {
    /**
     * Private constructor to prevent instantiation.
     */
    private HttpApi() {
        // Utility class
    }


    /**
     * Handles asynchronous HTTP requests using a provided function.
     * This method uses Vert.x's event loop to execute blocking functions asynchronously.
     *
     * <p>The result of the function is then passed to the HTTP response. In case of failure,
     * the error is handled by {@code handleError} method.</p>
     *
     * @param blockingFunction The function to execute asynchronously. It takes a {@code RoutingContext}
     *                         and returns a response object.
     * @param <T> The type of the response.
     * @return A Vert.x {@code Handler<RoutingContext>} that processes the request asynchronously.
     */
    public static <T> Handler<RoutingContext> handleAsync(Function<RoutingContext, T> blockingFunction) {
        return ctx -> VertxServer.getInstance().vertx.executeBlocking(() -> blockingFunction.apply(ctx))
                .onSuccess(response -> {
                    if(response instanceof BaseResponse<?> baseResponse) {
                        ctx.response().setStatusCode(baseResponse.code())
                                .end(baseResponse.toString());
                    }else{
                        ctx.response().end(response.toString());
                    }
                })
        .onFailure(err -> { // Handle failure
            handleError(ctx, err);
        });
    }


    /**
     * Handles synchronous HTTP requests using a provided function.
     * This method executes the function in the current thread, blocking the thread
     * while the function runs.
     *
     * <p>The result of the function is sent as the response body. If an error occurs during the execution,
     * it is caught and processed by {@code handleError} method.</p>
     *
     * @param syncFunction The function to execute synchronously. It takes a {@code RoutingContext}
     *                     and returns a response object.
     * @param <T> The type of the response.
     * @return A Vert.x {@code Handler<RoutingContext>} that processes the request synchronously.
     */
    public static <T> Handler<RoutingContext> handleSync(Function<RoutingContext, T> syncFunction) {
        return ctx -> {
            try {
                T result = syncFunction.apply(ctx);
                if(result instanceof BaseResponse<?> baseResponse){
                    ctx.response().setStatusCode(baseResponse.code())
                            .end(baseResponse.toString());
                }else {
                    ctx.response().end(result.toString()); // Send response
                }
            } catch (Exception err) {
                handleError(ctx, err); // Handle any exceptions during the execution
            }
        };
    }

    /**
     * Handles errors and sends an internal server error response.
     * This method is invoked when an exception or failure occurs during request processing.
     *
     * @param ctx The RoutingContext containing the HTTP request/response context.
     * @param err The error that occurred during request processing.
     */
    private static void handleError(RoutingContext ctx, Throwable err) {
        ctx.response().setStatusCode(CommonError.INTERNAL_SERVER.getCode())
                .end(BaseResponse.INTERNAL_SERVER_ERROR.toString());
    }
}
