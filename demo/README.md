# Demo data — gRPC Error Status Companion

For capturing the real Marketplace screenshot:

1. `./gradlew runIde`
2. Open `demo/src/main/java/com/acmecorp/grpc/OrderServiceImpl.java` as a
   scratch/standalone file (or drop it into any sandbox project) inside the
   sandbox IDE.
3. The `onError(e)` call inside `cancelOrder` shows the gutter warning icon
   — hover it for the tooltip. The `placeOrder` method's properly
   `Status`-converted `onError` call stays clean, for contrast.
4. Enter Full Screen (`View > Appearance > Enter Full Screen`), capture with
   `Win+Shift+S`, save directly to `docs/screenshots/` in this repo.
