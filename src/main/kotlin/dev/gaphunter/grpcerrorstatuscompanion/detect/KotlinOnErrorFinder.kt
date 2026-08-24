package dev.gaphunter.grpcerrorstatuscompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.grpcerrorstatuscompanion.model.OnErrorHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaOnErrorFinder]. */
object KotlinOnErrorFinder {

    fun findAll(file: PsiFile): List<OnErrorHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<OnErrorHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expression: KtDotQualifiedExpression): OnErrorHit? {
        val call = expression.selectorExpression as? KtCallExpression ?: return null
        if (call.calleeExpression?.text != "onError") return null

        val receiver = expression.receiverExpression as? KtNameReferenceExpression ?: return null
        val receiverName = receiver.getReferencedName()
        if (!GrpcSignals.looksLikeStreamObserver(receiverName)) return null

        val argument = call.valueArguments.firstOrNull()?.getArgumentExpression() ?: return null
        if (GrpcSignals.argumentLooksStatusConverted(argument.text)) return null

        return OnErrorHit(leafOf(receiver))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
