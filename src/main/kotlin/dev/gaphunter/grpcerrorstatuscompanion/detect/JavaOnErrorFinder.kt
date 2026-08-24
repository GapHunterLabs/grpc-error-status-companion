package dev.gaphunter.grpcerrorstatuscompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import dev.gaphunter.grpcerrorstatuscompanion.model.OnErrorHit

/**
 * Finds `observer.onError(rawException)` calls where the receiver
 * looks like a gRPC `StreamObserver` ([GrpcSignals]) and the argument
 * isn't converted through gRPC's own `Status` type -- gRPC's own
 * `StreamObserver.onError` javadoc states the parameter "should be a
 * StatusException or StatusRuntimeException, and callers should
 * generally convert from a Status via Status.asException() or
 * Status.asRuntimeException()". Passing a raw exception means its real
 * message/type is lost on the wire (gRPC only transmits the Status
 * code and description) -- exactly the opposite of what the caller
 * likely intended, and a real information-leak risk if the raw
 * exception's message ever does make it across in some other form
 * (logged, wrapped) without the caller realizing the conversion never
 * happened.
 *
 * **v0.1 scope, stated honestly:** matches by simple text/name only --
 * doesn't resolve the receiver's real type, so a variable named
 * `observer` that isn't actually a `StreamObserver` is a possible
 * (rare) false positive, and a real `StreamObserver` with an unusual
 * variable name isn't covered.
 */
object JavaOnErrorFinder {

    fun findAll(file: PsiFile): List<OnErrorHit> {
        val hits = mutableListOf<OnErrorHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(call: PsiMethodCallExpression): OnErrorHit? {
        if (call.methodExpression.referenceName != "onError") return null

        val receiver = call.methodExpression.qualifierExpression as? PsiReferenceExpression ?: return null
        val receiverName = receiver.referenceName ?: return null
        if (!GrpcSignals.looksLikeStreamObserver(receiverName)) return null

        val argument = call.argumentList.expressions.firstOrNull() ?: return null
        if (GrpcSignals.argumentLooksStatusConverted(argument.text)) return null

        return OnErrorHit(leafOf(receiver))
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
