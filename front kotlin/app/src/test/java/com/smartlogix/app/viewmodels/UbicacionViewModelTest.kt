package com.smartlogix.app.viewmodels
import android.app.Application
import com.smartlogix.app.models.Piso
import com.smartlogix.app.models.Ubicacion
import com.smartlogix.app.models.UiState
import com.smartlogix.app.repository.UbicacionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UbicacionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val app = mockk<Application>(relaxed = true)
    private val repository = mockk<UbicacionRepository>()
    private lateinit var viewModel: UbicacionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = UbicacionViewModel(app, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUbicacionesByPiso carga ubicaciones correctamente`() = runTest {
        // 1. GIVEN: Usamos el Enum directo (Piso.A)
        val pisoA = Piso.A

        // Creamos ubicaciones con la estructura REAL de tu archivo Ubicacion.kt
        val listaUbicaciones = listOf(
            Ubicacion(
                idUbicacion = 1,
                codigoUbicacion = "P1-A-01",
                pasillo = 1,
                piso = 'A',
                numero = 1,
                productos = null
            ),
            Ubicacion(
                idUbicacion = 2,
                codigoUbicacion = "P1-A-02",
                pasillo = 1,
                piso = 'A',
                numero = 2,
                productos = null
            )
        )

        // Configuramos el mock
        coEvery {
            repository.getUbicaciones(piso = "A", pasillo = any(), forceRefresh = any())
        } returns Result.success(listaUbicaciones)

        // 2. WHEN: Filtramos por Piso A
        viewModel.getUbicacionesByPiso(pisoA)
        advanceUntilIdle()

        // 3. THEN: Verificamos
        val estado = viewModel.ubicacionesState.value
        assertTrue("El estado debería ser Success", estado is UiState.Success)

        // Opcional: Verificamos que trajo datos
        assertTrue((estado as UiState.Success).data.isNotEmpty())
    }
}

/*test asegura que la lógica de filtrado funciona:
la app sabe pedir datos específicos (Piso A) y actualizar
la vista sin errores."
*/

