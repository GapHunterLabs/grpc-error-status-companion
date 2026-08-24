package dev.gaphunter.grpcerrorstatuscompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.grpcerrorstatuscompanion.detect.JavaOnErrorFinder
import dev.gaphunter.grpcerrorstatuscompanion.detect.KotlinOnErrorFinder
import dev.gaphunter.grpcerrorstatuscompanion.model.OnErrorHit
import dev.gaphunter.grpcerrorstatuscompanion.review.ReviewPrompt

class RawExceptionOnErrorLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "gRPC onError with a raw exception"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaOnErrorFinder.findAll(file)
            "kotlin" -> KotlinOnErrorFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.callElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: OnErrorHit): LineMarkerInfo<PsiElement> {
        val tooltip = "onError() is called with a raw exception -- gRPC's own javadoc says the argument " +
            "\"should be a StatusException or StatusRuntimeException\" (Status.asException()/asRuntimeException()); " +
            "a raw exception's real type/message never reaches the client"
        return LineMarkerInfo(
            hit.callElement,
            hit.callElement.textRange,
            GrpcIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
