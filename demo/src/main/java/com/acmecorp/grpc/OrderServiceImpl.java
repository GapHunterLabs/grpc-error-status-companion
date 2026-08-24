package com.acmecorp.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Demo data for gRPC Error Status Companion — used with `./gradlew runIde`
 * to capture the real Marketplace screenshot. Open this file, the gutter
 * icon should appear on the `onError` call inside `cancelOrder`.
 */
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    @Override
    public void placeOrder(OrderRequest request, StreamObserver<OrderReply> responseObserver) {
        try {
            OrderReply reply = orderProcessor.place(request);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (InvalidOrderException e) {
            // Correctly converted through gRPC's own Status type -- NOT flagged.
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void cancelOrder(CancelRequest request, StreamObserver<CancelReply> responseObserver) {
        try {
            orderProcessor.cancel(request.getOrderId());
            responseObserver.onCompleted();
        } catch (OrderNotFoundException e) {
            // Raw exception, never converted through Status -- FLAGGED.
            responseObserver.onError(e);
        }
    }
}
