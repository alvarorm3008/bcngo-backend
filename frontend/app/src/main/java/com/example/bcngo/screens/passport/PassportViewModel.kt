package com.example.bcngo.screens.passport

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.bcngo.R
import com.example.bcngo.model.PassportPoint
import com.example.bcngo.model.PassportPointWrapper
import com.example.bcngo.network.ApiService
import kotlinx.coroutines.launch

class PassportViewModel : ViewModel() {
    private var _passportpoints = MutableLiveData<List<PassportPointWrapper>?>()
    val passportpoints: LiveData<List<PassportPointWrapper>?> = _passportpoints

    // Mapa de id de puntos de interés a recursos de imágenes
    private val imageMapping = mapOf(
        22 to R.drawable.boqueria,
        29 to R.drawable.pueblo_espanol,
        73 to R.drawable.portal_angel,
        96 to R.drawable.barceloneta,
        114 to R.drawable.anella_olimpica,
        124 to R.drawable.hotel_vela,
        170 to R.drawable.colon,
        175 to R.drawable.ramblas,
        181 to R.drawable.castell_montjuic,
        226 to R.drawable.sagrada_familia,
        261 to R.drawable.macba,
        268 to R.drawable.barri_gotic,
        294 to R.drawable.pedrera,
        306 to R.drawable.camp_nou,
        331 to R.drawable.teatro_nacional,
        336 to R.drawable.mnac,
        356 to R.drawable.cosmocaixa2,
        361 to R.drawable.arc_de_triomf,
        424 to R.drawable.torres_venecianas,
        445 to R.drawable.plaza_cataluna,
        448 to R.drawable.catedral,
        474 to R.drawable.illa_diagonal,
        533 to R.drawable.casa_batllo,
        542 to R.drawable.paseo_gracia,
        595 to R.drawable.parc_guell,
        599 to R.drawable.laberinto_horta,
        603 to R.drawable.santa_maria_mar,
        199 to R.drawable.torre_agbar,
        408 to R.drawable.moll_fusta,
        182 to R.drawable.sant_pau,
        335 to R.drawable.palau_musica,
        11 to R.drawable.ciutadella,
        473 to R.drawable.tibidabo,
        437 to R.drawable.liceu,
    )

    fun updatePassportPoints(context: Context) {
        viewModelScope.launch {
            Log.d(
                "ViewModelPassport",
                "updatePassportPoints: Iniciando actualización de puntos del pasaporte"
            )
            try {
                val pointsResponse = ApiService.getPassportPoints(context)
                if (pointsResponse != null) {
                    // Asignar las imágenes a los puntos de interés
                    val pointsWithImages = pointsResponse.data.map { wrapper ->
                        wrapper.point_of_interest.imageResId =
                            imageMapping[wrapper.point_of_interest.id] // Asignar la imagen correspondiente
                        Log.d(
                            "API",
                            "ID: ${wrapper.point_of_interest.id}, Nombre: ${wrapper.point_of_interest.name}"
                        )
                        wrapper
                    }.sortedBy { it.point_of_interest.name } // Ordenar alfabéticamente

                    _passportpoints.value = pointsWithImages
                    Log.d(
                        "ViewModelPassport",
                        "updatePassportPoints: Puntos obtenidos: ${pointsWithImages.size}"
                    )
                } else {
                    _passportpoints.value = emptyList()
                    Log.d("ViewModelPassport", "updatePassportPoints: No se encontraron puntos.")
                }
            } catch (e: Exception) {
                Log.e(
                    "ViewModelPassport",
                    "updatePassportPoints: Error al obtener los puntos del pasaporte",
                    e
                )
            }
        }
    }

    fun markPointAsVisited(context: Context, pointId: Int) {
        viewModelScope.launch {
            try {
                val result = ApiService.markPointAsVisited(context, pointId)
                Log.d("PassportViewModel", "Punto marcado como visitado: $result")

                // Actualizar la lista de puntos después de marcar como visitado
                updatePassportPoints(context)
            } catch (e: Exception) {
                Log.e("PassportViewModel", "Error al marcar el punto como visitado", e)
            }
        }
    }
}