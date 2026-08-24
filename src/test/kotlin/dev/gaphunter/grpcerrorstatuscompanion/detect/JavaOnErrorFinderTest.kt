package dev.gaphunter.grpcerrorstatuscompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaOnErrorFinderTest : BasePlatformTestCase() {

    fun `test onError with a raw exception is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(StreamObserver<Reply> responseObserver) {
                    try {
                        doWork();
                    } catch (Exception e) {
                        responseObserver.onError(e);
                    }
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaOnErrorFinder.findAll(file).size)
    }

    fun `test onError with a properly converted Status is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(StreamObserver<Reply> responseObserver) {
                    try {
                        doWork();
                    } catch (Exception e) {
                        responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaOnErrorFinder.findAll(file).isEmpty())
    }

    fun `test a non-observer receiver is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.java",
            """
            class OrderService {
                void process(ErrorHandler handler) {
                    handler.onError(new RuntimeException());
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaOnErrorFinder.findAll(file).isEmpty())
    }
}
