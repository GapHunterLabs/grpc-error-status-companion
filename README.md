# gRPC Error Status Companion

Gutter warning icon on a `streamObserver.onError(rawException)` call
where the argument isn't converted through gRPC's own `Status` type —
gRPC's own `StreamObserver.onError` javadoc states the parameter
"should be a StatusException or StatusRuntimeException, and callers
should generally convert from a Status via Status.asException() or
Status.asRuntimeException()". Passing a raw exception means its real
type/message never reaches the client — gRPC only transmits the
Status code and description across the wire, not the original
exception.

## Why it exists

`onError(e)` compiles fine and looks correct — the call succeeds, but
the client only ever sees a generic `UNKNOWN` status with no useful
detail, because the exception was never converted through gRPC's own
error model. Nothing in the IDE flags this today.

## Why built this way

- **100% static text/PSI analysis** — matches the receiver variable
  name and the argument's converted-Status signals by simple text, so
  it works whether the real gRPC jar is on the classpath or not. Java
  and Kotlin.

## v0.1 scope — stated honestly, not exhaustively

Doesn't resolve the receiver's real type — a variable named
`observer` that isn't actually a `StreamObserver` is a possible
(rare) false positive, and a real `StreamObserver` with an unusual
variable name isn't covered.

## Usage

Open any Java/Kotlin gRPC service implementation. An `onError` call
with a raw, unconverted exception shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
