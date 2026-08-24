package dev.gaphunter.grpcerrorstatuscompanion.model

import com.intellij.psi.PsiElement

/** One `observer.onError(rawException)` call where the argument isn't converted through gRPC's `Status` type. */
data class OnErrorHit(val callElement: PsiElement)
