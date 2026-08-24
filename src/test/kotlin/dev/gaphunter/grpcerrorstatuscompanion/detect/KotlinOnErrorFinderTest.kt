package dev.gaphunter.grpcerrorstatuscompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinOnErrorFinderTest : BasePlatformTestCase() {

    fun `test onError with a raw exception is flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(responseObserver: StreamObserver<Reply>) {
                    try {
                        doWork()
                    } catch (e: Exception) {
                        responseObserver.onError(e)
                    }
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinOnErrorFinder.findAll(file).size)
    }

    fun `test onError with a properly converted Status is not flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(responseObserver: StreamObserver<Reply>) {
                    try {
                        doWork()
                    } catch (e: Exception) {
                        responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinOnErrorFinder.findAll(file).isEmpty())
    }

    fun `test a non-observer receiver is never flagged`() {
        val file = myFixture.configureByText(
            "OrderService.kt",
            """
            class OrderService {
                fun process(handler: ErrorHandler) {
                    handler.onError(RuntimeException())
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinOnErrorFinder.findAll(file).isEmpty())
    }
}
